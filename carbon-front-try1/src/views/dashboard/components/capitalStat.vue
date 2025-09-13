<template>
  <div class="divBox">
    <!-- 供应量模块 -->
    <div class="card">
      <div class="card-header">
        <span class="card-title">供应量</span>
        <span class="card-subtitle">上月{{ monthSupply.statDate }}</span>
      </div>
      <div class="card-content">
        <div class="content-left">
          <img class="flag-icon" src="@/assets/imgs/icon_capital_one.png" />
          <span class="sum-amount">{{ monthSupply.carbonSupply }}</span>
          <span class="unit">{{ assetsIncomeUnit }}</span>
        </div>
        <div class="vertical-divider"></div>
        <div class="content-right">
          <div class="rate-item">
            <span class="hint">碳信用：</span>
            <span class="rate-value">{{ monthSupply.carbonCredit }}</span>
            <span class="rate-unit">&nbsp;{{ carbonCreditUnit }}</span>
          </div>
          <div class="rate-item">
            <span class="hint">碳配额：</span>
            <span class="rate-value">{{ monthSupply.carbonQuota }}</span>
            <span class="rate-unit">&nbsp;{{ carbonQuotaUnit }}</span>
          </div>
<!--          <div class="rate-item">-->
<!--            <span class="hint">绿&nbsp;&nbsp;&nbsp;证：</span>-->
<!--            <span class="rate-value">{{ monthSupply.greenScore }}</span>-->
<!--            <span class="rate-unit">&nbsp;{{ greenCertUnit }}</span>-->
<!--          </div>-->
        </div>
      </div>
      <div class="card-footer">
        <div class="footer-item">
          <span class="hint">总供应量：</span>
          <span class="value">{{ monthSupply.carbonSupplyTotal }}</span>
          <span class="unit">&nbsp;{{ assetsIncomeUnit }}</span>
        </div>
        <div class="footer-item">
          <span class="hint">碳资产估值：</span>
          <span class="value">¥{{ monthSupply.carbonValuation }}</span>
        </div>
      </div>
    </div>

    <!-- 开发量模块 -->
    <div class="card">
      <div class="card-header">
        <span class="card-title">开发量</span>
        <span class="card-subtitle">上月{{ monthDevelopment.statDate }}</span>
      </div>
      <div class="card-content">
        <div class="content-left">
          <img class="flag-icon" src="@/assets/imgs/icon_capital_two.png" />
          <span class="sum-amount">{{ monthDevelopment.carbonSupply }}</span>
          <span class="unit">{{ assetsIncomeUnit }}</span>
        </div>
        <div class="vertical-divider"></div>
        <div class="content-right">
          <div class="rate-item">
            <span class="hint">碳信用：</span>
            <span class="rate-value">{{ monthDevelopment.carbonCredit }}</span>
            <span class="rate-unit">&nbsp;{{ carbonCreditUnit }}</span>
          </div>
          <div class="rate-item">
            <span class="hint">碳配额：</span>
            <span class="rate-value">{{ monthDevelopment.carbonQuota }}</span>
            <span class="rate-unit">&nbsp;{{ carbonQuotaUnit }}</span>
          </div>
<!--          <div class="rate-item">-->
<!--            <span class="hint">绿&nbsp;&nbsp;&nbsp;证：</span>-->
<!--            <span class="rate-value">{{ monthDevelopment.greenScore }}</span>-->
<!--            <span class="rate-unit">&nbsp;{{ greenCertUnit }}</span>-->
<!--          </div>-->
        </div>
      </div>
      <div class="card-footer">
        <div class="footer-item">
          <span class="hint">总供应量：</span>
          <span class="value">{{ monthDevelopment.carbonSupplyTotal }}</span>
          <span class="unit">&nbsp;{{ assetsIncomeUnit }}</span>
        </div>
        <div class="footer-item">
          <span class="hint">碳资产估值：</span>
          <span class="value">¥{{ monthDevelopment.carbonValuation }}</span>
        </div>
      </div>
    </div>

    <!-- 销售量模块 -->
    <div class="card">
      <div class="card-header">
        <span class="card-title">销售量</span>
        <span class="card-subtitle">上月{{ monthSales.statDate }}</span>
      </div>
      <div class="card-content">
        <div class="content-left">
          <img class="flag-icon" src="@/assets/imgs/icon_capital_three.png" />
          <span class="sum-amount">{{ monthSales.carbonSupply }}</span>
          <span class="unit">{{ assetsIncomeUnit }}</span>
        </div>
        <div class="vertical-divider"></div>
        <div class="content-right">
          <div class="rate-item">
            <span class="hint">碳信用：</span>
            <span class="rate-value">{{ monthSales.carbonCredit }}</span>
            <span class="rate-unit">&nbsp;{{ carbonCreditUnit }}</span>
          </div>
          <div class="rate-item">
            <span class="hint">碳配额：</span>
            <span class="rate-value">{{ monthSales.carbonQuota }}</span>
            <span class="rate-unit">&nbsp;{{ carbonQuotaUnit }}</span>
          </div>
<!--          <div class="rate-item">-->
<!--            <span class="hint">绿&nbsp;&nbsp;&nbsp;证：</span>-->
<!--            <span class="rate-value">{{ monthSales.greenScore }}</span>-->
<!--            <span class="rate-unit">&nbsp;{{ greenCertUnit }}</span>-->
<!--          </div>-->
        </div>
      </div>
      <div class="card-footer">
        <div class="footer-item">
          <span class="hint">总供应量：</span>
          <span class="value">{{ monthSales.carbonSupplyTotal }}</span>
          <span class="unit">&nbsp;{{ assetsIncomeUnit }}</span>
        </div>
        <div class="footer-item">
          <span class="hint">碳资产估值：</span>
          <span class="value">¥{{ monthSales.carbonValuation }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { greenUnit, creditUnit, quotaUnit, cncUnit } from "@/config/ComConfig";
export default {
  name: "companyPackage",
  data() {
    return {
      lastMonth: "2月",
      assetsIncomeUnit: cncUnit,
      carbonCreditUnit: creditUnit,
      carbonQuotaUnit: quotaUnit,
      greenCertUnit: greenUnit,
    };
  },
  props: {
    monthSupply: {},
    monthDevelopment: {},
    monthSales: {},
  },
  mounted() { },
  methods: {
    sumAmoutUnit() {
      return "sumAmoutUnit";
    },
    sumincome() {
      return "sumincome";
    },
  },
  created() { },
};
</script>
<style lang="scss" scoped>
.divBox {
  display: flex;
  flex-direction: row;
  display: grid;
  grid: auto / auto auto auto;
  grid-gap: 20px;
  // 添加响应式断点
  @media (max-width: 1200px) {
    flex-direction: column;   // 屏幕较小时垂直排列
    gap: 15px;
  }

  @media (max-width: 768px) {
    padding: 10px 10px 0px 10px;
    gap: 10px;
  }
}

.card {
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-radius: 10px; // 整体模块圆角
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3); // 增加阴影增强立体感
  overflow: hidden; // 防止内容超出模块
  //width: 420px; // 统一每个模块宽度，可根据需求调整
  //height: 200px;
  flex: 1;                    // 改为弹性布局
  min-width: 300px;           // 设置最小宽度，防止过小
}

.card-header {
  background: linear-gradient(to right, #42c08d, #27a777); // 头部渐变背景，可根据设计调整
  color: #FFFFFF;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
}

.card-subtitle {
  font-size: 12px;
  opacity: 0.9;
}

.card-content {
  padding: 16px;
  display: flex;
  align-items: center;
}

.content-left {
  display: flex;
  align-items: center;
  margin-right: 16px;
}

.flag-icon {
  width: 32px;
  height: 32px;
  margin-right: 8px;
}

.sum-amount {
  font-size: 24px;
  font-weight: bold;
  color: #27a777;
  margin-right: 4px;
}

.unit {
  font-size: 14px;
  color: #5e6c84;
}

.vertical-divider {
  width: 1px;
  height: 40px;
  background-color: #eef0f3;
  margin: 0 16px;
}

.content-right {
  display: flex;
  flex-direction: column;
}

.rate-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.hint {
  font-size: 14px;
  color: #5e6c84;
  margin-right: 8px;
}

.rate-value {
  font-size: 14px;
  font-weight: 500;
  color: #424c5c;
}

.rate-unit {
  font-size: 14px;
  color: #424c5c;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: #f7f8fa;
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
}

.footer-item {
  display: flex;
  align-items: center;
}

.footer-item .hint {
  margin-right: 4px;
}

.footer-item .value {
  font-size: 14px;
  font-weight: 500;
  color: #27a777;
}
</style>
