<template>
  <div class="root">
    <div class="header">
      <span class="baseInfo">基本信息</span>
      <span
        v-if="AccountBaseInfo.accountType === '0380000002'"
        class="icon-qy-status"
      >
        <img class="badge-icon" src="@/assets/carbon/icon-leaf.svg" alt="" />
        <span class="qy-text">{{ AccountBaseInfo.accountTypeName }}</span>
      </span>
      <span
        v-else-if="AccountBaseInfo.accountType === '0380000001'"
        class="icon-qy-status"
      >
        <img
          class="icon-ec-status-logo"
          src="@/assets/icon/on-trial-icon.png"
        />
        <span class="qy-text">{{ AccountBaseInfo.accountTypeName }}</span>
      </span>

      <div class="empty-div"></div>
    </div>
    <!-- 背景装饰：右上角叶片111 -->
    <img
      class="bg-leaf-top"
      src="@/assets/carbon/leaf-cluster.svg"
      alt=""
      aria-hidden="true"
    />

    <!-- 背景装饰：左下波纹111 -->
    <img
      class="bg-wave"
      src="@/assets/carbon/wave.svg"
      alt=""
      aria-hidden="true"
    />
    <div class="second-line-div">
      <div class="single-item">
        <span class="secon-line-hint">
          <img class="hint-icon" src="@/assets/carbon/icon-leaf.svg" alt="" />
          企业名称</span
        >
        <input
          style="flex-grow: 1"
          class="input-css"
          v-model="baseInfo.tenantName"
          disabled
        />
      </div>

      <div class="single-item">
        <span class="secon-line-hint"> <img class="hint-icon" src="@/assets/carbon/icon-phone.svg" alt="" />
  企业电话</span>
        <input
          style="flex-grow: 1"
          class="input-css"
          v-model="baseInfo.telephone"
          disabled
        />
      </div>

      <div class="single-item">
        <!-- <div class="div-holder" /> -->
        <span class="secon-line-hint"> <img class="hint-icon" src="@/assets/carbon/icon-fax.svg" alt="" />
  企业传真&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <input
          style="flex-grow: 1"
          class="input-css"
          v-model="baseInfo.faxNumber"
          disabled
        />
      </div>
      <div class="single-item">
        <span class="secon-line-hint"
        ><img class="hint-icon" src="@/assets/carbon/icon-co2.svg" alt="" />
          所属行业</span
        >
        <input
          style="flex-grow: 1"
          class="input-css"
          v-model="baseInfo.industry"
          disabled
        />
      </div>

      <div class="single-item">
        <span class="secon-line-hint">   <img class="hint-icon" src="@/assets/carbon/icon-date.svg" alt="" />
  开户日期</span>
        <input
          style="flex-grow: 1"
          class="input-css"
          v-model="baseInfo.createdTime"
          disabled
        />
      </div>

      <div class="single-item">
        <!-- <div class="div-holder" /> -->
        <span class="secon-line-hint"
        > <img class="hint-icon" src="@/assets/carbon/icon-date.svg" alt="" />
          租户有效期</span
        >
        <input
          style="flex-grow: 1"
          class="input-css"
          v-model="baseInfo.validityDate"
          disabled
        />
      </div>
    </div>
    <div class="single-item-1">
      <span class="secon-line-hint">
        <img class="hint-icon" src="@/assets/carbon/icon-globe.svg" alt="" />
        企业地址</span
      >
      <input
        style="flex-grow: 1; margin-right: 10px"
        class="input-css"
        v-model="baseInfo.address"
        disabled
      />
    </div>
    <div class="deliver"></div>
    <!-- 111 -->
    <div style="margin-bottom: 20px" class="header intro-header">
      <span class="intro-title">
        <img class="hint-icon" src="@/assets/carbon/icon-leaf.svg" alt="" />
        企业介绍
      </span>
      <div class="empty-div"></div>
    </div>
    <div class="companyInfo-card">
      <div class="card-left-accent"></div>
      <div class="companyInfo">
        {{ baseInfo.introduction }}
      </div>
    </div>
  </div>
</template>

<script>
import { getAccountEnterPriseInfo, getAccoutBaseInfo } from '@/api/tenant'
import Cookies from 'js-cookie'
export default {
  name: 'index',
  data() {
    return {
      baseInfo: {
        tenantName: '--',
        telephone: '--',
        faxNumber: '--',
        industry: '--',
        contactsEmail: '--',
        ecoTime: '--',
        validityDate: '--',
        createdTime: '--',
        introduction: '暂无该企业介绍信息',
      },
      AccountBaseInfo: {
        accountTypeName: '',
        accountType: ''
      },
    }
  },
  methods: {
    getAccountEnterPriseInfo(tenantId) {
      let tantId = tenantId;
      getAccountEnterPriseInfo(tantId).then(res => {
        this.baseInfo = res;
        console.log("baseInfo",this.baseInfo);
      }).catch(err => {
        debugger;
        console.warn('获取企业信息失败 from ', err);
      })
    },
    getUserBaseInfo() {
      let info = JSON.parse(Cookies.get('JavaInfo'))
      let accountId = info.accountId;
      getAccoutBaseInfo(accountId).then((res) => {
        this.AccountBaseInfo.accountType = res.accountType || '0380000001';
        this.AccountBaseInfo.accountTypeName = res.accountTypeName || '试用账户';
        this.getAccountEnterPriseInfo(res.tenantId)
      })
    },
  },
  mounted() {
    this.getUserBaseInfo();
  }
}
</script>

<style scoped>
.root {
  padding-left: 30px;
  padding-right: 30px;
  height: 598px;
  position: relative; /* 让装饰元素绝对定位基于容器 */
  background: radial-gradient(
    1200px 600px at 120% -10%,
    #f7fff9 0%,
    rgba(247, 255, 249, 0) 60%
  )
  no-repeat,
  #ffffff;
  box-shadow: 0px 2px 8px 0px #eaf0f3;
  border-radius: 8px;
  margin: 28px;
}
.header {
  display: flex;
  flex-direction: row;
  padding-top: 40px;
  align-items: center; /* 让徽标与文字垂直对齐 */
}
.head-icon {
  align-content: center;
  width: 50px;
  height: 50px;
  border-radius: 28px;
  margin-right: 20px;
}
.baseInfo {
  text-align: center;
  margin: auto;
  /* margin-top: 18px; */
  font-size: 18px;
  font-weight: 500;
  color: #242e35;
}
.div-holder {
  flex-grow: 1;
}
.second-line-div {
  display: grid;
  grid: auto / auto auto auto;
  grid-gap: 20px;
}
.single-item {
  margin-top: 20px;
  display: flex;
  flex-direction: row;
}
.single-item-1 {
  width: 66%;
  margin-top: 40px;
  display: flex;
  flex-direction: row;
}
.secon-line-hint {
  display: inline-flex; /* 覆盖建议：增强对齐 */
  align-items: center;
  gap: 8px;
  margin-right: 20px; /* 略收紧 */
  text-align: center;
  align-self: center;
  /* margin-right: 30px; */
  font-size: 15px;
  font-weight: 400;
  color: #3a4063;
}
.second-line-text {
  font-weight: 400;
  color: #808ea5;
}
.deliver {
  margin-top: 40px;
  height: 1px;
  border: 1px dashed #dadee5;
}
.deliver {
  margin-top: 40px;
  height: 1px;
  border: 1px dashed #dadee5;
}
.empty-div {
  flex-grow: 1;
}
.companyInfo {
  background-repeat: no-repeat;
  font-weight: 400;
  font-size: 14px;
  color: #5e6c84;
}
.qy-text {
  font-weight: 500;
  color: #04714f;
  vertical-align: middle;
  font-size: 16px; /* 与 .baseInfo 保持一致 */
}
.icon-ec-status-logo {
  width: 14px;
  height: 14px;
  vertical-align: middle;
}
.icon-qy-status {
  height: 22px;
  margin-left: 10px;
  background-image: url("~@/assets/icon/bg-icon-01.png");
  background-repeat: no-repeat;
  background-size: 100% 100%;
  background-position: center;
  padding: 4px 10px 4px 10px;
  e: 18px; /* 添加这行，与 .baseInfo 保持一致 */
  display: flex; /* 添加这行，用于更好地对齐内容 */
  align-items: center; /* 添加这行，垂直居中对齐 */
}
/* 碳中和徽标 */
.carbon-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: linear-gradient(90deg, #e9fff1 0%, #d9f6e5 100%);
  color: #2e8b57;
  font-weight: 600;
  font-size: 13px;
  box-shadow: 0 0 0 1px #e3f6ea inset;
}
.carbon-badge .badge-icon {
  width: 14px;
  height: 14px;
}
/* 背景装饰：右上叶片、左下波纹 */
.bg-leaf-top {
  position: absolute;
  top: 4px;
  right: 8px;
  width: 180px;
  height: auto;
  pointer-events: none;
  user-select: none;
  opacity: 0.6;
}
.bg-wave {
  position: absolute;
  left: 0;
  bottom: -6px;
  width: 100%;
  height: auto;
  pointer-events: none;
  user-select: none;
  opacity: 0.9;
}
.hint-icon {
  width: 16px;
  height: 16px;
}
/* 输入框轻阴影与圆角细化（仅视觉，不可编辑仍保留） */
.input-css {
  border: 1px solid #eef3f0;
  background: #f9fcfa;
  border-radius: 6px;
  height: 36px;
  padding: 0 12px;
  color: #3a4063;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.02) inset;
}

/* 介绍卡片：左侧绿色强调条 */
.companyInfo-card {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  background: #fafffc;
  border: 1px solid #e6f4ec;
  border-radius: 8px;
  padding: 14px 16px;
  max-width: 86%;
  box-shadow: 0 2px 6px rgba(34, 139, 84, 0.05);
}
.companyInfo-card .card-left-accent {
  width: 4px;
  border-radius: 3px;
  background: linear-gradient(180deg, #57c17b, #a6e3be);
}
.companyInfo {
  background: transparent; /* 覆盖原背景声明 */
  font-weight: 400;
  font-size: 14px;
  color: #5e6c84;
  line-height: 1.8;
}

/* “企业介绍”标题样式（可选增强） */
.intro-header {
  padding-top: 10px;
}
.intro-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #2e8b57;
}

/* 网格在较窄宽度下自适应为两列或一列 */
@media (max-width: 1260px) {
  .second-line-div {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .single-item-1 {
    width: 100%;
  }
}
@media (max-width: 820px) {
  .second-line-div {
    grid-template-columns: 1fr;
  }
}
</style>
