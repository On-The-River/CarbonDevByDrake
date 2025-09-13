<template>
  <div class="divBox">
    <!-- 资产收入 & 资金收入卡片：改为统一卡片容器样式 -->
    <div class="card asset-card">
      <!-- 卡片头部：新增统一卡片头结构 -->
      <div class="card-header">
        <div class="header-container">
          <span class="card-title">资产收入</span>
          <span class="card-subtitle">上月{{ assetsIncome.statDate || '--' }}</span>
        </div>
        <div class="header-container">
          <span class="card-title">资金收入</span>
          <span class="card-subtitle">上月{{ fundIncome.statDate || '--' }}</span>
        </div>
      </div>

      <!-- 卡片内容区：保留原有所有内容布局 -->
      <div class="card-content">
        <div class="content-container">
          <div class="content-pannel">
            <div class="amount-wrap">
              <span class="sumAmout">{{ assetsIncome.monthIncome || '0' }}</span>
              <span class="unit">{{ assetsIncomeUnit }}</span>
            </div>
            <div class="rate-bar">
              <div class="rate-container">
                <span class="hint">环比</span>
                <div class="rate">
                  <i :class="Number(assetsIncome.monthOnMonthRatio) >= 0 ? 'positive' : 'negative'">{{ assetsIncome.monthOnMonthRatio || '0' }}%</i>
                  <img class="arrow-icon" v-if="Number(assetsIncome.monthOnMonthRatio) >= 0" src="@/assets/imgs/icon_rate_up.png" alt="上升箭头" />
                  <img class="arrow-icon" v-if="Number(assetsIncome.monthOnMonthRatio) < 0" src="@/assets/imgs/icon_rate_down.png" alt="下降箭头" />
                </div>
              </div>
              <div class="rate-container">
                <span class="hint">同比</span>
                <div class="rate">
                  <i :class="Number(assetsIncome.yearOnYearRatio) >= 0 ? 'positive' : 'negative'">{{ assetsIncome.yearOnYearRatio || '0' }}%</i>
                  <img class="arrow-icon" v-if="Number(assetsIncome.yearOnYearRatio) >= 0" src="@/assets/imgs/icon_rate_up.png" alt="上升箭头" />
                  <img class="arrow-icon" v-if="Number(assetsIncome.yearOnYearRatio) < 0" src="@/assets/imgs/icon_rate_down.png" alt="下降箭头" />
                </div>
              </div>
<!--              <div class="rate-container">-->
<!--                <span class="hint">总收入</span>-->
<!--                <div class="rate">-->
<!--                  <i class="sumincome">¥ {{ assetsIncome.totalIncome || '0.00' }}</i>-->
<!--                </div>-->
<!--              </div>-->
            </div>
          </div>
          <div class="p-diliver" />
          <div style="display: flex; flex-direction: row; width: 100%">
            <div class="p-diliver" />
            <div style="margin-left: 30px" class="content-pannel">
              <div class="amount-wrap">
                <span class="sumAmout">{{ fundIncome.monthIncome || '0' }}</span>
                <span class="unit">¥ {{ capitalIncomeUnit || '' }}</span>
              </div>
              <div class="rate-bar">
                <div class="rate-container">
                  <span class="hint">环比</span>
                  <div class="rate">
                    <i :class="Number(fundIncome.monthOnMonthRatio) >= 0 ? 'positive' : 'down'">{{ fundIncome.monthOnMonthRatio || '0' }}%</i>
                    <img class="arrow-icon" v-if="Number(fundIncome.monthOnMonthRatio) >= 0" src="@/assets/imgs/icon_rate_up.png" alt="上升箭头" />
                    <img class="arrow-icon" v-if="Number(fundIncome.monthOnMonthRatio) < 0" src="@/assets/imgs/icon_rate_down.png" alt="下降箭头" />
                  </div>
                </div>
                <div class="rate-container">
                  <span class="hint">同比</span>
                  <div class="rate">
                    <i :class="Number(fundIncome.yearOnYearRatio) >= 0 ? 'positive' : 'down'">{{ fundIncome.yearOnYearRatio || '0' }}%</i>
                    <img class="arrow-icon" v-if="Number(fundIncome.yearOnYearRatio) >= 0" src="@/assets/imgs/icon_rate_up.png" alt="上升箭头" />
                    <img class="arrow-icon" v-if="Number(fundIncome.yearOnYearRatio) < 0" src="@/assets/imgs/icon_rate_down.png" alt="下降箭头" />
                  </div>
                </div>
<!--                <div class="rate-container">-->
<!--                  <span class="hint">总收入</span>-->
<!--                  <div class="rate">-->
<!--                    <i class="sumincome">¥ {{ fundIncome.totalIncome || '0.00' }}</i>-->
<!--                  </div>-->
<!--                </div>-->
              </div>
            </div>
          </div>
        </div>
        <el-divider class="diliver"></el-divider>

        <div class="bottom-bar">
          <div class="bottom-content-container">
            <span class="bottom-hint">碳信用：</span>
            <span class="bottom-value">{{ carbonCredit || '0' }}</span>
            <span class="bottom-value">{{ carbonCreditUnit || '' }}</span>
          </div>
          <div class="bottom-content-container">
            <span class="bottom-hint">碳配额：</span>
            <span class="bottom-value">{{ carbonQuota || '0' }}</span>
            <span class="bottom-value">{{ carbonQuotaUnit || '' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 账户卡片：改为统一卡片样式 -->
    <div @click="goUserInfo" class="card account-card" style="cursor: pointer">
      <!-- 卡片头部：新增统一卡片头结构 -->
      <div class="card-header">
        <div class="header-container">
          <span class="card-title">账户</span>
        </div>
      </div>

      <!-- 卡片内容区：保留原有所有内容布局 -->
      <div class="card-content">
        <div class="top-container">
          <div class="content-container">
            <div class="content-pannel">
              <div style="display: flex; align-items: center">
                <img class="account-logo" :src="accountVo.avatar || '@/assets/imgs/default-avatar.png'" alt="用户头像" />
                <span class="account-name">{{ accountVo.accountName || '未命名账户' }}</span>
                <span v-if="accountVo.roleNames && accountVo.roleNames.length" class="icon-qy-status">
                  <img class="icon-ec-status-logo" src="@/assets/icon/icon-qy-time.png" alt="角色图标" />
                  <span class="qy-text">{{ accountVo.roleNames[0] }}</span>
                </span>

                <!-- 试用版 -->
                <span v-if="accountVo.productVersion === '0400000001'" class="icon-ec-status">
                  <img class="icon-ec-status-logo" src="@/assets/icon/blue-time.png" alt="版本图标" />
                  <span class="ec-text">{{ accountVo.productVersionName || '试用版' }}</span>
                </span>
                <!-- 基础版 -->
                <span v-if="accountVo.productVersion === '0400000002'" class="icon-ec-status">
                  <img class="icon-ec-status-logo" src="@/assets/icon/icon-qy-me.png" alt="版本图标" />
                  <span class="ec-text">{{ accountVo.productVersionName || '基础版' }}</span>
                </span>
                <!-- 通用版 -->
                <span v-else-if="accountVo.productVersion === '0400000003'" class="icon-ec-status">
                  <img class="icon-ec-status-logo" src="@/assets/icon/icon-qy-major.png" alt="版本图标" />
                  <span class="ec-text">{{ accountVo.productVersionName || '通用版' }}</span>
                </span>
                <!-- 定制版 -->
                <span v-else-if="accountVo.productVersion === '0400000004'" class="icon-ec-status">
                  <img class="icon-ec-status-logo" src="@/assets/icon/icon-qy-f-vip.png" alt="版本图标" />
                  <span class="ec-text">{{ accountVo.productVersionName || '定制版' }}</span>
                </span>
              </div>
              <span class="account-company">{{ accountVo.enterpriseName || '未绑定企业' }}</span>
            </div>
          </div>
          <img class="icon_account_flag" src="@/assets/imgs/icon_account_flag.png" alt="账户标识" />
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
      capitalIncomeUnit: "",
    };
  },
  props: {
    accountVo: {
      type: Object,
      default: () => ({}) // Vue2 推荐对象默认值用函数返回，避免共享引用
    },
    assetsIncome: {
      type: Object,
      default: () => ({})
    },
    fundIncome: {
      type: Object,
      default: () => ({})
    },
    carbonCredit: {
      type: [Number, String],
      default: 0
    },
    carbonQuota: {
      type: [Number, String],
      default: 0
    },
    greenScore: {
      type: [Number, String],
      default: 0
    },
  },
  methods: {
    sumincome() {
      return "sumincome";
    },
    goUserInfo() {
      this.$router.push({
        path: "/account/info",
      });
    },
    formatData(time) {
      if (!time) return "--";
      var datetime = new Date(time);
      var year = datetime.getFullYear();
      var month =
        datetime.getMonth() + 1 < 10
          ? "0" + (datetime.getMonth() + 1)
          : datetime.getMonth() + 1;
      var date =
        datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
      return year + "-" + month + "-" + date;
    },
  },
};
</script>

<style lang="scss" scoped>
// 外层容器：保留原有flex布局和间距，仅调整卡片间距一致性
.divBox {
  margin-top: 5px;
  display: flex;
  flex-direction: row;
  padding: 10px 20px 0px 20px;
  gap: 20px; /* 统一卡片间距，与新卡片样式匹配 */
  align-items: flex-start; /* 避免卡片因高度不同导致对齐异常 */

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

// 统一卡片基础样式：两个卡片共用此样式，确保视觉一致
.card {
  background-color: #fff;
  border-radius: 10px; // 统一圆角
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.5); // 统一阴影，增强立体感
  overflow: hidden; // 防止内容超出卡片圆角
  flex: 1;                    // 改为弹性布局
  min-width: 300px;           // 设置最小宽度，防止过小
  max-height: 250px;
}

// 统一卡片头部样式：替代原titlebar，两个卡片共用
.card-header {
  background: linear-gradient(to right, #42c08d, #27a777); // 统一绿色渐变头
  color: #ffffff;
  padding: 12px 16px;
  display: flex;
  flex-direction: row;
  align-items: center;
}

// 头部内容容器：适配资产卡片双标题、账户卡片单标题
.header-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-grow: 1; // 让两个容器平分头部宽度（资产卡片）
}

// 卡片标题样式
.card-title {
  font-size: 16px;
  font-weight: 600;
  margin-right: 8px;
}

// 卡片副标题样式（日期）
.card-subtitle {
  font-size: 14px;
  opacity: 0.9; // 轻微透明，区分主次
}

// 统一卡片内容区样式：替代原el-card-bg，保留原有内边距
.card-content {
  padding: 15px;
  min-height: 250px; // 保留原有最小高度，确保布局稳定
}

// 以下为原有样式保留，仅调整与新卡片样式冲突的部分
// 移除原el-card-bg相关样式（已被card/card-content替代）
// 分割线：保留原有颜色，适配新卡片背景
.el-divider--horizontal {
  margin: 12px 0 8px 0;
  border-color: rgb(12, 131, 81);
}

// 内容容器：保留原有布局逻辑，无修改
.content-container {
  display: flex;
  flex-direction: row;
  flex-grow: 1;

  .content-pannel {
    display: flex;
    flex-direction: column;
    width: 100%;

    .amount-wrap {
      padding: 8px 12px;
      border-radius: 4px;
      margin-bottom: 12px;
    }

    .sumAmout {
      width: 100%;
      height: 40px;
      font-size: 22px;
      font-weight: 600;
      color: #030305;
      background: linear-gradient(135deg, #030305 0%, #030305 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      margin-right: 6px;
    }

    .unit {
      height: 24px;
      font-size: 16px;
      font-weight: 600;
      color: #030305;
    }

    .rate-bar {
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;
      gap: 8px;

      .rate-container {
        display: flex;
        flex-direction: column;
        margin-top: 8px;
        margin-right: 16px;
        min-width: 60px;

        .hint {
          height: 20px;
          font-size: 14px;
          font-weight: 550;
          color: #1f2937;
        }

        .rate {
          display: flex;
          flex-direction: row;
          margin-top: 4px;
          min-width: 36px;
          height: 20px;
          font-size: 14px;
          font-weight: 600;
          align-items: center;

          .positive {
            color: #fa5252;
          }

          .negative,
          .down {
            color: #15aabf;
          }

          .arrow-icon {
            width: 16px;
            height: 16px;
            margin-left: 4px;
          }
        }

        .sumincome {
          height: 24px;
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
        }
      }
    }
  }
}

// 分割线：保留原有样式
.diliver {
  width: 100%;
  height: 1px;
  border: 1px solid rgb(13, 126, 78);
}

.p-diliver {
  margin-top: 8px;
  width: 1px;
  height: 50px;
  border: 1px #0d7e4e;
  border-style: dotted solid double dashed;
}

// 底部信息栏：保留原有样式
.bottom-bar {
  width: 100%;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 10px;
  padding-top: 8px;

  .bottom-content-container {
    display: flex;
    flex-direction: row;
    flex-grow: 1;
    min-width: 120px;

    .bottom-hint {
      font-size: 16px;
      font-weight: 600;
      color: #0c8351;
      margin-right: 8px;
    }

    .bottom-value {
      margin-left: 0px;
      font-size: 16px;
      font-weight: 600;
      color: #0c8351;
      margin-right: 4px;
    }
  }
}

.account-container {
  display: flex;
  flex-direction: row;
}

// 账户头像：保持原有样式
.account-logo {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  vertical-align: middle;
  border: 2px solid #fff;
}

// 账户名称：保持原有样式
.account-name {
  margin-left: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  vertical-align: middle;
}

.account-unative-logo {
  margin-left: 8px;
  height: 18px;
  vertical-align: middle;
}

.account-company-active-logo {
  margin-left: 8px;
  height: 18px;
  vertical-align: middle;
}

// 企业名称：保持原有样式
.account-company {
  margin-top: 35px;
  height: 24px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.top-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 8px 0;
}

// 账户卡片右侧图标：保持原有样式
.icon_account_flag {
  width: 64px;
  height: 64px;
  vertical-align: middle;
  opacity: 0.85;
}

.account-validate-text {
  height: 20px;
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  position: relative;
  top: 2px;
}

// 账户角色标签：保持原有样式
.icon-qy-status {
  height: 24px;
  margin-left: 8px;
  background-image: url("../../../assets/icon/bg-icon-01.png");
  background-repeat: no-repeat;
  background-size: 100% 100%;
  background-position: center;
  padding: 4px 10px;
  display: flex;
  align-items: center;

  .qy-text {
    font-size: 13px;
    font-weight: 500;
    color: #d97706;
    vertical-align: middle;
  }
}

// 账户版本标签：保持原有样式
.icon-ec-status {
  height: 24px;
  margin-left: 8px;
  background-image: url("../../../assets/icon/bg-icon-02.png");
  background-repeat: no-repeat;
  background-size: 100% 100%;
  background-position: center;
  padding: 4px 10px;
  display: flex;
  align-items: center;

  .icon-ec-status-logo {
    width: 16px;
    height: 16px;
    vertical-align: middle;
    margin-right: 4px;
  }

  .ec-text {
    font-size: 13px;
    font-weight: 500;
    color: #2563eb;
    vertical-align: middle;
  }
}
</style>
