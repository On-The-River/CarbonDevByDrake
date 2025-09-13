import pandas as pd
import numpy as np
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, TensorDataset
from sklearn.model_selection import train_test_split, KFold
from sklearn.metrics import mean_squared_error, r2_score, mean_absolute_error
from sklearn.ensemble import RandomForestRegressor
import warnings
from model import OptimizedForestEmissionPredictor, SimplifiedEmissionMLP
import joblib


warnings.filterwarnings('ignore')


def train_with_early_stopping(model, train_loader, val_loader, epochs=200, lr=0.001):
    """改进的训练函数"""
    # 使用MSE Loss + L2正则化
    criterion = nn.MSELoss()
    optimizer = optim.Adam(model.parameters(), lr=lr, weight_decay=1e-4)

    # 简单的学习率调度
    scheduler = optim.lr_scheduler.ReduceLROnPlateau(
        optimizer, mode='min', factor=0.7, patience=15, verbose=True
    )

    train_losses, val_losses = [], []
    best_val_loss = float('inf')
    patience = 30
    patience_counter = 0

    for epoch in range(epochs):
        # 训练
        model.train()
        train_loss = 0.0
        for batch_X, batch_y in train_loader:
            batch_X, batch_y = batch_X.to(device), batch_y.to(device)

            optimizer.zero_grad()
            outputs = model(batch_X)
            loss = criterion(outputs, batch_y)
            loss.backward()

            # 梯度裁剪
            torch.nn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)
            optimizer.step()

            train_loss += loss.item()

        # 验证
        model.eval()
        val_loss = 0.0
        with torch.no_grad():
            for batch_X, batch_y in val_loader:
                batch_X, batch_y = batch_X.to(device), batch_y.to(device)
                outputs = model(batch_X)
                loss = criterion(outputs, batch_y)
                val_loss += loss.item()

        avg_train_loss = train_loss / len(train_loader)
        avg_val_loss = val_loss / len(val_loader)

        train_losses.append(avg_train_loss)
        val_losses.append(avg_val_loss)

        scheduler.step(avg_val_loss)

        # 早停
        if avg_val_loss < best_val_loss:
            best_val_loss = avg_val_loss
            patience_counter = 0
            torch.save(model.state_dict(), 'best_simple_model.pth')
        else:
            patience_counter += 1

        if epoch % 20 == 0:
            print(f'Epoch {epoch + 1}/{epochs} - Train: {avg_train_loss:.4f}, Val: {avg_val_loss:.4f}')

        if patience_counter >= patience:
            print(f"早停在轮次 {epoch + 1}")
            break

    # 加载最佳模型
    model.load_state_dict(torch.load('best_simple_model.pth'))
    return train_losses, val_losses


def cross_validate_model(X, y, n_splits=5):
    """交叉验证"""
    kf = KFold(n_splits=n_splits, shuffle=True, random_state=42)
    cv_scores = []

    for fold, (train_idx, val_idx) in enumerate(kf.split(X)):
        print(f"折{fold + 1} / {n_splits}")

        X_train, X_val = X[train_idx], X[val_idx]
        y_train, y_val = y[train_idx], y[val_idx]

        # 转换为张量
        X_train_tensor = torch.FloatTensor(X_train).to(device)
        y_train_tensor = torch.FloatTensor(y_train).to(device)
        X_val_tensor = torch.FloatTensor(X_val).to(device)
        y_val_tensor = torch.FloatTensor(y_val).to(device)

        # 创建数据加载器
        train_dataset = TensorDataset(X_train_tensor, y_train_tensor)
        val_dataset = TensorDataset(X_val_tensor, y_val_tensor)
        train_loader = DataLoader(train_dataset, batch_size=64, shuffle=True)
        val_loader = DataLoader(val_dataset, batch_size=64, shuffle=False)

        # 创建模型
        model = SimplifiedEmissionMLP(X.shape[1]).to(device)

        # 训练
        train_losses, val_losses = train_with_early_stopping(
            model, train_loader, val_loader, epochs=150, lr=0.001
        )

        # 评估
        model.eval()
        with torch.no_grad():
            val_pred = model(X_val_tensor).cpu().numpy()
        val_score = r2_score(y_val, val_pred)
        cv_scores.append(val_score)
        print(f"折 {fold + 1} R² 分数: {val_score:.4f}")

        print(f"平均R² 分数: {np.mean(cv_scores): .4f} ± {np.std(cv_scores): .4f}")
    return cv_scores


def compare_with_baseline(X, y):
    """与基线模型比较"""
    print("训练基线模型(RandomForest)...")

    # 划分数据
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # 随机森林
    rf = RandomForestRegressor(n_estimators=100, max_depth=10, random_state=42)
    rf.fit(X_train, y_train)
    rf_pred = rf.predict(X_test)
    rf_r2 = r2_score(y_test, rf_pred)
    rf_rmse = np.sqrt(mean_squared_error(y_test, rf_pred))

    print(f"随机森林 - R²: {rf_r2:.4f}, RMSE: {rf_rmse:.4f}")

    # 特征重要性
    if hasattr(rf, 'feature_importances_'):
        feature_importance = pd.DataFrame({
            'feature': range(len(rf.feature_importances_)),
            'importance': rf.feature_importances_
        }).sort_values('importance', ascending=False)

    print("前10个重要特征: ")
    print(feature_importance.head(10))

    return rf_r2, rf_rmse


def main():
    global device
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f'使用设备: {device}')

    # 初始化优化的预测器
    predictor = OptimizedForestEmissionPredictor()

    # 加载数据
    X, y = predictor.load_and_preprocess_data('trees.csv')

    # 准备数据
    X_scaled, y = predictor.prepare_data_with_cv(X, y)

    print(f"最终数据形状: X={X_scaled.shape}, y={y.shape}")
    print(f"目标变量统计: 均值={y.mean():.2f}, 标准差={y.std():.2f}")

    # 与基线模型比较
    baseline_r2, baseline_rmse = compare_with_baseline(X_scaled, y)

    # 交叉验证神经网络
    print("交叉验证神经网络...")
    cv_scores = cross_validate_model(X_scaled, y, n_splits=5)

    # 最终训练和评估
    print("最终模型训练...")
    X_train, X_test, y_train, y_test = train_test_split(X_scaled, y, test_size=0.2, random_state=42)

    # 转换为张量
    X_train_tensor = torch.FloatTensor(X_train).to(device)
    y_train_tensor = torch.FloatTensor(y_train).to(device)
    X_test_tensor = torch.FloatTensor(X_test).to(device)

    # 创建数据加载器
    train_dataset = TensorDataset(X_train_tensor, y_train_tensor)
    train_loader = DataLoader(train_dataset, batch_size=64, shuffle=True)

    # 最终模型
    final_model = SimplifiedEmissionMLP(X_train.shape[1]).to(device)

    # 简单训练（不用验证集，因为已经交叉验证过了）
    criterion = nn.MSELoss()
    optimizer = optim.Adam(final_model.parameters(), lr=0.001, weight_decay=1e-4)

    final_model.train()
    for epoch in range(100):
        for batch_X, batch_y in train_loader:
            optimizer.zero_grad()
            outputs = final_model(batch_X)
            loss = criterion(outputs, batch_y)
            loss.backward()
            optimizer.step()

    # 最终评估
    final_model.eval()
    with torch.no_grad():
        y_pred = final_model(X_test_tensor).cpu().numpy()

    final_r2 = r2_score(y_test, y_pred)
    final_rmse = np.sqrt(mean_squared_error(y_test, y_pred))
    final_mae = mean_absolute_error(y_test, y_pred)
    final_mape = np.mean(np.abs((y_test - y_pred) / (y_test + 1e-8))) * 100

    print(f"最终神经网络性能: ")
    print(f"R²: {final_r2:.4f}")
    print(f"RMSE: {final_rmse:.4f}")
    print(f"MAE: {final_mae:.4f}")
    print(f"MAPE: {final_mape:.2f}%")

    print(f"模型对比: ")
    print(f"随机森林: R²={baseline_r2:.4f}, RMSE={baseline_rmse:.4f}")
    print(f"神经网络: R²={final_r2:.4f}, RMSE={final_rmse:.4f}")

    joblib.dump(predictor.scaler, './robust_scaler.pkl')
    joblib.dump(predictor.area_quartiles, './area_quartiles.pkl')
    joblib.dump(predictor.time_quartiles, './time_quartiles.pkl')
    joblib.dump(predictor.feature_names, './feature_names.pkl')


if __name__ == "__main__":
    main()
