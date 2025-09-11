<template>
  <div class="root">
    <div class="divBox">
      <div class="content-container">
        <button style="margin-top: 0; margin-bottom: 20px; width: 100px" class="normal-white-btn" @click="addFather">
          +添加一级菜单
        </button>
      </div>
      <div class="container">
        <el-table :data="list" :row-style="{ height: '64px' }" :cell-style="cellStyle" stripe
                  :header-cell-style="headerStyle" row-key="id" style="width: 100%">
          <el-table-column align="left" prop="menuName" label="菜单名称" min-width="80" />
          <el-table-column align="left" prop="menuType" label="菜单类型" min-width="60" />
          <el-table-column align="left" prop="menuIcon" label="icon" min-width="80" />
          <el-table-column align="left" prop="menuUrl" label="路径" min-width="100" />
          <el-table-column align="left" prop="orderNum" label="排序" min-width="60" />
          <el-table-column align="left" prop="statusName" label="状态" min-width="60" />
          <el-table-column align="left" label="操作" min-width="130" fixed="right">
            <template slot-scope="scope">
              <template v-if="scope.row.isDel">
                <span></span>
              </template>
              <template v-else>
                <a class="list-blue-text" style="margin-left: 10px" @click="addChild(scope.row)">添加</a>
                <a class="list-blue-text" style="margin-left: 10px" @click="onEdit(scope.row, true)">编辑</a>
                <a class="list-red-text" style="margin-left: 10px" @click="onClickDelete(scope.row)">删除</a>
                <a class="list-green-text" style="margin-left: 10px" :class="startStyleChange(scope.row.status)"
                   @click="changeStatus(scope.row, 0)">启用</a>
                <a class="list-red-text" style="margin-left: 10px" :class="banStyleChange(scope.row.status)"
                   @click="changeStatus(scope.row, 1)">禁用</a>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <!-- <div style="margin-top: 30px; margin-bottom: 10px" class="pageBox">
      <div style="flex-grow: 1" />
      <a style="margin: auto" class="pageBox-total-num">共{{ total }}条</a>
      <el-pagination style="margin: auto" background @size-change="handleSizeChange"
        @current-change="handleCurrentChange" :current-page="current" :page-size="pageSize" :page-count="pageCount"
        layout="total, sizes, prev, pager, next, jumper" :total="total">
      </el-pagination>
    </div> -->
    <!-- Form -->
    <el-dialog title="title" :visible.sync="dialogFormVisible" width="30%">
      <el-form :model="form">
        <el-form-item label="菜单名称" :label-width="60">
          <span class="require">*</span>
          <el-input v-model="form.menuName" autocomplete="off" size="medium"></el-input>
        </el-form-item>
        <el-form-item label="菜单icon" :label-width="60">
          <el-input v-model="form.menuIcon" autocomplete="off" size="medium"></el-input>
        </el-form-item>
        <el-form-item label="菜单路径" :label-width="60">
          <span class="require">*</span>
          <el-input v-model="form.menuUrl" autocomplete="off" size="medium"></el-input>
        </el-form-item>
        <el-form-item label="菜单排序" :label-width="60">
          <el-input v-model="form.orderNum" autocomplete="off" size="medium"></el-input>
        </el-form-item>
        <el-form-item label="菜单状态" :label-width="60">
          <span class="require">*</span>
          <el-select v-model="form.status" placeholder="启用/禁用" size="medium">
            <el-option label="启用" value="0"></el-option>
            <el-option label="禁用" value="1"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false" class="normal-white-btn">取消</el-button>
        <el-button type="primary" @click="submitChange" class="light-green-btn">确定</el-button>
      </div>
    </el-dialog>
    <!-- 添加父菜单 -->
    <el-dialog title="添加父菜单" :visible.sync="addDictShow" width="30%">
      <el-form :model="addForm">
        <el-form-item label="菜单名称" :label-width="60">
          <span class="require">*</span>
          <el-input v-model="addForm.menuName" autocomplete="off" size="medium"></el-input>
        </el-form-item>
        <el-form-item label="菜单路径" :label-width="60">
          <span class="require">*</span>
          <el-input v-model="addForm.menuUrl" autocomplete="off" size="medium"></el-input>
        </el-form-item>
        <el-form-item label="菜单icon" :label-width="60">
          <el-input v-model="addForm.menuIcon" autocomplete="off" size="medium"></el-input>
        </el-form-item>
        <el-form-item label="菜单排序" :label-width="60">
          <el-input v-model="addForm.orderNum" autocomplete="off" size="medium"></el-input>
        </el-form-item>
        <el-form-item label="菜单状态" :label-width="60">
          <span class="require">*</span>
          <el-select v-model="addForm.status" placeholder="启用/禁用" size="medium">
            <el-option label="启用" value="0"></el-option>
            <el-option label="禁用" value="1"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addDictShow = false" class="normal-white-btn">取消</el-button>
        <el-button type="primary" @click="addSubmit" class="light-green-btn">确定</el-button>
      </div>
    </el-dialog>
    <!-- 添加子菜单 -->
    <el-dialog title="添加子菜单" :visible.sync="addChildShow" width="30%">
      <el-form>
        <el-form-item label="菜单名称" label-width="60"><span class="require">*</span>
          <el-input v-model="addForm.menuName" autocomplete="off" size="medium"></el-input>
        </el-form-item>
        <el-form-item label="菜单路径" label-width="60">
          <span class="require">*</span>
          <el-input v-model="addForm.menuUrl" autocomplete="off" size="medium"></el-input>
        </el-form-item>
        <el-form-item label="菜单icon" label-width="60">
          <el-input v-model="addForm.menuIcon" autocomplete="off" size="medium"></el-input>
        </el-form-item>
        <el-form-item label="菜单排序" label-width="60">
          <el-input v-model="addForm.orderNum" autocomplete="off" size="medium"></el-input>
        </el-form-item>
        <el-form-item label="菜单状态" label-width="60">
          <span class="require">*</span>
          <el-select v-model="addForm.status" placeholder="启用/禁用" size="medium">
            <el-option label="启用" value="0"></el-option>
            <el-option label="禁用" value="1"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addChildShow = false" class="normal-white-btn">取消</el-button>
        <el-button type="primary" @click="addChildSubmit" class="light-green-btn">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as systemAdminApi from "@/api/systemadmin.js"
import selectDropDownBox from "@/components/selectbox/selectDropDownBox.vue";
import { openUrlInNewWindow } from "@/libs/OpenHelper.js";
import { cursor } from "@/libs/element-table.js";

export default {
  name: "menuManager",
  components: { selectDropDownBox },
  data() {
    return {
      isAdd: false,
      reRender: false,
      addChildShow: false,
      searchDictName: "",
      searchDictCode: "",
      list: [],
      addForm: {
        menuName: "",
        menuUrl: "",
        menuIcon: "",
        orderNum: "",
        menuLevel: 1,
        parentId: 0,
        status: "",
      },
      total: 0,
      current: 1,
      pageCount: 1,
      levelNum: "",
      pageSize: 10,
      value: "",
      dialogFormVisible: false,
      dictList: [],
      dictShow: false,
      addDictShow: false,
      dict: {
        name: "",
        value: "",
      },
      row: {},
      form: {
        menuName: "",
        menuUrl: "",
        menuIcon: "",
        orderNum: null,
        menuLevel: 0,
        id: 0,
        status: "0",
        parentId: 0,
      },
      title: "",
    };
  },
  methods: {
    cellStyle({ row, rowIndex, column, columnIndex }) {
      if (column.label === "菜单名称") {
        return "padding-left: 20px";
      }
    },
    headerStyle({ row, rowIndex, column, columnIndex }) {
      if (columnIndex === 0) {
        return "" +
            "padding-left: 20px;" +
            "background: #F2F5F7;" +
            "border:0px solid #DDDDDD;" +
            "color:#242B35;" +
            "height:64px";
      } else {
        return "" +
            "background: #F2F5F7;" +
            "border:0px solid #DDDDDD;" +
            "color:#242B35;" +
            "height:64px";
      }
    },
    addFather() {
      this.addDictShow = true;
      this.addForm.menuLevel = 1;
      this.addForm.status = "0";
    },
    startStyleChange(status) {
      if (status === 0) {
        return "afterSubmitEdit";
      } else {
        return "list-green-text";
      }
    },
    banStyleChange(status) {
      console.log("status",status);
      if (status === 0) {
        return "list-red-text";
      } else {
        return "afterSubmitOffline";
      }
    },
    addDict() {
      this.addDictShow = true;
      this.form.dictName = "";
      this.form.id = "";
      this.form.dictCode = "";
      this.form.description = "";
      this.form.type = "";
    },
      nnnnaddSubmit() {
      this.addForm.parentId = 0;
      if (this.addForm.menuName && this.addForm.menuUrl && this.addForm.status) {
        if (!this.addForm.orderNum) {
          this.addForm.orderNum = this.list.length + 2;
        }
        if (!this.addForm.menuIcon) {
          this.addForm.menuIcon = "/";
        }
        systemAdminApi.addMenu(this.addForm).then(res => {
          this.$message.success("添加成功");
          this.addChildShow = false;
          this.getList(1);
        }).catch(err => {
          console.error(err.message);
        });
      } else {
        this.$message.warning("必填项不能为空");
      }
    },
    changeStatus(row, status) {
      const data = {
        id: row.id,
        status: status
      };
      systemAdminApi.editMenuStatus(data).then(res => {
        this.$message.success("修改成功");
        this.getList(1);
      }).catch(err => {
        this.$message.error(err.message);
      });
    },
    addChildSubmit() {
      if (this.addForm.menuName && this.addForm.menuUrl
          && this.addForm.menuLevel && this.addForm.status) {
        systemAdminApi.addMenu(this.addForm).then(res => {
          this.$message.success("添加成功");
          this.addChildShow = false;
        }).catch(err => {
          console.error(err.message);
        });
      } else {
        this.$message.warning("请输入必填项");
      }
    },
    delSingDict() {},
    onEdit(row, bool) {
      this.dialogFormVisible = bool;
      this.form.id = row.id;
      this.form.parentId = row.parentId;
      this.form.menuName = row.menuName;
      this.form.menuIcon = row.menuIcon;
      this.form.menuLevel = row.menuLevel;
      this.form.menuUrl = row.menuUrl;
      this.form.orderNum = row.orderNum;
      this.form.status = row.status;
      this.row = row;
    },
    submitChange() {
      if (this.form.menuIcon && this.form.menuName && this.form.menuUrl) {
        systemAdminApi.editMenu(this.form).then(res => {
          this.$message.success("修改成功");
          this.dialogFormVisible = false;
          location.reload();
        }).catch(err => {
          console.error(err.message);
        });
      } else {
        this.$message.warning("请输入必填项");
      }
    },
    onClickPublish() {},
    onClickDelete(row) {
      this.$confirm("是否删除此菜单项？").then(() => {
        systemAdminApi.delMenu(row.id).then(res => {
          this.$message.success("删除成功");
          location.reload();
        }).catch(err => {
          console.error(err.message);
        });
      }).catch(err => {
        console.error(err.message);
      });
    },
    addChild(row) {
      for (const i in this.addForm) {
        this.addForm[i] = null;
      }
      this.addForm.menuLevel = row.menuLevel + 1;
      this.row = row;
      this.addChildShow = true;
      this.addForm.parentId = row.id;
      this.addForm.menuUrl = row.menuUrl;
      this.addForm.status = "0";
      if (row.children) {
        this.addForm.orderNum = row.children.length + 1;
      } else {
        this.addForm.orderNum = 1;
      }
    },
    onClickOffline() {},
    handleSizeChange(val) {
      this.pageSize = val;
      this.getList(this.current);
    },
    handleCurrentChange(val) {
      this.current = val;
      this.getList(this.current);
    },
    loadSingleDictList(dictCode) {
      this.dictListShow = true;
      systemAdminApi.getSingleDictList(dictCode).then(res => {
        this.dictList = res;
      }).catch(err => {
        console.error(err.message);
      });
    },
    getList(page) {
      const params = {
        hidden: false,
        menuLevel: 1,
      };
      systemAdminApi.getMenuList(params).then(res => {
        this.list = res;
        this.total = Number(res.total);
        this.current = Number(res.current);
        this.pageCount = Math.ceil(parseInt(this.total) / this.pageSize);
        this.list.map(v => {
          v.menuType = v.menuType ? v.menuType : "菜单";
          if (v.status === 0) {
            v.statusName = "启用";
          } else {
            v.statusName = "禁用";
          }
          if (v.children) {
            v.children.map((value, index) => {
              value.menuType = "子菜单";
              value.menuIcon = value.menuIcon ? value.menuIcon : "--";
              if (value.status === 0) {
                value.statusName = "启用";
              } else {
                value.statusName = "禁用";
              }
              if (value.children) {
                value.children.map((s, i) => {
                  s.menuType = "子菜单";
                  s.menuIcon = s.menuIcon ? s.menuIcon : "--";
                  if (s.status === 0) {
                    s.statusName = "启用";
                  } else {
                    s.statusName = "禁用";
                  }
                });
              }
            });
          }
        });
      }).catch(err => {
        console.error(err.message);
      });
      console.log(this);
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

.iwu-pl-8 {
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

.require {
  color: red;
  font-size: 10px;
  position: relative;
  right: 8px;
  top: 2px;
}
</style>
