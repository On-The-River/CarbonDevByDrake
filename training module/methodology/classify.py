from model import CarbonProjectClassifier
import os, json, sys
import setting

def save_prediction_to_json(result, save_path=setting.root_path + '\\carbon_project_prediction.json'):
    """将预测结果保存到JSON文件"""
    try:
        with open(save_path, 'w', encoding='utf-8') as f:
            json.dump(result, f, ensure_ascii=False, indent=2)
        print(f"\n预测结果已成功保存到：{os.path.abspath(save_path)}")
    except Exception as e:
        print(f"保存JSON失败：{str(e)}")

def _predict():
    TEST_PROJECT = {
        "name": sys.argv[1],
        "introduce": sys.argv[2]
    }

    print("=" * 50)
    print("开始加载碳项目分类模型...")
    classifier = CarbonProjectClassifier.load_model(
        model_path=setting.model_save_path,
        bert_model_path=setting.bert_model_path
    )

    if classifier:
        print("\n" + "=" * 50)
        print("开始预测项目方法学...")
        print(f"项目名称：{TEST_PROJECT['name']}")

        # 执行预测
        prediction_result = classifier.predict_methodology(
            project_name=TEST_PROJECT['name'],
            project_introduce=TEST_PROJECT['introduce']
        )

        # 打印结果并保存
        if prediction_result:
            print("\n" + "=" * 50)
            print("预测结果概览：")
            print(f"最佳匹配方法学：{prediction_result['methodology']}")
            print(f"预测置信度：{prediction_result['confidence']}")

            # 保存到JSON
            save_prediction_to_json(prediction_result)
    else:
        print("\n模型加载失败，无法执行预测")
    print("=" * 50)


if __name__ == "__main__":
    _predict()
