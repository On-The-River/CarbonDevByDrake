<template>
  <div class="divBox">
    <div class="content-container">
      <div class="content">
        <div style="width: 300px" class="selectbox-root">
          <span class="selectbox-hint" style="width: 100px">字典名称</span>
          <div class="selectbox-deliver" />
          <el-input v-model="searchItemCh" placeholder="请输入字典名称" clearable size="medium"
                    @keyup.enter.native="onClickSearchName" @clear="onClickSearchName" />
        </div>
        <Button style="margin-left: 10px" class="light-green-btn" @click="onClickSearchName">
          查询
        </Button>
        <div style="width: 300px; margin-left: 20px" class="selectbox-root">
          <span class="selectbox-hint" style="width: 100px">字典编号</span>
          <div class="selectbox-deliver" />
          <el-input v-model="searchDictCode" placeholder="请输入字典编号" clearable size="medium"
                    @keyup.enter.native="onClickSearch" @clear="onClickSearch" />
        </div>
        <div style="clear: both" />
        <Button style="margin-left: 10px" class="light-green-btn" @click="onClickSearch">
          查询
        </Button>
      </div>
      <div class="content">
        <Button style="margin-top: 0px; margin-bottom: 20px" class="normal-white-btn" @click="addDict">
          +添加
        </Button>
        <Button style="margin-top: 0px; margin-bottom: 20px; margin-left: 20px" class="normal-white-btn">
          导出
        </Button>
        <Button style="margin-top: 0px; margin-bottom: 20px; margin-left: 20px" class="normal-white-btn">
          导入
        </Button>
      </div>
      <el-table :header-cell-style="{
        background: '#F5F5F7',
        border: '0px solid #000000',
        color: '#626265',
        height: '40px'
      }" :show-header="true" :data="list" stripe :row-style="{ height: '40px' }" :cell-style="cellStyle"
                style="width: 100%">
              <!--style="align: center"-->
        <!-- <template slot="header" >
          <el-checkbox v-model="columnChecked" :indeterminate="indeterminateFlag" checked="allChecked" label=""
                       @change="updateAllSelected"></el-checkbox>
        </template> -->
        <!-- <el-table-column>
          <template slot-scope="row">
            <el-checkbox @change="rowCheckChange" v-model="row.checked"></el-checkbox>
          </template>
        </el-table-column> -->
        <el-table-column min-width="3%"/>
        <el-table-column prop="dictName" label="字典名称" min-width="18%" align="left" />
        <el-table-column min-width="2%"/>
        <el-table-column prop="dictCode" label="字典编码" min-width="10%" align="left" />
        <el-table-column align="left" prop="description" label="描述" min-width="20%" />
        <el-table-column align="left" prop="createTime" label="创建时间" min-width="10%" />
        <el-table-column align="left" prop="updateTime" label="修改时间" min-width="10%" />
        <el-table-column label="操作" min-width="20%" fixed="right" align="center">
          <template #default="scope">
          <span v-if="scope.row.label">
            <span>
              <template v-if="scope.row.children && scope.row.children.length > 0">
                <a class="list-blue-text" style="margin-left: 10px" @click="onEdit(scope.row, true)">编辑</a>
                <a class="list-red-text" style="margin-left: 10px" @click="onClickDelete(scope.row.id)">删除</a>
              </template>
              <template v-else>
                <a class="list-blue-text" style="margin-left: 10px" @click="onEdit(scope.row, true)">编辑</a>
                <a class="list-red-text" style="margin-left: 10px" @click="onClickDelete(scope.row.id)">删除</a>
              </template>
            </span>
          </span>
            <template v-else>
              <a class="list-blue-text" style="margin-left: 10px" @click="onEdit(scope.row, true)">编辑</a>
              <a class="list-red-text" style="margin-left: 10px" @click="onClickDelete(scope.row.id)">删除</a>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 30px; margin-bottom: 10px" class="pageBox">
        <div style="flex-grow: 1" />
        <a style="margin: auto" class="pageBox-total-mm">共{{ total }}条</a>
        <el-pagination style="margin: auto" background @size-change="handleSizeChange"
                       @current-change="handleCurrentChange" :current-page="current" :page-size="pageSize" :page-count="pageCount"
                       layout="total, sizes, prev, pager, next, jumper" :total="total" />
      </div>
      <!-- 弹窗 -->
      <el-dialog :title="title" :visible.sync="dialogFormVisible" width="30%">
        <el-form :model="form" label-width="60">
          <el-form-item label="字典类型" :label-width="60">
            <el-input v-model="form.dictName" autocomplete="off" size="medium"></el-input>
          </el-form-item>
          <el-form-item label="字典编码" :label-width="60">
            <el-input v-model="form.dictCode" autocomplete="off" size="medium"></el-input>
          </el-form-item>
          <el-form-item label="描述" :label-width="60">
            <el-input v-model="form.description" autocomplete="off" size="medium"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogFormVisible = false" class="normal-white-btn">取消</el-button>
          <el-button type="primary" @click="submit" class="light-green-btn">确定</el-button>
        </div>
      </el-dialog>
      <!-- 单个字典编码弹窗 -->
      <el-dialog title="字典编码" :visible.sync="dictListShow">
        <el-row>
          <el-col :span="10">
            <div style="" class="selectbox-root">
              <a class="selectbox-hint" style="width: 100px">字典名称</a>
              <div class="selectbox-deliver" />
              <el-input v-model="itemCh" placeholder="请输入字典名称" clearable size="medium" @clear="onClickSearchSingDict"
                        @keyup.enter.native="onClickSearchSingDict" />
            </div>
          </el-col>
          <el-col :span="10">
            <div style="margin-left: 15px" class="selectbox-root">
              <a class="selectbox-hint" style="width: 100px">字典编号</a>
              <div class="selectbox-deliver" />
              <el-input v-model="itemValue" placeholder="请输入字典编号" clearable size="medium" @clear="onClickSearchSingDict"
                        @keyup.enter.native="onClickSearchSingDict" />
            </div>
          </el-col>
          <el-col :span="4">
            <Button style="margin-left: 10px; margin-top: 3px" class="light-green-btn" @click="onClickSearchSingDict">
              查询
            </Button>
          </el-col>
        </el-row>
        <Button style="margin-top: 10px" class="normal-white-btn" @click="showChildAdd">
          +添加
        </Button>
        <el-table :data="dictList" stripe>
          <el-table-column prop="itemCh" label="字典值" width="180" align="left"></el-table-column>
          <el-table-column prop="itemValue" label="字典编码" width="100"></el-table-column>
          <el-table-column prop="description" label="描述" width="200"></el-table-column>
          <el-table-column label="操作" min-width="130" fixed="right" align="center">
            <template slot-scope="scope">
              <a class="list-blue-text" style="margin-left: 10px" @click="onEditSingDict(scope.row)">编辑</a>
              <a class="list-red-text" style="margin-left: 10px" @click="delSingDict(scope.row.id)">删除</a>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 30px; margin-bottom: 10px" class="pageBox">
          <div style="flex-grow: 1" />
          <a style="margin: auto" class="pageBox-total-mm">
            共{{ dictPageCount }}页
          </a>
          <el-pagination style="margin: auto" background @size-change="handleDictSizeChange"
                         @current-change="handleDictCurrentChange" :current-page="dictCurrent" :page-size="dictPageSize"
                         :page-count="dictPageCount" layout="total, sizes, prev, pager, next, jumper" :total="dictTotal" />
        </div>
      </el-dialog>
      <!-- 编辑单个字典值 -->
      <el-dialog :title="title" :visible.sync="dictShow" width="30%">
        <el-form :model="dict" label-width="60">
          <el-form-item label="名称" :label-width="60">
            <el-input v-model="dict.itemCh" autocomplete="off" size="medium"></el-input>
          </el-form-item>
          <el-form-item label="字典编码" :label-width="60">
            <el-input v-model="dict.itemValue" autocomplete="off" size="medium"></el-input>
          </el-form-item>
          <el-form-item label="父级字典编码" :label-width="60">
            <el-input v-model="dict.dictCode" autocomplete="off" size="medium"></el-input>
          </el-form-item>
          <el-form-item label="描述" :label-width="60">
            <el-input v-model="dict.description" autocomplete="off" size="medium"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dictShow = false" class="normal-white-btn">取消</el-button>
          <el-button type="primary" @click="onClickEditSingDict" class="light-green-btn">确定</el-button>
        </div>
      </el-dialog>
      <!-- 添加字典 -->
      <el-dialog title="添加字典" :visible.sync="addDictShow" width="30%">
        <el-form :model="form">
          <el-form-item label="字典名称" :label-width="60">
            <el-input v-model="form.dictName" autocomplete="off" size="medium"></el-input>
          </el-form-item>
          <el-form-item label="字典编码" :label-width="60">
            <el-input v-model="form.dictCode" autocomplete="off" size="medium"></el-input>
          </el-form-item>
          <el-form-item label="描述" :label-width="60">
            <el-input v-model="form.description" autocomplete="off" size="medium"></el-input>
          </el-form-item>
          <el-form-item label="字典类型" :label-width="60">
            <el-select v-model="form.type" placeholder="请选择" size="medium">
              <el-option label="字符串" value="0"></el-option>
              <el-option label="数字" value="1"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="addDictShow = false" class="normal-white-btn">取消</el-button>
          <el-button type="primary" @click="addSubmit" class="light-green-btn">确定</el-button>
        </div>
      </el-dialog>
      <!-- 添加子字典 -->
      <el-dialog title="添加字典" :visible.sync="addDictChildShow" width="30%">
        <el-form :model="childForm">
          <el-form-item label="字典名称" :label-width="60">
            <el-input v-model="childForm.itemCh" autocomplete="off" size="medium"></el-input>
          </el-form-item>
          <el-form-item label="字典编码" :label-width="60">
            <el-input v-model="childForm.itemValue" autocomplete="off" size="medium"></el-input>
          </el-form-item>
          <el-form-item label="父级字典编码" :label-width="60">
            <el-input v-model="childForm.dictCode" autocomplete="off" size="medium"></el-input>
          </el-form-item>
          <el-form-item label="描述" :label-width="60">
            <el-input v-model="childForm.description" autocomplete="off" size="medium"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="addDictChildShow = false" class="normal-white-btn">取消</el-button>
          <el-button type="primary" @click="onClickAddChildDict" class="light-green-btn">确定</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import * as systemAdminApi from "@/api/systemadmin.js"
import selectDropDownBox from "@/components/selectbox/selectDropDownBox.vue";
import { getAllDiction } from "@/config/dictHelper.js";
import { cursor } from "@/libs/element-table.js";

export default {
  name: "companyPackage",
  components: {
    selectDropDownBox
  },
  data() {
    return {
      isAdd: false,
      isRender: true,
      searchDictName: "",
      searchDictCode: "",
      list: [],
      total: 0,
      current: 1,
      pageCount: 1,
      pageSize: 10,
      value: "",
      dialogFormVisible: false,
      dictListShow: false,
      dictList: [],
      dictShow: false,
      addDictShow: false,
      addDictChildShow: false,
      dict: {
        itemCh: "",
        id: 0,
        itemValue: "",
        dictCode: "",
        description: "",
      },
      form: {
        dictName: "",
        dictCode: "",
        description: "",
        id: "",
        type: "0",
      },
      childForm: {
        dictCode: "",
        itemCh: "",
        itemValue: "",
        description: "",
        status: 0,
      },
      title: "",
      dictTotal: 0,
      dictPageCount: 0,
      dictCurrent: 1,
      dictPageSize: 10,
      dictCode: "",
      itemValue: "",
      itemCh: ""
    };
  },
  methods: {
    cellStyle(data) {
      return cursor(data);
    },
    getAllDictionary() {
      getAllDiction(this.$store);
    },
    addDict() {
      this.addDictShow = true;
      this.form.dictName = "";
      this.form.id = "";
      this.form.dictCode = "";
      this.form.description = "";
      this.form.type = "";
    },
    addSubmit() {
      if (this.form.type && this.form.dictCode && this.form.dictName) {
        systemAdminApi.addDict(this.form).then(res => {
          this.$message.success("添加成功");
          this.getAllDictionary();
          this.addDictShow = false;
          this.getList(1);
        }).catch(err => {
          this.$message.error(err.message);
        })
      } else {
        this.$message.warning("应当填入必须字段");
      }
    },
    delSingDict(id) {
      this.$confirm("确认删除此字典？").then(() => {
        systemAdminApi.delChildDict(id).then(res => {
          this.$message.success("删除成功");
          this.getAllDictionary();
          this.loadSingleDictList(1);
        }).catch(err => {
          this.$message.error(err.message);
        });
      })
    },
    onClickSearchSingDict() {
      const data = {
        current: 1,
        size: this.dictPageSize,
        itemCh: this.itemCh,
        itemValue: this.itemValue,
        dictCode: this.dictCode,
        asc: true
      };
      systemAdminApi.getSingleDictList(data).then(res => {
        this.dictList = res.records;
        this.dictTotal = Number(res.total);
        this.dictCurrent = Number(res.current);
        this.dictPageCount = Math.ceil(parseInt(this.dictTotal) / this.dictPageSize);

        this.dictList.map(value => {
          for (const i in value) {
            value[i] = value[i] ? value[i] : "--";
            if (value[i] === " ") {
              value[i] = "--"
            }
          }
        });
      }).catch(error => {
        this.$message.error(error.message);
      });
    },
    onClickSearchName() {
      const params = {
        dictName: this.searchDictName,
      };
      systemAdminApi.getDictList(params).then(res => {
        this.list = res.records;
        this.list = this.list.map(item => ({ ...item, checked: false }));
        this.total = Number(res.total);
        this.current = Number(res.current);
        this.pageCount = Math.ceil(parseInt(this.total) / this.pageSize);

        this.list.map(value => {
          for (const i in value) {
            value[i] = value[i] ? value[i] : "--";
            if (value[i] === " ") {
              value[i] = "--"
            }
          }
        });
      }).catch(error => {
        this.$message.error(error.message);
      })
    },
    onClickSearch() {
      const params = {
        dictCode: this.searchDictCode,
      };
      systemAdminApi.getDictList(params).then(res => {
        this.list = res.records;
        this.list = this.list.map(item => ({ ...item, checked: false }));
        this.total = Number(res.total);
        this.current = Number(res.current);
        this.pageCount = Math.ceil(parseInt(this.total) / this.pageSize);

        this.list.map(value => {
          for (const i in value) {
            value[i] = value[i] ? value[i] : "--";
            if (value[i] === " ") {
              value[i] = "--"
            }
          }
        });
      }).catch(error => {
        this.$message.error(error.message);
      });
    },
    onEdit(row, bool) {
      this.dialogFormVisible = bool;
      if (bool) {
        this.title = "编辑 - "+row.dictName;
        this.form.dictName = row.dictName;
        this.form.id = row.id;
        this.form.dictCode = row.dictCode;
        this.form.description = row.description;
        this.form.type = row.type === "--" ? 0 : row.type;
      } else {
        this.dict.name = row.name;
        this.dict.value = row.value;
      }
    },
    onEditSingDict(row) {
      this.dictShow = true;
      this.dict.id = row.id;
      this.dict.itemCh = row.itemCh;
      this.dict.itemValue = row.itemValue;
      this.dict.dictCode = row.dictCode;
      this.dict.description = row.description;
    },
    onClickEditSingDict(row) {
      if (this.dict.itemCh && this.dict.itemValue && this.dict.dictCode) {
        systemAdminApi.editChildDict(this.dict).then(res => {
          this.$message.success("修改成功");
          this.getAllDictionary();
          this.getList(this.current);
        }).catch(err => {
          this.$message.error(err.message);
        })
      } else {
        this.$message.warning("请填入必须字段");
      }
    },
    onClickPublish() {},
    onClickDelete(id) {
      this.$confirm("确认删除此字典么？").then(() => {
        systemAdminApi.delDict(id).then(res => {
          this.$message.success("删除成功");
          this.getAllDictionary();
          this.getList(this.current);
        }).catch(err => {
          this.$message.error(err.message);
        });
      });
    },
    onClickOffline() {},
    handleSizeChange(value) {
      this.pageSize = value;
      this.getList(this.current);
    },
    handleCurrentChange(value) {
      this.current = value;
      this.getList(this.current);
    },
    handleDictSizeChange(value) {
      this.dictPageSize = value;
      this.loadSingleDictList(this.dictCurrent);
    },
    handleDictCurrentChange(value) {
      this.dictCurrent = value;
      this.loadSingleDictList(this.dictCurrent);
    },
    editDictConfig(dictCode) {
      this.itemCh = "";
      this.itemValue = "";
      this.dictCode = dictCode;
      this.dictListCode = dictCode;
      this.loadSingleDictList(1);
      this.dictListShow = true;
    },
    showChildAdd() {
      this.childForm.dictCode = this.dictListCode;
      this.addDictChildShow = true;
    },
    onClickAddChildDict() {
      if (this.childForm.dictCode && this.childForm.itemCh && this.childForm.itemValue) {
        systemAdminApi.addChildDict(this.childForm).then(res => {
          this.$message.success("添加成功");
          this.addDictChildShow = false;
          this.loadSingleDictList(1);
          this.getAllDictionary();
        }).catch(err => {
          this.$message.error(err.message);
        });
      } else {
        this.$message.warning("请填入必须字段");
      }
    },
    loadSingleDictList(current) {
      const data = {
        current: current,
        size: this.dictPageSize,
        dictCode: this.dictCode,
        asc: true
      };
      if (this.itemValue) {
        data["itemValue"] = this.itemValue;
      }
      if (this.itemCh) {
        data["itemCh"] = this.itemCh;
      }
      systemAdminApi.getSingleDictList(data).then(res => {
        this.dictList = res.records;
        this.dictTotal = Number(res.total);
        this.dictCurrent = Number(res.current);
        this.dictPageCount = Math.ceil(parseInt(this.dictTotal) / this.dictPageSize);

        this.dictList.map(value => {
          for (const i in value) {
            value[i] = value[i] ? value[i] : "--";
            if (value[i] === " ") {
              value[i] = "--"
            }
          }
        });
      }).catch(err => {
        this.$message.error(err.message);
      })
    },
    getList(page) {
      const params = {
        asc: true,
        current: page,
        size: this.pageSize,
        dictCode: this.searchDictCode,
        dictName: this.searchDictName
      };
      systemAdminApi.getDictList(params).then(res => {
        this.list = res.records;
        this.list = this.list.map(item => ({ ...item, checked: false }));
        this.total = Number(res.total);
        this.current = Number(res.current);
        this.pageCount = Math.ceil(parseInt(this.total) / this.pageSize);

        this.list.map(value => {
          for (const i in value) {
            value[i] = value[i] ? value[i] : "--";
            if (value[i] === " ") {
              value[i] = "--"
            }
          }
        });
      }).catch(error => {
        this.$message.error(error.message);
      });
    },
  },
  created() {},
  mounted() {
    this.getList(1);
  },
}

</script>

<style lang="scss" scoped>
.dialog-footer {
  text-align: center;
}

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

.content {
  display: flex;
  flex-direction: row;
  align-items: center;
  float: left;
  margin: 20px 0px 10px 20px;
}



:deep(.el-cascader .el-input .el-input__inner),
:deep(.el-cascader .el-input.is-focus .el-input__inner) {
  border-color: transparent;
}

.acea-row {
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
