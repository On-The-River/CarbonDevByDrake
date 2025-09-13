import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from scipy import stats
from sklearn.preprocessing import LabelEncoder
from sklearn.feature_selection import mutual_info_regression
from sklearn.ensemble import RandomForestRegressor
import warnings

warnings.filterwarnings('ignore')


class DataQualityAnalyzer:
    def __init__(self):
        self.signal_to_noise = None
        plt.rcParams['font.sans-serif'] = ['SimHei']
        plt.rcParams['axes.unicode_minus'] = False

    def load_and_analyze_data(self, csv_path):
        """全面分析数据质量"""
        print("=" * 60)
        print("深度数据质量分析")
        print("=" * 60)

        df = pd.read_csv(csv_path, encoding='utf-8')
        print(f"数据形状: {df.shape}")

        # 1. 基础统计分析
        self.basic_statistics(df)

        # 2. 目标变量分析
        self.analyze_target_variable(df)

        # 3. 特征与目标变量关系分析
        self.analyze_feature_target_relationships(df)

        # 4. 数据分布分析
        self.analyze_data_distributions(df)

        # 5. 特征重要性分析
        self.analyze_feature_importance(df)

        # 6. 数据模式分析
        self.analyze_data_patterns(df)

        # 7. 可预测性分析
        self.analyze_predictability(df)

        return df

    def basic_statistics(self, df):
        """基础统计分析"""
        print("1.基础统计信息")
        print("-" * 40)
        print("数据类型:")
        print(df.dtypes)
        print("缺失值: ")
        print(df.isnull().sum())
        print("\n数值特征统计:")
        print(df.describe())

        # 分类特征统计
        print("分类特征统计: ")
        categorical_cols = ['treeType', 'soilFertility', 'climateType']
        for col in categorical_cols:
            print(f"\n{col} 分布:")
        print(df[col].value_counts())

    def analyze_target_variable(self, df):
        """目标变量深度分析"""
        print("\n2. 目标变量深度分析")
        print("-" * 40)

        target = df['emissionReductionTCO2e']

        # 基础统计
        print(f"均值: {target.mean():.2f}")
        print(f"中位数: {target.median():.2f}")
        print(f"标准差: {target.std():.2f}")
        print(f"变异系数: {target.std() / target.mean():.2f}")
        print(f"偏度: {target.skew():.4f}")
        print(f"峰度: {target.kurtosis():.4f}")

        # 分布检验
        _, p_value = stats.normaltest(target)
        print(f"正态性检验 p值: {p_value:.6f} ({'正态' if p_value > 0.05 else '非正态'})")

        # 零值和异常值分析
        zero_count = (target == 0).sum()
        print(f"零值数量: {zero_count} ({zero_count / len(target) * 100:.1f}%)")

        Q1, Q3 = target.quantile([0.25, 0.75])
        IQR = Q3 - Q1
        outliers = target[(target < Q1 - 1.5 * IQR) | (target > Q3 + 1.5 * IQR)]
        print(f"异常值数量: {len(outliers)} ({len(outliers) / len(target) * 100:.1f}%)")

        # 可视化目标变量
        self.plot_target_analysis(target)

    def plot_target_analysis(self, target):
        """目标变量可视化"""
        fig, axes = plt.subplots(2, 3, figsize=(18, 12))

        # 直方图
        axes[0, 0].hist(target, bins=50, alpha=0.7, edgecolor='black')
        axes[0, 0].set_title('减排量分布')
        axes[0, 0].set_xlabel('减排量')
        axes[0, 0].set_ylabel('频数')

        # 对数直方图
        axes[0, 1].hist(np.log1p(target), bins=50, alpha=0.7, edgecolor='black')
        axes[0, 1].set_title('log(1+减排量)分布')
        axes[0, 1].set_xlabel('log(1+减排量)')
        axes[0, 1].set_ylabel('频数')

        # QQ图
        stats.probplot(target, dist="norm", plot=axes[0, 2])
        axes[0, 2].set_title('Q-Q图')

        # 箱线图
        axes[1, 0].boxplot(target)
        axes[1, 0].set_title('减排量箱线图')
        axes[1, 0].set_ylabel('减排量')

        # 累积分布
        sorted_target = np.sort(target)
        y = np.arange(1, len(sorted_target) + 1) / len(sorted_target)
        axes[1, 1].plot(sorted_target, y)
        axes[1, 1].set_title('累积分布函数')
        axes[1, 1].set_xlabel('减排量')
        axes[1, 1].set_ylabel('累积概率')

        # 分位数统计
        quantiles = [0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95, 0.99]
        q_values = target.quantile(quantiles)
        axes[1, 2].bar(range(len(quantiles)), q_values)
        axes[1, 2].set_xticks(range(len(quantiles)))
        axes[1, 2].set_xticklabels([f'{q:.0%}' for q in quantiles], rotation=45)
        axes[1, 2].set_title('分位数分布')
        axes[1, 2].set_ylabel('减排量')

        plt.tight_layout()
        plt.savefig('target_analysis.png', dpi=300, bbox_inches='tight')
        plt.show()

    def analyze_feature_target_relationships(self, df):
        """特征与目标变量关系分析"""
        print("3.特征与目标变量关系分析")
        print("-" * 40)

        target = df['emissionReductionTCO2e']

        # 数值特征相关性
        numeric_features = ['plantingAreaAcre', 'plantingMonths']
        print("数值特征与目标的相关性:")
        for feature in numeric_features:
            corr = df[feature].corr(target)
        print(f"{feature}: {corr:.4f}")

        # 分类特征与目标的关系
        categorical_features = ['treeType', 'soilFertility', 'climateType']

        print("分类特征的方差分析: ")
        for feature in categorical_features:
            groups = [group[1]['emissionReductionTCO2e'].values
                      for group in df.groupby(feature)]
        f_stat, p_value = stats.f_oneway(*groups)
        print(f"{feature}: F={f_stat:.4f}, p={p_value:.6f}")

        # 特征间的共线性分析
        self.analyze_multicollinearity(df)

        # 可视化特征关系
        self.plot_feature_relationships(df)

    def analyze_multicollinearity(self, df):
        """多重共线性分析"""
        print("多重共线性分析: ")
        numeric_df = df[['plantingAreaAcre', 'plantingMonths', 'emissionReductionTCO2e']]
        corr_matrix = numeric_df.corr()
        print("相关系数矩阵:")
        print(corr_matrix)

        # 方差膨胀因子（VIF）
        from statsmodels.stats.outliers_influence import variance_inflation_factor

        try:
            X = df[['plantingAreaAcre', 'plantingMonths']].values
            vif_data = pd.DataFrame()
            vif_data["特征"] = ['plantingAreaAcre', 'plantingMonths']
            vif_data["VIF"] = [variance_inflation_factor(X, i) for i in range(X.shape[1])]
            print("方差膨胀因子(VIF): ")
            print(vif_data)
        except:
            print("无法计算VIF")

    def plot_feature_relationships(self, df):
        """可视化特征关系"""
        fig, axes = plt.subplots(3, 3, figsize=(18, 15))

        # 面积 vs 减排量
        axes[0, 0].scatter(df['plantingAreaAcre'], df['emissionReductionTCO2e'], alpha=0.5)
        axes[0, 0].set_xlabel('种植面积')
        axes[0, 0].set_ylabel('减排量')
        axes[0, 0].set_title('面积 vs 减排量')

        # 时间 vs 减排量
        axes[0, 1].scatter(df['plantingMonths'], df['emissionReductionTCO2e'], alpha=0.5)
        axes[0, 1].set_xlabel('种植时间')
        axes[0, 1].set_ylabel('减排量')
        axes[0, 1].set_title('时间 vs 减排量')

        # 面积 vs 时间
        axes[0, 2].scatter(df['plantingAreaAcre'], df['plantingMonths'], alpha=0.5)
        axes[0, 2].set_xlabel('种植面积')
        axes[0, 2].set_ylabel('种植时间')
        axes[0, 2].set_title('面积 vs 时间')

        # 树种箱线图
        df.boxplot(column='emissionReductionTCO2e', by='treeType', ax=axes[1, 0])
        axes[1, 0].set_title('不同树种的减排量')
        axes[1, 0].set_xlabel('树种')
        axes[1, 0].tick_params(axis='x', rotation=45)

        # 土壤箱线图
        df.boxplot(column='emissionReductionTCO2e', by='soilFertility', ax=axes[1, 1])
        axes[1, 1].set_title('不同土壤的减排量')
        axes[1, 1].set_xlabel('土壤肥力')
        axes[1, 1].tick_params(axis='x', rotation=45)

        # 气候箱线图
        df.boxplot(column='emissionReductionTCO2e', by='climateType', ax=axes[1, 2])
        axes[1, 2].set_title('不同气候的减排量')
        axes[1, 2].set_xlabel('气候类型')
        axes[1, 2].tick_params(axis='x', rotation=45)

        # 面积分布
        axes[2, 0].hist(df['plantingAreaAcre'], bins=30, alpha=0.7)
        axes[2, 0].set_title('种植面积分布')
        axes[2, 0].set_xlabel('面积')

        # 时间分布
        axes[2, 1].hist(df['plantingMonths'], bins=20, alpha=0.7)
        axes[2, 1].set_title('种植时间分布')
        axes[2, 1].set_xlabel('时间')

        # 相关性热力图
        numeric_df = df[['plantingAreaAcre', 'plantingMonths', 'emissionReductionTCO2e']]
        sns.heatmap(numeric_df.corr(), annot=True, cmap='coolwarm', center=0, ax=axes[2, 2])
        axes[2, 2].set_title('特征相关性')

        plt.tight_layout()
        plt.savefig('feature_relationships.png', dpi=300, bbox_inches='tight')
        plt.show()

    def analyze_data_distributions(self, df):
        """数据分布分析"""
        print("4.数据分布分析")
        print("-" * 40)

        numeric_features = ['plantingAreaAcre', 'plantingMonths']

        for feature in numeric_features:
            data = df[feature]
            print(f"{feature}: ")
            print(f"  均值: {data.mean():.2f}")
            print(f"  中位数: {data.median():.2f}")
            print(f"  众数: {data.mode().iloc[0] if len(data.mode()) > 0 else 'N/A'}")
            print(f"  偏度: {data.skew():.4f}")
            print(f"  峰度: {data.kurtosis():.4f}")

        # 唯一值分析
            unique_values = data.nunique()
            unique_ratio = unique_values / len(data)
            print(f"  唯一值数量: {unique_values}")
            print(f"  唯一值比例: {unique_ratio:.4f}")

        # 检查是否为离散值
            if unique_ratio < 0.1:
                print(f"  警告: {feature} 可能是离散变量!")
            print(f"  值分布: {data.value_counts().head()}")

    def analyze_feature_importance(self, df):
        """特征重要性分析"""
        print("5.特征重要性分析")
        print("-" * 40)

        # 准备数据
        le_tree = LabelEncoder()
        le_soil = LabelEncoder()
        le_climate = LabelEncoder()

        X = pd.DataFrame({
            'plantingAreaAcre': df['plantingAreaAcre'],
            'plantingMonths': df['plantingMonths'],
            'treeType': le_tree.fit_transform(df['treeType']),
            'soilFertility': le_soil.fit_transform(df['soilFertility']),
            'climateType': le_climate.fit_transform(df['climateType'])
        })
        y = df['emissionReductionTCO2e']

        # 随机森林特征重要性
        rf = RandomForestRegressor(n_estimators=100, random_state=42)
        rf.fit(X, y)

        feature_importance = pd.DataFrame({
            'feature': X.columns,
            'importance': rf.feature_importances_
        }).sort_values('importance', ascending=False)

        print("随机森林特征重要性:")
        print(feature_importance)

        # 互信息
        mi_scores = mutual_info_regression(X, y, random_state=42)
        mi_importance = pd.DataFrame({
            'feature': X.columns,
            'mutual_info': mi_scores
        }).sort_values('mutual_info', ascending=False)

        print("互信息特征重要性: ")
        print(mi_importance)

    def analyze_data_patterns(self, df):
        """数据模式分析"""
        print("6.数据模式分析")
        print("-" * 40)

        # 检查数据是否人工生成
        self.check_artificial_patterns(df)

        # 检查数据的真实性
        self.check_data_realism(df)

        # 分析数据的可学习性
        self.analyze_learnability(df)

    def check_artificial_patterns(self, df):
        """检查人工生成模式"""
        print("检查人工生成模式:")

        # 检查数值的舍入模式
        area_decimals = df['plantingAreaAcre'] % 1
        months_decimals = df['plantingMonths'] % 1
        target_decimals = df['emissionReductionTCO2e'] % 1

        print(f"面积小数部分非零比例: {(area_decimals != 0).mean():.4f}")
        print(f"时间小数部分非零比例: {(months_decimals != 0).mean():.4f}")
        print(f"目标小数部分非零比例: {(target_decimals != 0).mean():.4f}")

        # 检查重复值
        duplicate_rows = df.duplicated().sum()
        print(f"重复行数: {duplicate_rows}")

        # 检查组合的唯一性
        combinations = df[['treeType', 'soilFertility', 'climateType']].value_counts()
        print(f"类别组合数: {len(combinations)}")
        print("前10个最常见组合:")
        print(combinations.head(10))

    def check_data_realism(self, df):
        """检查数据真实性"""
        print("检查数据真实性: ")

        # 物理合理性检查
        area_range = (df['plantingAreaAcre'].min(), df['plantingAreaAcre'].max())
        months_range = (df['plantingMonths'].min(), df['plantingMonths'].max())
        emission_range = (df['emissionReductionTCO2e'].min(), df['emissionReductionTCO2e'].max())

        print(f"面积范围: {area_range} 英亩")
        print(f"时间范围: {months_range} 月")
        print(f"减排范围: {emission_range} tCO2e")

        # 计算减排效率
        df_temp = df.copy()
        df_temp['efficiency'] = df_temp['emissionReductionTCO2e'] / (
                    df_temp['plantingAreaAcre'] * df_temp['plantingMonths'])

        print(f"减排效率统计(tCO2e / 英亩 / 月): ")
        print(f"平均: {df_temp['efficiency'].mean():.6f}")
        print(f"标准差: {df_temp['efficiency'].std():.6f}")
        print(f"变异系数: {df_temp['efficiency'].std() / df_temp['efficiency'].mean():.4f}")

        # 效率异常值
        eff_q1, eff_q3 = df_temp['efficiency'].quantile([0.25, 0.75])
        eff_iqr = eff_q3 - eff_q1
        eff_outliers = df_temp[(df_temp['efficiency'] < eff_q1 - 1.5 * eff_iqr) |
                               (df_temp['efficiency'] > eff_q3 + 1.5 * eff_iqr)]
        print(f"效率异常值数量: {len(eff_outliers)} ({len(eff_outliers) / len(df_temp) * 100:.1f}%)")

    def analyze_learnability(self, df):
        """分析数据可学习性"""
        print("数据可学习性分析: ")

        # 计算信噪比
        target = df['emissionReductionTCO2e']

        # 按主要特征分组分析变异
        area_groups = pd.cut(df['plantingAreaAcre'], bins=5, labels=False)
        months_groups = pd.cut(df['plantingMonths'], bins=5, labels=False)

        within_group_var = 0
        between_group_var = 0

        for area_g in range(5):
            for month_g in range(5):
                group_data = target[(area_groups == area_g) & (months_groups == month_g)]
                if len(group_data) > 1:
                    within_group_var += group_data.var() * len(group_data)
                between_group_var += (group_data.mean() - target.mean()) ** 2 * len(group_data)

        within_group_var /= len(target)
        between_group_var /= len(target)

        signal_to_noise = between_group_var / (within_group_var + 1e-8)
        print(f"信噪比: {signal_to_noise:.4f}")

        if signal_to_noise < 0.1:
            print("警告: 信噪比过低，数据可能难以学习")
        elif signal_to_noise < 0.5:
            print("注意: 信噪比较低，需要复杂模型")
        else:
            print("信噪比合理，数据应该可以学习")

        self.signal_to_noise = signal_to_noise

    def analyze_predictability(self, df):
        """可预测性分析"""
        print("7.可预测性分析")
        print("-" * 40)

        # 理论最佳预测性能
        target = df['emissionReductionTCO2e']

        # 基于分类特征的分组预测
        grouped_predictions = df.groupby(['treeType', 'soilFertility', 'climateType'])['emissionReductionTCO2e'].mean()

        # 计算如果每个样本用组均值预测的R²
        df_temp = df.copy()
        df_temp['group_key'] = df_temp['treeType'].astype(str) + '_' + \
                               df_temp['soilFertility'].astype(str) + '_' + \
                               df_temp['climateType'].astype(str)

        group_means = df_temp.groupby('group_key')['emissionReductionTCO2e'].transform('mean')

        from sklearn.metrics import r2_score
        theoretical_r2 = r2_score(target, group_means)
        print(f"基于分类特征分组的理论最佳R²: {theoretical_r2:.4f}")

        # 如果这个R²很低，说明分类特征本身解释力不强
        if theoretical_r2 < 0.3:
            print("警告: 分类特征解释力很低")
        elif theoretical_r2 < 0.6:
            print("分类特征解释力中等")
        else:
            print("分类特征解释力较好")

        # 分析线性可分性
        from sklearn.linear_model import LinearRegression

        # 只用数值特征的线性回归
        X_numeric = df[['plantingAreaAcre', 'plantingMonths']]
        lr = LinearRegression()
        lr.fit(X_numeric, target)
        numeric_pred = lr.predict(X_numeric)
        numeric_r2 = r2_score(target, numeric_pred)

        print(f"仅数值特征的线性R²: {numeric_r2:.4f}")

        # 给出总结建议
        self.provide_recommendations(theoretical_r2, numeric_r2, self.signal_to_noise)

    def provide_recommendations(self, theoretical_r2, numeric_r2, signal_to_noise):
        """提供改进建议"""
        print("\n=" * 60)
        print("数据质量诊断和建议")
        print("=" * 60)

        print(f"理论最佳R² (分类特征): {theoretical_r2:.4f}")
        print(f"数值特征线性R²: {numeric_r2:.4f}")
        print(f"信噪比: {signal_to_noise:.4f}")

        print("问题诊断: ")

        issues = []
        if theoretical_r2 < 0.5:
            issues.append("1. 分类特征(树种、土壤、气候)对目标变量的解释力不足")

        if numeric_r2 < 0.5:
            issues.append("2. 数值特征(面积、时间)与目标变量的线性关系较弱")

        if signal_to_noise < 0.5:
            issues.append("3. 数据中噪声过大，信噪比过低")

        if len(issues) == 0:
            print("数据质量良好，模型性能不佳可能是建模方法问题")
        else:
            for issue in issues:
                print(issue)

        print("改进建议: ")

        if theoretical_r2 < 0.5:
            print("• 收集更多相关特征(如树龄、土壤pH、降雨量等)")
        print("• 检查数据标注是否准确")
        print("• 考虑特征工程创建交互项")

        if numeric_r2 < 0.5:
            print("• 尝试非线性变换(对数、多项式等)")
        print("• 检查数值特征是否存在阈值效应")

        if signal_to_noise < 0.5:
            print("• 增加数据量以降低噪声影响")
        print("• 使用集成方法提高鲁棒性")
        print("• 考虑数据清洗和异常值处理")

        print("当前数据限制下的现实期望: ")
        expected_r2 = min(theoretical_r2 * 1.2, 0.8)  # 考虑模型能力的提升
        print(f"预期最佳R²范围: {theoretical_r2:.3f} - {expected_r2:.3f}")

        if expected_r2 < 0.6:
            print("⚠️ 警告: 当前数据质量限制下，很难达到高预测性能")


def main():
    analyzer = DataQualityAnalyzer()
    df = analyzer.load_and_analyze_data('trees.csv')

    print("" + " = "*60)
    print("分析完成！请查看生成的图表以获得更直观的理解。")
    print("=" * 60)

if __name__ == "__main__":
    main()
