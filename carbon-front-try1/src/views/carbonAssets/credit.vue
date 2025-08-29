<template>
  <div class="carbon-credit-page">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" style="margin: 10px 20px;">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>碳信用</el-breadcrumb-item>
    </el-breadcrumb>
    <!-- 我的碳信用资产概览区域 -->
    <el-card class="asset-overview-card">
      <div slot="header">
        <i class="el-icon-menu"></i>
        我的碳信用资产
      </div>
      <div class="asset-info">
        持仓总量 {{ totalHold }}(tCO2e) | 可用数量 {{ available }}(tCO2e) |
        锁定数量 {{ locked }}(tCO2e) | 冻结数量 {{ frozen }}(tCO2e)
      </div>
      <div class="operation-btns">
        <el-button type="success">上传</el-button>
        <el-button type="primary" style="margin-left: 10px;">我想买</el-button>
      </div>
    </el-card>
    <!-- 查询筛选区域 -->
    <el-card class="search-filter-card" style="margin-top: 20px;">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="核证标准">
          <el-select
            v-model="searchForm.certificationCriteria"
            placeholder="全部"
          >
            <el-option
              v-for="item in optionsStandard"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="领域">
          <el-select v-model="searchForm.industry" placeholder="全部">
            <el-option
              v-for="item in optionsIndustry"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="交易状态">
          <el-select v-model="searchForm.transactionStatus" placeholder="全部">
            <el-option
              v-for="item in optionsOnlines"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="资产状态">
          <el-select v-model="searchForm.assetsStatus" placeholder="全部">
            <el-option
              v-for="item in optionsAssetStatus"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="签发日期">
          <el-date-picker
            v-model="searchForm.issuingDate"
            type="daterange"
            range-separator="至"
            start-placeholder="选择开始时间"
            end-placeholder="选择结束时间"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="按项目搜索">
          <el-input
            v-model="searchForm.projectName"
            placeholder="输入项目名称"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="getList">查询</el-button>
        </el-form-item>
        <el-form-item label="方法学搜索">
          <el-input
            v-model="searchForm.methodName"
            placeholder="输入方法学名称"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <!-- 表格数据区域 -->
    <el-card class="table-card" style="margin-top: 20px;">
      <el-table
        :data="list"
        border
        style="width: 100%;"
        :header-cell-style="{ background: '#f2f5f7' }"
        :row-key="row => row.id"
        @selection-change="handleSelectionChange"
        :render-header="renderCheckHeader"
      >
        <el-table-column type="selection" width="55"> </el-table-column>
        <el-table-column label="序号" align="center" width="60">
          <template slot-scope="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="项目名称"> </el-table-column>
        <el-table-column prop="certificationCriteriaName" label="核证标准">
        </el-table-column>
        <el-table-column prop="holdAmount" label="持仓量(tCO2e)">
        </el-table-column>
        <el-table-column prop="assetValue" label="资产估值(¥)">
        </el-table-column>
        <el-table-column prop="type" label="类型"> </el-table-column>
        <el-table-column prop="assetsStatusName" label="资产状态">
        </el-table-column>
        <el-table-column prop="issuingDate" label="签发日期"> </el-table-column>
        <el-table-column label="操作" align="center">
          <template slot-scope="scope">
            <el-button type="text" @click="viewDetail(scope.row)"
              >查看</el-button
            >
            <el-button type="text" @click="outerShelve(scope.row)"
              >场外上架</el-button
            >
            <el-button type="text" @click="insideTransaction(scope.row)"
              >场内交易</el-button
            >
            <el-button type="text" @click="onEdit(scope.row)">修改</el-button>
            <el-button
              type="text"
              @click="onClickDelete(scope.row)"
              style="color: red;"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页组件 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="current"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="padding: 15px; text-align: right;"
      >
      </el-pagination>
    </el-card>
  </div>
</template>

<script>
import {
  getCityDict,
  getCreditData,
  searchKeyword
} from "@/api/carbonAssetApi";
import {
  CertificationCriteria,
  ProjectArea,
  AssetTradeStatus,
  AssetStatus,
  getAllDict
} from "@/config/dictHelper";

export default {
  name: "CarbonCredit",
  data() {
    return {
      // 分页相关
      list: [],
      total: 0,
      current: 1,
      pageSize: 10,
      pageCount: 0,
      // 查询条件
      searchForm: {
        certificationCriteria: "",
        industry: "",
        transactionStatus: "",
        assetsStatus: "",
        issuingDate: [],
        projectName: "",
        methodName: ""
      },
      // 字典数据
      optionsStandard: [],
      optionsIndustry: [],
      optionsOnlines: [],
      optionsAssetStatus: [],
      // 复选框相关
      allchecked: false,
      indeterminateFlag: false,
      articals: [],
      reRender: false,
      // 资产概览数据
      totalHold: 200,
      available: 200,
      locked: 0,
      frozen: 0
    };
  },
  methods: {
    // 处理表格数据格式化
    formatTableData(list) {
      return list.map(v => {
        v.industryCodeName = v.industryCodeName ? v.industryCodeName : "--";
        if (v.issuingDate) {
          let time = v.issuingDate.split(" ");
          v.issuingDate = time[0];
        }
        for (var i in v) {
          v[i] = v[i] ? v[i] : "--";
          if (v[i] === " ") {
            v[i] = "--";
          }
        }
        return v;
      });
    },
    onEdit(row) {
      // 编辑逻辑，可根据需求补充
      console.log("编辑", row);
    },
    onClickPublish(row) {
      // 发布逻辑，可根据需求补充
      console.log("发布", row);
    },
    onClickDelete(row) {
      // 删除逻辑，可根据需求补充（建议增加确认提示）
      console.log("删除", row);
    },
    onClickOffline(row) {
      // 下线逻辑，可根据需求补充
      console.log("下线", row);
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.getList();
    },
    handleCurrentChange(val) {
      this.current = val;
      this.getList();
    },
    // 监听页面宽度变化，刷新表格（若有需要可结合 resize 事件完善）
    handleResize() {
      // 若有图表等需resize的组件，补充逻辑
    },
    // 场内交易
    insideTransaction(row) {
      this.$router.push({
        path: "/trade/account/exchange",
        query: { row } // 传递数据，可根据需求调整
      });
    },
    update() {
      const data = {
        certificationCriteria: this.searchForm.certificationCriteria,
        transactionStatus: this.searchForm.transactionStatus,
        issuingDate:
          this.searchForm.issuingDate && this.searchForm.issuingDate.length
            ? this.searchForm.issuingDate[0]
            : "",
        issuingDateStart: "", // 原代码里未明确该字段逻辑，可根据实际接口调整
        projectName: this.searchForm.projectName,
        methodName: this.searchForm.methodName,
        assetsStatus: this.searchForm.assetsStatus,
        projectScopeCode: this.searchForm.industry,
        issuingDateEnd:
          this.searchForm.issuingDate && this.searchForm.issuingDate.length
            ? this.searchForm.issuingDate[1]
            : ""
      };
      loadCarbonCreditPageList(data)
        .then(res => {
          this.list = this.formatTableData(res.data.records);
          this.total = Number(res.data.total);
          this.current = Number(res.data.current);
          this.pageCount = Math.ceil(this.total / this.pageSize);
        })
        .catch(error => {
          console.error("请求失败", error);
        });
    },
    getList() {
      const data = {
        asc: true,
        current: this.current,
        size: this.pageSize
      };
      loadCarbonCreditPageList(data)
        .then(res => {
          if (res.data.records) {
            this.list = this.formatTableData(res.data.records);
          }
          this.total = Number(res.data.total);
          this.current = Number(res.data.current);
          this.pageCount = Math.ceil(this.total / this.pageSize);
        })
        .catch(error => {
          console.error("请求失败", error);
        });
    },
    // checkbox 相关
    signCheckChange() {
      let allCheckedFlag = true;
      let allReset = true;
      this.articals.forEach(item => {
        if (item.checked === true) {
          allReset = false;
        } else {
          allCheckedFlag = false;
        }
      });
      if (allCheckedFlag || allReset) {
        this.indeterminateFlag = false;
        this.allchecked = allCheckedFlag;
      } else {
        this.indeterminateFlag = true;
      }
      this.reRender = !this.reRender;
    },
    // render-header 方法
    renderCheckHeader(h, { column, $index }) {
      return h("span", {}, [
        h("el-checkbox", {
          props: {
            checked: this.allchecked,
            indeterminate: this.indeterminateFlag
          },
          on: {
            change: this.signCheckChange
          }
        })
      ]);
    },
    // 格式化验证标准字典
    formatCertification(data) {
      data.forEach(v => {
        let CertificationItem = {
          label: v.name === "全部" ? v.name : v.name,
          value: v.name !== "全部" ? v.value : ""
        };
        this.optionsStandard.push(CertificationItem);
      });
    },
    // 格式化行业类型字典
    formatIndustry(data) {
      data.forEach(v => {
        let CertificationItem = {
          label: v.name === "全部" ? v.name : v.name,
          value: v.name !== "全部" ? v.value : ""
        };
        this.optionsIndustry.push(CertificationItem);
      });
    },
    // 格式化状态类型字典
    formatStatus(data) {
      data.forEach(v => {
        let CertificationItem = {
          label: v.name === "全部" ? v.name : v.name,
          value: v.name !== "全部" ? v.value : ""
        };
        this.optionsOnlines.push(CertificationItem);
      });
    },
    // 格式化状态类型字典
    formatAssetStatus(data) {
      data.forEach(v => {
        let CertificationItem = {
          label: v.name === "全部" ? v.name : v.name,
          value: v.name !== "全部" ? v.value : ""
        };
        this.optionsAssetStatus.push(CertificationItem);
      });
    },
    // 查看详情
    viewDetail(row) {
      console.log("查看详情", row);
    },
    // 场外上架
    outerShelve(row) {
      console.log("场外上架", row);
    },
    // 处理多选
    handleSelectionChange(val) {
      this.articals = val;
      this.signCheckChange();
    }
  },
  created() {
    // this.handleChangeVisitType();
  },
  mounted() {
    this.getList();
    this.exchangeList = getExchangeDict(this.$store);
    this.tradeMethods = getDeliveryMethodDict(this.$store);
    let Certification = getCertificationCriteriaDict(this.$store);
    this.formatCertification(Certification);
    let projectArea = getProjectAreaDict(this.$store);
    this.formatIndustry(projectArea);
    let assetTradeStatus = getAssetTradeStatusDict(this.$store);
    this.formatStatus(assetTradeStatus);
    let assetStatus = getAssetStatusDict(this.$store);
    this.formatAssetStatus(assetStatus);
  }
};
</script>
<style lang="scss" scoped>
.root {
  background: #f2f5f7;
}

.divBox {
  margin: 20px;
  background: #ffffff;
  box-shadow: 0px 2px 8px 0px #eaf0f3;
  border-radius: 8px;
}

.container {
  margin: 10px 0px 20px 0px;
  display: flex;
  flex-direction: row;
}

.content-container {
  display: flex;
  flex-direction: column;
  width: 100%;
}

::v-deep(.el-cascader .el-input .el-input__inner),
::v-deep(.el-cascader .el-input.is-focus .el-input__inner) {
  border-color: transparent;
}

::v-deep(.el-date-picker.has-sidebar.has-time) {
  background: #0a5857d6;
  color: #fff;
  border: 1px solid #22f4d6;
}

::v-deep(.el-date-picker__header-label) {
  color: #ffffff;
}

.acea-row {
  ::v-deep(.el-avatar--small) {
    width: 22px;
    height: 22px;
  }
}

.checkTime {
  ::v-deep(.el-radio__input) {
    display: none;
  }
}

.iva-pl-8 {
  margin-left: 8px;
}

.dashboard-console-visit {
  ::v-deep(.el-card__header) {
    padding: 14px 20px !important;
  }

  ul {
    li {
      list-style-type: none;
      margin-top: 12px;
    }
  }
}
.ivu-mb {
  margin-bottom: 10px;
}

.newsImg {
  width: 30px;
  height: 30px;
  border-radius: 4px;
}

.myassets-div {
  width: 184px;
  display: flex;
  flex-direction: row;
}

.icon {
  width: 24px;
  height: 24px;
}

.text-left {
  margin: auto;
  cursor: default;
  font-weight: 500;
  color: #24a776;
}

.myassets-container {
  display: flex;
  flex-direction: row;
  margin-top: 16px;
  margin-bottom: 20px;
  padding-left: 10px;
  padding-right: 10px;
  height: 54px;
  background: #e3f2ec;
  border-radius: 6px;
  /* opacity: 0.1; */
}

.assets-hint {
  margin-top: auto;
  margin-bottom: auto;
  font-weight: 400;
  color: #424c5c;
}

.assets-text {
  margin-top: auto;
  margin-bottom: auto;
  margin-left: 6px;
  font-weight: 500;
  color: #24a776;
}
.assets-line {
  margin: auto;
  margin-left: 10px;
  margin-right: 10px;
  width: 1px;
  height: 16px;
  border: 1px solid rgba(38, 181, 129, 0.5);
}

.center-vertical {
  margin-top: auto;
  margin-bottom: auto;
}

.require {
  color: red;
  position: relative;
  right: 20px;
}
</style>
