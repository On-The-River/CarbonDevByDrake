from sklearn.preprocessing import StandardScaler, RobustScaler
import pandas as pd
import numpy as np
import torch.nn as nn


class OptimizedForestEmissionPredictor:
    def __init__(self):
        # 使用鲁棒缩放器，对异常值不敏感
        self.scaler = RobustScaler()
        self.feature_names = None
        self.area_quartiles = None
        self.time_quartiles = None

    def remove_outliers(self, df, column, method='iqr', factor=2.0):
        """改进的异常值处理"""
        if method == 'iqr':
            Q1 = df[column].quantile(0.25)
            Q3 = df[column].quantile(0.75)
            IQR = Q3 - Q1
            lower_bound = Q1 - factor * IQR
            upper_bound = Q3 + factor * IQR
        elif method == 'percentile':
            lower_bound = df[column].quantile(0.01)
            upper_bound = df[column].quantile(0.99)

        # 使用截断而不是删除
        df[column] = df[column].clip(lower_bound, upper_bound)
        return df

    def create_meaningful_features(self, X):
        """创建有意义的特征"""
        X_new = X.copy()

        X_new['area_per_month'] = X['plantingAreaAcre'] / (X['plantingMonths'] + 1e-8)
        X_new['log_area'] = np.log1p(X['plantingAreaAcre'])

        self.area_quartiles = X['plantingAreaAcre'].quantile([0.25, 0.5, 0.75])
        X_new['area_category'] = pd.cut(X['plantingAreaAcre'],
                                        bins=[0, self.area_quartiles[0.25], self.area_quartiles[0.5],
                                              self.area_quartiles[0.75], float('inf')],
                                        labels=[0, 1, 2, 3])

        self.time_quartiles = X['plantingMonths'].quantile([0.33, 0.67])
        X_new['time_category'] = pd.cut(X['plantingMonths'],
                                        bins=[0, self.time_quartiles[0.33], self.time_quartiles[0.67], float('inf')],
                                        labels=[0, 1, 2])

        return X_new

    def load_and_preprocess_data(self, csv_path):
        """优化的数据预处理"""
        print("加载数据...")
        df = pd.read_csv(csv_path, encoding='utf-8')

        # 异常值处理截断
        print("处理异常值...")
        df = self.remove_outliers(df, 'emissionReductionTCO2e', method='percentile')
        df = self.remove_outliers(df, 'plantingAreaAcre', method='iqr', factor=2.0)
        df = self.remove_outliers(df, 'plantingMonths', method='iqr', factor=2.0)

        features = ['treeType', 'plantingAreaAcre', 'plantingMonths', 'soilFertility', 'climateType']
        target = 'emissionReductionTCO2e'

        X = df[features].copy()
        y = df[target].values

        X = self.create_meaningful_features(X)

        print("编码特征...")
        tree_onehot = pd.get_dummies(X['treeType'], prefix='tree')
        soil_onehot = pd.get_dummies(X['soilFertility'], prefix='soil')
        climate_onehot = pd.get_dummies(X['climateType'], prefix='climate')

        # 合并特征
        numeric_features = ['plantingAreaAcre', 'plantingMonths', 'area_per_month',
                            'log_area', 'area_category', 'time_category']
        X_processed = pd.concat([
            X[numeric_features],
            tree_onehot,
            soil_onehot,
            climate_onehot
        ], axis=1)

        # 填充分类特征的NaN值
        X_processed = X_processed.fillna(0)

        self.feature_names = X_processed.columns.tolist()
        print(f"最终特征数: {len(self.feature_names)}")

        return X_processed.values, y

    def prepare_data_with_cv(self, X, y):
        """使用交叉验证准备数据"""
        # 标准化特征
        X_scaled = self.scaler.fit_transform(X)
        return X_scaled, y


class SimplifiedEmissionMLP(nn.Module):
    def __init__(self, input_size):
        super(SimplifiedEmissionMLP, self).__init__()
        # 更简单的网络架构，避免过拟合
        self.network = nn.Sequential(
            nn.Linear(input_size, 128),
            nn.BatchNorm1d(128),
            nn.ReLU(),
            nn.Dropout(0.3),

            nn.Linear(128, 64),
            nn.BatchNorm1d(64),
            nn.ReLU(),
            nn.Dropout(0.2),

            nn.Linear(64, 32),
            nn.ReLU(),
            nn.Dropout(0.1),

            nn.Linear(32, 1)
        )

        # Xavier初始化
        self.apply(self._init_weights)

    def _init_weights(self, m):
        if isinstance(m, nn.Linear):
            nn.init.xavier_normal_(m.weight)
            nn.init.constant_(m.bias, 0)

    def forward(self, x):
        return self.network(x).squeeze()