import pandas as pd
import os, warnings
from model import CarbonProjectClassifier
import setting

warnings.filterwarnings('ignore')


def save_predictions_to_csv(predictions, filename='predictions_results.csv'):
    """将预测结果保存到CSV文件"""
    results = []
    for i, pred in enumerate(predictions, 1):
        result = {
            '测试用例': f"测试用例{i}",
            '项目名称': pred['project_name'],
            '项目介绍': pred['project_introduce'],
            '预期类型': pred['expected_type'],
            '预测类型': pred['predicted_methodology'],
            '置信度': pred['confidence'],
            '预测正确': '是' if pred['is_correct'] else '否',
            '前三预测': '; '.join([f"{p['methodology']}({p['confidence']})"
                                   for p in pred['top_predictions'][:3]])
        }
        results.append(result)

    df = pd.DataFrame(results)
    df.to_csv(filename, index=False, encoding='utf-8-sig')
    print(f"\n预测结果已保存到: {filename}")


def main():
    if not os.path.exists(setting.bert_model_path):
        print(f"\n错误: 未找到BERT模型文件夹 {setting.bert_model_path}")
        print("请先下载并解压BERT模型到指定路径")
        exit(1)

    classifier = CarbonProjectClassifier(model_path=setting.bert_model_path)

    data_files = ["methodology.csv"]
    data = classifier.load_data(data_files)

    if data is not None:
        print("\n开始训练BERT模型...")
        classifier.train(data, epochs=10, learning_rate=1e-5)
        classifier.save_model(setting.model_save_path)
    else:
        print("\n尝试加载已保存的模型...")
        classifier = CarbonProjectClassifier.load_model('carbon_bert_model', setting.bert_model_path)
        if classifier is None:
            print("无法加载模型，请检查数据文件和模型文件")
            exit(1)

    test_projects = [
        {
            "name": "西北太阳能光伏发电基地",
            "introduce": "该项目在西北地区建设大型太阳能光伏发电站，利用丰富的太阳能资源发电，为当地提供清洁电力，减少对化石能源的依赖，降低碳排放",
            "expected": "太阳能集中式"
        },
        {
            "name": "农田精准施肥示范工程",
            "introduce": "该项目在农田中推广精准施肥技术，通过土壤检测和作物需求分析，精确控制氮肥使用量，减少农业源一氧化二氮排放，同时提高作物产量和品质",
            "expected": "氮管理"
        },
        {
            "name": "大型沼气发电项目",
            "introduce": "该项目利用养殖场粪便和农业废弃物生产沼气，通过沼气发电实现生物质能的高效利用，减少甲烷排放的同时提供可再生能源",
            "expected": "生物消化器"
        },
        {
            "name": "工业余热回收利用系统",
            "introduce": "该项目针对工业生产过程中产生的余热进行回收利用，通过余热发电和供热技术，提高能源利用效率，减少化石能源消耗",
            "expected": "余热回收"
        },
        {
            "name": "氢燃料电池公交车队",
            "introduce": "该项目引入氢燃料电池公交车替换传统燃油公交车，通过零排放的氢能源车辆减少交通运输领域的碳排放，改善城市空气质量",
            "expected": "公共交通"
        },
    ]

    predictions_results = []

    for i, project in enumerate(test_projects, 1):
        print(f"\n=== 测试用例 {i}: {project['name']} ===")
        prediction = classifier.predict_methodology(
            project["name"],
            project["introduce"],
            project["expected"]
        )

        if prediction:
            print(f"预测方法学: {prediction['methodology']}")
            print(f"预期方法学: {project['expected']}")

            confidence = float(prediction['confidence'].replace('%', '')) / 100
            print(f"置信度: {confidence:.2%}")

            print("其他可能的方法学:")
            for j, pred in enumerate(prediction['top_predictions'][1:4], 1):
                conf = float(pred['confidence'].replace('%', '')) / 100
                print(f" {j}. {pred['methodology']}: {conf:.2%}")

            is_correct = prediction['methodology'] == project['expected']
            print(f"预测结果: {'√ 正确' if is_correct else '× 错误'}")

            # 收集结果保存到CSV
            predictions_results.append({
                'project_name': project['name'],
                'project_introduce': project['introduce'],
                'expected_type': project['expected'],
                'predicted_methodology': prediction['methodology'],
                'confidence': prediction['confidence'],
                'is_correct': is_correct,
                'top_predictions': prediction['top_predictions']
            })

    if predictions_results:
        save_predictions_to_csv(predictions_results, './carbon_methodology_predictions.csv')

    print(f"\n程序执行完成！使用设备: {classifier.device}")

if __name__ == "__main__":
    main()
