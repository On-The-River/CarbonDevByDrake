<template>
  <div class="root">
    <div class="divBox">
      <div class="content-container">
        <div class="container">
          <div style="width: 300px" class="selectbox-oot">
            <span class="selector-hint" style="width: 0px"> 搜索</span>
            <div class="selector-0liver" />
            <el-input
                v-model="searchWord"
                placeholder="输入名称"
                clearable
                size="medium"
                @keyup.enter.native="searchUser"
            />
          </div>
          <!-- <div style="float: right" /> -->
          <button
              style="margin-left: 10px"
              class="light-green-btn"
              @click="searchUser"
          >
            查询
          </button>
        </div>
        <div>
          <button
              style="margin-top: 0px; margin-bottom: 20px; width: 100px"
              class="normal-white-btn"
              @click="addUser"
          >
            添加角色
          </button>
          <el-table
              :header-cell-style="{
            background: '#F2F5F7',
            border: '1px solid #000000',
            color: '#626585',
            height: '46px',
          }"
              show-header="true"
              :data="list"
              stripe
              :style="{ height: '40px' }"
              :cell-style="cellStyle"
              style="width: 100%"
          >
            <!-- <el-table-column label="" align="center">
              <template slot-scope="column">
                <el-checkbox v-model="column.checked" :indeterminate="indeterminateFlag" :checked="allchecked" label="全选" @change="updateAllSelected"></el-checkbox>
              </template>
              <template slot-scope="scope">
                <el-checkbox v-model="scope.row.checked" @change="checkChange"></el-checkbox>
              </template>
            </el-table-column> -->
            <el-table-column min-width="10%"></el-table-column>
              <el-table-column label="序号" align="left" min-width="60">
                <template slot-scope="scope">
                  <span>{{ getCurListNo(scope.$index) }}</span>
                </template>
              </el-table-column>
              <el-table-column
                  :show-overflow-tooltip="true"
                  align="left"
                  prop="roleName"
                  label="角色昵称"
                  min-width="100"
              />
              <el-table-column
                  align="left"
                  prop="roleCode"
                  label="角色编码"
                  min-width="100"
              />
              <el-table-column
                  align="left"
                  prop="statusName"
                  label="状态"
                  min-width="90"
              />
              <el-table-column
                  align="left"
                  prop="createdTime"
                  label="创建时间"
                  min-width="100"
              />
              <el-table-column
                  align="left"
                  prop="updatedTime"
                  label="更新时间"
                  min-width="100"
              />
              <el-table-column label="操作" min-width="130" fixed="right">
                <template slot-scope="scope">
                  <template v-if="scope.row.isDel">
                    <span></span>
                  </template>
                  <template v-else>
                    <a
                        size="mini"
                        @click="editUser(scope.row)"
                        class="list-blue-text"
                    >编辑</a>
                    <a
                        size="mini"
                        style="margin-left: 10px"
                        @click="delUser(scope.row.id)"
                        class="list-red-text"
                    >删除</a>
                  </template>
                </template>
              </el-table-column>
          </el-table>
        </div>
        <div style="margin-top: 30px; margin-bottom: 10px" class="pageBox">
          <div style="flex-grow: 1" />
          <a style="margin: auto" class="pageBox-total-num">共{{ total }}条</a>
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
        <!-- Form -->
        <el-drawer
            title="角色权限配置"
            :visible.sync="dialogFormVisible"
            style="font-size: 16px"
        >
          <div class="my_table">
            <el-tree
                :data="data"
                show-checkbox
                node-key="id"
                :default-expanded-keys="[2, 3]"
                :default-checked-keys="userMenu"
                :props="defaultProps"
                ref="tree"
            />
          </div>
          <el-divider></el-divider>
          <button
              style="
            margin-top: 20px;
            margin-left: 120px;
            margin-bottom: 20px;
            width: 80px;
          "
              class="normal-white-btn"
              @click="dialogFormVisible = false"
          >
            取消
          </button>
          <button
              style="
            margin-top: 20px;
            margin-left: 15px;
            margin-bottom: 20px;
            width: 80px;
          "
              class="light-green-btn"
              @click="saveChange"
          >
            保存
          </button>
          <!-- <button
            style="
              margin-top: 20px;
              margin-left: 15px;
              margin-bottom: 20px;
              width: 108px;
            "
            class="light-green-btn"
            @click="saveChangeAndExit"
          >
            保存并关闭
          </button> -->
        </el-drawer>
        <!-- 添加身份 -->
        <el-dialog title="添加角色" :visible.sync="addUserShow" width="500px">
          <el-form :model="addForm" :label-position="left">
            <el-form-item label="角色名称">
              <el-input
                  v-model="addForm.roleName"
                  size="medium"
                  style="width: 350px"
              />
            </el-form-item>
            <el-form-item label="角色编码">
              <el-input
                  v-model="addForm.roleCode"
                  size="medium"
                  style="width: 350px"
              />
            </el-form-item>
            <el-form-item label="角色状态">
              <el-select
                  v-model="addForm.status"
                  placeholder="请选择角色状态"
                  size="medium"
              >
                <el-option label="启用" value="0"></el-option>
                <el-option label="禁用" value="1"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="角色描述">
              <el-input
                  v-model="addForm.remarks"
                  type="textarea"
                  style="width: 350px"
              />
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button @click="addUserShow = false" class="normal-white-btn">取消</el-button>
            <el-button type="primary" @click="addSubmit" class="light-green-btn">确定</el-button>
          </div>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script>
import * as systemAdminApi from "@/api/systemadmin.js"
import * as roleApi from "@/api/role.js"
import selectDropDownBox from "@/components/selectbox/selectDropDownBox.vue";
import { setListNo } from "@/libs/public.js";

export default {
  name: "companyPackage",
  components: { selectDropDownBox },
  data() {
    return {
      isAdd: false,
      reRender: true,
      searchKeyWord: "",
      addUserShow: false,
      list: [],
      userMenu: [],
      addForm: {
        roleName: "",
        roleCode: "",
        status: "",
        remarks: "",
      },
      data: [],
      defaultProps: {
        children: "children",
        label: "menuName",
      },
      total: 0,
      current: 1,
      pageCount: 1,
      pageSize: 10,
      value: "",
      dialogFormVisible: false,
      editForm: {
        roleId: 0,
        menuIds: [],
      },
      title: "",
      menuList: [],
      roleMenu: [],
      multipleSelection: [],
    };
  },
  methods: {
    cellStyle(data) {},
    onClickSearch() {},
    onEdit() {},
    saveChange() {
      const data = this.$refs.tree.getCheckedKeys();
      this.$message({
        type: "success",
        duration: 500,
        message: "权限配置中...",
      });
      this.editForm.menuIds.length = 0;
      this.editForm.menuIds = data;
      systemAdminApi.editRolePermissions(this.editForm).then(res => {
        this.$message.success("保存成功");
        let deleteElement = "roleMenu" + this.editForm.roleId;
        sessionStorage.removeItem(deleteElement);
        sessionStorage.setItem(
            "roleMenu" + this.editForm.roleId,
            JSON.stringify(this.editForm.menuIds)
        );
        this.dialogFormVisible = false;
      }).catch(err => {
        console.error("保存失败: ", err.message);
      });
    },
    clickCheck(currentRow) {
      if (currentRow.menuLevel === 1) {
        currentRow.children.map(value => {
          value.checked = currentRow.checked;
          if (value.children) {
            value.children.map(i => {
              i.checked = currentRow.checked;
            });
          }
        });
      } else if (currentRow.menuLevel === 2) {
        this.menuList.map(s => {
          if (s.id === currentRow.parentId) {
            if (currentRow.checked) {
              s.checked = true;
            }
          }
        });
        if (currentRow.children) {
          currentRow.children.map(value => {
            value.checked = currentRow.checked;
            if (value.children) {
              value.children.map(i => {
                i.checked = currentRow.checked;
              });
            }
          });
        }
      } else if (currentRow.menuLevel === 3) {
        if (currentRow.checked) {
          this.menuList.map(value => {
            if (value.children) {
              value.children.map(i => {
                if (i.id === currentRow.parentId) {
                  i.checked = true;
                  value.checked = true;
                }
              });
            }
          });
        }
      }
    },
    handle() {},
    onClickPublish() {},
    onClickDelete() {},
    onClickOffline() {},
    handleSizeChange(val) {
      this.pageSize = val;
      this.getList(this.current);
    },
    handleCurrentChange(val) {
      this.current = val;
      this.getList(this.current);
    },
    getUserMenu(data) {
      this.userMenu = [];
      if (data) {
        data.map(item => {
          this.userMenu.push(Number(item.value));
        });
        this.$refs.tree.getCheckedKeys(this.userMenu);
      }
    },
    editUser(row) {
      this.dialogFormVisible = true;
      this.userMenu.length = 0;
      this.editForm.roleId = row.id;
      const menu = sessionStorage.getItem("roleMenu" + row.id);
      roleApi.getRoleMenu(row.id).then(res => {
        this.getUserMenu(res);
        sessionStorage.setItem(
            "roleMenu" + this.editForm.roleId,
            JSON.stringify(this.userMenu)
        );
      }).catch(err => {
        console.log(err.message);
      });
    },
    submit() {
      if (this.isAdd) {
        roleApi.addRole(this.form).then(res => {
          this.$message.success("添加成功");
          this.dialogFormVisible = false;
          this.getList(this.current);
        }).catch(err => {
          console.error(err.message);
        });
      } else {
        roleApi.updateRole(this.form).then(res => {
          this.$message.success("修改成功");
          this.dialogFormVisible = false;
          this.getList(this.current);
        }).catch(err => {
          console.error(err.message);
        });
      }
    },
    addUser() {
      this.addUserShow = true;
    },
    addSubmit() {
      if (this.addForm.roleName && this.addForm.roleCode) {
        if (!this.addForm.status) {
          this.addForm.status = 0;
        }
        roleApi.addRole(this.addForm).then(res => {
          this.$message.success("添加成功");
          this.dialogFormVisible = false;
          this.getList(1);
        }).catch(err => {
          console.error(err.message);
        });
      }
    },
    delUser(id) {
      this.$confirm("是否删除此角色？").then(() => {
        roleApi.delRole(id).then(res => {
          this.$message.success("删除成功");
          this.getList(this.current);
        }).catch(err => {
          console.error(err.message);
        });
      }).catch((err) => {
        console.error(err.message);
      });
    },
    getCurListNo(index) {
      let curNo = parseInt(index) + 1;
      let size = this.size || 10;
      let page = this.current - 1;
      let no = setListNo(page, size, curNo);
      return no ? no : 1;
    },
    searchUser() {
      const params = {
        asc: true,
        current: 1,
        size: this.pageSize,
        roleName: this.searchKeyWord,
        sortField: "",
        status: "",
      };
      roleApi.getRoleList(params).then(res => {
        this.list = res.records;
        this.total = Number(res.total);
        this.current = Number(res.current);
        this.pageCount = Math.ceil(parseInt(this.total) / this.pageSize);
        this.list.map(v => {
          v.id = v.id ? v.id : "--";
          v.roleName = v.roleName ? v.roleName : "--";
        });
      }).catch(err => {
        console.error(err.message);
      })
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    saveChangeAndExit() {},
    getList(page) {
      const params = {
        asc: true,
        current: page,
        size: this.pageSize,
      };
      roleApi.getRoleList(params).then(res => {
        this.list = res.records;
        this.total = Number(res.total);
        this.current = Number(res.current);
        this.pageCount = Math.ceil(parseInt(this.total) / this.pageSize);
        this.list.map(v => {
          for (const i in v) {
            v[i] = v[i] ? v[i] : "--";
            if (v[i] === " ") {
              v[i] = "--";
            }
          }
          if (v.status === 0) {
            v.statusName = "启用";
          } else {
            v.statusName = "禁用";
          }
        });
      }).catch(err => {
        console.error(err.message);
      });
      systemAdminApi.getMenuList({menuLevel: 1}).then(res => {
        this.menuList = res;
      }).catch(err => {
        console.error(err.message);
      });
    }
  },
  created() {},
  mounted() {
    this.getList(0);
    systemAdminApi.getMenuList({}).then(res => {
      this.data = res;
    })
  }
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
  box-shadow: 0px 2px 5px 0px #eaf0f3;
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

.ivu-p1-8 {
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

.my_table :deep(.el-table__row > td) {
  border: none;
}
</style>