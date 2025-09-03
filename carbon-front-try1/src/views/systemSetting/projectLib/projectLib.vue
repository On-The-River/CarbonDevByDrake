<template>
  <div class="root">
    <div class="divBox">
      <div class="content-container">
        <div class="container">
          <div style="width: 200px" class="selectbox-root">
            <span class="selectbox-hint" style="min-width: 80px">核证标准</span>
            <div class="selectbox-deliver" />
            <el-cascader
                placeholder="全部"
                @change="update"
                class="selectbox-input"
                v-model="selectedCertification"
                :options="Certification"
                clearable
            >
            </el-cascader>
          </div>
          <div style="margin-left: 10px; width: 200px" class="selectbox-root">
            <span class="selectbox-hint" style="min-width: 60px">领域</span>
            <div class="selectbox-deliver" />
            <el-cascader
                placeholder="全部"
                @change="update"
                class="selectbox-input"
                v-model="selectedArea"
                :options="ProjectAreaDict"
                clearable
            >
            </el-cascader>
          </div>
          <div style="margin-left: 10px; width: 200px" class="selectbox-root">
            <span class="selectbox-hint" style="min-width: 60px">类型</span>
            <div class="selectbox-deliver" />
            <el-cascader
                placeholder="全部"
                @change="update"
                class="selectbox-input"
                v-model="selectedProjectType"
                :options="projectTypeList"
                clearable
            >
            </el-cascader>
          </div>
          <div style="margin-left: 10px; width: 200px" class="selectbox-root">
            <span class="selectbox-hint" style="min-width: 60px">状态</span>
            <div class="selectbox-deliver" />
            <el-cascader
                placeholder="全部"
                @change="update"
                class="selectbox-input"
                v-model="selectedStatus"
                :options="statusList"
                clearable
            >
            </el-cascader>
          </div>
          <div
              style="width: 225px; margin-left: 16px; padding: 0 0 0 0"
              class="selectbox-root"
          >
            <span class="selectbox-hint" style="min-width: 80px">备案时间</span>
            <div class="selectbox-deliver" />
            <el-date-picker
                v-model="selectRecordDate"
                @change="update"
                type="daterange"
                start-placeholder="开始"
                end-placeholder="结束"
                align="right"
                size="medium"
            >
            </el-date-picker>
          </div>
          <div
              style="width: 230px; margin-left: 16px; padding: 0 0 0 0"
              class="selectbox-root"
          >
            <span class="selectbox-hint" style="min-width: 80px">签发时间</span>
            <div class="selectbox-deliver" />
            <el-date-picker
                v-model="selectIssueDate"
                @change="update"
                type="daterange"
                start-placeholder="开始"
                end-placeholder="结束"
                align="right"
                size="medium"
            >
            </el-date-picker>
          </div>
        </div>
        <div style="clear: both; height: 10px"></div>
        <div class="container" style="margin-top: 0">
          <div style="width: 200px" class="selectbox-root">
            <span class="selectbox-hint" style="min-width: 60px">行业</span>
            <div class="selectbox-deliver" />
            <el-cascader
                placeholder="全部"
                @change="update"
                class="selectbox-input"
                v-model="selectedIndustry"
                :options="IndustryList"
                clearable
            >
            </el-cascader>
          </div>
          <div
              style="margin-left: 15px; margin-right: 15px; width: 450px"
              class="selectbox-root"
          >
            <span class="selectbox-hint" style="min-width: 120px">按项目搜索</span>
            <div class="selectbox-deliver" />
            <el-input
                v-model="searchProjectName"
                placeholder="输入名称或编号"
                clearable
                size="medium"
                @clear="onClickSearch"
                @keyup.enter.native="onClickSearch(0)"
            />
            <button class="light-green-btn" @click="onClickSearch(0)">查询</button>
          </div>
          <div
              style="margin-right: 15px; width: 457px; margin-left: 15px"
              class="selectbox-root"
          >
            <span class="selectbox-hint" style="min-width: 120px">按方法学搜索</span>
            <div class="selectbox-deliver" />
            <el-input
                v-model="searchMethodName"
                placeholder="输入名称"
                clearable
                size="medium"
                @clear="onClickSearch"
                @keyup.enter.native="onClickSearch(1)"
            />
            <button class="light-green-btn" @click="onClickSearch(1)">查询</button>
          </div>
        </div>
      </div>
      <div>
        <el-table
            :header-cell-style="{
            background: '#f2f5f7',
            border: '0px solid #dddddd',
            color: '#4242b35',
            height: '64px',
          }"
            :show-header="true"
            :data="list"
            stripe
            :row-style="{ height: '64px' }"
            style="width: 100%"
        >
          <el-table-column min-width="10"></el-table-column>
          <el-table-column label="序号" align="left" min-width="60">
            <template slot-scope="scope">
              <span>{{ getCurListNo(scope.$index) }}</span>
            </template>
          </el-table-column>
          <el-table-column
              label="备案号"
              align="left"
              prop="refId"
              min-width="60"
          ></el-table-column>
          <el-table-column
              :show-overflow-tooltip="true"
              prop="projectName"
              label="项目名称"
              min-width="100"
          />
          <el-table-column
              prop="projectScope"
              label="项目领域"
              min-width="100"
          />
          <el-table-column
              prop="projectScopeType"
              label="项目类型"
              min-width="100"
          />
          <el-table-column
              align="left"
              prop="projectOwner"
              label="业主名称"
              min-width="80"
          />
          <el-table-column
              align="left"
              prop="certifiedStandard"
              label="核证标准"
              min-width="100"
          />
          <el-table-column
              align="left"
              prop="methodologyName"
              label="方法学"
              min-width="100"
          />
          <el-table-column
              align="left"
              prop="projectStatus"
              label="状态"
              min-width="80"
          />
          <el-table-column align="left" label="审定日期" min-width="100">
            <template slot-scope="scope">
              {{ scope.row.approvalDate | formatDate }}
            </template>
          </el-table-column>
          <el-table-column align="left" label="备案日期" min-width="100">
            <template slot-scope="scope">
              {{ scope.row.recordFilingDate | formatDate }}
            </template>
          </el-table-column>
          <el-table-column align="left" label="核证日期" min-width="100">
            <template slot-scope="scope">
              {{ scope.row.certifiedDate | formatDate }}
            </template>
          </el-table-column>
          <el-table-column align="left" label="签发日期" min-width="100">
            <template slot-scope="scope">
              {{ scope.row.issuingDate | formatDate }}
            </template>
          </el-table-column>
          <el-table-column
              align="left"
              prop="estimatedAnnualReduction"
              label="年排放量"
              min-width="100"
          >
            <template slot-scope="scope">
              <span>
                {{ scope.row.estimatedAnnualReduction }}
                <span v-if="scope.row.estimatedAnnualReduction !== '--'"
                >tCO2e</span
                >
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="150" align="center">
            <template slot-scope="scope">
              <a
                  class="list-green-text"
                  @click="toDetail(scope.row)"
                  style="margin-left: 10px"
              >查看</a
              >
              <!-- <a
                class="list-blue-text"
                @click="toWork(scope.row)"
                style="margin-left: 10px"
                >工作台</a
              > -->
              <a
                  class="list-blue-text"
                  @click="toFile(scope.row.id)"
                  style="margin-left: 10px"
              >文档</a
              >
            </template>
          </el-table-column>
        </el-table>
      </div>
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
    </div>
  </div>
</template>

<script>
import { getFeishuProjectFile } from "@/api/feishuApi.js";
import {
  editMethod, getEscarbonMetaregistryList,
  getCarbonMetaregistryList, delCarbonExchanger
} from "@/api/carbonAssetApi.js";
import { openUrlInNewWindow } from '@/libs/OpenHelper';
import {
  getCertificationCriteriaDict, getProjectAreaDict,
  getBusinessDict, getProjectStatusDict, getProjectTypeDict
} from '@/config/dictHelper';
import { setListNo } from '@/libs/public';

export default {
  name: 'metaregistry',
  data() {
    return {
      indeterminateFlag: false,
      reRender: true,
      allchecked: false,
      selectedCertification: '',
      selectedArea: '',
      selectedIndustry: '',
      selectedStatus: '',
      list: [],
      isSearch: false,
      selectRecordDate: '',
      selectIssueDate: '',
      selectedProjectType: '',
      searchProjectName: '',
      searchMethodName: '',
      searchByType: true,
      total: 0,
      current: 1,
      pageCount: 1,
      pageSize: 10,
      projectTypeList: [],
      projectTypeDict: [],
      Certification: [],
      IndustryList: [],
      ProjectAreaDict: [],
      methodUrl: 'https://carbonmsger.feishu.cn/drive/folder/XXX',
      statusList: [],
      value: '',
      selectEndDate: null,
    };
  },
  filters: {
    formatDate(val) {
      if (val !== '--') {
        let date = val.split('~');
        return date[0];
      }
      return val;
    },
  },
  watch: {
    selectedArea() {
      const typeList = [];
      console.log(111);
      if (this.selectedArea) {
        if (Array.isArray(this.selectedArea)) {
          this.projectTypeDict.map((v) => {
            let code = this.selectedArea[0];
            code = code.substring(3, 5);
            let typeCode = v.value.substring(3, 5);
            console.log(code, typeCode);
            if (code === typeCode) {
              typeList.push({ label: v.label, value: v.value });
            }
          });
        }
        this.projectTypeList = typeList;
      }
    },
  },
  methods: {
    cellStyle({ row, column, rowIndex, columnIndex }) {
      if (column.label !== '操作') {
        return 'cursor:pointer;';
      }
    },
    getCurListNo(index) {
      let curNo = parseInt(index) + 1;
      let size = this.size || '10';
      let page = this.current - 1;
      let no = setListNo(page, size, curNo);
      return no ? no : 1;
    },
    editStyleChange(status) {
      if (status === 1) {
        return 'afterSubmitEdit';
      } else {
        return 'list-green-text';
      }
    },
    toDetail(row) {
      let routeData = this.$router.resolve({
        path: '/sys/projectDetail',
        query: { refId: row.refId, typeCode: row.projectScopeTypeCode },
      });
      window.open(routeData.href, '_blank');
    },
    toFile(registryId) {
      let routeData = this.$router.resolve({
        path: '/sys/metaregistrydoc',
        query: { id: registryId },
      });
      window.open(routeData.href, '_blank');
    },
    publishStyleChange(status) {
      if (status === 1) {
        return 'afterSubmitPublish';
      } else {
        return 'list-blue-text';
      }
    },
    offlineStyleChange(status) {
      if (status === 1) {
        return 'list-red-text';
      } else {
        return 'afterSubmitOffline';
      }
    },
    onclickAdd() {
      openUrlInNewWindow(this.methodUrl);
    },
    onClickPublish(row_id) {
      const data = {
        id: row_id,
        status: 1,
      };
      editMethod(data).then(
          (res) => {
            this.$message.success('发布成功');
            this.getList(this.current);
          },
          (err) => {
            this.$message.success('发布失败');
          }
      );
    },
    onClickDelete(id) {
      this.$confirm('删除内容不可复原，请谨慎操作').then(() => {
        delCarbonExchanger(id).then(
            (res) => {
              this.$message.success('删除成功');
              this.getList(this.current);
            },
            (err) => {
              this.$message.success('删除失败');
            }
        );
      });
    },
    onClickOffline(row_id) {
      const data = {
        id: row_id,
        status: 2,
      };
      editMethod(data).then(
          (res) => {
            this.$message.success('下架成功');
            this.getList(this.current);
          },
          (err) => {
            this.$message.success('下架失败');
          }
      );
    },
    toWork(data) {
      let datas = {
        projectId: data.id,
        projectName: data.projectName,
      };
      getFeishuProjectFile(datas).then((res) => {
        let routeData = this.$router.resolve({
          path: '/pad',
          query: {
            templateNum: res.code,
            position: '0400000001',
            projectName: data.projectName,
          },
        });
        window.open(routeData.href, '_blank');
      });
    },
    update() {
      let data = {};
      if (this.selectRecordDate && this.selectRecordDate[0]) {
        data['refDateStart'] = this.selectRecordDate[0];
      }
      if (this.selectRecordDate && this.selectRecordDate[1]) {
        data['refDateEnd'] = this.selectRecordDate[1];
      }
      if (this.selectIssueDate && this.selectIssueDate[0]) {
        data['issueDateStart'] = this.selectIssueDate[0];
      }
      if (this.selectIssueDate && this.selectIssueDate[1]) {
        data['issueDateEnd'] = this.selectIssueDate[1];
      }
      if (this.searchProjectName) {
        data['searchKey'] = this.searchProjectName;
      }
      if (this.selectedCertification && this.selectedCertification[0]) {
        data['certifiedStandardCode'] = this.selectedCertification[0];
      }
      if (this.selectedProjectType && this.selectedProjectType[0]) {
        data['projectTypeCode'] = this.selectedProjectType[0];
      }
      if (this.selectedArea && this.selectedArea[0]) {
        data['projectScopeCode'] = this.selectedArea[0];
      }
      if (this.selectedIndustry && this.selectedIndustry[0]) {
        data['projectIndustryCode'] = this.selectedIndustry[0];
      }
      if (this.selectedStatus && this.selectedStatus[0]) {
        data['projectStatusCode'] = this.selectedStatus[0];
      }

      if (this.searchByType) {
        if (this.searchMethodName) {
          data['methodSearchKey'] = this.searchMethodName;
        }
      }
      this.getByCarbonMetaregistryList(data);
    },
    onClickSearch(type) {
      if (type) {
        this.searchByType = true;
      }
      if (type === 0) {
        this.searchByType = false;
        this.searchMethodName = '';
      }
      const data = {
        asc: true,
        size: this.pageSize,
        current: 1,
        refDateStart: this.selectRecordDate[0],
        refDateEnd: this.selectRecordDate[1],
        issueDateStart: this.selectIssueDate[0],
        issueDateEnd: this.selectIssueDate[1],
      };
      if (this.searchProjectName) {
        data['searchKey'] = this.searchProjectName;
      }
      if (this.selectedProjectType) {
        data['projectTypeCode'] = this.selectedProjectType[0];
      }
      if (this.selectedCertification) {
        data['certifiedStandardCode'] = this.selectedCertification[0];
      }
      if (this.selectedArea) {
        data['fieldCode'] = this.selectedArea[0];
      }
      if (this.selectedIndustry) {
        data['projectIndustryCode'] = this.selectedIndustry[0];
      }
      if (this.selectedStatus) {
        data['projectStatusCode'] = this.selectedStatus[0];
      }

      if (this.searchByType) {
        if (this.searchProjectName) {
          delete data['searchKey'];
          this.searchProjectName = '';
        }
        if (this.searchMethodName) {
          data['methodSearchKey'] = this.searchMethodName;
        }
      }

      this.getByCarbonMetaregistryList(data);
    },
    getByEscarbonMetaregistryList(data) {
      let n = this.$message.success('查询中...');
      getEscarbonMetaregistryList(data)
          .then((res) => {
            n.close();
            console.log("records",res.data.records);
            this.list = res.data.records;
            this.total = Number(res.data.total);
            this.current = Number(res.data.current);
            this.pageCount = Math.ceil(parseInt(res.data.total) / this.pageSize);
            this.list.map((v, i) => {
              v.order = i + 1;
              for (const i in v) {
                v[i] = v[i] && v[i] !== '0000-00-00 00:00:00' &&
                    v[i] !== '0000-00-00' ? v[i] : '';
                if (v[i] === '') {
                  v[i] = '--';
                }
              }
            });
          })
          .catch((error) => {
            n.close();
            // console.log("records",error);
            this.$message.error(error);
          });
    },
    getByCarbonMetaregistryList(data) {
      getCarbonMetaregistryList(data)
          .then((res) => {
            this.list = res.data.records;
            this.total = Number(res.data.total);
            this.current = Number(res.data.current);
            this.pageCount = Math.ceil(parseInt(res.data.total) / this.pageSize);
            this.list.map((v, i) => {
              v.order = i + 1;
              for (const i in v) {
                v[i] = v[i] ? v[i] : '--';
                if (v[i] === '') {
                  v[i] = '--';
                }
              }
            });
          })
          .catch((error) => {});
    },
    onEdit(url) {
      openUrlInNewWindow(url);
    },
    handle(row, column) {
      if (column.label !== '操作') {
        this.toDetail(row.id);
      }
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.getList(1);
    },
    handleCurrentChange(val) {
      this.current = val;
      this.getList(val);
    },
    handleResize() {
      if (this.infoList) this.$refs.visitChart.handleResize();
    },
    statusName(status) {
      if (status === 1) {
        return '启用';
      } else {
        return '禁用';
      }
    },
    getList(page) {
      const data = {
        asc: true,
        size: this.pageSize,
        current: this.current,
        refDateStart: this.selectRecordDate[0],
        refDateEnd: this.selectRecordDate[1],
        issueDateStart: this.selectIssueDate[0],
        issueDateEnd: this.selectIssueDate[1],
      };
      if (this.selectedCertification) {
        data['certifiedStandardCode'] = this.selectedCertification[0];
      }
      if (this.selectedProjectType) {
        data['projectTypeCode'] = this.selectedProjectType[0];
      }
      if (this.selectedArea) {
        data['fieldCode'] = this.selectedArea[0];
      }
      if (this.selectedIndustry) {
        data['projectIndustryCode'] = this.selectedIndustry[0];
      }
      if (this.selectedStatus) {
        data['projectStatusCode'] = this.selectedStatus[0];
      }
      if (this.searchProjectName) {
        data['searchKey'] = this.searchProjectName;
      }

      if (this.searchByType) {
        if (this.searchMethodName) {
          data['methodSearchKey'] = this.searchMethodName;
        }
      }
      this.getByCarbonMetaregistryList(data);
    },
    formatTime(time) {
      const newTime = time.split(' ');
      return newTime[0];
    },
    signCheckChange() {
      let allCheckedFlag = true;
      let allReset = true;
      this.list.forEach((item) => {
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
    updateAllSelected(val) {
      this.indeterminateFlag = false;
      if (val) {
        this.list.forEach((item) => {
          item.checked = true;
        });
      } else {
        this.list.forEach((item) => {
          item.checked = false;
        });
      }
    },
    renderCheckHeader(h, { column, $index }) {
      return h('span', {}, [
        h('el-checkbox', {
          props: {
            checked: this.allchecked,
            indeterminate: this.indeterminateFlag,
          },
          on: {
            change: this.updateAllSelected,
          },
        }),
      ]);
    },
    formatCertification(data) {
      data.map((v) => {
        let CertificationItem = {
          label: '',
        };
        if (v.name === '全部') {
          CertificationItem.label = v.name;
        } else {
          CertificationItem.value = v.value;
          CertificationItem.label = v.name;
        }
        this.Certification.push(CertificationItem);
      });
    },
    formatIndustry(data) {
      data.map((v) => {
        let CertificationItem = {
          label: '',
        };
        if (v.name === '全部') {
          CertificationItem.label = v.name;
        } else {
          CertificationItem.value = v.value;
          CertificationItem.label = v.name;
        }
        this.IndustryList.push(CertificationItem);
      });
    },
    formatArea(data) {
      data.map((v) => {
        let CertificationItem = {
          label: '',
        };
        if (v.name === '全部') {
          CertificationItem.label = v.name;
        } else {
          CertificationItem.value = v.value;
          CertificationItem.label = v.name;
        }
        this.ProjectAreaDict.push(CertificationItem);
      });
    },
    formatStatus(data) {
      data.map((v) => {
        let CertificationItem = {
          label: '',
        };
        if (v.name === '全部') {
          CertificationItem.label = v.name;
        } else {
          CertificationItem.value = v.value;
          CertificationItem.label = v.name;
        }
        this.statusList.push(CertificationItem);
      });
    },
    isProject(status) {
      return "afterSubmitPublish";
    },
  },
  created() {},
  mounted() {
    this.getList(1);
    this.formatCertification(getCertificationCriteriaDict(this.$store));
    this.formatIndustry(getBusinessDict(this.$store));
    this.formatArea(getProjectAreaDict(this.$store));
    this.formatStatus(getProjectStatusDict(this.$store));
    let data = getProjectTypeDict(this.$store);
    data.map((v) => {
      let CertificationItem = {
        label: "",
        value: "",
      };
      CertificationItem.value = v.value;
      CertificationItem.label = v.name;
      this.projectTypeDict.push(CertificationItem);
    });
    this.projectTypeList = this.projectTypeDict;
  }
}
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
  margin: 20px 0px 20px 0px;
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

.area-row {
  :deep(.el-avatar--small) {
    width: 22px;
    height: 22px;
    line-height: 22px;
  }
}

.checkTime {
  :deep(.el-radio__input) {
    display: none;
  }
}

.ivu-pl-8 {
  margin-left: 8px;
  font-size: 14px;
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
</style>
