import torch
from torch.optim.adamw import AdamW
from torch.utils.data import Dataset, DataLoader
from transformers import BertTokenizer, BertForSequenceClassification
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, accuracy_score
from tqdm import tqdm
import pandas as pd
import json, re, os
from transformers import BertConfig
import setting


class CarbonProjectDataset(Dataset):
    """自定义数据集类，用于BERT模型训练"""
    def __init__(self, texts, labels, tokenizer, max_length=256):
        self.texts = texts
        self.labels = labels
        self.tokenizer = tokenizer
        self.max_length = max_length

    def __len__(self):
        return len(self.texts)

    def __getitem__(self, idx):
        text = str(self.texts[idx])
        label = self.labels[idx]

        encoding = self.tokenizer(
            text,
            truncation=True,
            padding='max_length',
            max_length=self.max_length,
            return_tensors='pt'
        )

        return {
            'input_ids': encoding['input_ids'].flatten(),
            'attention_mask': encoding['attention_mask'].flatten(),
            'label': torch.tensor(label, dtype=torch.long)
        }


class CarbonProjectClassifier:
    def __init__(self, model_path=setting.model_save_path, max_length=256, batch_size=16):
        """
        初始化碳项目分类器
        Args:
            model_path: 本地BERT模型路径
            max_length: 最大序列长度
            batch_size: 批次大小
        """

        # 碳减排项目类型表
        self.type_list = [
            '牧场添加堆肥', '饲料添加剂', '改善灌溉管理', '粪肥沼气池', '氮管理', '水稻减排',
            '固体废物分离', '可持续农业', '可持续放牧', '碳捕获和提高石油采收率',
            '碳捕获和储存在塑料中', '己二酸生产中N2O的消除', '硝酸生产中N2O的消除',
            '环氧丙烷生产', 'SF6更替', '高级制冷剂', 'HFC制冷剂回收', '泡沫生产中HFC的替代',
            'HFC23消除', '臭氧消耗物质的回收与消除', '制冷剂泄漏检测', '造林和再造林',
            '避免森林转化', '避免草场转化', '改善森林管理', 'REDD+（减少毁林和森林退化造成的排放）',
            'REDD + 管辖权', '湿地恢复', '生物消化器', '捆绑能源效率', '净水', '社区钻井',
            '锅灶', '照明', '供暖', '铝冶炼厂减排', '制砖减排', '能源效率', '燃料转换',
            '气体系统中的泄漏检测和维修', '矿井甲烷捕集', '天然气发电', '石油回收',
            '气动改造', '大学校园减排', '废气回收', '余热回收', '生物量', '地热发电',
            '水力发电', '可再生能源组合使用', '太阳能集中式', '太阳能分布式', '太阳灶',
            '太阳能照明', '太阳能热水器', '风能发电', '电动汽车充电', '车队效率',
            '燃料运输', '公共交通', '柴油车停车电气化', '堆肥', '填埋甲烷',
            '废水中甲烷的回收', '垃圾焚烧', '废物管理'
        ]

        self.model_path = model_path
        self.max_length = max_length
        self.batch_size = batch_size
        self.num_classes = len(self.type_list)
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        print(f"使用设备: {self.device}")

        self.type_to_index = {t: i for i, t in enumerate(self.type_list)}
        self.index_to_type = {i: t for i, t in enumerate(self.type_list)}


        self.tokenizer = None
        self.model = None

        # BERT通常不需要手动去除停用词
        self.stopwords = {'的', '了', '在', '是', '和', '就', '也', '有', '对', '以'}

        self._load_tokenizer()

    def _load_tokenizer(self):
        """加载BERT tokenizer"""
        try:
            self.tokenizer = BertTokenizer.from_pretrained(
                self.model_path,
                num_labels=self.num_classes,
                load_in_8bit=False,  # 禁用8位量化（避免额外依赖）
                device_map=None,  # 手动指定设备，不自动分配
                use_safetensors=True  # 强制使用safetensors格式
            )
            print(f"成功加载tokenizer: {self.model_path}")
        except Exception as e:
            print(f"加载tokenizer失败: {e}")
            print("请确保BERT模型文件存在于指定路径")

    def _process_text(self, text):
        """文本预处理（简化版，清理特殊字符）"""
        if not isinstance(text, str):
            return ""

        # 去除特殊字符与多余空格，保留中文、英文、数字和基本标点
        text = re.sub(r'[^\u4e00-\u9fa5a-zA-Z0-9\s，。！？；：（）]', '', text)
        text = re.sub(r'\s+', ' ', text).strip()

        return text

    def _map_carbon_field_to_type(self, carbon_field):
        """将carbon_field映射到标准类型"""
        if not isinstance(carbon_field, str):
            return None

        carbon_field = carbon_field.replace(' ', '')

        for carbon_type in self.type_list:
            processed_type = carbon_type.replace(' ', '')
            keywords = [processed_type, carbon_type]

            # 处理括号中的内容
            if '（' in carbon_type and '）' in carbon_type:
                main_part = carbon_type.split('（')[0]
                sub_part = carbon_type.split('（')[1].split('）')[0]
                keywords.extend([main_part, sub_part])

            for keyword in keywords:
                if keyword in carbon_field:
                    return carbon_type

        return None

    def load_data(self, file_paths):
        """
        加载数据，支持单个文件或多个文件
        Args:
            file_paths: 文件路径，可以是字符串或列表
        """
        if isinstance(file_paths, str):
            file_paths = [file_paths]

        all_dfs = []

        for file_path in file_paths:
            try:
                df = pd.read_csv(file_path)
                print(f"成功加载数据文件 {file_path}，共{len(df)}条记录")
                all_dfs.append(df)
            except Exception as e:
                print(f"加载数据文件 {file_path} 失败: {e}")

        if not all_dfs:
            print("没有成功加载任何数据文件")
            return None

        # 合并所有数据
        df = pd.concat(all_dfs, ignore_index=True)
        print(f"总共加载数据：{len(df)}条记录")

        # 映射carbon_field到目标类型
        df['type'] = df['carbon_field'].apply(self._map_carbon_field_to_type)

        # 过滤无法映射的记录
        invalid_records = df[df['type'].isna()]
        if not invalid_records.empty:
            print(f"警告：{len(invalid_records)}条记录无法映射到已知类型")

        df = df.dropna(subset=['type'])

        # 合并name和introduce作为文本特征
        df['text'] = df['name'] + ' ' + df['introduce']

        # 文本预处理
        df['processed_text'] = df['text'].apply(self._process_text)

        # 创建标签索引
        df['label'] = df['type'].map(self.type_to_index)

        return df

    def _create_model(self):
        """创建BERT分类模型"""
        try:
            model = BertForSequenceClassification.from_pretrained(
                self.model_path,
                num_labels=self.num_classes
            )
            return model.to(self.device)
        except Exception as e:
            print(f"创建模型失败: {e}")
            return None

    def train(self, df, test_size=0.2, random_state=42, epochs=3, learning_rate=2e-5):
        """训练BERT模型"""
        if df is None or df.empty:
            print("没有数据可用于训练")
            return False

        if self.tokenizer is None:
            print("Tokenizer未加载")
            return False

        X = df['processed_text'].values
        y = df['label'].values

        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=test_size, random_state=random_state, stratify=y
        )

        print(f"训练集大小: {len(X_train)}, 测试集大小: {len(X_test)}")

        train_dataset = CarbonProjectDataset(X_train, y_train, self.tokenizer, self.max_length)
        test_dataset = CarbonProjectDataset(X_test, y_test, self.tokenizer, self.max_length)
        train_loader = DataLoader(train_dataset, batch_size=self.batch_size, shuffle=True)
        test_loader = DataLoader(test_dataset, batch_size=self.batch_size, shuffle=False)

        self.model = self._create_model()
        if self.model is None:
            return False

        optimizer = AdamW(self.model.parameters(), lr=learning_rate)

        self.model.train()
        for epoch in range(epochs):
            total_loss = 0
            progress_bar = tqdm(train_loader, desc=f'Epoch {epoch + 1}/{epochs}')

            for batch in progress_bar:
                optimizer.zero_grad()

                input_ids = batch['input_ids'].to(self.device)
                attention_mask = batch['attention_mask'].to(self.device)
                labels = batch['label'].to(self.device)

                outputs = self.model(input_ids=input_ids,
                                     attention_mask=attention_mask,
                                     labels=labels)

                loss = outputs.loss
                loss.backward()
                optimizer.step()

                total_loss += loss.item()
                progress_bar.set_postfix({'loss': loss.item()})

            avg_loss = total_loss / len(train_loader)
            print(f'Epoch {epoch + 1}/{epochs}, 平均损失: {avg_loss:.4f}')

        self._evaluate_model(test_loader, y_test)
        return True

    def _evaluate_model(self, test_loader, y_true):
        """评估模型性能"""
        self.model.eval()
        predictions = []

        with torch.no_grad():
            for batch in tqdm(test_loader, desc='评估中'):
                input_ids = batch['input_ids'].to(self.device)
                attention_mask = batch['attention_mask'].to(self.device)

                outputs = self.model(input_ids=input_ids, attention_mask=attention_mask)
                logits = outputs.logits

                preds = torch.argmax(logits, dim=-1)
                predictions.extend(preds.cpu().numpy())

        accuracy = accuracy_score(y_true, predictions)
        print(f'\n测试集准确率: {accuracy:.4f}')

        y_true_labels = [self.index_to_type[i] for i in y_true]
        y_pred_labels = [self.index_to_type[i] for i in predictions]

        print("\n分类报告:")
        print(classification_report(y_true_labels, y_pred_labels))

    def predict_methodology(self, project_name, project_introduce, expected_type=None):
        """预测新项目的方法学"""
        if self.model is None or self.tokenizer is None:
            print("模型未训练，请先调用train方法")
            return None

        text = project_name + ' ' + project_introduce
        processed_text = self._process_text(text)
        print(f"\n【测试用例关键词】: {processed_text}")

        if expected_type:
            print(f"【预期类型】: {expected_type}")

        encoding = self.tokenizer(
            processed_text,
            truncation=True,
            padding='max_length',
            max_length=self.max_length,
            return_tensors='pt'
        )

        self.model.eval()
        with torch.no_grad():
            input_ids = encoding['input_ids'].to(self.device)
            attention_mask = encoding['attention_mask'].to(self.device)

            outputs = self.model(input_ids=input_ids, attention_mask=attention_mask)
            logits = outputs.logits

            probs = torch.softmax(logits, dim=-1).cpu().numpy()[0]

        top_indices = probs.argsort()[::-1]

        top_predictions = []
        for idx in top_indices:
            carbon_type = self.type_list[idx]
            probability = probs[idx] * 100
            top_predictions.append({
                'methodology': carbon_type,
                'confidence': f"{probability:.2f}%"
            })

        return {
            'methodology': top_predictions[0]['methodology'],
            'confidence': top_predictions[0]['confidence'],
            'top_predictions': top_predictions
        }

    def save_model(self, save_path=setting.model_save_path):
        """保存训练好的模型"""
        if self.model is None or self.tokenizer is None:
            print("模型未训练，无法保存")
            return False

        try:
            os.makedirs(save_path, exist_ok=True)

            self.model.save_pretrained(save_path)
            self.tokenizer.save_pretrained(save_path)

            with open(os.path.join(save_path, 'type_list.json'), 'w', encoding='utf-8') as f:
                json.dump(self.type_list, f, ensure_ascii=False, indent=2)

            print(f"模型已保存到 {save_path}")
            return True
        except Exception as e:
            print(f"模型保存失败: {e}")
            return False

    @staticmethod
    def load_model(model_path=setting.model_save_path, bert_model_path='./bert-base-chinese'):
        """从文件加载训练好的模型"""
        try:
            # 加载类型列表
            with open(os.path.join(model_path, 'type_list.json'), 'r', encoding='utf-8') as f:
                type_list = json.load(f)

            # 创建分类器实例
            classifier = CarbonProjectClassifier(model_path=bert_model_path)
            classifier.type_list = type_list
            classifier.num_classes = len(type_list)
            classifier.type_to_index = {t: i for i, t in enumerate(type_list)}
            classifier.index_to_type = {i: t for i, t in enumerate(type_list)}

            config = BertConfig.from_pretrained(model_path)
            config.use_flash_attention_2 = True

            classifier.tokenizer = BertTokenizer.from_pretrained(model_path)
            classifier.model = BertForSequenceClassification.from_pretrained(model_path, config=config)
            classifier.model = classifier.model.to(classifier.device)

            print(f"模型已从 {model_path} 加载")
            return classifier

        except Exception as e:
            print(f"加载模型失败: {e}")
            return None
