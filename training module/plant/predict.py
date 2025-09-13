import pandas as pd
import numpy as np
import torch
import joblib
import json, sys
from sklearn.preprocessing import RobustScaler
from model import SimplifiedEmissionMLP
from setting import root_path


def predict_emission(
        tree_type: str,
        planting_area_acre: float,
        planting_months: float,
        soil_fertility: str,
        climate_type: str,
        model_path: str = root_path + "best_simple_model.pth",
        scaler_path: str = root_path + "robust_scaler.pkl",
        area_quartiles_path: str = root_path + "area_quartiles.pkl",
        time_quartiles_path: str = root_path + "time_quartiles.pkl",
        feature_names_path: str = root_path + "feature_names.pkl",
        output_json_path: str = root_path + "prediction_result.json"
) -> dict:
    """
    树木减排量预测函数

    参数:
        tree_type: 树木类型（如"松树"、"杨树"等，需与训练数据类别一致）
        planting_area_acre: 种植面积（英亩）
        planting_months: 种植时长（月）
        soil_fertility: 土壤肥力（如"高"、"中"、"低"等，需与训练数据类别一致）
        climate_type: 气候类型（如"温带"、"热带"等，需与训练数据类别一致）
        model_path: 训练好的模型权重路径
        scaler_path: 训练好的RobustScaler路径
        area_quartiles_path: 训练数据的种植面积四分位数路径
        time_quartiles_path: 训练数据的种植时间三分位数路径
        feature_names_path: 训练数据的特征名称列表路径
        output_json_path: 预测结果输出的JSON文件路径

    返回:
        包含输入参数和预测结果的字典
    """
    # 加载预处理组件
    scaler: RobustScaler = joblib.load(scaler_path)
    area_quartiles = joblib.load(area_quartiles_path)
    time_quartiles = joblib.load(time_quartiles_path)
    feature_names = joblib.load(feature_names_path)

    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    input_size = len(feature_names)
    model = SimplifiedEmissionMLP(input_size).to(device)
    model.load_state_dict(torch.load(model_path, map_location=device))
    model.eval()  # 切换为评估模式（禁用Dropout/BatchNorm更新）

    input_data = pd.DataFrame({
        "treeType": [tree_type],
        "plantingAreaAcre": [planting_area_acre],
        "plantingMonths": [planting_months],
        "soilFertility": [soil_fertility],
        "climateType": [climate_type]
    })

    Q1_area = area_quartiles[0.25]
    Q3_area = area_quartiles[0.75]
    IQR_area = Q3_area - Q1_area
    lower_area = Q1_area - 2.0 * IQR_area
    upper_area = Q3_area + 2.0 * IQR_area
    input_data["plantingAreaAcre"] = input_data["plantingAreaAcre"].clip(lower_area, upper_area)

    Q1_time = input_data["plantingMonths"].quantile(0.25)
    Q3_time = input_data["plantingMonths"].quantile(0.75)
    IQR_time = Q3_time - Q1_time
    lower_time = Q1_time - 2.0 * IQR_time
    upper_time = Q3_time + 2.0 * IQR_time
    input_data["plantingMonths"] = input_data["plantingMonths"].clip(lower_time, upper_time)

    input_processed = input_data.copy()
    # 每月管理面积
    input_processed["area_per_month"] = input_processed["plantingAreaAcre"] / (input_processed["plantingMonths"] + 1e-8)
    # 种植面积对数变换（处理右偏分布）
    input_processed["log_area"] = np.log1p(input_processed["plantingAreaAcre"])
    # 种植强度分类（基于训练数据四分位数）
    input_processed["area_category"] = pd.cut(
        input_processed["plantingAreaAcre"],
        bins=[0, area_quartiles[0.25], area_quartiles[0.5], area_quartiles[0.75], float('inf')],
        labels=[0, 1, 2, 3],
        include_lowest=True  # 包含边界值
    ).astype(int)
    # 种植时间分类（基于训练数据三分位数）
    input_processed["time_category"] = pd.cut(
        input_processed["plantingMonths"],
        bins=[0, time_quartiles[0.33], time_quartiles[0.67], float('inf')],
        labels=[0, 1, 2],
        include_lowest=True
    ).astype(int)

    # 对每个分类特征做独热编码，并确保列名与训练一致
    tree_onehot = pd.get_dummies(input_processed["treeType"], prefix="tree")
    soil_onehot = pd.get_dummies(input_processed["soilFertility"], prefix="soil")
    climate_onehot = pd.get_dummies(input_processed["climateType"], prefix="climate")

    numeric_features = ["plantingAreaAcre", "plantingMonths", "area_per_month", "log_area", "area_category",
                        "time_category"]
    all_features = pd.concat(
        [input_processed[numeric_features], tree_onehot, soil_onehot, climate_onehot],
        axis=1
    )
    # 补全训练时存在但当前输入缺失的特征列（填充0）
    for col in feature_names:
        if col not in all_features.columns:
            all_features[col] = 0
    # 严格按照训练时的特征顺序排序
    all_features = all_features[feature_names]
    features_scaled = scaler.transform(all_features.values)

    # 转换为torch张量并移动到对应设备
    input_tensor = torch.FloatTensor(features_scaled).to(device)
    # 禁用梯度计算（提升预测速度）
    with torch.no_grad():
        predicted_emission = model(input_tensor).cpu().numpy().item()

    # 构建结果字典
    result = {
        "input_parameters": {
            "treeType": tree_type,
            "plantingAreaAcre": round(planting_area_acre, 2),
            "plantingMonths": round(planting_months, 2),
            "soilFertility": soil_fertility,
            "climateType": climate_type
        },
        "prediction_result": {
            "emissionReductionTCO2e": round(predicted_emission, 4),  # 预测的CO2减排量（吨）
            "unit": "TCO2e (吨二氧化碳当量)",
            "prediction_time": pd.Timestamp.now().strftime("%Y-%m-%d %H:%M:%S")  # 预测时间戳
        }
    }

    # 写入JSON文件（确保中文正常显示）
    with open(output_json_path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=4)

    print(f"预测完成！结果已保存至: {output_json_path}")
    return result


if __name__ == "__main__":
    prediction_result = predict_emission(
        tree_type=sys.argv[1],
        planting_area_acre=float(sys.argv[2]),
        planting_months=float(sys.argv[3]),
        soil_fertility=sys.argv[4],
        climate_type=sys.argv[5]
    )
    print("\n预测结果详情:")
    print(json.dumps(prediction_result, ensure_ascii=False, indent=4))
