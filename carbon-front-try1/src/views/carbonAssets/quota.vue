<template>
  <div class="root">
    <div class="divBox">
      <div class="content-container">
        <div class="myasset-div">
          <img class="icon" src="@/assets/icon/icon_my_assets.png" />
          <span style="margin-left: 8px" class="list-blue-text text-left">
            我的配额碳资产
          </span>
        </div>
        <div class="myassets-container">
          <span class="assets-hint">持仓总量</span>
          <div class="assets-line" />
          <span class="assets-text">{{ setNumber(topData.total) }}(tCO2e)</span>
          <span class="assets-hint">可用数量</span>
          <div class="assets-line" />
          <span class="assets-text"
            >{{ setNumber(topData.availableAmount) }}(tCO2e)</span
          >
          <span class="assets-hint">锁定数量</span>
          <div class="assets-line" />
          <span class="assets-text"
            >{{ setNumber(topData.lockedAmount) }}(tCO2e)</span
          >
          <span class="assets-hint">冻结数量</span>
          <div class="assets-line" />
          <span class="assets-text"
            >{{ setNumber(topData.frozenAmount) }}(tCO2e)</span
          >
          <div class="empty-holder" />
          <button
            style="width: 68px; margin-left: 16px"
            class="normal-blue-btn"
            @click="onClickUpload"
          >
            上传
          </button>
          <button
            style="width: 68px; margin-left: 16px"
            class="normal-white-btn vertical-center"
            @click="onClickBuy"
          >
            我想买
          </button>
        </div>
      </div>
      <div class="container">
        <div style="width: 270px; margin-right: 16px" class="selectbox-root">
          <a class="selectbox-hint">资产状态</a>
          <div class="selectbox-deliver" />
          <el-cascader
            style="width: 120px"
            placeholder="全部"
            class="selectbox-input"
            v-model="selectedAssetStatus"
            :options="assetStatusList"
            clearable
            @change="update"
          />
        </div>
        <div
          style="margin-right: 16px; padding-right: 0px"
          class="selectbox-root"
        >
          <a class="selectbox-hint" style="width: 100px">有效日期</a>
          <div class="selectbox-deliver" />
          <el-date-picker
            v-model="selectDate"
            type="datetime"
            placeholder="选择开始时间"
            align="right"
            :picker-options="pickerOptions"
            @change="update"
            size="medium"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
          <el-date-picker
            v-model="selectEndDate"
            type="datetime"
            placeholder="选择结束时间"
            align="right"
            :picker-options="pickerOptions"
            @change="update"
            size="medium"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </div>
        <div
          style="flex-grow: 1; margin-left: 16px; margin-right: 16px"
          class="selectbox-root"
        >
          <a class="selectbox-hint" style="min-width: 100px">关键词搜索</a>
          <div class="selectbox-deliver" />
          <el-input
            v-model="searchKeyword"
            placeholder="请输入名称"
            clearable
            size="medium"
            @clear="onClickSearch"
            @keyup.enter.native="onClickSearch"
          />
        </div>
        <button
          style="margin: auto"
          class="light-green-btn"
          @click="onClickSearch"
        >
          查询
        </button>
      </div>
      <el-table
        :header-cell-style="{
          background: '#F2F5F7',
          border: '0px solid #DDDDDD',
          color: '#424B35',
          height: '64px',
        }"
        show-header
        :data="list"
        stripe
        @cell-click="handle"
        :row-style="{ height: '64px' }"
        :cell-style="cellStyle"
        style="width: 100%"
      >
        <!-- <el-table-column label="" align="center" min-width="30">
          <template slot="header" slot-scope="{ column }">
            <el-checkbox
              v-model="column.checked"
              :indeterminate="indeterminateFlag"
              :checked="allchecked"
              label=""
              @change="updateAllSelected"
            />
          </template>
          <template slot-scope="scope">
            <el-checkbox
              @change="signCheckChange"
              v-model="scope.row.checked"
            />
          </template>
        </el-table-column> -->
        <el-table-column min-width="10" />
        <el-table-column label="序号" align="left" min-width="40">
          <template slot-scope="scope">
            <span>{{ getCurListNo(scope.$index) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :show-overflow-tooltip="true"
          prop="agencyName"
          align="left"
          label="一级市场持有机构"
          min-width="120"
        />
        <el-table-column
          align="left"
          prop="total"
          label="持仓量(tCO2e)"
          min-width="90"
        >
          <template slot-scope="scope">
            <span>{{ setNumber(scope.row.total) }}</span>
          </template>
        </el-table-column>
        <!-- <el-table-column
          align="left"
          prop="availableAmount"
          label="可用量(tCO2e)"
          min-width="90"
        /> -->
        <el-table-column
          align="left"
          prop="valuation"
          label="资产估值(¥)"
          min-width="90"
        >
          <template slot-scope="scope">
            <span>{{ setNumber(scope.row.valuation) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          align="left"
          prop="assetsStatusName"
          label="资产状态"
          min-width="60"
        />
        <!-- <el-table-column
          align="left"
          prop="transactionStatusName"
          label="交易状态"
          min-width="60"
        /> -->
        <el-table-column
          align="left"
          prop="expiryDate"
          label="有效日期"
          min-width="60"
        />
        <el-table-column label="操作" min-width="150" align="center">
          <template slot-scope="scope">
            <a class="list-blue-text" @click="toDetail(scope.row.id)">查看</a>
            <a
              style="margin-left: 10px"
              class="list-blue-text"
              @click="outsizeTransaction(scope.row)"
            >
              场外报价
            </a>
            <a
              style="margin-left: 10px"
              class="list-green-text"
              @click="insideTransaction"
            >
              场内交易
            </a>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 30px; margin-bottom: 10px" class="pageBox">
        <div style="flex-grow: 1" />
        <el-pagination
          style="margin: auto"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="current"
          :page-size="pageSize"
          :page-count="pageCount"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
      <!-- 场外交易按钮弹出页面 -->
      <el-dialog :title="title" :visible.sync="dialogFormVisible" width="720px">
        <el-form label-position="left" label-width="130px" :model="form">
          <el-form-item label="出售数量(tCO2e)" prop="tradeQuantity">
            <span class="require">*</span>
            <el-input
              v-model="form.tradeQuantity"
              size="medium"
              style="width: 268px; top: -5px"
            />
          </el-form-item>
          <el-form-item label="出售单价(¥)" prop="negotiatedPrice">
            <el-input
              v-model="form.assetUnitPrice"
              size="medium"
              style="width: 268px; top: -5px"
            />
          </el-form-item>
          <el-form-item label="出售截止时间" prop="expirationDate">
            <el-date-picker
              type="date"
              placeholder="选择日期"
              size="medium"
              v-model="form.expirationDate"
              value-format="yyyy-MM-dd HH:mm:ss"
              style="width: 268px; top: -5px"
            />
          </el-form-item>
          <el-form-item label="期望交割时间" prop="deliveryTime">
            <el-date-picker
              type="date"
              placeholder="选择日期"
              size="medium"
              v-model="form.deliveryTime"
              value-format="yyyy-MM-dd HH:mm:ss"
              style="width: 268px; top: -5px"
            />
          </el-form-item>
          <el-form-item label="期望交割方式" prop="deliveryMethod">
            <el-select
              v-model="form.deliveryMethod"
              placeholder="协议转入、竞价交易、定价交易"
              size="medium"
              style="width: 536px; top: -5px"
            >
              <el-option
                v-for="(item, index) in tradeMethods"
                :key="index"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="期望交割场所" prop="deliveryExchange">
            <el-select
              v-model="form.deliveryExchange"
              placeholder="全国碳排放权交易中心、北京环境交易所、上海环境能源交易所"
              size="medium"
              style="width: 536px; top: -5px"
            >
              <el-option
                v-for="(item, index) in exchangeList"
                :key="index"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button
            type="primary"
            @click="submit('form')"
            class="light-green-btn"
          >
            确定
          </el-button>
        </div>
      </el-dialog>
      <el-dialog title="上架成功" :visible.sync="showQuotation" width="30%">
        <span>
          您的采购单已提交，可在供需行情中查看。确定为您跳转供需行情沟通
        </span>
        <span slot="footer" class="dialog-footer">
          <el-button @click="showQuotation = false">取消</el-button>
          <el-button @click="toQuotation" type="primary">确定</el-button>
        </span>
      </el-dialog>
      <BuyAssets
        :dialogFormVisible="buyAssetsDlg"
        @changeBuyAssetsDialogFormVisible="changeDialogFormVisible"
      />
      <carbon-upload-vue
        :dialogFormVisible="carbonUploadDlg"
        :selData="list"
        :isCredit="false"
        title="碳配额项目上传"
        @changeVisible="changeCarbonVisible"
        @submit="submited"
      />
    </div>
  </div>
</template>

<script>
import {
  getExchangeDict,
  getDiliveryMethodeDict,
  getAssetStatusDict,
  AssetStatus,
} from "@/config/dictHelper";
import carbonUploadVue from "./carbonUpload.vue";
import { setListNo } from "@/libs/public";
import * as quota from "@/api/carbonAssetApi";
import BuyAssets from "@/views/carbonTrade/quotation/buyAssets.vue";
import { setLargeNumber } from "@/libs/public";

export default {
  name: "quota",
  components: { BuyAssets, carbonUploadVue },
  data() {
    return {
      pickerOptions: {
        disabledDate(time) {
          return (
            time.getTime() <
            new Date().setTime(new Date().getTime() - 3600 * 1000 * 24)
          );
        },
      },
      indeterminateFlag: false, // 表头复选框状态
      reRender: true, // 重新渲染列表使用
      allchecked: false,
      exchangeList: [], // 交易所字典
      tradeMethods: [], // 交易方式字典
      searchKeyword: "",
      assetStatusList: [], // 资产状态字典
      dialogFormVisible: false,
      buyAssetsDlg: false,
      list: [],
      total: 0,
      current: 1,
      carbonUploadDlg: false,
      pageCount: 1,
      showQuotation: false,
      sellCarbonTotal: 0,
      flag: 0,
      topData: {
        total: 0,
        availableAmount: 0,
        lockedAmount: 0,
        frozenAmount: 0,
      },
      form: {
        id: 0,
        tradeQuantity: "", // 出售数量
        assetUnitPrice: "", // 出售单价
        expirationDate: "", // 出售截止时间
        deliveryTime: "", // 期望交割时间
        deliveryMethod: "", // 期望交割方式
        deliveryExchange: "", // 期望交割地点
        projectType: "", // 项目类型
        tradeRole: "0270000002", // 方向
        status: "",
        projectId: null,
        assetType: "0140000002", // 资产类型
        institutionName: "",
      },
      // 校验规则
      rules: {
        tradeQuantity: [
          { required: true, message: "请输入出售数量", trigger: "blur" },
        ],
        // negotiatedPrice: [
        //   { required: true, message: "请输入出售单价", trigger: "blur" }
        // ],
        // expirationDate: [
        //   {
        //     type: "date",
        //     required: true,
        //     message: "请选择日期",
        //     trigger: "change"
        //   }
        // ],
        // deliveryTime: [
        //   {
        //     type: "date",
        //     required: true,
        //     message: "请选择时间",
        //     trigger: "change"
        //   }
        // ],
        // deliveryMethod: [
        //   {
        //     required: true,
        //     message: "请选择交割方式",
        //     trigger: "change"
        //   }
        // ],
        // deliveryExchange: [
        //   {
        //     required: true,
        //     message: "请选择交割场所",
        //     trigger: "change"
        //   }
        // ];
      },
      title: "",
      pageSize: 10,
      optionsStandard: [
        {
          value: "",
          label: "",
        },
      ],
      optionsOnlines: [
        {
          value: "",
          label: "",
        },
      ],
      value: "",
      selectedAssetStatus: "", // 选中的资产状态
      selectDate: "",
      selectEndDate: "",
    };
  },
  mounted() {
    
    this.getList(1);
    this.getTopData();
    this.exchangeList = getExchangeDict(this.$store);
    this.tradeMethods = getDiliveryMethodeDict(this.$store);
    this.formatAssetStatus(getAssetStatusDict(this.$store));
    
    // console.log(this.switchTradeStatus("160000001"));
  },
  methods: {
    setNumber(str) {
      return setLargeNumber(str);
    },
    showTip() {
      this.showQuotation = true;
    },
    toQuotation() {
      this.$router.push("/trade/quotation");
    },
    changeCarbonVisible(res) {
      this.carbonUploadDlg = res;
    },
    changeDialogFormVisible(res) {
      this.buyAssetsDlg = res;
    },
    // switchTradeStatus(status) {
    //   switch (status) {
    //     case "01600000000":
    //       return "全部";
    //     case "01600000001":
    //       return "询报价";
    //     case "01600000002":
    //       return "意向成交";
    //     case "01600000003":
    //       return "交易履约";
    //     case "01600000004":
    //       return "已交易";
    //     case "01600000005":
    //       return "已取消";
    //   }
    // },
    cellStyle({ row, column, rowIndex, columnIndex }) {
      if (column.label != "操作") {
        return "cursor:pointer;";
      }
    },
    submited(res) {
      if (res) {
        this.getList(1);
        this.getTopData();
      }
    },
    // 格式化状态类型字典
    formatAssetStatus(data) {
      data.map((v) => {
        let CertificationItem = {
          label: "",
        };
        if (v.name === "全部") {
          CertificationItem.label = v.name;
        } else {
          CertificationItem.value = v.value;
          CertificationItem.label = v.name;
        }
        this.assetStatusList.push(CertificationItem);
      });
      console.log("assetStatusList",this.assetStatusList);
    },
    onClickBuy() {
      this.buyAssetsDlg = true;
    },
    toDetail(id) {
      
      this.$router.push({
        path: "/assets/quotaDetail",
        query: { id: id },
      });
    },
    onClickSearch() {
      const data = {
        AssetStatus: this.selectedAssetStatus[0],
        expiryDataStart: this.selectDate,
        expiryDataEnd: this.selectEndDate,
        agencyName: this.searchKeyword,
      };
      quota.loadCarbonQuotaPageList(data).then(
        (res) => {
          // 将接口返回的记录数据赋值给 list
          this.list = res.data.records;
          // 处理总条数、当前页码、总页数
          this.total = Number(res.data.total);
          this.current = Number(res.data.current);
          this.pageCount = Math.ceil(parseInt(res.total) / this.pageSize);

          // 遍历表格数据做格式化处理
          this.list.map((v) => {
            // 切割日期时间，只保留日期部分
            let time = v.expiryDate.split(" ");
            v.expiryDate = time[0];

            // 注释的交易状态转换逻辑（可根据需求启用）
            // v.transactionStatusName = this.switchTradeStatus(v.transactionStatus);

            // 遍历对象，将空值替换为 "--"
            for (var i in v) {
              v[i] = v[i] ? v[i] : "--";
            }
          });
        },
        (err) => {
          // 接口报错时提示（注意：通常查询失败用 error，成功用 success 可能语义不对，可根据实际场景调整）
          this.$message.success("查询失败");
        }
      );
    },
    // 空方法（可扩展编辑逻辑）
    onEdit() {},

    // 空方法（可扩展发布逻辑）
    onClickPublish() {},

    // 空方法（可扩展删除逻辑）
    onClickDelete() {},

    // 空方法（可扩展离线逻辑）
    onClickOffline() {},

    // 场外报价按钮逻辑：打开弹窗并赋值行数据
    outsizeTransaction(row) {
      this.dialogFormVisible = true;
      this.title = "场外报价：" + row.agencyName;
      this.form.id = row.id;
      this.form.projectId = row.carbonProjectId;
      this.form.assetType = row.transactionStatus;
      this.form.institutionName = row.agencyName;
      this.sellCarbonTotal = row.availableAmount;
      // this.form.projectType = row.fieldChildCode; // 注释的字段赋值（可根据需求启用）;
    },

    // 每页条数改变逻辑：更新 pageSize 并重新获取列表
    handleSizeChange(val) {
      this.pageSize = val;
      this.getList(this.current);
    },

    // 当前页改变逻辑：更新 current 并重新获取列表
    handleCurrentChange(val) {
      this.current = val;
      this.getList(val);
    },

    // 单元格点击逻辑：非“操作”列时跳转到详情页
    handle(row, column, cell, event) {
      if (column.label !== "操作") {
        this.toDetail(row.id);
      }
      // openUrlInNewWindow(row.sourceFileUrl) // 注释的打开新窗口逻辑（可根据需求启用）;
    },

    // 监听页面宽度变化：触发图表自适应（需保证 infoList 和 visitChart 存在）
    handleResize() {
      if (this.infoList) this.$refs.visitChart.handleResize();
    },
    getList(page) {
      // 构造请求参数
      const data = {
        asc: true,
        current: page,
        size: this.pageSize,
        sortField: "",
        // "status": 0,
        // "type": 0;
      };

      // 调用接口获取碳配额分页数据
      quota
        .loadCarbonQuotaPageList(data)
        .then((res) => {
          // 遍历数据，根据资产状态码映射状态名称
          res.data.records.forEach(function (e) {
            if (e.assetsStatus === "0130000004") {
              e.assetsStatusName = "待审核";
            } else if (e.assetsStatus === "0130000001") {
              e.assetsStatusName = "已签发";
            } else if (e.assetsStatus === "0130000002") {
              e.assetsStatusName = "已锁定";
            } else if (e.assetsStatus === "0130000003") {
              e.assetsStatusName = "已冻结";
            } else if (e.assetsStatus === "0130000005") {
              e.assetsStatusName = "驳回";
            }
          });

          // 赋值分页数据到组件变量
          console.log("QuotaRecords",res.data.records);
          this.list = res.data.records;
          this.total = Number(res.data.total);
          this.current = Number(res.data.current);
          this.pageCount = Math.ceil(parseInt(res.total) / this.pageSize);

          // 遍历表格数据做格式化处理
          this.list.map((v) => {
            // 切割日期，仅保留日期部分（去掉时间）
            if (v.expiryDate) {
              let time = v.expiryDate.split(" ");
              v.expiryDate = time[0];
            }
            // 空值/空字符串处理为 "--"
            for (var i in v) {
              v[i] = v[i] ? v[i] : "--";
              if (v[i] === " ") {
                v[i] = "--";
              }
            }
            // 注释的交易状态转换逻辑（可按需启用）
            // v.transactionStatusName = this.switchTradeStatus(v.transactionStatus);
          });
        })
        .catch((error) => {});
    },
    update() {
      // 构造查询参数：资产状态、日期范围、关键词
      const data = {
        assetsStatus: this.selectedAssetStatus[0],
        expiryDateStart: this.selectDate,
        expiryDateEnd: this.selectEndDate,
        agencyName: this.searchKeyword,
      };

      // 调用接口加载碳配额分页数据
      quota
        .loadCarbonQuotaPageList(data)
        .then((res) => {
          // 更新表格数据及分页信息
          this.list = res.data.records;
          this.total = Number(res.data.total);
          this.current = Number(res.data.current);
          this.pageCount = Math.ceil(parseInt(res.data.total) / this.pageSize);

          // 遍历数据做格式化处理
          this.list.map((v) => {
            v.checked = false; // 重置复选框状态
            v.type = v.type ? v.type : "--"; // 空值处理
            // 切割日期，仅保留日期部分（去掉时间）
            if (v.issuingDate) {
              let time = v.issuingDate.split(" ");
              v.issuingDate = time[0];
            }
            // 遍历字段，空值/空字符串替换为 "--"
            for (var i in v) {
              v[i] = v[i] ? v[i] : "--";
            }
          });
        })
        .catch((error) => {}); // 捕获异常（可补充具体逻辑）;
    },
    // 提交表单按钮：修改碳信用状态、校验及接口调用
    submit(formName) {
      // 构造修改碳信用状态的参数
      const data = {
        id: this.form.id,
        transactionStatus: "0160000001",
      };

      // 校验：出售数量不能超过可用量
      if (this.form.tradeQuantity > this.sellCarbonTotal) {
        this.$message.warning("出售数量不能大于可用量");
        return;
      }

      // 校验：出售数量必填
      if (this.form.tradeQuantity) {
        this.form.assetType = "0140000002";
        // 调用接口：添加碳资产到市场
        quota.addcarbonAssetMarket(this.form).then(
          (res) => {
            // 调用接口：变更配额
            quota.changeQuota(data).then(
              (res) => {
                this.$message.success("操作成功");
                this.showTip(); // 需确保 showTip 方法已定义
                this.dialogFormVisible = false; // 关闭弹窗
                this.getList(1); // 重新获取列表数据;
              },
              (err) => {} // 捕获变更配额的异常（可补充逻辑）
            );
          },
          (err) => {
            this.$message.warning("操作失败"); // 添加碳资产到市场失败提示;
          }
        );
      } else {
        this.$message.warning("必填项不能为空"); // 出售数量未填提示;
      }
    },

    // 打开碳配额上传弹窗
    onClickUpload() {
      this.carbonUploadDlg = true;
    },

    // 获取顶部统计数据（带节流控制）
    getTopData() {
      if (this.flag === 1) {
        return; // 标记为 1 时跳过，避免重复请求;
      }
      this.flag = 1; // 设置标记
      // 调用接口：获取配额数据
      quota.getQuotaData().then((res) => {
        this.topData = res.data; // 赋值顶部统计数据
        this.flag = 0; // 重置标记，允许下次请求;
      });
    },
    // 复选框选中状态变更：处理全选、半选逻辑
    signCheckChange() {
      let allCheckedFlag = true;
      let allReset = true;

      // 遍历数据，判断是否全选/全不选
      this.articals.forEach((item) => {
        if (item.checked === true) {
          allReset = false; // 存在选中项，不是“全不选”;
        } else {
          allCheckedFlag = false; // 存在未选中项，不是“全选”;
        }
      });

      // 更新全选标记和半选状态
      if (allCheckedFlag || allReset) {
        this.indeterminateFlag = false; // 非半选
        this.allchecked = allCheckedFlag; // 全选/全不选;
      } else {
        this.indeterminateFlag = true; // 半选状态;
      }

      this.reRender = !this.reRender; // 触发重新渲染（用于强制更新视图）;
    },

    // 场内交易：跳转到交易账户页面
    insideTransaction() {
      this.$router.push("/trade/account/exchange");
    },

    // 更新全选状态：批量设置表格数据的 checked 值
    updateAllSelected(val) {
      this.indeterminateFlag = false; // 关闭半选状态
      if (val) {
        // 全选：遍历设置 checked = true
        this.list.forEach((item) => {
          item.checked = true;
        });
      } else {
        // 取消全选：遍历设置 checked = false
        this.list.forEach((item) => {
          item.checked = false;
        });
      }
    },

    // 计算当前列表序号（结合分页）
    getCurListNo(index) {
      let curNo = parseInt(index) + 1; // 基础序号
      let size = this.size || "10"; // 每页条数
      let page = this.current - 1; // 当前页（页码从 1 开始时需减 1）
      let no = setListNo(page, size, curNo); // 调用工具方法计算最终序号
      return no ? no : 1;
    },

    // 渲染表头复选框（JSX/Render 函数语法）
    renderCheckHeader(h, { column, $index }) {
      return h("span", {}, [
        h("el-checkbox", {
          props: {
            checked: this.allchecked, // 全选状态绑定
            indeterminate: this.indeterminateFlag, // 半选状态绑定;
          },
          on: {
            change: this.updateAllSelected, // 全选事件绑定;
          },
        }),
      ]);
    },

    // 导出功能（当前仅提示，需补充实际逻辑）
    onClickExport() {
      this.$message.success("功能即将推出，敬请期待！");
    },
  },
  created() {
    this.getTopData();
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

// 覆盖 el-cascader 输入框样式
:deep(.el-cascader .el-input .el-input__inner),
:deep(.el-cascader .el-input.is-focus .el-input__inner) {
  border-color: transparent;
}

// 覆盖带时间选择的 el-date-picker 样式
:deep(.el-date-picker.has-sidebar.has-time) {
  background: #0a5857d6;
  color: #fff;
  border: 1px solid #22f4d6;
}

// 覆盖 el-date-picker 表头标签样式
:deep(.el-date-picker__header-label) {
  color: #ffffff;
}

.acea-row {
  :deep(.el-avatar--small) {
    width: 22px;
    height: 22px;
  }
}

.checkline {
  :deep(.el-radio__input) {
    display: none;
  }
}

.ivu-pl-8 {
  margin-left: 8px;
}

.dashboard-console-visit {
  :deep(.el-card__header) {
    padding: 14px 20px !important;
  }
}
ul {
  li {
    list-style-type: none;
    margin-top: 12px;
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
  font-weight: 500;
  color: #24a776;
}

.myassets-container {
  display: flex;
  flex-direction: row;
  margin-top: 15px;
  margin-bottom: 20px;
  padding-left: 10px;
  padding-right: 10px;
  height: 54px;
  background: #e3f2ec;
  border-radius: 6px;
  // opacity: 0.1;
}

.assets-hint {
  margin-top: auto;
  margin-bottom: auto;
  font-weight: 400;
  color: #424cbc;
}

.require {
  color: red;
  position: absolute;
  left: -20px;
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

.vertical-center {
  margin-top: auto;
  margin-bottom: auto;
}
</style>
