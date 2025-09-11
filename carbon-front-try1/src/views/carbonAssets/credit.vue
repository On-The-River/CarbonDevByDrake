<template>
  <div class="carbon-credit-page">
    <!-- 资产概览 -->
    <!--        <i class="el-icon-menu"></i>-->
<!--    持仓总量 {{ totalHold }}(tCO2e) | 可用数量 {{ available }}(tCO2e) |-->
<!--    锁定数量 {{ locked }}(tCO2e) | 冻结数量 {{ frozen }}(tCO2e)-->
    <el-card class="asset-overview-card" >
      <div slot="header" class="title">
        <i class="icon">📊</i>
        我的碳信用资产
      </div>
      <div class="asset-content-row">
      <div class="asset-info">
        <!-- 左侧：资产信息 -->
        <span class="item">持仓总量 <strong>{{ totalHold }}(tCO2e)</strong></span>
        <span class="divider">|</span>
        <span class="item">可用数量 <strong>{{ available }}(tCO2e)</strong></span>
        <span class="divider">|</span>
        <span class="item">锁定数量 <strong>{{ locked }}(tCO2e)</strong></span>
        <span class="divider">|</span>
        <span class="item">冻结数量 <strong>{{ frozen }}(tCO2e)</strong></span>
      </div>
        <!-- 右侧：操作按钮 -->
      <div class="operation-btns">
        <el-button type="success" class="btn-upload" plain @click="onUpload">上传</el-button>
        <el-button type="primary" class="btn-buy" @click="onBuyClick">我想买</el-button>
      </div>
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
    <!-- :render-header="renderCheckHeader" 暂时注释这个el-table的render-header -->
    <!-- @selection-change="handleSelectionChange" 暂时注释掉这个-->
    <el-card class="table-card" style="margin-top: 20px">
      <el-table
        :data="list"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#e6f7e6' }"
        :row-key="row => row.id"
      >
        <el-table-column type="selection" width="55"> </el-table-column>
        <el-table-column label="序号" align="center" width="60">
          <template v-slot:default="scope">
            {{ (current - 1) * pageSize + scope.$index + 1 }}
          </template>
          <!-- 自定义的import里面的名字作为标签 -->
        </el-table-column>
        <el-table-column prop="projectName" label="项目名称" width="236"> </el-table-column>
        <el-table-column prop="certificationCriteriaName" label="核证标准" width="200">
        </el-table-column>
        <el-table-column prop="total" label="持仓量(tCO2e)" width="100">
        </el-table-column>
        <el-table-column prop="valuation" label="资产估值(¥)" width="100">
        </el-table-column>
        <el-table-column prop="projectScopeType" label="类型" width="100"> </el-table-column>
        <el-table-column prop="assetsStatusName" label="资产状态" width="80">
        </el-table-column>
        <el-table-column prop="issuingDate" label="签发日期" width="160"> </el-table-column>
        <el-table-column label="操作" align="center" width="500">
          <template v-slot="scope">
            <div class="operation-buttons">
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
            </div>
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

    <!-- 上传弹窗 -->
    <!-- @submit="submited" 这个需要整改，还没有提交的一个调用方法，暂时注释-->
    <!--      :selData="list"-->
    <carbon-upload
      :dialogFormVisible="carbonUploadDlg"
      :isCredit="true"
      title="碳信用项目上传"
      @changeVisible="changeCarbonVisible"
    ></carbon-upload>

    <!-- 场外上架按钮弹出页面 -->
    <!-- @submit="outerShelveSubmit" 暂时注释掉这个方法，待提交 -->
    <otc-listing
      :dialogFormVisible="outerShelveDlg"
      :selData="outerShelveRow"
      @changeVisible="changeOuterShelveVisible"
    ></otc-listing>
    <!-- 编辑方法学弹出页面
    :selData="editMethodRow"-->
    <carbon-edit
      :dialogFormVisible="editMethodDlg"
      :row="editMethodRow"
      title="碳信用项目修改"
      @changeVisible="changeCarbonEditVisible"
    ></carbon-edit>
    <!-- @submit="editMethodSubmit" 暂时注释掉这个方法，后续调用 -->
<!--    <edit-method-->
<!--      :dialogFormVisible="editMethodDlg"-->
<!--      :selData="editMethodRow"-->
<!--      @changeVisible="changeEditMethodVisible"-->
<!--    ></edit-method>-->
    <buy-assets
      :dialog-form-visible="buyAssetsDlgVisible"
      @changeBuyAssetsDialogFormVisible="changeDialogFormVisible"
    >
    </buy-assets>
  </div>
</template>

<script>
// 确保接口方法正确引入
import { loadCarbonCreditPageList, getCreditTotal } from "@/api/carbonAssetApi";
import { delCredit } from "@/api/carbonAssetApi";
// 引用场外上架的页面~~~~~
import carbonUpload from "./carbonUpload.vue";
// import assetDetail from "./assetDetail.vue";
import otcListing from "@/views/carbonAssets/otcListing";
// import editMethod from "./method/editMethod.vue";
import carbonEdit from "./carbonEdit.vue";
import buyAssets from "@/views/carbonTrade/quotation/buyAssets";

import {
  getCertificationCriteriaDict,
  getProjectAreaDict,
  getAssetTradeStatusDict,
  getAssetStatusDict
} from "@/config/dictHelper";

export default {
  name: "CarbonCredit",
  components: {
    // 引用场外上架的页面
    // OuterShelve: () => import("../outerShelve.vue"),
    // import里面的名字
    carbonUpload,
    otcListing,
    // editMethod,
    carbonEdit,
    buyAssets,
  },
  data() {
    return {
      list: [],
      total: 0,
      current: 1,
      pageSize: 10,
      dateRange: [],
      carbonUploadDlg: false, // 控制上传弹窗显示
      buyAssetsDlgVisible: false,
      onUploadRow: null, // 存储点击“上传”的表格行数据（用于弹窗回显）
      outerShelveDlg: false, //控制场外上架的弹窗显示，自加
      outerShelveRow: null, // 存储点击“场外上架”的表格行数据（用于弹窗回显），自加
      editMethodDlg: false, // 控制编辑方法学的弹窗显示，自加
      editMethodRow: null, // 存储点击“编辑”的表格行数据（用于弹窗回显），自加
      searchForm: {
        certificationCriteria: "",
        industry: "",
        transactionStatus: "",
        assetsStatus: "",
        issuingDateStart: "",
        issuingDateEnd: "",
        projectName: "",
        methodName: "",
        creatorId: "",
        createdTime: ""
      },
      dialogFormVisible: false,
      optionsStandard: [],
      optionsIndustry: [],
      optionsOnlines: [],
      optionsAssetStatus: [],
      totalHold: -1,
      available: -1,
      locked: -1,
      frozen: -1,
    };
  },
  methods: {

    loadStatisticData()
    {
      getCreditTotal().then(res => {
        this.totalHold=res.data.total;
        this.available=res.data.availableAmount;
        this.locked=res.data.lockedAmount;
        this.frozen=res.data.frozenAmount;
      })
    },

    formatTableData(list) {
      // 格式化表格数据
      return list.map(item => ({
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
          : "0.00"
      }));
    },
    getStatusName(status) {
      const statusMap = {
        1: "正常",
        2: "锁定",
        3: "冻结",
        4: "已用完"
      };
      return statusMap[status] || "未知";
    },
    onEdit(row) {
      this.editMethodRow = row;
      this.editMethodDlg = true;

    },
    onClickDelete(row) {
      this.$confirm("确认删除该碳资产记录吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          // 使用异步请求
          const deleteAsync = async () => {
            try {
              console.log("row:", row);
              console.log("row.id:", row.id);
              const res = await delCredit(row.id);
              if (res) {
                this.$message.success("删除成功");
                await this.getList(); // 刷新列表
              } else {
                this.$message.error(res.msg || "删除失败");
              }
            } catch (error) {
              console.error("删除失败:", error);
              this.$awaitmessage.error("删除失败：" + (error.message || "未知错误"));
            }
          };
          deleteAsync();
        })
        .catch(() => {
          this.$message.info("已取消删除");
        });
    },

    showBuyAssetsDlg(){
      this.buyAssetsDlgVisible=true;
    },

    closeBuyAssetsDlg() {
      this.buyAssetsDlgVisible=false;
    },
    changeDialogFormVisible(res)
    {
      this.buyAssetsDlgVisible=res;
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
        query: { row }
      });
    },
    getList() {
      const data = {
        asc: true,
        current: this.current,
        size: this.pageSize,
        ...this.searchForm
      };
      console.log("请求参数", data);
      loadCarbonCreditPageList(data).then(res => {
        console.log("接口返回", res);

        if (res.code === 200) {
          this.list = res.data.records;
          console.log("上传的数据为：", this.list);
          this.total = Number(res.data.total);
          console.log("持仓总量：",this.total);
        } else {
          this.$message.error(res.msg || "获取列表失败");
        }
      }).catch(error => {
        console.error("请求失败", error);
        this.$message.error("获取列表失败：" + (error.message || "未知错误"));
      });
    },
    // 定义的问题，组长的定义是这个
    // viewDetail(row) {
    //   // 跳转到碳资产详情页面
    //   this.$router.push({
    //     path: "/assets/creditDetail",
    //     query: { id: row.id }
    //   });
    // },
    viewDetail(row) {
      // 跳转到碳资产详情页面
      this.$router.push({
        path: "/assets/creditDetail",
        query: { id: row.id }
      });
    },
    outerShelve(row) {
      this.outerShelveRow = row;
      // console.log("传输的场外上架的数据为：",this.outerShelveRow);
      this.outerShelveDlg = true;
      // this.form=row;
      // this.dialogFormVisible=true;
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
      this.showBuyAssetsDlg();
    },

    changeCarbonVisible(res) {
      // console.log("res11111111:",res);
      this.carbonUploadDlg = res;
      if(res==false){
        this.getList();
      }
    },
    // 自加，修改
    changeCarbonEditVisible(res) {
      this.editMethodDlg = res;
    },
    // 自加
    changeOuterShelveVisible(res) {
      this.outerShelveDlg = res;
    },
    onUpload() {
      this.carbonUploadDlg = true;
      this.getList();
    },
    formatCertification(data) {
      if (!Array.isArray(data)) return;
      this.optionsStandard = data.map(v => ({
        label: v.name === "全部" ? v.name : v.name,
        value: v.name !== "全部" ? v.value : ""
      }));
    },
    formatIndustry(data) {
      if (!Array.isArray(data)) return;
      this.optionsIndustry = data.map(v => ({
        label: v.name === "全部" ? v.name : v.name,
        value: v.name !== "全部" ? v.value : ""
      }));
    },
    formatStatus(data) {
      if (!Array.isArray(data)) return;
      this.optionsOnlines = data.map(v => ({
        label: v.name === "全部" ? v.name : v.name,
        value: v.name !== "全部" ? v.value : ""
      }));
    },
    formatAssetStatus(data) {
      if (!Array.isArray(data)) return;
      this.optionsAssetStatus = data.map(v => ({
        label: v.name === "全部" ? v.name : v.name,
        value: v.name !== "全部" ? v.value : ""
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
    }
  },
  mounted() {
    console.log("credit mounted");
    this.getList();
    this.formatCertification(getCertificationCriteriaDict(this.$store));
    this.formatIndustry(getProjectAreaDict(this.$store));
    this.formatStatus(getAssetTradeStatusDict(this.$store));
    this.formatAssetStatus(getAssetStatusDict(this.$store));
    this.loadStatisticData();
  }
};
</script>

<style lang="scss" scoped>
.root {
  background: #f2f5f7;
}
.asset-overview-card {
  //background-color: #e6f7e6; /* 浅绿色背景 */
  border-radius: 8px;
  padding: 12px 16px;
  margin-top: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.asset-overview-card .title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #5FB878;
  margin: 0;
  background-color: #FFFFFF;
  justify-content: flex-start; /* 强制左对齐 */
}
.asset-content-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  background-color: #e6f7e6;
}
::v-deep(.el-card__header) {
  padding: 0 !important; /* 移除默认 padding */
  background-color: white;
  border-bottom: 1px solid #e6e6e6;
}
.asset-overview-card ::v-deep(.el-card__body) {
  padding: 0 !important;
}
.asset-info {
  font-size: 14px;
  color: #555;
  line-height: 1.4;
  display: flex;
  gap: 12px;
}

.asset-info .item {
  display: inline-block;
}

.asset-info .divider {
  color: #999;
  margin: 0 4px;
}

.operation-btns {
  display: flex;
  gap: 8px;
}

.btn-upload {
  border: 1px solid #007bff;
  background-color: white;
  color: #007bff;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-buy {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.operation-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
  white-space: nowrap;
  font-size: 12px;
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
  height: 18px;
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
