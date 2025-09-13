<template>
  <div class="root">
    <div class="divBox">
      <div class="content-container">
        <div class="cardBody">
          <span class="header">创建项目</span>
          <!-- <span class="createTips" style="margin-left: 10px">{{ this.form.creatorId }} {{ this.form.createdTime }} 创建</span> -->
          <span class="updateTips">{{ this.form.updatedId }} {{ this.form.updatedTime }} 更新</span>
          <el-divider></el-divider>
          <div class="outer20 flex-col">
            <span class="info5">1&nbsp;填写项目基本信息</span>
          </div>
          <div class="outer21 flex-col" @click="toChangePage">
            <span class="word9">2&nbsp;上传项目业主资料</span>
          </div>
          <div class="changeline"></div>
          <button
            style="
              margin-top: 0px;
              margin-bottom: 20px;
              width: 119px;
              height: 36px;
            "
            class="normal-white-btn"
            @click="toMethodManager"
          >
            选择方法学
          </button>
          <el-input
            v-model="form.name"
            size="medium"
            placeholder="左侧按钮选择方法学"
            style="width: 350px; margin-left: 16px"
            disabled
          ></el-input>
          <br />
          <br />
          <img src="@/assets/icon/icon_plant.png" alt="" class="icon" />
          <span class="asset-little-title">项目信息</span>
          <br />
          <br />
          <div class="right">
            <el-row>
              <el-col :span="12">
                <div style="margin-right: 15px; padding: 0 0 0 0" class="selectbox-root">
                  <span class="selectbox-hint" style="width: 80px">项目<span style="color: red">*</span></span>
                  <div class="selectbox-deliver"></div>
                  <el-input
                    size="medium"
                    class="selectbox-input"
                    v-model="form.projectName"
                    placeholder="输入项目名称"
                    clearable
                  />
                </div>
              </el-col>
              <el-col :span="12">
                <div style="padding: 0 0 0 0" class="selectbox-root">
                  <span class="selectbox-hint" style="width: 120px">业主名称<span style="color: red">*</span></span>
                  <div class="selectbox-deliver"></div>
                  <el-autocomplete
                    v-model="form.ownerName"
                    size="medium"
                    style="width: 80%"
                    :fetch-suggestions="querySearchAsync"
                    placeholder="请输入内容"
                    @select="handleSelect"
                  ></el-autocomplete>
                </div>
              </el-col>
            </el-row>
            <div style="clear: both; height: 40px"></div>
            <el-row>
              <el-col :span="8">
                <div style="padding: 0 0 0 0" class="selectbox-root">
                  <span class="selectbox-hint" style="width: 70px">国家<span style="color: red">*</span></span>
                  <div class="selectbox-deliver"></div>
                  <el-cascader
                    placeholder="全部"
                    class="selectbox-input"
                    v-model="form.country"
                    :options="countryList"
                    @change="changeCity"
                    clearable
                    size="medium"
                  ></el-cascader>
                </div>
              </el-col>
              <el-col :span="8">
                <div style="margin-left: 17px; padding: 0 0 0 0" class="selectbox-root">
                  <span class="selectbox-hint" style="width: 80px">省份<span style="color: red">*</span></span>
                  <div class="selectbox-deliver"></div>
                  <el-cascader
                    style=""
                    placeholder="请选择"
                    class="selectbox-input"
                    v-model="form.province"
                    :options="provinceList"
                    @change="changeProvince"
                    clearable
                    size="medium"
                    @clear="clearCity"
                  ></el-cascader>
                </div>
              </el-col>
              <el-col :span="8">
                <div style="margin-left: 17px; padding: 0 0 0 0" class="selectbox-root">
                  <span class="selectbox-hint" style="width: 130px">市/县/郡<span style="color: red">*</span></span>
                  <div class="selectbox-deliver"></div>
                  <el-cascader
                    style="width: 85%"
                    placeholder="请选择"
                    class="selectbox-input"
                    v-model="form.city"
                    :options="nowCityList"
                    @change="changeCity"
                    clearable
                    size="medium"
                  ></el-cascader>
                </div>
              </el-col>
            </el-row>
            <div style="clear: both; height: 40px"></div>
            <el-row>
              <el-col :span="12">
                <div style="margin-right: 15px" class="selectbox-root">
                  <span class="selectbox-hint" style="width: 100px">项目地点<span style="color: red">*</span></span>
                  <div class="selectbox-deliver"></div>
                  <el-input
                    size="medium"
                    style="width: 85%"
                    class="selectbox-input"
                    v-model="form.address"
                    placeholder="请输入项目地点"
                    clearable
                  />
                </div>
              </el-col>
              <el-col :span="12">
                <div class="selectbox-root">
                  <span class="selectbox-hint" style="width: 200px">资产开发机构名称<span style="color: red">*</span></span>
                  <div class="selectbox-deliver"></div>
                  <el-autocomplete
                    v-model="form.assetsDevelopAgency"
                    size="medium"
                    style="width: 85%"
                    :fetch-suggestions="querySearchAsync"
                    placeholder="请输入内容"
                    @select="handleSelect"
></el-autocomplete>
</div>
</el-col>
<!--  -->
<div style="clear: both; height: 40px"></div>
<el-row>
  <el-col :span="8">
    <div class="selectbox-root">
      <span class="selectbox-hint" style="width: 200px">减排量<span style="color: red">*</span></span>
      <div class="selectbox-deliver"></div>
      <el-input size="medium" class="selectbox-input" v-model="form.estimatedReduction" placeholder="输入减排量" clearable></el-input>
    </div>
  </el-col>


  <el-col :span="7">
    <div class="selectbox-root" style="margin: 0 20px;">
      <span class="selectbox-hint" style="width: 100px">项目领域<span style="color: red">*</span></span>
      <div class="selectbox-deliver"></div>
      <el-select
        size="medium"
        v-model="form.projectScope"
        :value-key="'value'"
        placeholder="请选择项目领域"
        style="flex: 1; height: 36px;"
        class="project-type-select"
        @change = "handleProjectScopeChanged"
      >

        <el-option
          v-for="item in projectAreaOptions"
          :key="item.value"
          :label="item.label"
          :value="item"
        >
        </el-option>
      </el-select>
    </div>
  </el-col>

  <el-col :span="6">
    <div class="selectbox-root" style="flex: 1;">
      <span class="selectbox-hint" style="width: 100px">项目类型</span>
      <div class="selectbox-deliver"></div>
      <el-select
        size="medium"
        v-model="form.projectType"
        :value-key="'value'"
        placeholder="请选择项目类型"
        style="flex: 1; height: 36px;"
        class="project-type-select"
        @change = "handleProjectTypeChanged"
      >

        <el-option
          v-for="item in projectTypeOptions"
          :key="item.value"
          :label="item.label"
          :value="item"
        >
        </el-option>
      </el-select>
    </div>
  </el-col>

  <el-col :span="3">
    <button
      style="width: 96px; float: right; margin-right: 20px"
      class="light-green-btn"
      @click="autoClassify"
    >
      智能预测
    </button>
  </el-col>
</el-row>
<!-- <hr> -->
<div style="clear: both; height: 20px"></div>
<img
  src="@/assets/icon/icon_plant.png"
  alt=""
  class="icon"
  style="position: relative; right: 30px"
/>
<span
  class="asset-little-title"
  style="position: relative; right: 30px"
  >项目介绍</span
>
<!-- <hr> -->
<div style="clear: both; height: 20px"></div>
<el-col :span="24">
  <div
    class="basic-div"
    style="height: 189px; padding: 20px 20px 20px 20px"
  >
    <textarea
      v-model="form.projectIntroduction"
      placeholder="输入项目介绍"
      name=""
      id=""
      cols="30"
      rows="10"
      class="projectInfo"
    ></textarea>
  </div>
</el-col>
</el-row>
<div style="clear: both; height: 20px"></div>
<button
  style="width: 96px; float: right"
  class="light-green-btn"
  @click="toNext"
>
  下一步
</button>
<button
  style="width: 96px; float: right; margin-right: 20px"
  class="light-green-btn"
  @click="onSave"
>
  保存
</button>
<!-- <button style="width: 96px; float: right; margin-right: 20px" :class="isReturn()" v-if="showCancel" @click="onCancel">
  上一步
</button> -->
</div>
</div>
<!-- 选择方法学 -->
<el-dialog
  title="方法学列表"
  :visible.sync="dialogTableVisible"
  width="75%"
>
  <el-row>
    <el-col :span="8">
      <div style="" class="selectbox-root">
        <a class="selectbox-hint" style="width: 95px">核证标准</a>
        <div class="selectbox-deliver"></div>
        <el-cascader
          style="width: 85%"
          size="medium"
          placeholder="全部"
          class="selectbox-input"
          v-model="selectedCertification"
          :options="Certification"
          clearable
          @change="update"
        ></el-cascader>
      </div>
    </el-col>
    <el-col :span="8">
      <div style="margin-left: 16px" class="selectbox-root">
        <a class="selectbox-hint" style="width: 60px">领域</a>
        <div class="selectbox-deliver"></div>
        <el-cascader
          style="width: 80%"
          placeholder="全部"
          size="medium"
          class="selectbox-input"
          v-model="selectedAreaDict"
          :options="projectAreaDict"
          clearable
          @change="update"
        ></el-cascader>
      </div>
    </el-col>
    <el-col :span="8">
      <div style="margin-left: 16px" class="selectbox-root">
        <a class="selectbox-hint" style="width: 90px">行业</a>
        <div class="selectbox-deliver"></div>
        <el-cascader
          style="width: 80%"
          size="medium"
          placeholder="全部"
          class="selectbox-input"
          v-model="selectedIndustry"
          :options="IndustryList"
          clearable
          @change="update"
        ></el-cascader>
      </div>
    </el-col>
  </el-row>
  <div style="width: 400px"></div>
  <el-row style="margin-top: 10px">
    <el-col :span="16">
      <div style="margin-right: 10px" class="selectbox-root">
        <a class="selectbox-hint" style="width: 120px">方法学搜索</a>
        <div class="selectbox-deliver"></div>
        <el-input
          class="selectbox-input"
          @keyup.enter.native="searchEnter"
          v-model="searchKeyword"
          style="width: 80%"
          size="medium"
          placeholder="输入名称、编号、关键词等"
          clearable
        ></el-input>
      </div>
    </el-col>
    <el-col :span="8">
      <button
        class="light-green-btn"
        @click="onClickSearch"
        style="position: relative; top: 3px"
      >
        查询
      </button>
    </el-col>
  </el-row>
  <el-table :data="methodList" style="width: 100%" stripe>
    <el-table-column min-width="10"></el-table-column>
    <el-table-column
      label="编号"
      align="left"
      prop="methodCode"
      min-width="80"
    ></el-table-column>
    <el-table-column
      :show-overflow-tooltip="true"
      align="left"
      prop="name"
      label="名称"
      min-width="230"
    />
    <el-table-column
      align="left"
      prop="industryCodeName"
      label="行业"
      min-width="80"
    />
    <el-table-column
  align="left"
  prop="certificationCriteriaName"
  label="核证标准"
  min-width="80"
/>
<!-- <el-table-column align="left" prop="statusName" label="状态" min-width="60" /> -->
<el-table-column label="操作" min-width="150" align="center">
  <template slot-scope="scope">
    <a class="list-blue-text" @click="pickMethod(scope.row)">选择</a>
  </template>
</el-table-column>

</el-table>
<div style="margin-top: 30px; margin-bottom: 10px" class="pageBox">
  <div style="flex-grow: 1" />
  <a style="margin: auto" class="pageBox-total-num">共{{ total }}条</a>
  <el-pagination
    style="margin: auto"
    background
    @size-change="handleSizeChange"
    @current-change="handleCurrentChange"
    :current-page="methodCurrent"
    :page-size="methodPageSize"
    :page-count="pageCount"
    layout="total, sizes, prev, pager, next, jumper"
    :total="total"
  />
</div>
</el-dialog>
</div>
    </div>
  </div>
</template>

<script>
import { loadMethodListAsUser } from "@/api/carbonAssetApi";
import { openUrlInNewWindow } from "@/libs/OpenHelper";
import {getProjectTypeDict, getProvinceDict} from "@/config/dictHelper";
import { getCountryDict } from "@/config/dictHelper";
import { getCertificationCriteriaDict } from "@/config/dictHelper";
import { getBusinessDict } from "@/config/dictHelper";
import { getProjectAreaDict } from "@/config/dictHelper";
import {
    getMethodologyDict,
    getCityDict
} from "@/config/dictHelper";
import axios from "axios";

export default {
  name: "projectAdd",
  data() {
    return {
      searchKeyword: "",
      state: "",
      projectAreaDict: [], //项目领域字典
      selectedCertification: "", //被选中的标准
      selectedIndustry: "", //被选中的行业
      selectedAreaDict: "", //被选中的领域
      Certification: [], //核证标准字典
      IndustryList: [], //行业字典
      carbonMethodologyId: "",
      provinceList: [], //省份
      cityList: [], //城市
      nowCityList: [], //当前选中省份对应的城市
      dialogTableVisible: false, //是否显示方法学弹框
      methodList: [], //方法学列表
      countryList: [], //国家
      submit: false,
      isCreate: true, //是创建还是修改？
      projectTypeOptions: [],        //项目类型
      projectAreaOptions: [],
      // OwnerNameOptions: [
      //   {
      //     value: "北京我爱我家地产有限公司",
      //     label: "北京我爱我家地产有限公司",
      //   },
      //   {
      //     value: "上海我爱我家地产有限公司",
      //     label: "上海我爱我家地产有限公司",
      //   },
      //   {
      //     value: "河南我爱我家地产有限公司",
      //     label: "河南我爱我家地产有限公司",
      //   },
      // ], //搜索业主名称的提示框
      form: {
        id: "",
        name: "",
        carbonMethodology: "",
        projectName: "",
        ownerName: "",
        country: "",
        province: "",
        city: "",
        address: "",
        assetsDevelopAgency: "",
        projectIntroduction: "",
        projectStatus: "", //已创建
        projectType: "",
        projectScope: "",
        projectScopeCode: "",
        projectTypeCode: "",
        approvalDate: "",
        estimatedReduction: "", //减排量
      },
      fromPath: "",
      list: [],
      next: false,
      showCancel: true,
      pageCount: 0,
      total: 0,
      states: [],
      methodPageSize: 10,
      methodCurrent: 1,
    };
  },
  beforeRouteEnter(to, from, next) {
    next((vm) => {
      vm.fromPath = from.path; //获取上一级路由的路径
    });
  },
  methods: {
    handleProjectScopeChanged(selectedItem) {
      if (selectedItem) {
        this.form.projectScope = selectedItem.label;
        this.form.projectScopeCode = selectedItem.value;
      } else {
        this.form.projectScope = "";
        this.form.projectScopeCode = "";
      }
    },
    handleProjectTypeChanged(selectedItem) {
      if (selectedItem) {
        this.form.projectType = selectedItem.label;
        this.form.projectTypeCode = selectedItem.value;
      } else {
        this.form.projectType = "";
        this.form.projectTypeCode = "";
      }
    },
    autoClassify() {
      if (!this.form.projectName || !this.form.projectIntroduction) {
        this.$message.warning("请填写项目名称和介绍");
        return;
      }

      const data = {
        name: this.form.projectName,
        introduce: this.form.projectIntroduction
      };

      const apiUrl = 'http://localhost:9002/system/python/classifyProjectType';
      const requestPromise = () => {
        return axios.post(apiUrl, data)
          .then(response => response.data);
      };

      const timeoutPromise = new Promise((resolve, reject) => {
        setTimeout(() => {
          reject(new Error('请求超时，请稍后重试'));
        }, 60000); // 60秒超时
      });

      Promise.race([requestPromise(), timeoutPromise])
        .then(res => {
          if (res.code === 200) {
            if (Number(res.data.confidence) > 30.00) {
              this.form.projectType = res.data.type;
              for (let i = 0; i < this.projectTypeOptions.length; i++) {
                var iter = this.projectTypeOptions[i];
                if (iter.label === res.data.type) {
                  this.form.projectTypeCode = iter.value;
                }
              }
              this.$message.success("智能分类成功");
            } else {
              this.$message.warning("智能检测置信度不足，请丰富项目介绍");
            }
          } else {
            this.$message.error(`请求失败: ${res.msg || '未知错误'}`);
          }
        }).catch(err => {
        console.error("分类请求出错:", err);
        this.$message.error(err.message || "智能检测失败");
      });
    },
    formatProjectType(data) {
      data.map((v) => {
        let typeName = {
          value: "",
          label: "",
        };
        if (v.name !== "全部") {
          typeName.value = v.value;
          typeName.label = v.name;
          this.projectTypeOptions.push(typeName);
        }
      });
    },
    formatProjectArea(data) {
      data.map((v) => {
        let typeName = {
          value: "",
          label: "",
        };
        if (v.name !== "全部") {
          typeName.value = v.value;
          typeName.label = v.name;
          this.projectAreaOptions.push(typeName);
        }
      });
    },
    loadAll() {
      return [
        {
          value: "北京我爱我家地产有限公司",
          label: "北京我爱我家地产有限公司",
        },
        {
          value: "上海我爱我家地产有限公司",
          label: "上海我爱我家地产有限公司",
        },
        {
          value: "河南我爱我家地产有限公司",
          label: "河南我爱我家地产有限公司",
        },
      ];
    },
    isReturn() {
      if (this.fromPath == "/carbon/projectCreate/ownerAdd") {
        this.showCancel = false;
        return "normal-white-btn-no";
      } else {
        return "normal-white-btn";
      }
    },
    //跳转到方法学管理
    toMethodManager() {
      this.dialogTableVisible = true;
      this.getMethodList(1);
    },
    handleCurrentChange(val) {
      this.methodCurrent = val;
      this.getMethodList(val);
    },
    handleSizeChange(val) {
      this.methodPageSize = val;
      this.getMethodList(1);
    },
    update() {
      const data = {
        asc: true,
        size: this.methodPageSize,
        searchKey: this.searchKeyword,
        fieldCode: this.selectedAreaDict[0],
        industryCode: this.selectedIndustry[0],
        certificationCriteria: this.selectedCertification[0],
        current: this.methodCurrent,
        status: "0450000002",
      };
      loadMethodListAsUser(data)
        .then((res) => {
          this.methodList = res.data.records;
          this.total = Number(res.data.total);
          this.methodCurrent = Number(res.data.current);
          this.pageCount = Math.ceil(
            parseInt(res.data.total) / this.methodPageSize
          );
          this.methodList.map((v) => {
            for (var i in v) {
              v[i] = v[i] ? v[i] : "---";
              if (v[i] == " ") {
                v[i] = "---";
              }
            }
          });
        })
        .catch((error) => 0);
    },
    querySearchAsync(queryString, cb) {
      var restaurants = this.states;
      var results = queryString
        ? restaurants.filter(this.createStateFilter(queryString))
        : restaurants;

      clearTimeout(this.timeout);
      this.timeout = setTimeout(() => {
        cb(results);
      }, 1000 * Math.random());
    },
    createStateFilter(queryString) {
      return (state) => {
        return (
          state.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
        );
      };
    },
    handleSelect(item) {
      console.log(item);
    },
    toOnerPage() {
      this.$router.push({
        path: "/carbon/projectCreate/ownerAdd",
        query: { id: this.form.id },
      });
    },
    pickMethod(row) {
      this.form.name = row.name;
      this.form.carbonMethodology = row.dictCode;
      this.dialogTableVisible = false;
    },
    //搜索业主的提示框
    remoteMethod(query) {
      if (query !== "") {
        this.loading = true;
        if (this.OwnerNameOptions) {
          this.loading = false;
        }
      }
    },
    onSave() {
      sessionStorage.setItem("projectAdd", JSON.stringify(this.form));
      if (sessionStorage.getItem("projectAdd")) {
        this.$message.success("保存成功");
      } else {
        this.$message.error("保存失败");
      }
    },
    onCancel() {
      if (this.fromPath === "/carbon/projectCreate/ownerAdd") {
      } else {
        this.$router.go(-1);
      }


},
// onSubmit() {
//   // if (this.onSave()) {
//   this.$router.push({
//     path: "/carbon/ownerAdd",
//     query: { id: this.form.id },
//   });
//   // }
// },
onClickSearch() {
  const data = {
    asc: true,
    size: this.pageSize,
    searchKey: this.searchKeyword,
    fieldCode: this.selectedAreaDict[0],
    industryCode: this.selectedIndustry[0],
    certificationCriteria: this.selectedCertification[0],
    status: "0450000002",
  };
  loadMethodListAsUser(data)
    .then((res) => {
      this.methodList = res.data.records;
      this.total = Number(res.data.total);
      this.methodCurrent = Number(res.data.current);
      this.pageCount = Math.ceil(
        parseInt(res.data.total) / this.methodPageSize
      );
      this.methodList.map((v) => {
        //遍历表格数据
        for (var i in v) {
          v[i] = v[i] ? v[i] : "---";
          if (v[i] == " ") {
            v[i] = "---";
          }
        }
      });
    })
    .catch((error) => 0);
},
getMethodList(val) {
  const data = {
    asc: true,
    size: this.methodPageSize,
    current: val,
    searchKey: this.searchKeyword,
    fieldCode: this.selectedAreaDict[0],
    industryCode: this.selectedIndustry[0],
    certificationCriteria: this.selectedCertification[0],
    status: "0450000002",
    // status: 1,

  };
  loadMethodListAsUser(data)
    .then((res) => {
      this.methodList = res.data.records;
      this.total = Number(res.data.total);
      this.methodCurrent = Number(res.data.current);
      this.pageCount = Math.ceil(
        parseInt(res.data.total) / this.methodPageSize
      );
      this.methodList.map((v) => {
        //遍历表格数据
        for (var i in v) {
          v[i] = v[i] ? v[i] : "---";
          if (v[i] == " ") {
            v[i] = "---";
          }
        }
      });
    })
    .catch((error) => 0);
},
// 格式化省字典
formatProvince(data) {

    console.log("ProvinceData",data);
  data.map((v) => {
    let CertificationItem = {
      value: "",
      label: "",
    };
    if (v.label != "全部") {
      CertificationItem.value = v.value;
      CertificationItem.label = v.name;
    }
    this.provinceList.push(CertificationItem);
  });
},
toNext() {
  if (!(this.form.country && this.form.province && this.form.projectName && this.form.ownerName && this.form.address && this.form.assetsDevelopAgency)) {
    this.$message.error("必填项不能为空！");
  }

  sessionStorage.setItem("projectAdd", JSON.stringify(this.form));
  if (sessionStorage.getItem("projectAdd")) {
  } else {
    this.$message.error("保存失败");
  }
  this.$router.push({
    path: "/carbon/projectCreate/ownerAdd",
    query: { form: this.form },
  });
},
/**
 * 作者:
 * 时间: 2022-06-20 11:07:39
 * 功能: 切换页面
 */
toChangePage() {
  sessionStorage.setItem("projectAdd", JSON.stringify(this.form));
  this.$router.push({
    path: "/carbon/projectCreate/ownerAdd",
    query: { form: this.form },
  });
},
clearCity() {
  this.nowCityList = [];
},
changeCity() {
  if (this.form.city) {
    this.form.city = this.form.city[0];
  }
},
changeProvince() {
  this.nowCityList.length = 0;
//   this.nowCityList=[];
  if (this.form.province) {
    if (Array.isArray(this.form.province)) {
      this.form.province = this.form.province[0];
    }

    console.log("cityList",this.cityList);
    // console.log(this.form);
    var provinceCode = this.form.province.slice(8, 10);
    console.log("provinceCode",provinceCode);
    var bDoOnce=true;
    for (var i in this.cityList)
    {
        var curCityProvinceCode = this.cityList[i].value.slice(6,8);
        if (curCityProvinceCode === provinceCode) {
            this.list.push(this.cityList[i]);
        }
    }
    console.log("list",this.list);

    if (this.list) {
    //   for (var j in this.list) {
    //     this.nowCityList.push({ value: "012015" + j, label: this.list[j] });
    //   }
        this.nowCityList=this.list;
        this.list=[];
    }
  }
},
/*
*@Description: 回车搜索
*@MethodAuthor: liuboting
*@Date: 2022-05-22 16:19:09
*/
searchEnter() {
  this.onClickSearch();
},
// 格式化市字典
formatCity(data) {
  data.map((v) => {
    let CertificationItem = {
      value: "",
      label: "",
    };
    if (v.label == "全部") {
      CertificationItem.value = "";
      CertificationItem.label = v.name;
    } else {
      CertificationItem.value = v.value;
      CertificationItem.label = v.name;
    }
    this.cityList.push(CertificationItem);
  });
},
// 格式化国家字典
formatCountry(data) {
  data.map((v) => {
    let CertificationItem = {
      value: "",
      label: "",
    };
    if (v.label === "全部") {
      CertificationItem.value = "";
      CertificationItem.label = v.name;
    } else {
      CertificationItem.value = v.value;
      CertificationItem.label = v.name;
    }
    this.countryList.push(CertificationItem);
  });
},
    loadProject() {
      var form = localStorage.getItem("projectAdd");
      if (form && form !== "undefined") {
        var myform = JSON.parse(form);
        console.log("myform2222222:",myform);
        this.form = myform;
        if (this.form.province) {
          this.changeProvince();
        }
      }

      var data = this.$route.query;
      var method = getMethodologyDict(this.$store);
      if (data.code) {
        this.form.carbonMethodology = data.code;
      }

      method.map((v) => {
        if (v.value === this.form.carbonMethodology) {
          this.form.name = v.name;
        }
      });
    },
loadCityDict() {
getCityDict(this.$store).then((res) => {
  this.cityList = res.data;
  this.loadProject();
});
},
// 格式化标准字典
formatStatus(data) {
data.map((v) => {
  let CertificationItem = {
    label: "",
  };
  if (v.name == "全部") {
    CertificationItem.label = v.name;
  } else {
    CertificationItem.value = v.value;
    CertificationItem.label = v.name;
  }
  this.Certification.push(CertificationItem);
});
},
// 格式化行业字典
formatIndustry(data) {
data.map((v) => {
  let CertificationItem = {
    label: "",
  };
  if (v.name == "全部") {
    CertificationItem.label = v.name;
  } else {
    CertificationItem.value = v.value;
    CertificationItem.label = v.name;
  }
  this.IndustryList.push(CertificationItem);
});
},
// 格式化领域字典
formatArea(data) {
data.map((v) => {
  let CertificationItem = {
    label: "",
  };
  if (v.name == "全部") {
    CertificationItem.label = v.name;
  } else {
    CertificationItem.value = v.value;
    CertificationItem.label = v.name;
  }
  this.projectAreaDict.push(CertificationItem);
});
},
  },
mounted() {
  let data = this.$route.query;
  if (data) {
    if(data.form)
    {
      this.form=data.form;
    }
    else{
      this.loadProject();
    }
    this.form.name = data.methodName;
    this.form.carbonMethodology = data.code;
  }
  this.formatProjectType(getProjectTypeDict(this.$store));
  this.formatProjectArea(getProjectAreaDict(this.$store));
  this.formatProvince(getProvinceDict(this.$store));
  this.formatCountry(getCountryDict(this.$store));
  this.formatStatus(getCertificationCriteriaDict(this.$store));
  this.formatIndustry(getBusinessDict(this.$store));
  // this.formatArea(getProjectAreaDict(this.$store));
  // this.loadCityDict();
  this.formatCity(getCityDict(this.$store));
  if (this.countryList.length > 0) {
    this.form.country = this.countryList[0].value; // 建议用 value，而非整个对象（级联选择器通常绑定 value）
  }
  this.states = this.loadAll();
// this.list = this.states.map(item => {
//   return { value: item, label: item };
// });
// console.log(this.methodList);
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
  box-shadow: 0px 2px 8px 0px #eaf0f2;
  border-radius: 8px;
}

// .container {
//   margin: 10px 0px 20px 0px;
//   display: flex;
//   flex-direction: row;
// }

.cardBody {
  margin: 30px 30px 30px 30px;
}

.header {
  overflow-wrap: break-word;
  font-size: 18px;
  color: rgba(36, 167, 118, 1);
}

.createTips {
  color: rgba(128, 142, 165, 1);

  float: right;
}

.updateTips {
  overflow-wrap: break-word;
  color: rgba(128, 142, 165, 1);

  float: right;
  margin-right: 20px;
}

.el-divider {
  margin-top: 20px;
}

.outer20 {
  background-color: #1a4441;
  height: 38px;
  width: 194px;
  cursor: pointer;
  float: left;
  margin-top: 20px;

  .info5 {
    width: 123px;
    height: 14px;
    overflow-wrap: break-word;
    color: rgba(255, 255, 255, 1);

    text-align: center;
    white-space: nowrap;

    // display: block;
    margin: 20px 0 0 35px;
    position: relative;
    top: 12px;
  }
}

.basic-div {
  // width: 933px;
  height: 174px;
  border-radius: 6px;
  border: 1px solid #e3e7eb;
  // margin-left: 30px;
  // margin-top: 10px;
}

.outer21 {
  background-color: rgba(38, 181, 129, 0.5);
  height: 38px;
  width: 194px;
  float: left;
  cursor: pointer;
  margin-left: 20px;
  margin-top: 20px;

  .word9 {
    width: 126px;
    height: 14px;
    overflow-wrap: break-word;
    color: rgba(255, 255, 255, 1);

    text-align: center;
    white-space: nowrap;

    // display: block;
    margin: 20px 0 0 33px;
    position: relative;
    top: 12px;
  }
}

.changeline {
  clear: both;
  height: 40px;
}

.icon {
  height: 16px;
  width: 22px;
}

.asset-little-title {
  width: 64px;
  height: 16px;

  font-weight: 500;
  color: #424c5c;

  margin-left: 10px;
}

.country-span {
  width: 200px;
}

// .left {
//   float: left;
//   // width: 10px;
//   // height: auto;
// }

.right {
  // float: left;
  width: 100%;
  margin-left: 30px;
}

.pic2 {
  height: 180px;
  width: 2px;
  position: absolute;
  top: 335px;
  left: 80px;
}

.pic3 {
  height: 210px;
  width: 2px;
  position: absolute;
  top: 550px;
  left: 80px;
}

.projectInfo {
  height: 100%;
  width: 100%;
  border-radius: 6px;
  border: 0px solid;

  font-weight: 400;

  text-align: justify;
}

/* 按钮样式 */
</style>
