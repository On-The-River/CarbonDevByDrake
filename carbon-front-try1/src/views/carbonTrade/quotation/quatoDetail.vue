<template>
  <div class="root">
    <div class="divBox">
      <div class="content-container">
        <!-- 顶部卡片 - 配额详情 -->
        <div class="cardBody">
          <span class="header">配额详情</span>
          <span class="asset-title">{{ assetDetail.projectName }}</span>
          <el-divider />
          <img src="@/assets/icon/plant.png" alt="" class="icon" />
          <span class="asset-title-right">基本信息</span>
          <div class="item-div">
            <span class="item-title">类型：</span>
            <span class="item-content">{{ assetDetail.type || "—" }}</span>
          </div>
          <div class="item-div">
            <span class="item-title">签发日期：</span>
            <span class="item-content">{{
              assetDetail.issuingDate || "—"
            }}</span>
          </div>
          <div class="item-div">
            <span class="item-title">签发凭证：</span>
            <span
              class="item-content"
              style="color: #1020ff; cursor: pointer"
              @click="toDetail(assetDetail.issuingCertificate)"
            >
              {{ assetDetail.issuingCertificate || "—" }}
            </span>
          </div>
          <div class="item-div">
            <span class="item-title">有效期：</span>
            <span class="item-content">{{
              assetDetail.expiryDate || "—"
            }}</span>
          </div>
          <el-divider />

          <!-- 项目信息 -->
          <img src="@/assets/icon/plant.png" alt="" class="icon" />
          <span class="asset-title-right">项目信息</span>
          <span
            class="detail"
            @click="toDetail(assetDetail.projectIntroduction)"
            v-if="assetDetail.projectIntroduction"
          >
            查看详情<i class="el-icon-arrow-right blue" />
          </span>
          <div class="basic-div" style="padding: 20px 10px 20px 16px">
            <p class="paragraph">
              {{ assetDetail.projectIntroduction | format }}
            </p>
          </div>
          <el-divider />

          <!-- 资产状态与数量 -->
          <img src="@/assets/icon/plant.png" alt="" class="icon" />
          <span class="asset-title-right">资产信息</span>
          <div class="item-div">
            <span class="item-title">资产状态：</span>
            <span class="item-content">{{
              assetDetail.assetsStatusName || "—"
            }}</span>
          </div>
          <div class="item-div">
            <span class="item-title">持仓总量(tCO2e)：</span>
            <span class="item-content">{{
              setNumber(assetDetail.total) || "—"
            }}</span>
          </div>
          <div class="item-div">
            <span class="item-title">资产估值(￥)：</span>
            <span class="item-content">{{
              setNumber(assetDetail.valuation) || "—"
            }}</span>
          </div>
          <div class="item-div">
            <span class="item-title">可用数量(tCO2e)：</span>
            <span class="item-content">{{
              setNumber(assetDetail.availableAmount) || "—"
            }}</span>
          </div>
          <div class="item-div">
            <span class="item-title">锁定数量(tCO2e)：</span>
            <span class="item-content">{{
              setNumber(assetDetail.lockedAmount) || "—"
            }}</span>
          </div>
          <div class="item-div">
            <span class="item-title">冻结数量(tCO2e)：</span>
            <span class="item-content">{{
              setNumber(assetDetail.frozenAmount) || "—"
            }}</span>
          </div>
        </div>
        <!-- 交易信息模块 -->
<br/>
<br>
  <img
    src="@/assets/icon/com_plant.png"
    alt=""
    class="icon"
  />
  <span class="asset-little-title">交易信息</span>
  <span
    class="detail"
    @click="toAccount"
  >
    查看详情<i class="el-icon-arrow-right" />
  </span>
</br>
<div class="basic-div" style="height: 174px">
  <!-- 交易状态 -->
  <div class="item-div">
    <span class="item-title">交易状态</span>
    <br />
    <span class="item-content">
      {{ assetDetail.transactionStatusName || '—' }}
    </span>
  </div>
  <!-- 交割场所 -->
  <div class="item-div">
    <span class="item-title">交割场所</span>
    <br />
    <span class="item-content">
      {{ assetDetail.carbonExchangeId || '—' }}
    </span>
  </div>
  <!-- 购入凭证 -->
  <div class="item-div">
    <span class="item-title">购入凭证</span>
    <br />
    <span
      class="item-content"
      style="color: #120fff; cursor: pointer"
      @click="toBuyFile"
    >
      {{ assetDetail.buyCertificateFileName || '—' }}
    </span>
  </div>
  <!-- 购入单价 -->
  <div class="item-div">
    <span class="item-title">购入单价(￥)</span>
    <br />
    <span class="item-content">
      {{ assetDetail.buyUnitPrice || '—' }}
    </span>
  </div>
  <!-- 购入总价 -->
  <div class="item-div">
    <span class="item-title">购入总价(￥)</span>
    <br />
    <span class="item-content">
      {{ assetDetail.buyTotalPrice || '—' }}
    </span>
  </div>
  <!-- 购入日期 -->
  <div class="item-div">
    <span class="item-title">购入日期</span>
    <br />
    <span class="item-content">
      {{ assetDetail.buyDate || '—' }}
    </span>
  </div>
</div>
<br/>
<br/>
  <div
    class="basic-div"
    style="height: 89px; background-color: white"
  >
    <!-- 场内交易按钮 -->
    <button
      style="width: 90px; float: right"
      class="light-green-btn"
      @click="insideTransaction"
    >
      场内交易
    </button>
    <!-- 场外报价按钮 -->
    <button
      style="width: 90px; float: right; margin-right: 20px"
      class="light-green-btn"
      @click="outsideTransaction"
    >
      场外报价
    </button>
    <!-- 返回按钮 -->
    <button
      style="width: 90px; float: right; margin-right: 20px"
      class="normal-white-btn"
      @click="goBack"
    >
      返回
    </button>
  </div>
  <!-- 场外报价弹窗 -->
<el-dialog
  title="场外报价"
  :visible.sync="dialogFormVisible"
  width="720px"
>
  <el-form
    label-position="left"
    label-width="130px"
    :model="form"
    :rules="rules"
  >
    <!-- 出售数量 -->
    <el-form-item label="出售数量(tCO2e)" prop="tradeQuantity">
      <el-input
        v-model="form.tradeQuantity"
        size="medium"
        style="width: 268px; top: -5px"
      />
    </el-form-item>
    <!-- 出售单价 -->
    <el-form-item label="出售单价(￥)" prop="negotiatedPrice">
      <el-input
        v-model="form.negotiatedPrice"
        size="medium"
        style="width: 268px; top: -5px"
      />
    </el-form-item>
    <!-- 出售截止时间 -->
    <el-form-item label="出售截止时间" prop="expirationDate">
      <el-date-picker
        type="date"
        placeholder="选择日期"
        size="medium"
        v-model="form.expirationDate"
        style="width: 268px; top: -5px"
      />
    </el-form-item>
    <!-- 期望交割时间 -->
    <el-form-item label="期望交割时间" prop="deliveryTime">
      <el-date-picker
        type="date"
        placeholder="选择日期"
        size="medium"
        v-model="form.deliveryTime"
        style="width: 268px; top: -5px"
      />
    </el-form-item>
    <!-- 期望交割方式 -->
    <el-form-item label="期望交割方式" prop="deliveryMethod">
      <el-select
        v-model="form.deliveryMethod"
        placeholder="协议转入、竞价交易、定价交易"
        size="medium"
        style="width: 550px; top: -5px"
      >
        <el-option
          v-for="(item, index) in tradeMethods"
          :key="index"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <!-- 期望交割场所 -->
    <el-form-item label="期望交割场所" prop="deliveryExchange">
      <el-select
        v-model="form.deliveryExchange"
        placeholder="全国碳排放权交易中心、北京环境交易所、上海环境能源交易所"
        size="medium"
        style="width: 550px; top: -5px"
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
      @click="submit"
      class="light-green-btn"
    >
      确定
    </el-button>
  </div>
</el-dialog>

<!-- 报价确认弹窗 -->
<el-dialog
  title="报价成功"
  :visible.sync="showQuotation"
  width="300px"
>
  <span>是否进入供需行情，参与碳交易？</span>
  <span slot="footer" class="dialog-footer">
    <el-button
      @click="showQuotation = false"
      class="normal-white-btn"
    >
      取消
    </el-button>
    <el-button
      @click="toQuotation"
      class="light-green-btn"
    >
      确定
    </el-button>
  </span>
</el-dialog>
</div>
</div>
</div>
</template>
<script>
import { carbonQuotaDetail } from "@/api/carbonAssetApi";
import { openUrlInNewWindow } from "@/libs/OpenHelper";
import { getExchangeDict } from "@/config/dictHelper";
import { getDeliveryMethodDict } from "@/config/dictHelper";
import * as credit from "@/api/carbonAssetApi";
import { setLargeNumber } from "@/libs/public";
import { title } from "@/settings";

export default {
  name: "",
  data() {
    return {
      assetId: 0,
      assetDetail: [],
      url: "https://carbonmsger.feishu.cn/drive/folder/fldcn6oyoD40xWZgEMH1L60QSg?from=space_personal_filelist",
      dialogFormVisible: false, // 场外报价弹窗显隐
      exchangeList: [], // 交割场所字典
      tradeMethods: [], // 交割方式字典
      showQuotation: false, // 场外上架弹窗显隐
      form: {
        id: 0,
        tradeQuantity: "", // 出售数量
        negotiatedPrice: "", // 出售单价
        expirationDate: "", // 出售截止时间
        deliveryTime: "", // 期望交割时间
        deliveryMethod: "", // 期望交割方式
        deliveryExchange: "", // 期望交割地点
        projectType: "", // 项目类型
        tradeRole: "0270000002", // 交易方向
        status: "",
        projectId: null,
        assetType: "0140000001", // 资产类型
        institutionName: "",
      },
      // 校验规则
      rules: {
        tradeQuantity: [
          { required: true, message: "请输入出售数量", trigger: "blur" },
        ],
        // negotiatedPrice: [
        //   { required: true, message: "请输入出售单价", trigger: "blur" },
        // ],

        // expirationDate: [
        //   {
        //     type: "date",
        //     required: true,
        //     message: "请选择日期",
        //     trigger: "change",
        //   },
        // ],

        // deliveryTime: [
        //   {
        //     type: "date",
        //     required: true,
        //     message: "请选择时间",
        //     trigger: "change",
        //   },
        // ],

        // deliveryMethod: [
        //   {
        //     required: true,
        //     message: "请选择交割方式",
        //     trigger: "change",
        //   },
        // ],

        // deliveryExchange: [
        //   {
        //     required: true,
        //     message: "请选择交割场所",
        //     trigger: "change",
        //   },
        // ],
      },
      title: "",
    };
  },
  mounted() {
    this.loadDetail();
    this.exchangeList = getExchangeDict(this.$store);
    this.tradeMethods = getDeliveryMethodDict(this.$store);
  },
  filters: {
    file(value) {
      if (value) {
        let values = value.split('/');
        return values[values.length - 1];
      }
    },
    format(val) {
      if (val) {
        return '--';
      }
    },
  },
  methods: {
    // 数字格式化（调用工具方法）
    setNumber(str) {
      return setLargeNumber(str);
    },

    // 跳转详情页（示例：跳转到钱包页面，需确认实际路径）
    toDetail() {
      this.$router.push("/account/wallet");
    },

    // 跳转到上架页面
    toQuotation() {
      this.$router.push("/trade/quotation");
    },

    // 打开购入凭证文件（新窗口）
    toBuyFile() {
      if (this.assetDetail.buyCertificate) {
        openUrlInNewWindow(this.assetDetail.buyCertificate);
      }
    },

    // 打开交易所页面（新窗口）
    toAccount() {
      openUrlInNewWindow(this.assetDetail.carbonExchangeWebsite);
    },

    // 加载资产详情（接口调用 + 数据格式化）
    loadDetail() {
      let id = this.$route.query;
      this.assetId = id.id;

      carbonQuotaDetail(this.assetId).then(
        (res) => {
          this.assetDetail = res.data;

          // 日期格式化（签发日期、到期日期、购入日期）
          this.assetDetail.issuingDate = this.formatDate(
            this.assetDetail.issuingDate
          );
          this.assetDetail.expiryDate = this.formatDate(
            this.assetDetail.expiryDate
          );
          this.assetDetail.buyDate = this.formatDate(
            this.assetDetail.buyDate
          );

          // 空值处理：遍历字段，空值替换为 "--"
          for (var i in this.assetDetail) {
            this.assetDetail[i] = this.assetDetail[i]
              ? this.assetDetail[i]
              : "--";
            if (this.assetDetail[i] === " ") {
              this.assetDetail[i] = "--";
            }
          }
        },
        (err) => {
          this.$message.success("加载数据失败");
        }
      );
    },

    // 日期格式化：提取日期部分（分割后取第一项）
    formatDate(date) {
      if (date) {
        let dateList = date.split(" ");
        return dateList[0];
      }
    },
    methods: {
      // 场外报价弹窗初始化
      outsideTransaction() {
        this.dialogFormVisible = true;
        this.form.id = this.assetDetail.id;
        this.form.projectType = this.assetDetail.fieldChildCode;
        this.form.projectId = this.assetDetail.carbonProjectId;
        this.form.assetType = this.assetDetail.transactionStatus;
        this.form.institutionName = this.assetDetail.projectName;
        let title = "场外报价：" + this.assetDetail.agencyName;
        this.title = title;
        this.form.deliveryMethod = "0130000000";
        this.form.deliveryExchange = "0170000000";
      },

      // 返回上一页
      goBack() {
        this.$router.go(-1);
      },

      // 场内交易跳转
      insideTransaction() {
        this.$router.push("/trade/account/exchange");
      },

      // 提交表单（修改碳信用状态）
      submit() {
        // 构造修改状态的参数
        const data = {
          id: this.form.id,
          transactionStatus: "0160000001",
        };

        // 校验：出售数量不能超过可用量
        if (this.assetDetail.availableAmount < this.form.tradeQuantity) {
          this.$message.warning("出售数量不能大于可用量");
          return;
        }

        // 调用接口：添加碳资产到市场
        credit.addcarbonAssetMarket(this.form).then(
          (res) => {
            // 调用接口：变更配额
            credit.changeQuota(data).then(
              (res) => {
                this.$message.success("操作成功");
                this.dialogFormVisible = false; // 关闭弹窗
                this.loadDetail(); // 重新加载详情
              },
              (err) => { } // 捕获变更配额异常
            );
          },
          (err) => {
            this.$message.success("操作失败"); // 添加资产失败提示
          }
        );
      },
    },
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

.cardBody {
  margin: 30px 30px 30px 30px;
}

.header {
  width: 95px;
  height: 18px;
  font-weight: 500;
  color: #24a776;
}

.asset-title {
  width: 380px;
  height: 18px;
  font-weight: 500;
  color: #24a385;
  margin-left: 20px;
}

.el-divider {
  margin-top: 40px;
}

.icon {
  width: 22px;
  height: 18px;
}

.asset-little-title {
  width: 54px;
  height: 16px;
  font-weight: 500;
  color: #424cbc;
  margin-left: 10px;
}
/* 响应式：屏幕宽度 ≤1900px 时的样式（注释状态） */
/* 
@media screen and (max-width: 1900px) {
 .basic-div {
    width: 97%;
    height: 89px;
    background: #e7f9f4;
    border-radius: 8px;
    margin-left: 20px;
    margin-top: 40px;
  }
}
*/

/* 响应式：屏幕宽度 ≤1900px 时的样式（注释状态，与上一段重复，可能是冗余代码） */
/* 
@media screen and (max-width: 1900px) {
 .basic-div {
    width: 97%;
    height: 174px;
    background: #e7f9f4;
    border-radius: 8px;
    margin-left: 20px;
    margin-top: 40px;
  }
}
*/

/* 基础容器样式 */
.basic-div {
  width: 97%;
  height: 174px;
  background: #e7f9f4;
  border-radius: 8px;
  margin-left: 20px;
  margin-top: 40px;
}

/* 列表标题样式 */
.item-title {
  width: 80px;
  height: 14px;
  font-weight: 400;
  color: #424cbc;
}

/* 列表内容样式 */
.item-content {
  width: 420px;
  height: 15px;
  font-weight: 400;
  color: #24a385;
  position: relative;
  top: -3px;
}

/* 列表项容器样式 */
.item-div {
  float: left;
  margin: 14px 20px 18px;
  width: 180px;
  height: 40px;
}

/* 辅助文本样式 */
.accessory {
  width: 80px;
  height: 14px;
  font-weight: 400;
  color: #24a776;
  margin-left: 10px;
}

/* 段落文本样式 */
.paragraph {
  width: 100%;
  height: 100px;
  font-weight: 400;
  color: #424cbc;
  text-align: justify;
}
.detail {
  width: 58px;
  height: 14px;
  font-weight: 400;
  color: #24a776;
  margin-left: 15px;
  cursor: pointer;
}
</style>