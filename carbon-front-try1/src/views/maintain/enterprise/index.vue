<template>
  <div class="root">
    <div class="header">
      <span class="baseInfo">基本信息</span>
      <span v-if="AccountBaseInfo.accountType === '0380000002'" class="icon-qy-status">
        <img class="icon-ec-status-logo" src="@/assets/icon/icon-qy-time.png" />
        <span class="qy-text">{{ AccountBaseInfo.accountTypeName }}</span>
      </span>
      <span v-else-if="AccountBaseInfo.accountType === '0380000001'" class="icon-qy-status">
        <img class="icon-ec-status-logo" src="@/assets/icon/on-trial-icon.png" />
        <span class="qy-text">{{ AccountBaseInfo.accountTypeName }}</span>
      </span>
      <div class="empty-div"></div>
    </div>

    <div class="second-line-div">
      <div class="single-item">
        <span class="secon-line-hint">企业名称</span>
        <input style="flex-grow: 1;" class="input-css" v-model="baseInfo.tenantName" disabled />
      </div>

      <div class="single-item">
        <span class="secon-line-hint">企业电话</span>
        <input style="flex-grow: 1;" class="input-css" v-model="baseInfo.telephone" disabled />
      </div>

      <div class="single-item">
        <!-- <div class="div-holder" /> -->
        <span class="secon-line-hint">企业传真&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <input style="flex-grow: 1;" class="input-css" v-model="baseInfo.faxNumber" disabled />
      </div>
      <div class="single-item">
        <span class="secon-line-hint">所属行业</span>
        <input style="flex-grow: 1;" class="input-css" v-model="baseInfo.industry" disabled />
      </div>

      <div class="single-item">
        <span class="secon-line-hint">开户日期</span>
        <input style="flex-grow: 1;" class="input-css" v-model="baseInfo.createdTime" disabled />
      </div>

      <div class="single-item">
        <!-- <div class="div-holder" /> -->
        <span class="secon-line-hint">租户有效期</span>
        <input style="flex-grow: 1;" class="input-css" v-model="baseInfo.validityDate" disabled />
      </div>


    </div>
    <div class="single-item-1">
      <span class="secon-line-hint">企业地址</span>
      <input style="flex-grow: 1; margin-right:10px;" class="input-css" v-model="baseInfo.address" disabled />
    </div>
    <div class="deliver"></div>

    <div style="margin-bottom: 20px;" class="header">
        <span class="baseInfo">企业介绍</span>
        <div class="empty-div"></div>
      </div>
    <span class="companyInfo">{{ baseInfo.introduction }}</span>

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
  background: #FFFFFF;
  box-shadow: 0px 2px 8px 0px #EAF0F3;
  border-radius: 8px;
  margin: 28px;
}
.header {
  display: flex;
  flex-direction: row;
  padding-top: 40px;
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
  font-weight: 500;
  color: #242E35;
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
  text-align: center;
  align-self: center;
  margin-right: 30px;
  font-weight: 400;
  color: #3A4063;
}
.second-line-text {
  font-weight: 400;
  color: #808EA5;
}
.deliver {
  margin-top: 40px;
  height: 1px;
  border: 1px dashed #DADEE5;
}
.deliver {
  margin-top: 40px;
  height: 1px;
  border: 1px dashed #DADEE5;
}
.empty-div {
  flex-grow: 1;
}
.companyInfo {
  background-repeat: no-repeat;
  font-weight: 400;
  color: #5E6C84;
}
.qy-text {
  font-weight: 400;
  color: #FDD7E1;
  vertical-align: middle;
}
.icon-ec-status-logo {
  width: 14px;
  height: 14px;
  vertical-align: middle;
}
.icon-qy-status {
  height: 22px;
  margin-left: 10px;
  background-image: url("/src/assets/icon/bg-icon-01.png");
  background-repeat: no-repeat;
  background-size: 100% 100%;
  background-position: center;
  padding: 4px 10px 4px 10px;
}
</style>
