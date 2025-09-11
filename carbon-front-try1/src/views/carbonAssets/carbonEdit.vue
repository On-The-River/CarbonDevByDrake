<template>
  <div>
    <el-dialog
      :title="title"
      :visible.sync="show"
      :before-close="clickClose"
      width="720px"
    >
    <el-form :model="form" :inline="true">
      <el-form-item>
        <!-- <span class="required-text">*</span> -->
        <span class="label">项目名称<span style="color: red;">*</span></span>
        <el-input
          v-model="form.projectName"
          disabled
          size="medium"
          style="width: 420px"
        />
      </el-form-item>
      <el-button
        style="width: 100px; text-align: center"
        type="primary"
      >选择项目</el-button
      >
      <el-form-item>
        <span class="label">采用方法学</span>
        <el-input
          v-model="form.carbonMethodologyName"
          size="medium"
          style="width: 180px"
          disabled
        />
      </el-form-item>
      <el-form-item style="margin-left: 40px">
        <span class="label">类型</span>
        <el-input
          v-model="form.projectScopeType"
          size="medium"
          style="width: 180px"
          disabled
        />
      </el-form-item>
      <el-form-item>
          <span class="label"
          >持仓总量(tCO2e)<span style="color: red;">*</span></span
          >
        <el-input
          v-model="form.total"
          autocomplete="off"
          size="medium"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item style="margin-left: 40px">
        <span class="label">交易单价(¥)</span>
        <el-input
          v-model="form.buyUnitPrice"
          autocomplete="off"
          size="medium"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item>
        <span class="label">交易总价(¥)</span>
        <el-input
          v-model="form.buyTotalPrice"
          autocomplete="off"
          size="medium"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item style="margin-left: 40px">
        <span class="label">核证机构</span>
        <el-input
          v-model="form.certifiedAgency"
          style="width: 180px"
          size="medium"
          disabled
        />
      </el-form-item>
      <el-form-item>
        <span class="label">签发日期</span>
        <el-date-picker
          type="date"
          v-model="form.issuingDate"
          autocomplete="off"
          size="medium"
          style="width: 180px"
          disabled
        />
      </el-form-item>
      <el-form-item>
        <span class="label" style="margin-left: 40px">交易日期</span>
        <el-date-picker
          type="date"
          v-model="form.buyDate"
          autocomplete="off"
          size="medium"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item>
        <span class="label">交易所</span>
        <el-select
          v-model="form.carbonExchangeName"
          placeholder="请选择"
          style="width: 180px"
          size="medium"
        >
          <el-option
            v-for="(item, index) in exchangeList"
            :key="index"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <br />
      <el-form-item>
          <span class="label" style="width: 305px"
          >持仓凭证<span style="color: red;">*</span></span
          >
        <el-upload
          class="upload-demo"
          ref="upload1"
          :action="upLoadParam.url"
          :file-list="issueFileList"
          :limit="2"
          :auto-upload="true"
          :on-success="creditSuccess1"
          :on-error="handleError1"
          style="width: 180px"
          :headers="{ token: upLoadParam.token }"
          :on-change="handleIssueChange"
          :on-preview="creditHandleFile"
        >
          <el-button type="primary">上传</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item style="margin-left: 40px">
        <span class="label">交易凭证</span>
        <el-upload
          class="upload-demo"
          ref="upload2"
          :action="upLoadParam.url"
          :file-list="tranFileList"
          :auto-upload="true"
          :limit="2"
          style="width: 180px"
          :on-success="creditSuccess2"
          :headers="{ token: upLoadParam.token }"
          :on-change="handleTranChange"
          :on-preview="creditHandleFile"
          :on-error="handleError2"
        >
          <el-button type="primary">上传</el-button>
        </el-upload>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="clickClose">取 消</el-button>
      <el-button type="primary" @click="clickSave">保存</el-button>
      <el-button type="primary" @click="submit" style="margin-right: 25px"
      >提交</el-button
      >
    </div>
    </el-dialog>
  </div>
</template>
<script>
import {openUrlInNewWindow} from "@/libs/OpenHelper";
import {changeCredit} from "@/api/carbonAssetApi";
import {getFeiShuUploadProjectParams} from "@/api/tenant";
import {getIssueInstitution} from "@/config/dictHelper";
import { loadCarbonExchangeList } from "@/api/carbonAssetApi";

export default {
  name: "carbonEdit",
  props: {
    dialogFormVisible: false,
    title: "",
    row: {},
    isEdit: false,
    selData: {
      type: Object, // 明确指定类型
      default: () => ({}) // 对象/数组的默认值必须用函数返回，避免引用类型共享
    }
  },
  data() {
    return {
      states: null,
      form: {
        industryCodeName: "",
        carbonMethodologyName: "",
        issuingCertificates: "",
        issuingCertificatesFileName: "",
        certifiedAgency: "",
        certifiedAgencyName:"",
        projectName: "",
        assetsStatus: "0130000004",
        issuingDate: null, //签发日期
        total: null,
        buyCertificate: null,
        buyCertificateFileName: "",
        carbonExchangeId: null,
        buyUnitPrice: null,
        carbonProjectId: null,
        buyTotalPrice: null,
        buyDate: null,
        carbonExchangeName:"" //交易所名称
        // certifiedAgency: null //核证机构
      },
      localForm:JSON.parse(localStorage.getItem("carbonEdit")),
      pageSize: 10,
      current: 1,
      total: null,
      creditCreditialUrl: "",
      pageCount: 0,
      havePickProject: false, //是否禁用
      exchangeList: [], //交易所列表
      issueFileList: [],
      tranFileList: [],
      buyFileList: [],
      issueUrl: "",
      tranUrl: "",
      show: false,
      upLoadParam: {
        url: "",
        token: ""
      },
    }
  },
  methods: {
    handleError1(){
      console.warn("handleError1");
    },
    handleError2(){
      console.warn("handleError2");
    },
    clickClose() {
      this.$emit("changeVisible", false);
      this.show = false;
    },
    handleIssueChange(file, fileList) {
      // console.log("this.upLoadParam",this.upLoadParam);
      // 转换操作可以不放到这个函数里面，
      if (fileList && fileList.length >= 2) {
        fileList.shift();
      }
      // 因为这个函数会被多次触发，上传时触发，上传成功也触发
      let reader = new FileReader();
      this.issueFileList = fileList;
      reader.readAsDataURL(file.raw); // 这里也可以直接写参数event.raw
      // 转换成功后的操作，reader.result即为转换后的DataURL ,
      // 它不需要自己定义，你可以console.log(reader.result)看一下
      reader.onload = () => {
        this.issueUrl = reader.result;
      };
    },
    handleTranChange(file, fileList) {
      if (fileList && fileList.length >= 2) {
        fileList.shift();
      }

      // 转换操作可以不放到这个函数里面，
      // 因为这个函数会被多次触发，上传时触发，上传成功也触发
      let reader = new FileReader();
      this.tranFileList = fileList;
      reader.readAsDataURL(file.raw); // 这里也可以直接写参数event.raw
      // 转换成功后的操作，reader.result即为转换后的DataURL ,
      // 它不需要自己定义，你可以console.log(reader.result)看一下
      reader.onload = () => {
        this.tranUrl = reader.result;
      };
    },
    clickSave() {
      // if (this.issueFileList.length != 0) {
      //   this.form.url.push({
      //     name: this.issueFileList[0].name,
      //     url: this.issueUrl,
      //   });
      // }
      // if (this.tranFileList.length != 0) {
      //   this.form.url.push({
      //     name: this.tranFileList[0].name,
      //     url: this.issueUrl,
      //   });
      // }
        localStorage.setItem("carbonEdit", JSON.stringify(this.form));
        console.log("修改之后本地的数据为：",JSON.parse(localStorage.getItem("carbonEdit")));
        this.$message.success("保存成功");
        // this.$message.success("保存成功");
      },
      // this.$emit("changeVisible", false);
      // this.show = false;
    submit() {
        this.submitCredit();
    },
    creditHandleFile(file) {
      console.log(file);
      if (file.response) {
        openUrlInNewWindow(file.response.msg);
      }
      if (file.url) {
        openUrlInNewWindow(file.url);
      }
    },
    submitCredit() {
      for (let i in this.form) {
        if (this.form[i] === "--") {
          this.form[i] = "";
        }
      }
      if (!this.isEdit) {
        if (this.form.projectName && this.form.total && this.issueFileList[0]) {
          this.form.assetsStatus = "0130000004";
          if(this.issueFileList.length>0){
            this.$refs.upload1.submit();
          }
          if (this.tranFileList.length > 0) {
            this.$refs.upload2.submit();
          }
          changeCredit(this.form)
              .then(res => {
                console.log("返回的数据：", res);
                this.$message.success("修改成功");
                this.$emit("changeVisible", false);
                this.show = false;

                //未在父组件中找到submit,但可能有用
                // this.$emit("submit", true);
              })
              .catch(err => {
                this.$message.error(err);
              });
        }
        else {
          this.$message.warning("必填项不能为空");
        }
      }
      else {
        var data = JSON.parse(JSON.stringify(this.form));
        for (var i in data) {
          if (data[i] === "--") {
            data[i] = "";
          }
        }
        changeCredit(data)
          .then(res => {
            this.$message.success("修改成功");
            this.$emit("changeVisible", false);
            this.show = false;
            this.$emit("submit", true);
          })
          .catch(err => {
            this.$message.error(err);
          });
      }
    },
    creditSuccess1(res, file, fileList) {
      // console.log("1111111111111111111111111111111");
      this.form.issuingCertificates = res.msg;
      console.log("issuingCertificatesURL",res.msg);
      this.form.issuingCertificatesFileName = file.name;
      this.creditCreditialUrl = res.msg;
      // this.$message.success("上传成功");
    },
    creditSuccess2(res, file, fileList) {
      this.form.buyCertificate = res.msg;
      this.form.buyCertificateFileName = file.name;
      // addCarbonCredit(this.form)
      //   .then(res => {
      //     this.$message.success("提交成功");
      //     this.$emit("changeVisible", false);
      //     this.show = false;
      //     this.$emit("submit", true);
      //   })
      //   .catch(err => {
      //     this.$message.error(err);
      //   });
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.getProjectList(1);
    },
  },
  watch: {
    dialogFormVisible() {
      this.show = this.dialogFormVisible;
    },
    selData: {
      immediate: true,
      handler(newVal){
        if (newVal && Object.keys(newVal).length) {
          // 这里不是formData，现在正在寻找中，这个就是form
          this.form = { ...newVal };
          // console.log("===EDIT-FORM===",this.form);
        }
      }
    },
    row() {
      this.form = this.row;
      if (this.form.issuingCertificates) {
        this.issueFileList = [];
        this.issueFileList.push({
          name: this.form.issuingCertificatesFileName,
          url: this.form.issuingCertificates
        });
      }
      if (this.form.buyCertificate) {
        this.tranFileList = [];
        this.tranFileList.push({
          name: this.form.buyCertificateFileName,
          url: this.form.buyCertificate
        });
      }
    }
  },
  mounted() {
    console.log("正在挂载");
    // this.formatCertification(getCertificationInstitutionDict(this.$store));
    this.upLoadParam = getFeiShuUploadProjectParams();
    console.log("this.upLoadParam", this.upLoadParam);
    let localForm = localStorage.getItem("carbonEdit");
    console.log("本地存储的form3333333：", localForm);
    this.issueInstitution = getIssueInstitution(this.$store);
    loadCarbonExchangeList({asc: true})
      .then(res => {
        console.log("交易所数组：", res.data.records);
        this.exchangeList = res.data.records;
      })
      .catch(err => {
        this.$message.error("获取交易所列表失败");
      });
    if (localForm) {
      this.form = JSON.parse(localForm);
    }
  }
}
</script>
<style lang="scss" scoped>
.label {
  font-weight: 700;
  width: 120px;
  display: inline-block;
}
</style>
