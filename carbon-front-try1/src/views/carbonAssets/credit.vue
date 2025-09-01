<template>
  <div class="carbon-credit-page">
    <!-- 面包屑 -->
    <el-breadcrumb separator="/" style="margin: 10px 20px">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>碳信用</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 资产概览 -->
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
        <el-button type="success" plain @click="onUpload">上传</el-button>
        <el-button type="primary" @click="onBuyClick">我想买</el-button>
      </div>
    </el-card>

    <!-- 查询筛选 -->
    <el-card class="search-filter-card" style="margin-top: 20px">
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
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="领域">
          <el-select v-model="searchForm.industry" placeholder="全部">
            <el-option
              v-for="item in optionsIndustry"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="交易状态">
          <el-select v-model="searchForm.transactionStatus" placeholder="全部">
            <el-option
              v-for="item in optionsOnlines"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="资产状态">
          <el-select v-model="searchForm.assetsStatus" placeholder="全部">
            <el-option
              v-for="item in optionsAssetStatus"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="签发日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="选择开始时间"
            end-placeholder="选择结束时间"
            @change="handleDateChange"
          ></el-date-picker>
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
    <el-card class="table-card" style="margin-top: 20px">
      <el-table
        :data="list"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f2f5f7' }"
        :row-key="(row) => row.id"
        @selection-change="handleSelectionChange"
        :render-header="renderCheckHeader"
      >
        <el-table-column type="selection" width="55"> </el-table-column>
        <el-table-column label="序号" align="center" width="60">
          <template v-slot:default="scope">
            {{ (current - 1) * pageSize + scope.$index + 1 }}
          </template>
          <!-- 自定义的import里面的名字作为标签 -->
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
          <template v-slot="scope">
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
              style="color: red"
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
        style="padding: 15px; text-align: right"
      >
      </el-pagination>
    </el-card>
  </div>
</template>

<script>
// 确保接口方法正确引入
import { loadCarbonCreditPageList } from "../../api/carbonAssetApi";
import { delCredit } from "../../api/carbonAssetApi";
// 引用场外上架的页面~~~~~
import {
  getCertificationCriteriaDict,
  getProjectAreaDict,
  getAssetTradeStatusDict,
  getAssetStatusDict,
} from "../../config/dictHelper";

export default {
  name: "CarbonCredit",
  components: {
    // 引用场外上架的页面
    // OuterShelve: () => import("../outerShelve.vue"),
    // import里面的名字
  },
  data() {
    return {
      list: [],
      total: 0,
      current: 1,
      pageSize: 10,
      dateRange: [],
      searchForm: {
        certificationCriteria: "",
        industry: "",
        transactionStatus: "",
        assetsStatus: "",
        issuingDateStart: "",
        issuingDateEnd: "",
        projectName: "",
        methodName: "",
      },
      optionsStandard: [],
      optionsIndustry: [],
      optionsOnlines: [],
      optionsAssetStatus: [],
      totalHold: 200,
      available: 200,
      locked: 0,
      frozen: 0,
    };
  },
  methods: {
    formatTableData(list) {
      // 格式化表格数据
      return list.map((item) => ({
        ...item,
        // 格式化日期
        // 分别处理开始和结束时间
        issuingDateStart: item.issuingDateStart
          ? new Date(item.issuingDateStart).toLocaleDateString()
          : "",
        issuingDateEnd: item.issuingDateEnd
          ? new Date(item.issuingDateEnd).toLocaleDateString()
          : "",
        // 处理状态显示
        assetsStatusName: this.getStatusName(item.assetsStatus),
        // 格式化资产估值，保留2位小数
        assetValue: item.assetValue
          ? Number(item.assetValue).toFixed(2)
          : "0.00",
      }));
    },
    getStatusName(status) {
      const statusMap = {
        1: "正常",
        2: "锁定",
        3: "冻结",
        4: "已用完",
      };
      return statusMap[status] || "未知";
    },
    onEdit(row) {
      // 跳转到编辑页面
      this.$router.push({
        path: "/carbonAssets/method/editMethod",
        query: {
          id: row.id,
          type: "edit",
        },
      });
    },
    onClickDelete(row) {
      this.$confirm("确认删除该碳资产记录吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          try {
            // TODO: 调用删除API
            const res = await delCredit(row.id);
            if (res.code === 200) {
              this.$message.success("删除成功");
              this.getList(); // 刷新列表
            } else {
              this.$message.error(res.msg || "删除失败");
            }
          } catch (error) {
            console.error("删除失败:", error);
            this.$message.error("删除失败：" + (error.message || "未知错误"));
          }
        })
        .catch(() => {
          this.$message.info("已取消删除");
        });
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.getList();
    },
    handleCurrentChange(val) {
      this.current = val;
      this.getList();
    },
    insideTransaction(row) {
      this.$router.push({
        path: "/systemSetting/exchangeManager",
        query: { row },
      });
    },
    async getList() {
      try {
        const data = {
          asc: true,
          current: this.current,
          size: this.pageSize,
          ...this.searchForm,
        };
        console.log("请求参数", data);

        const res = await loadCarbonCreditPageList(data);
        console.log("接口返回", res);

        if (res.code === 200) {
          this.list = this.formatTableData(res.data? res.data.records : []);
          this.total = res.data? res.data.total : 0;
        } else {
          this.$message.error(res.msg || "获取列表失败");
        }
      } catch (error) {
        console.error("请求失败", error);
        this.$message.error("获取列表失败：" + (error.message || "未知错误"));
      }
    },
    viewDetail(row) {
      // 跳转到碳资产详情页面
      this.$router.push({
        path: "/carbonAssets/assetDetail",
        query: { id: row.id },
      });
    },
    outerShelve(row) {
      // 跳转到场外上架页面,与详情页中的场外报价功能相同
      // this.$router.push({
      //   path: "/carbonAssets/assetDetail",
      //   query: {
      //     id: row.id,
      //     action: "shelve", // 标记是从场外上架按钮进入
      //   },
      // });
    },
    onBuyClick() {
      // 跳转到交易页面
      this.$router.push({
        path: "/carbonTrade/quotation/buyAssets",
      });
    },
    onUpload() {
      // 跳转到上传页面
      this.$router.push({
        path: "/carbonAssets/carbonUpload",
      });
    },
    formatCertification(data) {
      if (!Array.isArray(data)) return;
      this.optionsStandard = data.map((v) => ({
        label: v.name === "全部" ? v.name : v.name,
        value: v.name !== "全部" ? v.value : "",
      }));
    },
    formatIndustry(data) {
      if (!Array.isArray(data)) return;
      this.optionsIndustry = data.map((v) => ({
        label: v.name === "全部" ? v.name : v.name,
        value: v.name !== "全部" ? v.value : "",
      }));
    },
    formatStatus(data) {
      if (!Array.isArray(data)) return;
      this.optionsOnlines = data.map((v) => ({
        label: v.name === "全部" ? v.name : v.name,
        value: v.name !== "全部" ? v.value : "",
      }));
    },
    formatAssetStatus(data) {
      if (!Array.isArray(data)) return;
      this.optionsAssetStatus = data.map((v) => ({
        label: v.name === "全部" ? v.name : v.name,
        value: v.name !== "全部" ? v.value : "",
      }));
    },
    handleDateChange(value) {
      if (value && value.length === 2) {
        this.searchForm.issuingDateStart = value[0];
        this.searchForm.issuingDateEnd = value[1];
      } else {
        this.searchForm.issuingDateStart = "";
        this.searchForm.issuingDateEnd = "";
      }
    },
  },
  mounted() {
    console.log("credit mounted");
    this.getList();
    this.formatCertification(getCertificationCriteriaDict(this.$store));
    this.formatIndustry(getProjectAreaDict(this.$store));
    this.formatStatus(getAssetTradeStatusDict(this.$store));
    this.formatAssetStatus(getAssetStatusDict(this.$store));
  },
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

:deep(.el-cascader .el-input .el-input__inner),
:deep(.el-cascader .el-input.is-focus .el-input__inner) {
  border-color: transparent;
}

:deep(.el-date-picker.has-sidebar.has-time) {
  background: #0a5857d6;
  color: #fff;
  border: 1px solid #22f4d6;
}

:deep(.el-date-picker__header-label) {
  color: #ffffff;
}

.acea-row {
  :deep(.el-avatar--small) {
    width: 22px;
    height: 22px;
  }
}

.checkTime {
  :deep(.el-radio__input) {
    display: none;
  }
}

.iva-pl-8 {
  margin-left: 8px;
}

.dashboard-console-visit {
  :deep(.el-card__header) {
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
