import pandas as pd
import numpy as np
from sklearn.preprocessing import MinMaxScaler, StandardScaler
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, TensorDataset
import matplotlib.pyplot as plt
import seaborn as sns
import os
import warnings
from datetime import datetime, timedelta
import math
from typing import Tuple, List, Optional
import json
import pickle
from chronos import ChronosPipeline

# 尝试导入预训练模型相关库
try:
    from transformers import AutoModelForSeq2SeqLM, AutoTokenizer
    CHRONOS_AVAILABLE = True
    print("Chronos模型库可用")
except ImportError:
    CHRONOS_AVAILABLE = False
    print("警告: Chronos模型库未安装，将使用自定义Transformer模型")

warnings.filterwarnings('ignore')


class PositionalEncoding(nn.Module):
    """位置编码"""
    def __init__(self, d_model, max_seq_length=500):
        super(PositionalEncoding, self).__init__()
        
        pe = torch.zeros(max_seq_length, d_model)
        position = torch.arange(0, max_seq_length, dtype=torch.float).unsqueeze(1)
        div_term = torch.exp(torch.arange(0, d_model, 2).float() * (-math.log(10000.0) / d_model))
        
        pe[:, 0::2] = torch.sin(position * div_term)
        pe[:, 1::2] = torch.cos(position * div_term)
        pe = pe.unsqueeze(0).transpose(0, 1)
        
        self.register_buffer('pe', pe)
        
    def forward(self, x):
        return x + self.pe[:x.size(0), :]


class TimeSeriesTransformer(nn.Module):
    """基于Transformer的时间序列预测模型"""
    def __init__(self, input_size, d_model=128, nhead=8, num_layers=6, 
                 output_size=10, max_seq_length=500, dropout=0.1):
        super(TimeSeriesTransformer, self).__init__()
        
        self.input_projection = nn.Linear(input_size, d_model)
        self.pos_encoding = PositionalEncoding(d_model, max_seq_length)
        
        encoder_layer = nn.TransformerEncoderLayer(
            d_model=d_model, nhead=nhead, dim_feedforward=d_model*4,
            dropout=dropout, activation='gelu', batch_first=True
        )
        self.transformer = nn.TransformerEncoder(encoder_layer, num_layers=num_layers)
        
        # 多头输出
        self.output_heads = nn.ModuleList([
            nn.Sequential(
                nn.Linear(d_model, d_model//2),
                nn.ReLU(),
                nn.Dropout(dropout),
                nn.Linear(d_model//2, d_model//4),
                nn.ReLU(),
                nn.Dropout(dropout),
                nn.Linear(d_model//4, 2)  # 每天预测最低价和最高价
            ) for _ in range(5)  # 5天
        ])
        
        self.dropout = nn.Dropout(dropout)
        self.d_model = d_model
        
    def forward(self, x):
        # x shape: (batch_size, seq_len, input_size)
        x = self.input_projection(x) * math.sqrt(self.d_model)
        x = x.permute(1, 0, 2)  # (seq_len, batch_size, d_model)
        x = self.pos_encoding(x)
        x = x.permute(1, 0, 2)  # (batch_size, seq_len, d_model)
        
        transformer_out = self.transformer(x)
        
        # 使用最后一个时间步的输出
        last_hidden = transformer_out[:, -1, :]
        last_hidden = self.dropout(last_hidden)
        
        # 多头预测
        outputs = []
        for head in self.output_heads:
            outputs.append(head(last_hidden))
        
        return torch.cat(outputs, dim=1)  # (batch_size, 10)


class ChronosWrapper:
    """Chronos预训练模型包装器"""
    def __init__(self, model_size="small"):
        if not CHRONOS_AVAILABLE:
            raise ImportError("Chronos library not available")
        
        model_map = {
            "tiny": "amazon/chronos-t5-tiny",
            "mini": "amazon/chronos-t5-mini", 
            "small": "amazon/chronos-t5-small",
            "base": "amazon/chronos-t5-base",
            "large": "amazon/chronos-t5-large"
        }
        
        model_id = model_map.get(model_size, "amazon/chronos-t5-small")
        print(f"Loading Chronos model: {model_id}")
        
        try:
            self.pipeline = ChronosPipeline.from_pretrained(
                model_id,
                device_map="auto" if torch.cuda.is_available() else "cpu",
                dtype=torch.bfloat16 if torch.cuda.is_available() else torch.float32,
            )
            print("Chronos model loaded successfully!")
        except Exception as e:
            print(f"Failed to load Chronos model: {e}")
            raise
    
    def predict(self, context_data, prediction_length=5):
        """使用Chronos进行预测"""
        if self.pipeline is None:
            return None

        predictions = []
        for target_idx in [0, 1]:
            if target_idx < context_data.shape[1]:  # 添加边界检查
                context = context_data[:, target_idx]  # 使用最后30天的目标价格作为上下文
                pred = self.pipeline.predict(context, prediction_length=5)
                if pred is not None:
                    predictions.append(pred)

        if len(predictions) == 2:
            result = []
            for i in range(5):
                result.extend([predictions[0][i], predictions[1][i]])
            return np.array([result])

        return None


def calculate_technical_indicators(df):
    """计算技术指标"""
    # 移动平均线
    for window in [5, 10, 20, 30, 60]:
        df[f'MA{window}'] = df['收盘价'].rolling(window=window).mean()
    
    # 指数移动平均
    for span in [12, 26, 50]:
        df[f'EMA{span}'] = df['收盘价'].ewm(span=span, adjust=False).mean()
    
    # MACD
    df['MACD'] = df['EMA12'] - df['EMA26']
    df['MACD_signal'] = df['MACD'].ewm(span=9, adjust=False).mean()
    df['MACD_hist'] = df['MACD'] - df['MACD_signal']
    
    # RSI
    delta = df['收盘价'].diff()
    gain = (delta.where(delta > 0, 0)).rolling(window=14).mean()
    loss = (-delta.where(delta < 0, 0)).rolling(window=14).mean()
    rs = gain / loss
    df['RSI'] = 100 - (100 / (1 + rs))
    
    # 布林带
    for window in [20]:
        ma = df['收盘价'].rolling(window=window).mean()
        std = df['收盘价'].rolling(window=window).std()
        df[f'BB_upper_{window}'] = ma + (std * 2)
        df[f'BB_lower_{window}'] = ma - (std * 2)
        df[f'BB_width_{window}'] = df[f'BB_upper_{window}'] - df[f'BB_lower_{window}']
        df[f'BB_position_{window}'] = (df['收盘价'] - df[f'BB_lower_{window}']) / df[f'BB_width_{window}']
    
    # ATR (Average True Range)
    high_low = df['最高价'] - df['最低价']
    high_close = np.abs(df['最高价'] - df['收盘价'].shift())
    low_close = np.abs(df['最低价'] - df['收盘价'].shift())
    ranges = pd.concat([high_low, high_close, low_close], axis=1)
    true_range = np.max(ranges, axis=1)
    df['ATR'] = pd.Series(true_range).rolling(window=14).mean()
    
    # 成交量指标
    df['volume_ma_20'] = df['成交量'].rolling(window=20).mean()
    df['volume_ratio'] = df['成交量'] / df['volume_ma_20']
    
    # 价格动量
    for period in [5, 10, 20]:
        df[f'momentum_{period}'] = df['收盘价'] / df['收盘价'].shift(period) - 1
    
    # 波动率
    for window in [10, 20, 30]:
        df[f'volatility_{window}'] = df['收盘价'].pct_change().rolling(window=window).std()
    
    # 价格位置
    for window in [20, 50]:
        df[f'price_position_{window}'] = (df['收盘价'] - df['收盘价'].rolling(window=window).min()) / \
                                        (df['收盘价'].rolling(window=window).max() - df['收盘价'].rolling(window=window).min())
    
    # 成交量加权平均价格 (VWAP)
    df['VWAP'] = (df['成交量'] * (df['最高价'] + df['最低价'] + df['收盘价']) / 3).cumsum() / df['成交量'].cumsum()
    
    return df


def load_and_preprocess_data(file_path):
    """加载和预处理数据"""
    try:
        df = pd.read_csv(file_path, encoding='utf-8-sig')
    except:
        df = pd.read_csv(file_path, encoding='gbk')
    
    df['日期'] = pd.to_datetime(df['日期'])
    df = df.sort_values('日期').reset_index(drop=True)
    
    # 数据清洗
    numeric_columns = ['开盘价', '收盘价', '最低价', '最高价', '成交量']
    for col in numeric_columns:
        df[col] = pd.to_numeric(df[col], errors='coerce')
    
    # 移除异常值
    for col in numeric_columns[:-1]:  # 除了成交量
        Q1 = df[col].quantile(0.01)
        Q3 = df[col].quantile(0.99)
        IQR = Q3 - Q1
        lower_bound = Q1 - 1.5 * IQR
        upper_bound = Q3 + 1.5 * IQR
        df[col] = df[col].clip(lower_bound, upper_bound)
    
    # 计算技术指标
    df = calculate_technical_indicators(df)
    
    # 移除包含NaN的行
    df = df.dropna()
    
    # 定义特征列
    feature_columns = [
        '开盘价', '收盘价', '最低价', '最高价', '成交量',
        'MA5', 'MA10', 'MA20', 'MA30', 'MA60',
        'EMA12', 'EMA26', 'EMA50',
        'MACD', 'MACD_signal', 'MACD_hist',
        'RSI', 'ATR',
        'BB_upper_20', 'BB_lower_20', 'BB_width_20', 'BB_position_20',
        'volume_ma_20', 'volume_ratio',
        'momentum_5', 'momentum_10', 'momentum_20',
        'volatility_10', 'volatility_20', 'volatility_30',
        'price_position_20', 'price_position_50',
        'VWAP'
    ]
    
    target_columns = ['最低价', '最高价']
    
    # 确保所有特征列都存在
    existing_features = [col for col in feature_columns if col in df.columns]
    
    return df, existing_features, target_columns


def create_sequences(df, feature_columns, target_columns, input_days=30, pred_days=5):
    """创建时间序列数据"""
    # 特征标准化
    feature_scaler = StandardScaler()
    target_scaler = MinMaxScaler()
    
    # 分别标准化特征和目标
    df_features_scaled = pd.DataFrame(
        feature_scaler.fit_transform(df[feature_columns]),
        columns=feature_columns,
        index=df.index
    )
    
    df_targets_scaled = pd.DataFrame(
        target_scaler.fit_transform(df[target_columns]),
        columns=target_columns,
        index=df.index
    )
    
    X, y = [], []
    
    for i in range(len(df) - input_days - pred_days + 1):
        # 输入序列
        X.append(df_features_scaled.iloc[i:i + input_days].values)
        
        # 目标序列 - 未来pred_days天的最低价和最高价
        target_seq = df_targets_scaled.iloc[i + input_days:i + input_days + pred_days][target_columns].values
        y.append(target_seq.flatten())  # 展平为一维数组
    
    return (np.array(X), np.array(y), 
            feature_scaler, target_scaler,
            df[target_columns].min().values,
            df[target_columns].max().values)


class EarlyStopping:
    """早停机制"""
    def __init__(self, patience=15, min_delta=1e-6, restore_best_weights=True):
        self.patience = patience
        self.min_delta = min_delta
        self.restore_best_weights = restore_best_weights
        self.counter = 0
        self.best_loss = float('inf')
        self.best_weights = None
        
    def __call__(self, val_loss, model):
        if val_loss < self.best_loss - self.min_delta:
            self.best_loss = val_loss
            self.counter = 0
            if self.restore_best_weights:
                self.best_weights = model.state_dict().copy()
        else:
            self.counter += 1
            
        if self.counter >= self.patience:
            if self.restore_best_weights and self.best_weights is not None:
                model.load_state_dict(self.best_weights)
            return True
        return False


def train_transformer_model(model, train_loader, val_loader, epochs=100, 
                           learning_rate=0.001, device='cuda'):
    """训练Transformer模型"""
    model = model.to(device)
    
    # 优化器和调度器
    optimizer = optim.AdamW(model.parameters(), lr=learning_rate, weight_decay=0.01)
    scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=epochs)
    
    # 损失函数
    criterion = nn.MSELoss()
    
    # 早停
    early_stopping = EarlyStopping(patience=20, min_delta=1e-6)
    
    train_losses = []
    val_losses = []
    best_val_loss = float('inf')
    
    print("开始训练Transformer模型...")
    for epoch in range(epochs):
        # 训练阶段
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
        
        # 验证阶段
        model.eval()
        val_loss = 0.0
        with torch.no_grad():
            for batch_X, batch_y in val_loader:
                batch_X, batch_y = batch_X.to(device), batch_y.to(device)
                outputs = model(batch_X)
                loss = criterion(outputs, batch_y)
                val_loss += loss.item()
        
        train_loss /= len(train_loader)
        val_loss /= len(val_loader)
        
        train_losses.append(train_loss)
        val_losses.append(val_loss)
        
        if val_loss < best_val_loss:
            best_val_loss = val_loss
        
        if epoch % 10 == 0:
            print(f'Epoch [{epoch+1}/{epochs}], Train Loss: {train_loss:.6f}, '
                  f'Val Loss: {val_loss:.6f}, LR: {scheduler.get_last_lr()[0]:.6f}')
        
        scheduler.step()
        
        # 早停检查
        if early_stopping(val_loss, model):
            print(f"Early stopping at epoch {epoch+1}")
            break
    
    return model, train_losses, val_losses


class HybridModel:
    """混合预测模型"""
    def __init__(self, input_size, device='cuda'):
        self.device = device
        
        # Transformer模型
        self.transformer_model = TimeSeriesTransformer(
            input_size=input_size,
            d_model=256,
            nhead=8,
            num_layers=6,
            output_size=10,
            dropout=0.1
        ).to(device)
        
        # Chronos模型 (如果可用)
        self.chronos_model = None
        if CHRONOS_AVAILABLE:
            try:
                self.chronos_model = ChronosWrapper("small")
            except Exception as e:
                print(f"Chronos model not available: {e}")
        
        self.is_trained = False
        
    def train(self, train_loader, val_loader, epochs=100, learning_rate=0.001):
        """训练Transformer模型"""
        self.transformer_model, train_losses, val_losses = train_transformer_model(
            self.transformer_model, train_loader, val_loader, 
            epochs, learning_rate, self.device
        )
        self.is_trained = True
        return train_losses, val_losses
    
    def predict_transformer(self, X):
        """使用Transformer预测"""
        if not self.is_trained:
            raise ValueError("Model must be trained first")
        
        self.transformer_model.eval()
        X_tensor = torch.FloatTensor(X).to(self.device)
        
        with torch.no_grad():
            predictions = self.transformer_model(X_tensor)
        
        return predictions.cpu().numpy()
    
    def predict_chronos(self, context_data, target_scaler):
        """使用Chronos预测"""
        if self.chronos_model is None:
            return None
        
        predictions = []
        
        # 对最低价和最高价分别预测
        for target_idx in [2, 3]:  # 假设最低价和最高价在位置2和3
            context = context_data[:, target_idx]  # 使用最后30天的目标价格作为上下文
            pred = self.chronos_model.predict(context, prediction_length=5)
            if pred is not None:
                predictions.append(pred)
        
        if len(predictions) == 2:
            # 重构为 [day1_low, day1_high, day2_low, day2_high, ...]
            result = []
            for i in range(5):
                result.extend([predictions[0][i], predictions[1][i]])
            return np.array([result])
        
        return None
    
    def predict_ensemble(self, X, context_data=None, target_scaler=None, weights=[0.7, 0.3]):
        """集成预测"""
        transformer_pred = self.predict_transformer(X)
        
        if self.chronos_model is not None and context_data is not None:
            chronos_pred = self.predict_chronos(context_data, target_scaler)
            
            if chronos_pred is not None:
                # 加权集成
                ensemble_pred = (weights[0] * transformer_pred + 
                               weights[1] * chronos_pred)
                return ensemble_pred
        
        return transformer_pred


def evaluate_model(model, test_loader, target_scaler, device='cuda'):
    """评估模型性能"""
    model.eval()
    predictions = []
    actuals = []
    
    with torch.no_grad():
        for batch_X, batch_y in test_loader:
            batch_X, batch_y = batch_X.to(device), batch_y.to(device)
            outputs = model(batch_X)
            predictions.extend(outputs.cpu().numpy())
            actuals.extend(batch_y.cpu().numpy())
    
    predictions = np.array(predictions)
    actuals = np.array(actuals)
    
    # 反标准化
    pred_reshaped = predictions.reshape(-1, 5, 2)
    actual_reshaped = actuals.reshape(-1, 5, 2)
    
    pred_denorm = np.zeros_like(pred_reshaped)
    actual_denorm = np.zeros_like(actual_reshaped)
    
    for i in range(pred_reshaped.shape[0]):
        pred_denorm[i] = target_scaler.inverse_transform(pred_reshaped[i])
        actual_denorm[i] = target_scaler.inverse_transform(actual_reshaped[i])
    
    # 计算指标
    mae = np.mean(np.abs(pred_denorm - actual_denorm))
    mse = np.mean((pred_denorm - actual_denorm) ** 2)
    rmse = np.sqrt(mse)
    
    # MAPE
    mape = np.mean(np.abs((pred_denorm - actual_denorm) / (actual_denorm + 1e-8))) * 100
    
    # 方向准确率
    pred_direction = np.sign(np.diff(pred_denorm.mean(axis=2), axis=1))
    actual_direction = np.sign(np.diff(actual_denorm.mean(axis=2), axis=1))
    direction_accuracy = np.mean(pred_direction == actual_direction) * 100
    
    return {
        'MAE': mae,
        'MSE': mse, 
        'RMSE': rmse,
        'MAPE': mape,
        'Direction_Accuracy': direction_accuracy
    }


def generate_future_dates(last_date, days=5):
    """生成未来工作日"""
    future_dates = []
    current_date = last_date + timedelta(days=1)
    
    while len(future_dates) < days:
        # 跳过周末
        if current_date.weekday() < 5:  # 0-6, 0是周一
            future_dates.append(current_date)
        current_date += timedelta(days=1)
    
    return future_dates


def plot_results(train_losses, val_losses, predictions, actual_prices=None, 
                future_dates=None, save_path="results/"):
    """绘制结果"""
    os.makedirs(save_path, exist_ok=True)
    
    # 训练历史
    plt.figure(figsize=(15, 5))
    
    plt.subplot(1, 2, 1)
    plt.plot(train_losses, label='Training Loss', alpha=0.7)
    plt.plot(val_losses, label='Validation Loss', alpha=0.7)
    plt.title('Training History')
    plt.xlabel('Epoch')
    plt.ylabel('Loss')
    plt.legend()
    plt.grid(True)
    
    plt.subplot(1, 2, 2)
    plt.plot(np.log(train_losses), label='Log Training Loss', alpha=0.7)
    plt.plot(np.log(val_losses), label='Log Validation Loss', alpha=0.7)
    plt.title('Training History (Log Scale)')
    plt.xlabel('Epoch')
    plt.ylabel('Log Loss')
    plt.legend()
    plt.grid(True)
    
    plt.tight_layout()
    plt.savefig(f"{save_path}/training_history.png", dpi=300, bbox_inches='tight')
    plt.show()
    
    # 预测结果
    if predictions is not None:
        plt.figure(figsize=(12, 8))
        
        pred_reshaped = predictions.reshape(-1, 5, 2)
        
        days = range(1, 6)
        
        plt.subplot(2, 1, 1)
        plt.plot(days, pred_reshaped[0, :, 0], 'ro-', label='Predicted Low', linewidth=2)
        plt.plot(days, pred_reshaped[0, :, 1], 'bo-', label='Predicted High', linewidth=2)
        plt.fill_between(days, pred_reshaped[0, :, 0], pred_reshaped[0, :, 1], 
                        alpha=0.3, color='gray', label='Price Range')
        
        if actual_prices is not None:
            actual_reshaped = actual_prices.reshape(-1, 5, 2)
            plt.plot(days, actual_reshaped[0, :, 0], 'r^--', label='Actual Low', alpha=0.7)
            plt.plot(days, actual_reshaped[0, :, 1], 'b^--', label='Actual High', alpha=0.7)
        
        plt.title('Price Range Prediction')
        plt.xlabel('Future Days')
        plt.ylabel('Price')
        plt.legend()
        plt.grid(True)
        
        plt.subplot(2, 1, 2)
        pred_mid = (pred_reshaped[0, :, 0] + pred_reshaped[0, :, 1]) / 2
        plt.plot(days, pred_mid, 'go-', label='Predicted Mid Price', linewidth=2)
        
        if actual_prices is not None:
            actual_mid = (actual_reshaped[0, :, 0] + actual_reshaped[0, :, 1]) / 2
            plt.plot(days, actual_mid, 'g^--', label='Actual Mid Price', alpha=0.7)
        
        plt.title('Mid Price Prediction')
        plt.xlabel('Future Days')
        plt.ylabel('Price')
        plt.legend()
        plt.grid(True)
        
        plt.tight_layout()
        plt.savefig(f"{save_path}/predictions.png", dpi=300, bbox_inches='tight')
        plt.show()


def save_model_artifacts(model, feature_scaler, target_scaler, feature_columns, 
                        train_losses, val_losses, metrics, save_path="models/"):
    """保存模型相关文件"""
    os.makedirs(save_path, exist_ok=True)
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    
    # 保存模型
    if hasattr(model, 'transformer_model'):
        torch.save({
            'model_state_dict': model.transformer_model.state_dict(),
            'model_config': {
                'input_size': len(feature_columns),
                'd_model': 256,
                'nhead': 8,
                'num_layers': 6,
                'output_size': 10
            }
        }, f"{save_path}/transformer_model_{timestamp}.pth")
    else:
        torch.save({
            'model_state_dict': model.state_dict(),
            'model_config': {
                'input_size': len(feature_columns)
            }
        }, f"{save_path}/model_{timestamp}.pth")
    
    # 保存预处理器
    with open(f"{save_path}/scalers_{timestamp}.pkl", 'wb') as f:
        pickle.dump({
            'feature_scaler': feature_scaler,
            'target_scaler': target_scaler,
            'feature_columns': feature_columns
        }, f)

    def convert_to_serializable(obj):
        """将numpy类型转换为可序列化的Python类型"""
        if isinstance(obj, np.ndarray):
            return obj.tolist()
        elif isinstance(obj, (np.float32, np.float64)):
            return float(obj)
        elif isinstance(obj, (np.int32, np.int64)):
            return int(obj)
        elif isinstance(obj, dict):
            return {key: convert_to_serializable(value) for key, value in obj.items()}
        elif isinstance(obj, list):
            return [convert_to_serializable(item) for item in obj]
        return obj

    # 保存训练历史和指标
    serializable_data = {
        'train_losses': convert_to_serializable(train_losses),
        'val_losses': convert_to_serializable(val_losses),
        'metrics': convert_to_serializable(metrics),
        'timestamp': timestamp,
        'feature_columns': feature_columns
    }

    with open(f"{save_path}/training_info_{timestamp}.json", 'w') as f:
        json.dump(serializable_data, f, indent=2, ensure_ascii=False)

    print(f"模型文件已保存到 {save_path} 目录")
    return f"{save_path}/transformer_model_{timestamp}.pth"


def main():
    """主函数"""
    # 配置
    CONFIG = {
        'file_path': "碳交易数据.csv",
        'output_file': "碳交易价格预测_未来5天_Transformer.csv",
        'input_days': 30,
        'pred_days': 5,
        'train_ratio': 0.7,
        'val_ratio': 0.15,
        'batch_size': 32,
        'epochs': 100,
        'learning_rate': 0.001,
        'device': 'cuda' if torch.cuda.is_available() else 'cpu'
    }
    
    print(f"使用设备: {CONFIG['device']}")
    print("=" * 60)
    
    try:
        # 1. 数据加载和预处理
        print("1. 加载和预处理数据...")
        df, feature_columns, target_columns = load_and_preprocess_data(CONFIG['file_path'])
        print(f"数据形状: {df.shape}")
        print(f"特征数量: {len(feature_columns)}")
        print(f"数据时间范围: {df['日期'].min()} 到 {df['日期'].max()}")
        
        # 检查数据量
        min_required = CONFIG['input_days'] + CONFIG['pred_days'] + 50
        if len(df) < min_required:
            raise ValueError(f"数据量不足！需要至少 {min_required} 条记录，当前只有 {len(df)} 条")
        
        # 2. 创建序列数据
        print("2. 创建时间序列数据...")
        X, y, feature_scaler, target_scaler, target_min, target_max = create_sequences(
            df, feature_columns, target_columns, 
            CONFIG['input_days'], CONFIG['pred_days']
        )
        print(f"序列数据形状: X={X.shape}, y={y.shape}")
        
        # 3. 数据集划分
        print("3. 划分训练/验证/测试集...")
        total_samples = len(X)
        train_size = int(total_samples * CONFIG['train_ratio'])
        val_size = int(total_samples * CONFIG['val_ratio'])
        
        X_train = X[:train_size]
        y_train = y[:train_size]
        X_val = X[train_size:train_size + val_size]
        y_val = y[train_size:train_size + val_size]
        X_test = X[train_size + val_size:]
        y_test = y[train_size + val_size:]
        
        print(f"训练集: {len(X_train)} 样本")
        print(f"验证集: {len(X_val)} 样本")
        print(f"测试集: {len(X_test)} 样本")
        
        # 创建数据加载器
        train_dataset = TensorDataset(torch.FloatTensor(X_train), torch.FloatTensor(y_train))
        val_dataset = TensorDataset(torch.FloatTensor(X_val), torch.FloatTensor(y_val))
        test_dataset = TensorDataset(torch.FloatTensor(X_test), torch.FloatTensor(y_test))
        
        train_loader = DataLoader(train_dataset, batch_size=CONFIG['batch_size'], shuffle=True)
        val_loader = DataLoader(val_dataset, batch_size=CONFIG['batch_size'], shuffle=False)
        test_loader = DataLoader(test_dataset, batch_size=CONFIG['batch_size'], shuffle=False)
        
        # 4. 构建混合模型
        print("4. 构建混合预测模型...")
        hybrid_model = HybridModel(
            input_size=len(feature_columns),
            device=CONFIG['device']
        )
        
        # 5. 训练模型
        print("5. 开始训练模型...")
        train_losses, val_losses = hybrid_model.train(
            train_loader, val_loader,
            epochs=CONFIG['epochs'],
            learning_rate=CONFIG['learning_rate']
        )
        
        print(f"训练完成! 最佳验证损失: {min(val_losses):.6f}")
        
        # 6. 模型评估
        print("6. 评估模型性能...")
        test_metrics = evaluate_model(
            hybrid_model.transformer_model, 
            test_loader, 
            target_scaler, 
            CONFIG['device']
        )
        
        print("测试集评估结果:")
        for metric, value in test_metrics.items():
            print(f"  {metric}: {value:.4f}")
        
        # 7. 预测未来价格
        print("7. 预测未来5天价格...")
        
        # 获取最新的输入数据
        latest_features = df[feature_columns].iloc[-CONFIG['input_days']:].values
        latest_features_scaled = feature_scaler.transform(latest_features)
        latest_input = latest_features_scaled.reshape(1, CONFIG['input_days'], -1)
        
        # 获取用于Chronos的上下文数据
        context_data = df[target_columns].iloc[-CONFIG['input_days']:].values
        
        # 进行预测
        try:
            # 尝试集成预测
            future_predictions = hybrid_model.predict_ensemble(
                latest_input, 
                context_data=context_data,
                target_scaler=target_scaler
            )
        except Exception as e:
            print(f"集成预测失败，使用Transformer预测: {e}")
            future_predictions = hybrid_model.predict_transformer(latest_input)
        
        # 反标准化预测结果
        future_predictions_reshaped = future_predictions.reshape(-1, CONFIG['pred_days'], 2)
        future_predictions_denorm = np.zeros_like(future_predictions_reshaped)
        
        for i in range(future_predictions_reshaped.shape[0]):
            future_predictions_denorm[i] = target_scaler.inverse_transform(
                future_predictions_reshaped[i]
            )
        
        # 后处理：确保最低价 <= 最高价
        for i in range(future_predictions_denorm.shape[0]):
            for j in range(future_predictions_denorm.shape[1]):
                low, high = future_predictions_denorm[i, j]
                if low > high:
                    future_predictions_denorm[i, j] = [high, low]
        
        # 8. 生成未来日期
        last_date = df['日期'].iloc[-1]
        future_dates = generate_future_dates(last_date, CONFIG['pred_days'])
        
        # 9. 创建结果DataFrame
        results_df = pd.DataFrame({
            '日期': [date.strftime('%Y-%m-%d') for date in future_dates],
            '预测最低价': [round(float(val), 2) for val in future_predictions_denorm[0, :, 0]],
            '预测最高价': [round(float(val), 2) for val in future_predictions_denorm[0, :, 1]]
        })
        
        # 计算预测区间中点和区间宽度
        results_df['预测中位价'] = ((results_df['预测最低价'] + results_df['预测最高价']) / 2).round(2)
        results_df['价格区间宽度'] = (results_df['预测最高价'] - results_df['预测最低价']).round(2)
        
        # 计算相对前一天的变化
        last_close_price = df['收盘价'].iloc[-1]
        results_df['相对变化率(%)'] = ((results_df['预测中位价'] / last_close_price - 1) * 100).round(2)
        
        # 10. 保存结果
        print("8. 保存预测结果...")
        results_df.to_csv(CONFIG['output_file'], index=False, encoding='utf-8-sig')
        
        # 保存详细报告
        report_filename = f"预测报告_{datetime.now().strftime('%Y%m%d_%H%M%S')}.txt"
        with open(report_filename, 'w', encoding='utf-8') as f:
            f.write("碳交易价格预测报告")
            f.write("=" * 50 + "")
            f.write(f"生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
            f.write(f"数据来源: {CONFIG['file_path']}")
            f.write(f"训练数据期间: {df['日期'].min()} 到 {df['日期'].max()}")
            f.write(f"总数据量: {len(df)} 条记录")
            f.write(f"特征数量: {len(feature_columns)}")
            
            f.write("模型配置:")
            f.write(f"  - 输入序列长度: {CONFIG['input_days']} 天")
            f.write(f"  - 预测序列长度: {CONFIG['pred_days']} 天")
            f.write(f"  - 训练epochs: {CONFIG['epochs']}")
            f.write(f"  - 学习率: {CONFIG['learning_rate']}")
            f.write(f"  - 批大小: {CONFIG['batch_size']}")
            
            f.write("模型性能指标:")
            for metric, value in test_metrics.items():
                f.write(f"  - {metric}: {value:.4f}")
            f.write("")

            f.write("预测结果:")
            f.write(results_df.to_string(index=False))
            f.write("")
            
            f.write("风险提示:")
            f.write("- 本预测仅供参考，不构成投资建议")
            f.write("- 金融市场存在不确定性，实际价格可能与预测存在差异")
            f.write("- 建议结合其他分析方法综合判断")
        
        print("9. 生成可视化图表...")
        plot_results(
            train_losses, val_losses, 
            future_predictions, 
            future_dates=future_dates,
            save_path="results/"
        )
        
        # 12. 保存模型
        print("10. 保存模型文件...")
        model_path = save_model_artifacts(
            hybrid_model,
            feature_scaler,
            target_scaler,
            feature_columns,
            train_losses,
            val_losses,
            test_metrics
        )
        
        # 13. 输出结果摘要
        print("" + "=" * 60)
        print("预测完成!")
        print("=" * 60)
        print("预测结果摘要:")
        print(results_df.to_string(index=False))
        
        print(f"文件输出:")
        print(f"  - 预测结果: {CONFIG['output_file']}")
        print(f"  - 详细报告: {report_filename}")
        print(f"  - 模型文件: {model_path}")
        print(f"  - 图表文件: results/ 目录")
        
        print(f"模型性能:")
        for metric, value in test_metrics.items():
            print(f"  - {metric}: {value:.4f}")
        
        # 简单的趋势分析
        print(f"趋势分析:")
        avg_change = results_df['相对变化率(%)'].mean()
        if avg_change > 1:
            trend = "上涨"
        elif avg_change < -1:
            trend = "下跌"
        else:
            trend = "震荡"
        
        print(f"  - 未来5天平均预期变化: {avg_change:.2f}%")
        print(f"  - 整体趋势判断: {trend}")
        print(f"  - 最大预测区间宽度: {results_df['价格区间宽度'].max():.2f}")
        print(f"  - 平均预测区间宽度: {results_df['价格区间宽度'].mean():.2f}")
        
        # 置信度评估
        avg_interval_width = results_df['价格区间宽度'].mean()
        last_volatility = df['收盘价'].pct_change().rolling(20).std().iloc[-1] * 100
        
        if avg_interval_width / results_df['预测中位价'].mean() < 0.05:
            confidence = "高"
        elif avg_interval_width / results_df['预测中位价'].mean() < 0.10:
            confidence = "中"
        else:
            confidence = "低"
        
        print(f"  - 预测置信度: {confidence}")
        print(f"  - 近期波动率: {last_volatility:.2f}%")
        
        print("\n" + "=" * 60)
        
        return results_df, test_metrics
        
    except FileNotFoundError:
        print(f"错误: 找不到数据文件 '{CONFIG['file_path']}'")
        print("请确保数据文件存在且路径正确")
    except Exception as e:
        print(f"发生错误: {str(e)}")
        import traceback
        traceback.print_exc()
        return None, None


if __name__ == "__main__":
    # 设置随机种子确保结果可重现
    torch.manual_seed(42)
    np.random.seed(42)
    
    # 运行主程序
    results, metrics = main()
