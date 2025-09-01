<template>
  <div>
    <el-form ref="pran" :model="pran" :rules="rules" label-width="100px" @submit.native.prevent>
      <el-form-item label="管理员账号" prop="account">
        <el-input v-model="pran.account" placeholder="管理员账号" />
      </el-form-item>
      <el-form-item label="管理员密码" prop="pwd">
        <el-input
            v-model="pran.pwd"
            placeholder="管理员密码"
            clearable
            @input="handlerPwdInput"
            @clear="handlerPwdInput"
        />
      </el-form-item>
      <el-form-item v-if="pran.pwd" label="确认密码" prop="repwd">
        <el-input v-model="pran.repwd" placeholder="确认密码" clearable />
      </el-form-item>
      <el-form-item label="管理员姓名" prop="realName">
        <el-input v-model="pran.realName" placeholder="管理员姓名" />
      </el-form-item>
      <el-form-item label="管理员身份" prop="roles">
        <el-select v-model="pran.roles" placeholder="身份" clearable multiple>
          <el-option
              v-for="{item, index} in roleList.list"
              :key="index"
              :label="item.roleName"
              :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="pran.status" :active-value="true" :inactive-value="false" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handlerSubmit('pran')">{{ isCreate ? '确定' : '更新' }}</el-button>
        <el-button @click="close">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import * as roleApi from "@/api/role.js"
import * as systemAdminApi from "@/api/systemadmin.js"

export default {
  components: {},
  props: {
    isCreate: {
      type: Number,
      required: true,
    },
    editData: {
      type: Object,
      default: () => {
        return { rules: [] }
      }
    },
    data() {
      const validatePass = (rule, value, callback) => {
        if (value === "") {
          callback(new Error("请再输入密码"));
        } else if (value !== this.pram.pwd) {
          callback(new Error("两次密码不一致"));
        } else {
          callback();
        }
      }
      return {
        constants: this.$constants,
        pram: {
          account: null,
          level: null,
          pwd: null,
          repwd: null,
          realName: null,
          roles: [],
          status: null,
          id: null,
        },
        roleList: [],
        rules: {
          account: [{
            required: true,
            message: "请填写管理员账号",
            trigger: ["blur", "change"],
          }],
          pwd: [{
            required: true,
            message: "请填写管理员密码",
            trigger: ["blur", "change"],
          }],
          repwd: [{
            required: true,
            message: "请确认密码",
            validator: validatePass,
            trigger: ["blur", "change"],
          }],
          realName: [{
            required: true,
            message: "管理员姓名",
            trigger: ["blur", "change"],
          }],
          roles: [{
            required: true,
            message: "管理员身份",
            type: "array",
            trigger: ["blur", "change"],
          }],
        }
      }
    },
  },
  methods: {
    close() {
      this.$emit("hideEditDialog");
    },
    handleGetRoleList() {
      const params = {
        page: 1,
        limit: this.constants.page.limit[4],
        status: 1,
      };
      roleApi.getRoleList(params).then(res=>{
        this.roleList = res;
      }).catch(err => {
        console.error(err.message);
      });
    },
    initEditData() {
      if (this.isCreate !== 1) return;
      const { account, level, realName, roles, status, id } = this.editData;
      this.pram.account = account;
      this.pram.realName = realName;
      const roles_tmp = [];
      if (roles.length > 0 && !roles.includes(',')) {
        roles_tmp.push(Number.parseInt(roles));
      } else {
        roles_tmp.push(...roles.split(',').map(item => Number.parseInt(item)));
      }
      this.pram.roles = roles_tmp;
      this.pram.status = status;
      this.pram.id = id;
      this.pram.pwd = [];
      this.pram.repwd = [];
    },
    handlerSubmit(form) {
      this.$refs[form].validate((valid) => {
        if (!valid) return;
        if (this.isCreate === 0) {
          this.handlerSave();
        } else {
          this.handlerEdit();
        }
      });
    },
    handlerSave() {
      systemAdminApi.adminAdd(this.pram).then(res => {
        this.$message.success("创建成功");
        this.$emit("hideEditDialog");
      }).catch(err => {
        console.log(err.message);
      });
    },
    handlerEdit() {
      this.pram.roles = this.pram.join(',');
      systemAdminApi.adminUpdate(this.pram).then(res => {
        this.$message.success("更新成功");
        this.$emit("hideEditDialog");
      }).catch(err => {
        console.error(err.message);
      })
    },
    rulesSelect(selectKey) {
      this.pram.rules = selectKey;
    },
    handlerPwdInput(val) {
      if (!val) {
        this.rules.pwd = [];
        this.rules.repwd = [];
        return;
      }
      this.rules.pwd = [
        { required: true, message: "请输入管理员密码", trigger: ["blur", "change"] },
        { min: 6, max: 20, message: "长度为6-20个字符", trigger: ["blur", "change"] },
      ];
      this.rules.repwd = [{
        required: true, message: "两次输入密码不一致", trigger: ["blur", "change"],
        validator: (rule, value, callback) => {
          if (value !== this.pram.pwd) {
            callback(new Error("两次输入密码不一致"));
          } else {
            callback();
          }
        }
      }];
    }
  },
  created() {},
  mounted() {
    this.initEditData();
    this.handleGetRoleList();
  }
}

</script>

<style scoped/>
