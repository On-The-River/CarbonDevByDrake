<template>
  <div>
    <el-dialog
      :title="title"
      :visible.sync="show"
      :before-close="clickClose"
      width="720px"
    >
      <!-- 碳信用表单（inline 布局） -->
      <el-form v-if="isCredit" :model="form" inline="true">
        <!-- 项目名称 -->
        <el-form-item>
          <!-- <span class="required-text">*</span> -->
          <span class="label">
            项目名称<span style="color: red;">*</span>
          </span>
          <el-input
            v-model="form.projectName"
            disabled
            size="medium"
            style="width: 420px"
          />
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 选择项目按钮 -->
    <el-form-item>
      <el-button
        @click="pickProject"
        style="width: 100px; text-align: center"
        type="primary"
      >
        选择项目
      </el-button>
    </el-form-item>

    <!-- 采用方法学 -->
    <el-form-item>
      <span class="label">采用方法学</span>
      <el-input
        v-model="form.carbonMethodologyName"
        size="medium"
        style="width: 180px"
        disabled
      />
    </el-form-item>

    <!-- 类型（左侧间距 40px） -->
    <el-form-item style="margin-left: 40px">
      <span class="label">类型</span>
      <el-input
        v-model="form.projectScopeType"
        size="medium"
        style="width: 180px"
        disabled
      />
    </el-form-item>

    <!-- 产生量(CO2e) -->
    <el-form-item>
      <span class="label">
        产生量(CO2e)<span style="color: red;">*</span>
      </span>
      <el-input
        v-model="form.total"
        autocomplete="off"
        size="medium"
        style="width: 180px"
      />
    </el-form-item>

    <!-- 交易单价(￥)（左侧间距 40px） -->
    <el-form-item style="margin-left: 40px">
      <span class="label">交易单价(￥)</span>
      <el-input
        v-model="form.buyUnitPrice"
        autocomplete="off"
        size="medium"
        style="width: 180px"
      />
    </el-form-item>

    <!-- 交易总价(￥) -->
    <el-form-item>
      <span class="label">交易总价(￥)</span>
      <el-input
        v-model="form.buyTotalPrice"
        autocomplete="off"
        size="medium"
        style="width: 180px"
      />
    </el-form-item>

    <!-- 验证码/批次（左侧间距 40px） -->
    <el-form-item style="margin-left: 40px">
      <span class="label">验证码/批次</span>
      <el-input
        v-model="form.projectVerifyCodeName"
        style="width: 180px"
        size="medium"
        disabled
      />
    </el-form-item>
    <!-- 签发日期 -->
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

    <!-- 交易日期（左侧间距 40px） -->
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

    <!-- 交易所（下拉选择） -->
    <el-form-item>
      <span class="label">交易所</span>
      <el-select
        v-model="form.carbonExchangeId"
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

    <!-- 存证文件（文件上传） -->
    <el-form-item>
      <span class="label" style="width: 205px">
        存证文件<span style="color: red;">*</span>
      </span>
      <el-upload
        class="upload-demo"
        ref="upload"
        :action="uploadParam.url"
        :file-list="issueFileList"
        :limit="2"
        :auto-upload="true"
        :on-success="creditSuccess1"
        style="width: 180px"
        :headers="{ token: uploadParam.token }"
        :on-change="handleIssueChange"
        :on-preview="creditHandleFile"
      >
        <el-button type="primary">上传</el-button>
      </el-upload>
    </el-form-item>
    <!-- 交易凭证上传（左侧间距 40px） -->
    <el-form-item style="margin-left: 40px">
      <span class="label">交易凭证</span>
      <el-upload
        class="upload-demo"
        ref="upload2"
        :action="uploadParam.url"
        :file-list="tradeFileList"
        :auto-upload="false"
        :limit="2"
        style="width: 180px"
        :on-success="creditSuccess2"
        :headers="{ token: uploadParam.token }"
        :on-change="handleTradeChange"
        :on-preview="creditHandleFile"
      >
        <el-button type="primary">上传</el-button>
      </el-upload>
    </el-form-item>

    <!-- 碳配票表单（inline 布局，条件渲染） -->
    <el-form v-if="isCredit" :model="form" inline="true">
      <!-- 名称（一般持有机构） -->
      <el-form-item>
        <span class="label" style="width: 100px">
          名称（一般持有机构）<span style="color: red;">*</span>
        </span>
        <el-autocomplete
          v-model="quoteForm.agencyName"
          size="medium"
          style="width: 430px"
          :fetch-suggestions="querySearchAgencyName"
          placeholder="请输入内容"
          @select="handleSelect"
        />
      </el-form-item>

      <!-- 持有总量(tCO2e) -->
      <el-form-item>
        <span class="label">
          持有总量(tCO2e)<span style="color: red;">*</span>
        </span>
        <el-input
          v-model="quoteForm.total"
          size="medium"
          style="width: 180px"
          type="number"
          @input="setFormValidation"
        />
      </el-form-item>

      <!-- 购入单价(￥)（左侧间距 40px） -->
      <el-form-item style="margin-left: 40px">
        <span class="label">购入单价(￥)</span>
        <el-input
          v-model="quoteForm.buyUnitPrice"
          size="medium"
          style="width: 180px"
        />
      </el-form-item>
      <!-- 冻结数量(tCO2e)（左侧间距 40px） -->
      <el-form-item style="margin-left: 40px">
        <span class="label">
          冻结数量(tCO2e)<span style="color: red;">*</span>
        </span>
        <el-input
          v-model="quoteForm.frozenAmount"
          autocomplete="off"
          size="medium"
          style="width: 180px"
        />
      </el-form-item>

      <!-- 资产估值(￥) -->
      <el-form-item>
        <span class="label">资产估值(￥)</span>
        <el-input
          v-model="quoteForm.valuation"
          autocomplete="off"
          size="medium"
          style="width: 180px"
        />
      </el-form-item>

      <!-- 资产状态（下拉选择） -->
      <el-form-item label="资产状态">
        <el-select
          v-model="quoteForm.assetsStatus"
          placeholder="请选择"
          style="width: 180px"
          size="medium"
        >
          <el-option
            v-for="item in optionsStandard"
            :key="item.value"
            :label="item.name"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <!-- 有效期（日期选择，左侧间距 40px） -->
      <el-form-item style="margin-left: 40px">
        <span class="label">有效期</span>
        <el-date-picker
          type="date"
          v-model="quoteForm.expiryDate"
          autocomplete="off"
          size="medium"
          style="width: 180px"
        />
      </el-form-item>

      <!-- 签发日期（日期选择） -->
      <el-form-item>
        <span class="label"> 签发日期<span style="color: red;">*</span> </span>
        <el-date-picker
          type="date"
          v-model="quoteForm.issuingDate"
          autocomplete="off"
          size="medium"
          style="width: 180px"
        />
      </el-form-item>
      <!-- 签发机构（左侧间距 40px） -->
      <el-form-item style="margin-left: 40px">
        <span class="label"> 签发机构<span style="color: red;">*</span> </span>
        <el-select
          v-model="quoteForm.issuingAgency"
          placeholder="请选择"
          style="width: 180px"
          size="medium"
        >
          <el-option
            v-for="item in issueInstitution"
            :key="item.value"
            :label="item.name"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <br />

      <!-- 存证凭证上传（左侧间距 40px） -->
      <el-form-item style="margin-left: 40px">
        <span class="label"> 存证凭证<span style="color: red;">*</span> </span>
        <el-upload
          class="upload-demo"
          ref="upload3"
          :action="uploadParam.url"
          :file-list="buyFileList"
          :auto-upload="true"
          :limit="2"
          :on-success="quoteSuccess"
          style="width: 180px"
          :headers="{ token: uploadParam.token }"
          :on-change="handleBuyChange"
          :on-preview="creditHandleFile"
        >
          <el-button type="primary">上传</el-button>
        </el-upload>
      </el-form-item>

      <!-- 项目描述（多行输入） -->
      <el-form-item label="项目描述">
        <el-input
          v-model="quoteForm.valuation"
          autocomplete="off"
          type="textarea"
          size="medium"
          :rows="5"
          style="width: 430px"
        />
      </el-form-item>

      <!-- 弹窗底部按钮 -->
      <div slot="footer" class="dialog-footer">
        <el-button @click="clickClose">取 消</el-button>
        <el-button type="primary" @click="clickSave">保 存</el-button>
        <el-button type="primary" @click="submit" style="margin-right: 25px">
          提交
        </el-button>
      </div>

      <!-- 项目列表弹窗 -->
      <el-dialog
        title="项目列表"
        :visible.sync="dialogTableVisible"
        width="800px"
      >
        <el-input
          v-model="searchProjectKeyword"
          placeholder="输入项目名称"
          clearable
          size="medium"
          style="width: 60%"
          @clear="search"
        />
        <button
          style="margin-left: 10px"
          class="light-green-btn"
          @click="search"
        >
          查询
        </button>

        <!-- 项目列表表格 -->
        <el-table :data="projectList" style="width: 100%" stripe>
          <el-table-column min-width="10%" />
          <el-table-column
            label="序号"
            align="left"
            prop="order"
            min-width="80"
          />
          <el-table-column
            show-overflow-tooltip="true"
            align="left"
            prop="projectName"
            label="名称"
            min-width="180"
          />
          <el-table-column
            align="left"
            prop="projectScope"
            label="领域"
            min-width="120"
          />
          <!-- 认证标准列 -->
          <el-table-column
            align="left"
            prop="certifiedStandard"
            label="认证标准"
            min-width="80"
          />

          <!-- 操作列（选择项目） -->
          <el-table-column label="操作" min-width="150" align="center">
            <template slot-scope="scope">
              <a class="list-blue-text" @click="pickProjectDone(scope.row)">
                选择
              </a>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页组件 -->
        <div style="margin-top: 30px; margin-bottom: 10px" class="pageBox">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="current"
            :page-sizes="pageSize"
            :page-count="pageCount"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            style="margin: auto"
          />
        </div>
      </el-dialog>
    </el-form>
  </div>
</template>

<script>
import {
  CarbonMetaRegistryPageList,
  loadMethodList,
  addCarbonCredit,
  addCarbonQuota,
  changeCredit,
  changeQuota,
  getCarbonProjectPageList
} from "@/api/carbonAssetApi";
import { openUrlInNewWindow } from "@/libs/OpenHelper";
import {
  getCertificationInstitutionDict,
  getIssuingInstitution,
  getCarbonEnterprise
} from "@/config/dictHelper";
import { getFileUrlAndProjectParameters } from "@/api/format";
import { loadCarbonExchangeList } from "@/api/carbonAssetApi";

export default {
  name: "carbonUpload",
  props: {
    dialogFormVisible: false,
    title: "",
    isCredit: true,
    row: {},
    isEdit: false,
    sellData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      status: null,
      quotaSuccess: true,
      searchProjecKeyword: "",
      form: {
        industryCodeName: "",
        carbonMethodologyName: "",
        issuingCertificateFileNames: "",
        certifiedAgency: "",
        assetStatus: null,
        projectName: "",
        assetStatus: "0100000004",
        issuedDate: "", // 签发日期
        total: null,
        buyCertificate: null,
        buyCertificateFileNames: "",
        carbonExchangeId: null,
        buyUnitPrice: null,
        carbonProjectId: null,
        buyTotalPrice: null,
        issuingDate: null,
        buyDate: null,
        projectVerifyCodeName: null // 验证码/批次
      },
      issueInstitution: [],
      quotaForm: {
        agencyName: "",
        agencyCode: "",
        total: null,
        buyUnitPrice: null,
        buyTotalPrice: null,
        carbonExchangeId: null,
        availableAmount: null,
        lockedAmount: null,
        frozenAmount: null,
        valuation: null,
        assetStatus: "0160000004",
        expiryDate: null,
        buyCertificateFileNames: "",
        issuingDate: null,
        issuingAgency: null,
        buyCertificate: null
      },
      dialogTableVisible: false,
      pageSize: 10,
      optionsStandard: [], // 碳标准
      methodList: [],
      current: 1,
      total: 0,
      creditCreditailUrl: "",
      creditTradeCreditailUrl: "",
      quotaCreditailUrl: "",
      isCredit: true,
      pageCount: 0,
      havePickProject: false, // 是否禁用
      fileList: [],
      exchangeList: [], // 交易所列表
      issueFileList: [],
      tradeFileList: [],
      buyFileList: [],
      carbonEnterpriseList: [], // 全国碳市场控排企业
      exchangeOptions: [],
      issueUrl: "",
      show: false,
      projectList: [],
      uploadParam: {
        url: null,
        token: null
      },
      buyUrl: ""
    };
  },
  methods: {
    // 计算估值（示例：估值 = 事件值 * 50）
    setFormValidation(event) {
      this.quotaForm.valuation = event * 50;
    },

    // 关闭弹窗（触发父组件事件 + 隐藏弹窗）
    clickClose() {
      this.$emit("changeVisible", false);
      this.show = false;
    },

    // 转换下拉选项数据（示例：处理 value/label 格式）
    setSelData(data) {
      let list = data;
      let arr = [];
      for (let i = 0; i < list.length; i++) {
        let obj = {};
        if (list[i].value === "00280000007") {
          obj.label = list[i].value;
          obj.value = list[i].name;
        }
        arr.push(obj);
      }
      return arr;
    },

    // 加载企业列表（模拟静态数据）
    loadAll() {
      // return this.carbonEnterpriseList;
      return [
        {
          value: "北京我爱我家地产有限公司",
          label: "北京我爱我家地产有限公司"
        },
        {
          value: "上海我爱我家地产有限公司",
          label: "上海我爱我家地产有限公司"
        },
        {
          value: "河南我爱我家地产有限公司",
          label: "河南我爱我家地产有限公司"
        }
      ];
    },

    // 搜索联想方法（模拟异步搜索）
    querySearchAgencyName(queryString, cb) {
      var restaurants = this.states;
      var results = queryString
        ? restaurants.filter(this.createStateFilter(queryString))
        : restaurants;

      clearTimeout(this.timeout);
      this.timeout = setTimeout(() => {
        cb(results);
      }, 1000 * Math.random());
    },

    // 搜索过滤函数（不区分大小写匹配）
    createStateFilter(queryString) {
      return state => {
        return (
          state.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
        );
      };
    },

    // 签发文件上传变化（模拟文件读取）
    handleIssueChange(file, fileList) {
      // 限制最多传 2 个文件
      if (fileList && fileList.length >= 2) {
        fileList.shift();
      }

      // 模拟文件读取（实际需结合 FileReader 处理）
      let reader = new FileReader();
      this.issueFileList = fileList;
      reader.readAsDataURL(file.raw); // 可替换为 event.raw
      // 读取完成后赋值（示例）
      reader.onload = () => {
        this.issueUrl = reader.result;
      };
    },

    // 交易文件上传变化（带结果校验）- 覆盖原重复方法
    handleTradeChange(file, fileList) {
      this.quotaSuccess = false;
      if (fileList && fileList.length >= 2) {
        fileList.shift();
      }

      // 模拟文件读取（实际需结合 FileReader 处理）
      let reader = new FileReader();
      this.tradeFileList = fileList;
      reader.readAsDataURL(file.raw);
      reader.onload = () => {
        this.tradeUrl = reader.result;
      };
    },

    // 买入文件上传变化（带结果校验）
    handleBuyChange(file, fileList) {
      this.quotaSuccess = false;
      if (fileList && fileList.length >= 2) {
        fileList.shift();
      }

      // 模拟文件读取（实际需结合 FileReader 处理）
      let reader = new FileReader();
      this.buyFileList = fileList;
      reader.readAsDataURL(file.raw);
      reader.onload = () => {
        this.buyUrl = reader.result;
      };
    },

    // 保存表单到 sessionStorage
    clickSave() {
      // 签发文件处理（示例）
      if (this.issueFileList.length !== 0) {
        // 初始化 form.url 避免报错
        if (!this.form.url) this.form.url = [];
        this.form.url.push({
          name: this.issueFileList[0].name,
          url: this.issueUrl
        });
      }
      // 交易文件处理（示例）
      if (this.tradeFileList.length !== 0) {
        if (!this.form.url) this.form.url = [];
        this.form.url.push({
          name: this.tradeFileList[0].name,
          url: this.tradeUrl
        });
      }

      // 根据表单类型保存到 sessionStorage
      if (this.isCredit) {
        sessionStorage.setItem("carbonUpload", JSON.stringify(this.form));
        this.$message.success("保存成功");
      } else {
        sessionStorage.setItem(
          "carbonQuotaUpload",
          JSON.stringify(this.quotaForm)
        );
        this.$message.success("保存成功");
      }

      // 关闭弹窗
      this.$emit("changeVisible", false);
      this.show = false;
    },

    // 选择项目后赋值表单
    pickProjectDone(row) {
      this.form.projectName = row.projectName;
      this.form.carbonMethodologyName = row.methodologyName;
      this.form.projectScopeType = row.projectScope;
      this.form.carbonProjectId = row.id;
      this.form.issuingDate = row.issueTime;
      this.form.certifiedAgency = row.projectVerifierCode;
      this.form.certifiedAgencyName = row.projectVerifier;
      this.dialogTableVisible = false;
      this.havePickProject = true;
      this.form.projectVerifierCodeName = row.projectVerifierCodeName;
    },

    // 分页当前页变化
    handleCurrentChange(val) {
      this.current = val;
      this.getProjectList(val);
    },

    // 提交主方法（区分信用/配额提交）
    submit() {
      if (this.isCredit) {
        this.submitCredit();
      } else {
        this.submitQuota();
      }
    },

    // 搜索方法（获取项目列表）
    search() {
      const data = {
        assetStatus: "0100000004",
        current: this.current,
        size: this.pageSize,
        projectName: this.searchProjectKeyword,
        projectStatusCode: "0100000012"
      };

      getCarbonMetaRegistryPageList(data).then(res => {
        this.projectList = res.data.records || [];
        this.total = Number(res.data.total || 0);
        this.current = Number(res.data.current || 1);
        this.pageCount = Math.ceil(this.total / this.pageSize);
        this.projectList.map((v, i) => {
          v.order = i + 1;
          for (var key in v) {
            if (v[key] === "-") {
              v[key] = "--";
            }
          }
        });
      });
    },

    // 文件预览方法（支持响应式 URL 或直接 URL）
    creditHandleFile(file) {
      console.log(file);
      if (file.response) {
        openUrlInNewWindow(file.response.msg);
      }
      if (file.url) {
        openUrlInNewWindow(file.url);
      }
    },

    // 选择机构时赋值编码
    handleSelect(item) {
      this.quoteForm.agencyCode = item.label;
      this.quoteForm.agencyCode = item.label;
    }
  },

  // 提交信用表单（新增/编辑逻辑）
  submitCredit() {
    console.log("submit 执行了");
    // 清理空值
    for (let i in this.form) {
      if (this.form[i] === "--") {
        this.form[i] = null;
      }
    }

    // 编辑逻辑
    if (this.isEdit) {
      if (this.form.projectName && this.form.total && this.issueFileList[0]) {
        this.form.assetStatus = "0100000004";
        if (this.tradeFileList.length > 0 && this.$refs.tradeUpload) {
          this.$refs.tradeUpload.submit();
        } else {
          addCarbonCredit(this.form)
            .then(res => {
              this.$message.success("提交成功");
              this.$emit("changeVisible", false);
              this.show = false;
              this.$emit("submit", true);
            })
            .catch(err => {
              this.$message.error(err.message || "提交失败");
            });
        }
      } else {
        this.$message.warning("必填项不能为空");
      }
    }
    // 新增逻辑（原逻辑可能颠倒，保留原代码）
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
          this.$message.error(err.message || "修改失败");
        });
    }
  },

  // 信用文件上传成功（签发凭证）
  creditSuccess(res, file, fileList) {
    this.form.issuingCertificate = res.msg;
    this.form.issuingCertificateFileName = file.name;
    this.creditCreditailUrl = res.msg;
    this.$message.success("上传成功");
  },

  // 信用文件上传成功（交易凭证，提交信用表单）
  creditSuccess2(res, file, fileList) {
    this.form.buyCertificate = res.msg;
    this.form.buyCertificateFileName = file.name;
    addCarbonCredit(this.form)
      .then(res => {
        this.$message.success("提交成功");
        this.$emit("changeVisible", false);
        this.show = false;
        this.$emit("submit", true);
      })
      .catch(err => {
        this.$message.error(err.message || "提交失败");
      });
  },

  // 配额文件上传成功（交易凭证）
  quotaSuccess(res, file, fileList) {
    this.quotaForm.buyCertificate = res.msg;
    this.quotaForm.buyCertificateFileName = file.name;
    this.$message.success("上传成功");
  },

  // 提交配额项目（校验 + 接口调用）
  submitQuota() {
    // 校验文件是否上传
    if (!this.quotaForm.buyCertificate) {
      this.$message.warning("请先上传文件，或等待文件上传成功再提交");
      return;
    }

    // 编辑模式校验
    if (this.isEdit) {
      if (
        this.quotaForm.agencyName &&
        this.quotaForm.total &&
        this.quotaForm.availableAmount !== undefined &&
        this.quotaForm.lockedAmount !== undefined &&
        this.quotaForm.frozenAmount !== undefined &&
        this.quotaForm.issuingDate &&
        this.quotaForm.issuingAgency &&
        this.buyFileList[0]
      ) {
        // 总量校验
        if (
          Number(this.quotaForm.total) !==
          Number(this.quotaForm.availableAmount) +
            Number(this.quotaForm.lockedAmount) +
            Number(this.quotaForm.frozenAmount)
        ) {
          return this.$message.warning(
            "可用数量、锁定数量、冻结数量总和必须等于存在总量"
          );
        }

        this.quotaForm.assetStatus = "0100000004";
        addCarbonQuota(this.quotaForm)
          .then(res => {
            if (this.buyFileList.length > 0) {
              this.$message.success("提交成功");
              this.$emit("changeVisible", false);
              this.show = false;
              this.$emit("submit", true);
            }
          })
          .catch(err => {
            this.$message.error(err.message || "提交失败");
          });
      } else {
        this.$message.warning("必填项不能为空");
        return;
      }
    }
    // 新增模式处理
    else {
      var data = JSON.parse(JSON.stringify(this.quotaForm));
      for (var i in data) {
        if (data[i] === "--") {
          data[i] = "";
        }
      }
      // 修复 changeQuota 方法定义（原嵌套错误）
      this.changeQuota(data);
    }
  },

  // 修复：提取独立的 changeQuota 方法（原嵌套在 submitQuota 内）
  changeQuota(data) {
    return new Promise((resolve, reject) => {
      // 模拟接口调用：实际需替换为真实接口
      // addCarbonQuota(data).then(resolve).catch(reject)
      setTimeout(() => {
        if (this.buyFileList.length > 0) {
          resolve({ code: 200, msg: "提交成功" });
        } else {
          reject(new Error("文件未上传"));
        }
      }, 500);
    })
      .then(res => {
        if (this.buyFileList.length > 0) {
          this.$message.success("提交成功");
          this.$emit("changeVisible", false);
          this.show = false;
          this.$emit("submit", true);
        }
      })
      .catch(err => {
        this.$message.error(err.message || "提交失败");
      });
  },

  // 获取项目列表（分页）
  getProjectList(current) {
    const data = {
      current: current || this.current,
      size: this.pageSize,
      async: true,
      projectName: this.searchProjectKeyword,
      projectStatusCode: "0100000012"
    };

    // 模拟接口调用：实际替换为 getCarbonMetaRegistryPageList 真实逻辑
    // getCarbonMetaRegistryPageList(data).then((res) => {
    setTimeout(() => {
      const mockRes = {
        data: {
          records: [], // 模拟空数据，实际需接口返回
          total: 0,
          current: 1
        }
      };
      this.projectList = mockRes.data.records;
      this.total = Number(mockRes.data.total);
      this.current = Number(mockRes.data.current);
      this.pageCount = Math.ceil(this.total / this.pageSize);

      // 空值转换
      this.projectList.map((v, i) => {
        v.order = i + 1;
        for (var key in v) {
          if (v[key] === "") {
            v[key] = "--";
          }
        }
      });
    }, 500);
    // }).catch(err => {
    //   console.error('获取项目列表失败:', err)
    // });// });
  },

  // 每页条数变化处理
  handleSizeChange(val) {
    this.pageSize = val;
    this.getProjectList();
  },

  // 打开项目选择弹窗（并加载列表）
  pickProject() {
    this.dialogTableVisible = true;
    this.getProjectList();
  },
  watch: {
    // 弹窗显隐联动
    dialogFormVisible(newVal) {
      this.show = newVal;
    },
    // 编辑数据回显（文件列表恢复）
    row(newRow) {
      this.form = { ...newRow };
      // 恢复签发文件
      if (this.form.issuingCertificate) {
        this.issueFileList = [
          {
            name: this.form.issuingCertificateFileName || "",
            url: this.form.issuingCertificate
          }
        ];
      }
      // 恢复交易文件
      if (this.form.buyCertificate) {
        this.tradeFileList = [
          {
            name: this.form.buyCertificateFileName || "",
            url: this.form.buyCertificate
          }
        ];
      }
    }
  },
  mounted() {
    this.formatCertification(getCertificationInstitutionDict(this.$store));
    this.uploadParam = getSelfUploadProjectParams();
    this.carbonEnterpriseList = this.setSelData(
      getCarbonEnterprise(this.$store)
    );
    this.optionsStandard = getCertificationInstitutionList(this.$store);
    let form = sessionStorage.getItem("carbonInstitutionForm");
    let quotaForm = sessionStorage.getItem("carbonQuotaUpload");
    loadQuotaChangeList({
      size: true
    }).then(res => {
      this.exchangeList = res.data.records;
    });
    this.carbonEnterpriseList();
    this.state = this.loadH1();
    this.institution = getInstitutionInstitution(this.$store);
    if (form) {
      this.form = JSON.parse(form);
    }
    if (quotaForm) {
      this.quotaForm = JSON.parse(quotaForm);
    }
  }
};
</script>
<style lang="scss" scoped>
.required-text {
  color: red;
  position: absolute;
  left: -40px;
  top: 20px;
}

.label {
  font-weight: 700;
  width: 100px;
  display: inline-block;
  display: inline-block;
}
</style>
