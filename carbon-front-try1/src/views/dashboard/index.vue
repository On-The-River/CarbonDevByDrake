<template>
  <div class="dashboard-container">
    <!-- 背景图容器 -->
    <div class="background-container"></div>

    <!-- 内容容器 -->
    <div class="content-wrapper">
      <!-- 轮播图组件 -->
      <el-carousel :interval="4000" type="card" height="400px">
        <el-carousel-item v-for="item in carouselImages" :key="item.id">
          <img :src="item.url" class="carousel-image" />
        </el-carousel-item>
      </el-carousel>
      <!--头部-->
      <company-package
        ref="companyPackage"
        :accountVo="accountVo"
        :assetsIncome="assetsIncome"
        :fundIncome="fundIncome"
        :carbonCredit="carbonCredit"
        :carbonQuota="carbonQuota"
        :greenScore="greenScore"
      />

      <capital-stat
        ref="capitalStat"
        :monthSupply="monthSupply"
        :monthDevelopment="monthDevelopment"
        :monthSales="monthSales"
      />

      <!--小方块一-->
      <apply-project />
      <!-- <project-stat ref="projectStat" :projectStat="projectStat" /> -->
      <projectStatistical v-if="(projectStat.projectList && projectStat.projectList.length > 0)" :projectStat="projectStat" />
      <trade-info :quotation="quotation" />
      <carbon-trading-chart />
      <carbon-market-chart />
      <price-prediction-chart/>
      <carbon-emission-factors/>

      <newsContaiert ref="newsList" />

      <commom-usage-grid />
    </div>
  </div>

</template>

<script>
import { mapGetters } from "vuex";
import applyProject from "./components/applyProject";
import companyPackage from "./components/companyPackage.vue";
import capitalStat from "./components/capitalStat.vue";
import newsContaiert from "./components/newsContaiert.vue";
import tradeInfo from "./components/tradeInfo.vue";
import commomUsageGrid from "./components/commomUsageGrid.vue";
import { getHomePanelData } from "@/api/homeApi.js";
import { getToken } from "@/utils/auth";
import { getAllDiction } from "@/config/dictHelper";
import projectStatistical from "./components/projectStatistical.vue";
import { setLargeNumber } from "@/libs/public";
import CommomUsageGrid from "@/views/dashboard/components/commomUsageGrid.vue";
import CarbonTradingChart from "@/views/dashboard/components/carbonTradingChart.vue";
import CarbonMarketChart from "@/views/dashboard/components/carbonMarketChart.vue";
import PricePredictionChart from "@/views/dashboard/components/PricePredictionChart.vue";
import CarbonEmissionFactors from "@/views/dashboard/components/CarbonEmissionFactors.vue";

export default {
  name: "Dashboard",
  components: {
    PricePredictionChart,
    CarbonMarketChart,
    CommomUsageGrid,
    applyProject,
    tradeInfo,
    companyPackage,
    capitalStat,
    newsContaiert,
    commomUsageGrid,
    projectStatistical,
    CarbonTradingChart,
    CarbonEmissionFactors,

  },
  data() {
    return {
      currentRole: "adminDashboard",
      accountVo: {},
      assetsIncome: {},
      carbonCredit: {},
      carbonQuota: {},
      fundIncome: {},
      greenScore: {},
      monthDevelopment: {},
      monthSales: {},
      monthSupply: {},
      projectStat: {},
      quotation: {},
      // 轮播图数据
      carouselImages: [
        { id: 1, url: require('../../assets/videos/lun1.png')},
        { id: 2, url: require('../../assets/videos/lun2.png') },
        { id: 3, url: require('../../assets/videos/lun3.png') },
        { id: 4, url: require('../../assets/videos/lun4.png') },
      ],
    };
  },
  methods: {
    setDataNumber(obj) {
      for (let i in obj) {
        if (typeof (obj[i]) == "number") {
          obj[i] = setLargeNumber(obj[i]);
        }
      }
      return obj;
    },
    getNewsList() {
    },
    getAllDictionary() {
      getAllDiction(this.$store)
    },
    getLastMonth(date) {
      var dateArr = date.split("-");
      var m = parseInt(dateArr[1])
      if (m == 1) {
        return 12 + '月'
      } else {
        return (m - 1) + '月'
      }
    },
    getHomePanelData() {
      let token = getToken()
      getHomePanelData(token).then(res => {
        this.accountVo = res.accountVo
        if (!this.accountVo.avatar || this.accountVo.avatar == '' || (this.accountVo.avatar).match(/[^\x00-\xff]/)) {
          this.accountVo.avatar = '/static/img/icon_account_logo_edd51e4.jpg';
        } else {
          window.localStorage.setItem('avatar', this.accountVo.avatar);
        }
        this.assetsIncome = res.assetsIncome
        for (let i in this.assetsIncome) {
          if (typeof (this.assetsIncome[i]) == "number") {
            this.assetsIncome[i] = setLargeNumber(this.assetsIncome[i]);
          }
        }
        this.carbonCredit = setLargeNumber(res.carbonCredit)
        this.carbonQuota = setLargeNumber(res.carbonQuota)
        this.fundIncome = res.fundIncome
        this.greenScore = res.greenScore
        this.monthDevelopment = this.setDataNumber(res.monthDevelopment)
        this.monthSales = this.setDataNumber(res.monthSales)
        this.monthSupply = this.setDataNumber(res.monthSupply)
        this.projectStat = this.setDataNumber(res.projectStat)
        for (let i = 0; i < this.projectStat.projectList.length; i++) {
          this.projectStat.projectList[i].reduction = setLargeNumber(this.projectStat.projectList[i].reduction)
          this.projectStat.projectList[i].carbonValuation = setLargeNumber(this.projectStat.projectList[i].carbonValuation)
        }
        this.quotation = this.setDataNumber(res.quotation)
        this.quotation.projects = this.setDataNumber(res.quotation.projects)
        this.carbonCredit = setLargeNumber(res.carbonCredit)
        this.carbonQuota = setLargeNumber(res.carbonQuota)
        this.greenScore = setLargeNumber(res.greenScore)
        this.assetsIncome.statDate = this.getLastMonth(this.assetsIncome.statDate)
        this.fundIncome.statDate = this.getLastMonth(this.fundIncome.statDate)
        this.monthSupply.statDate = this.getLastMonth(this.monthSupply.statDate)
        this.monthDevelopment.statDate = this.getLastMonth(this.monthDevelopment.statDate)
        this.monthSales.statDate = this.getLastMonth(this.monthSales.statDate)
      }).catch(err => {
      });
    },
  },
  mounted() {
    this.getAllDictionary()
    this.getHomePanelData()
    this.getNewsList()
  },
  computed: {
    ...mapGetters([
      'roles'
    ])
  },
  created() {
    if (!(this.roles.includes('admin'))) {
      this.currentRole = 'editorDashboard'
    }
  }
}
</script>

<style scoped>
/* 背景图样式 */
.dashboard-container {
  position: relative;
  min-height: 100vh;
}

.background-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* 使用feng1.jpg作为背景图 */
  background-image: url("../../assets/videos/feng2.png"); /* 假设图片放在src/assets目录下 */
  background-size: cover; /* 背景图覆盖整个容器 */
  background-position: center; /* 背景图居中显示 */
  background-repeat: no-repeat; /* 背景图不重复 */
  background-attachment: fixed; /* 背景图固定，内容滚动时背景不动 */
  z-index: 1; /* 背景图在最底层 */
  /* 可选：添加半透明遮罩，使内容更易读 */
  opacity: 0.9;
}

/* 内容容器样式 */
.content-wrapper {
  position: relative;
  z-index: 2; /* 内容在背景图之上 */
  padding: 20px;
}

/* 可选：为内容区块添加背景色，增强可读性 */
::v-deep .content-block {
  background-color: rgba(255, 255, 255, 0.85);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
/* 轮播图样式 */
.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  margin: 0 auto 20px auto;
}
</style>
