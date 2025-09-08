<template>
  <div>
    <!-- 场外上架按钮弹出页面 -->
    <el-dialog
      :title="title"
      :visible.sync="show"
      :before-close="clickClose"
      width="720px"
    >
      <el-form label-position="left" label-width="130px" :model="form">
        <el-form-item label="出售数量(tCO2e)" prop="tradeQuantity">
          <span class="require">*</span>
          <el-input
            v-model="form.tradeQuantity"
            size="medium"
            style="width: 268px; top: -5px"
          ></el-input>
        </el-form-item>
        <el-form-item label="出售单价(¥)" prop="negotiatedPrice">
          <el-input
            v-model="form.negotiatedPrice"
            size="medium"
            style="width: 268px; top: -5px"
          ></el-input>
        </el-form-item>
        <el-form-item label="出售截止时间" prop="expirationDate">
          <el-date-picker
            type="date"
            placeholder="选择日期"
            size="medium"
            v-model="form.expirationDate"
            style="width: 268px; top: -5px"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="期望交割时间" prop="deliveryTime">
          <el-date-picker
            type="date"
            placeholder="选择日期"
            size="medium"
            v-model="form.deliveryTime"
            style="width: 268px; top: -5px"
          ></el-date-picker>
        </el-form-item>

        <el-form-item label="期望交割方式" prop="deliveryMethod">
          <el-select
            v-model="form.deliveryMethod"
            placeholder="选择期望交割方式"
            size="medium"
            style="width: 536px; top: -5px"
          >
            <el-option
              v-for="(item, index) in tradeMethods"
              :key="index"
              :label="item.name"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="期望交割场所" prop="deliveryExchange">
          <el-select
            v-model="form.deliveryExchange"
            placeholder="选择期望交割场所"
            size="medium"
            style="width: 536px; top: -5px"
          >
            <el-option
              v-for="(item, index) in exchangeList"
              :key="index"
              :label="item.name"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submit">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="上架成功" :visible.sync="showQuotation" width="30%">
      <span>是否进入供需行情，参与碳交易？</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showQuotation = false">取消</el-button>
        <el-button @click="toQuotation" type="primary">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import * as credit from "@/api/carbonAssetApi"
import {getTenantInfo} from "@/api/systemadmin";
export default {
  props:{
    dialogFormVisible: false,
    // 选中的数据：类型为 Object，默认值为空对象
    selData: {
      type: Object, // 明确指定类型
      default: () => ({}) // 对象/数组的默认值必须用函数返回，避免引用类型共享
    }
  },
  name: "otcListing",
  data() {
    return {
      show: this.dialogFormVisible, //自加
      showQuotation: false, //场外上架弹出框
      exchangeList: [
        { value: "全国碳排放权交易中心", label: "全国碳排放权交易中心" },
        { value: "北京环境交易所", label: "北京环境交易所" },
        { value: "上海环境能源交易所", label: "上海环境能源交易所" }
      ], // 交割场所列表
      tradeMethods: [
        { value: "协议转入", label: "协议转入" },
        { value: "竞价交易", label: "竞价交易" },
        { value: "定价交易", label: "定价交易" }
      ],
      form: {
        id: "",
        tradeQuantity: "", //出售数量
        negotiatedPrice: "", //出售单价
        expirationDate: "", //出售截止时间
        deliveryTime: "", //期望交割时间
        deliveryMethod: "", //期望交割方式
        deliveryExchange: "", //期望交割场所
        projectType: "", //项目类型
        carbonProjectId: null,
        direction: "0200000002", //方向
        status: "",
        assetType: "0140000001", //资产类型
        tenantName: "",
        availableAmount:"" , //可用数量
        tenantId:"", //租户的id
        contactsName:"", //从租户中获取的
        contactsPhone:"",//从租户中获取的
        contactsEmail:""
      },
      // 校验规则
      rules: {
        tradeQuantity: [{ message: "请输入出售数量", trigger: "blur" }],
        negotiatedPrice: [
          { required: true, message: "请输入出售单价", trigger: "blur" }
        ],
        expirationDate: [
          {
            type: "date",
            required: true,
            message: "请选择日期",
            trigger: "change"
          }
        ],
        deliveryTime: [
          {
            type: "date",
            required: true,
            message: "请选择时间",
            trigger: "change"
          }
        ],
        deliveryMethod: [
          {
            required: true,
            message: "请选择交割方式",
            trigger: "change"
          }
        ],
        deliveryExchange: [
          {
            required: true,
            message: "请选择交割场所",
            trigger: "change"
          }
        ]
      },
      title: "场外上架"
    };
  },
  methods: {
    toQuotation() {

    },
    // 自加close方法
    clickClose() {
      this.$emit("changeVisible", false);
      this.show = false;
    },
    // 格式化日期（提取日期部分），这里先检查一下是否为null
    formatDate(date) {
      // 检查 date 是否存在且为字符串
      if (!date || typeof date !== 'string') {
        return ''; // 或者返回默认值
      }
      let dateList = date.split(" ");
      return dateList[0];
    },
    // 场外报价按钮逻辑,无用
    // outsideTransaction() {
    //   this.dialogFormVisible = true;
    //   this.form.id = this.assetDetail["id"];
    //   this.form.projectId = this.assetDetail["carbonProjectId"];
    //   this.form.projectType = this.assetDetail["fieldChildCode"];
    //   this.form.assetType = this.assetDetail["transactionStatus"];
    //   this.form.institutionName = this.assetDetail["projectName"];
    //   this.title = this.assetDetail["projectName"];
    //   this.form.deliveryMethod = "0190000000";
    //   this.form.deliveryExchange = "0170000000";
    // },
    // 提交表单按钮逻辑
    async submit() {


        console.log("测试的tenantedId为：",this.form.tenantId);
        // 通过租户获取联系方式、机构名称等信息
        const res = await getTenantInfo(this.form.tenantId);

        console.log("租户的信息为11111：",res);
        this.form.tenantName = res.tenantName;
        this.form.contactsName = res.contactsName;
        this.form.contactsPhone = res.contactsPhone;
        this.form.contactsEmail = res.contactsEmail;
        // console.log("sod：",this.form.carbonProjectId);
        // console.log("租户的信息为11111：",this.form);
        // 添加场外上架表单
        // 交易角色，机构名称、联系人姓名、联系人手机、邮箱需要根据登录的时候携带？！
        let data = {
          projectId:this.form.carbonProjectId,
          tradeRole: "0270000002",
          institutionName: this.form.tenantName,
          contactsName: this.form.contactsName,
          contactsPhone: this.form.contactsPhone,
          contactsEmail: this.form.contactsEmail,
          tradeQuantity: this.form.tradeQuantity,
          assetUnitPrice: this.form.negotiatedPrice,
          expirationDate: this.formatDate(this.form.expirationDate),
          deliveryTime: this.formatDate(this.form.deliveryTime),
          deliveryMethod: this.form.deliveryMethod,
          deliveryExchange: this.form.deliveryExchange
        };

        // 校验出售数量是否超过可用量
        if (this.form.availableAmount < this.form.tradeQuantity) {
          this.$message.warning("出售数量不能大于可用量");
          return;
        }
        console.log("现在的传入id: ",this.form.id);
        console.log("测试的数据222222：",data);
        // 调用接口提交表单
        const result = await credit.addcarbonAssetMarket(data);
            let changeCreditData= {
              id: this.form.id,
              availableAmount: this.form.availableAmount - this.form.tradeQuantity
            }
        // 提交成功后修改信用状态
        await credit.changeCredit(changeCreditData);
        this.$message.success("操作成功");
        // this.dialogFormVisible = false;
        this.show = false;
        this.showQuotation = true;

      // catch (error)
      // {
      //     console.error("操作失败：", error);
      //     this.$message.error("操作失败");
      // }
    }
  },
  watch: {
    dialogFormVisible() {
      this.show = this.dialogFormVisible;
    },
    selData:{
      immediate: true,
      handler(newVal) {
        if (newVal && Object.keys(newVal).length) {
          this.form = { ...newVal };
          console.log("当前的场外上架的formData数据为：", this.form);
          console.log("当前初始化的场外上架的id为：",this.form.id);
          if(this.form.id){
            this.id = Number(this.form.id)
          }
        }
      }
    }
  }
};
</script>
<style lang="scss" scoped>
// .root {
//   background: #f2f5f7; // 推测原 "#f2f5f7" 更合理，若需严格还原可改回 "#f2f5f7"
// }
// .container {
//   margin: 10px 0px 20px 0px;
//   display: flex;
//   flex-direction: row;
// }

// .cardBody {
//   margin: 30px 30px 30px 30px;
// }
.require {
  color: red;
  position: absolute;
  left: -20px;
}
</style>
