<template>
  <div class="pageContainer">
    <!-- 顶部Logo区域 -->
    <div class="top-logo">
      <img class="logo" :src="loginLogo" alt="平台Logo" />
    </div>
    <!-- 主体登录容器：左侧插画 + 右侧表单 -->
    <div class="loginContainer">
      <!-- 左侧插画（仅大屏显示） -->
      <img
        v-if="fullWidth > 768"
        class="loginLeftPic"
        :src="loginLeftPic"
        alt="登录插画"
      />
      <!-- 右侧登录表单容器 -->
      <div
        class="loginInputContainer"
        :class="{
          'container-small': fullWidth > 768,
          'container-big': fullWidth <= 768
        }"
      >
        <div class="index_from page-account-container">
          <!-- 表单顶部：公司图标 + 平台标题 -->
          <div class="page-account-top">
            <img class="company-icon" :src="loginCompanyIcon" alt="公司图标" />
            <h2 class="platform-title">碳中和资产管理平台</h2>
            <p class="platform-en">XCarbon Management Platform</p>
          </div>
          <!-- Element UI 表单 -->
          <el-form
            ref="loginForm"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            autocomplete="on"
            label-position="left"
          >
            <!-- 用户名输入框 -->
            <div class="login-input">
              <span class="txt1">用户名</span>
              <img
                class="label1"
                referrerpolicy="no-referrer"
                :src="require('@/assets/imgs/label1.jpg')"
                alt="用户名图标"
              />
              <el-input
                class="user-input"
                ref="account"
                v-model="loginForm.account"
                name="username"
                type="text"
                placeholder="请输入用户名"
                autocomplete="on"
                @keyup.enter.native="handleLogin"
              />
            </div>

            <!-- 密码输入框 + 显示/隐藏切换 -->
            <div class="login-input">
              <span class="txt1">密&nbsp;&nbsp;&nbsp;&nbsp;码</span>
              <img
                class="label1"
                referrerpolicy="no-referrer"
                :src="require('@/assets/imgs/label1.jpg')"
                alt="密码图标"
              />
              <el-input
                class="pwd-input"
                ref="pwd"
                v-model="loginForm.pwd"
                :type="passwordType"
                placeholder="请输入密码"
                name="pwd"
                autocomplete="on"
                @keyup.enter.native="handleLogin"
              >
                <i
                  slot="suffix"
                  class="el-icon-view show-pwd"
                  @click="showPwd"
                ></i>
              </el-input>
            </div>

            <!-- 记住我 -->
            <div class="section6">
              <el-checkbox
                label="记住我"
                @change="handleChecked"
                v-model="loginForm.rememberMe"
              ></el-checkbox>
            </div>

            <!-- 验证码
            <div class="captcha">
              <el-input
                ref="code"
                v-model="loginForm.code"
                style="width: 65%;"
                prefix-icon="el-icon-message"
                placeholder="请输入验证码"
                name="code"
                type="text"
                autocomplete="on"
                @keyup.enter.native="handleLogin"
              />
              <div
                class="captcha-img"
                style="margin-left: 10px"
                @click="getCaptcha()"
              >
                <img :src="captchImg" alt="验证码" />
              </div>
            </div> -->

            <!-- 登录按钮 -->
            <div class="btn-container">
              <el-button
                :loading="loading"
                class="login-btn"
                type="primary"
                @click="handleLogin"
              >
                登录
              </el-button>
            </div>

            <!-- 注册/忘记密码链接 -->
            <div class="link-group">
              <div class="register-link">
                <span>还没有账号？</span>
                <el-link type="success" @click="toRegister">去注册</el-link>
              </div>
              <el-link type="info" @click="toForgetPwd">忘记密码</el-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 引入项目工具/接口
import { validUsername } from "@/utils/validate";
import { captchaApi, login } from "@/api/user"; // 登录/验证码接口
import { getWXCodeByUrl, loginByWxCode } from "@/libs/wechat"; // 微信登录工具
import { getToken, setToken } from "@/utils/auth"; // Token存储工具
import Cookies from "js-cookie";
import { log } from 'console';

export default {
  name: "Login",
  data() {
    // 用户名校验规则
    const validateUsername = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请输入用户名"));
      } else if (!validUsername(value)) {
        callback(new Error("用户名格式错误（仅支持字母/数字）"));
      } else {
        callback();
      }
    };
    // 密码校验规则
    const validatePassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请输入密码"));
      } else if (value.length < 6 || value.length > 12) {
        callback(new Error("密码位数为6-12位"));
      } else {
        callback();
      }
    };
    // 验证码校验规则
    const validateCode = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请输入验证码"));
      } else if (value.length !== 4) {
        // 假设验证码为4位
        callback(new Error("验证码为4位字符"));
      } else {
        callback();
      }
    };

    return {
      // 图片资源（替换为项目实际路径）
      loginLogo: require("@/assets/imgs/login_logo.png"),
      captchImg: require("@/assets/imgs/no.png"), // 验证码默认图（无 captcha 图时临时用 logo 占位）
      loginLeftPic: require("@/assets/imgs/login_left_pic.png"),
      loginCompanyIcon: require("@/assets/imgs/icon_login_company.png"),

      // 响应式相关
      fullWidth: document.body.clientWidth,
      timer: null, // 防抖定时器

      // 表单数据
      loginForm: {
        account: Cookies.get("rememberAccount") || "", // 记住用户名（从Cookie读取）
        pwd: "",
        code: "",
        wxCode: "",
        rememberMe: !!Cookies.get("rememberAccount") // 记住我状态
      },

      // 表单校验规则
      loginRules: {
        account: [
          { required: true, trigger: "blur", validator: validateUsername }
        ],
        pwd: [{ required: true, trigger: "blur", validator: validatePassword }],
        code: [{ required: true, trigger: "blur", validator: validateCode }]
      },

      // 其他状态
      passwordType: "password", // 密码输入框类型
      loading: false, // 登录按钮加载状态
      redirect: this.$route.query.redirect || "/", // 登录后跳转地址
      otherQuery: this.getOtherQuery(this.$route.query) // 其他路由参数
    };
  },
  watch: {
    // 监听屏幕宽度变化（防抖处理）
    fullWidth(val) {
      if (!this.timer) {
        this.timer = true;
        setTimeout(() => {
          this.timer = false;
        }, 300);
      }
    },
    // 监听路由变化（更新跳转地址）
    $route(route) {
      this.redirect = route.query.redirect || "/";
      this.otherQuery = this.getOtherQuery(route.query);
    }
  },
  created() {
    // 1. 监听回车键登录（仅当前页面生效）
    const _this = this;
    document.addEventListener("keydown", function(e) {
      if (_this.$route.path === "/login" && e.keyCode === 13) {
        _this.handleLogin();
      }
    });
    // 2. 监听窗口 resize 事件
    window.addEventListener("resize", this.handleResize);
    // 3. 初始化验证码
    this.getCaptcha();
    // 4. 微信环境自动登录（按需启用）
    // this.agentWeiXinLogin()
  },
  mounted() {
    // 自动聚焦输入框（优先聚焦空字段）
    if (!this.loginForm.account) {
      this.$nextTick(() => {
        this.$refs.account.focus();
      });
    } else if (!this.loginForm.pwd) {
      this.$nextTick(() => {
        this.$refs.pwd.focus();
      });
    }
  },
  beforeDestroy() {
    // 移除事件监听（避免内存泄漏）
    document.removeEventListener("keydown", this.handleEnterLogin);
    window.removeEventListener("resize", this.handleResize);
  },
  methods: {
    // 处理屏幕 resize
    handleResize() {
      this.fullWidth = document.body.clientWidth;
    },
    // 密码显示/隐藏切换
    showPwd() {
      this.passwordType =
        this.passwordType === "password" ? "text" : "password";
      // 切换后重新聚焦
      this.$nextTick(() => {
        this.$refs.pwd.focus();
      });
    },
    // 记住我状态变更（存储用户名到Cookie）
    handleChecked() {
      if (this.loginForm.rememberMe) {
        // 记住：存储用户名到Cookie（有效期7天）
        Cookies.set("rememberAccount", this.loginForm.account, { expires: 7 });
      } else {
        // 不记住：删除Cookie
        Cookies.remove("rememberAccount");
      }
    },
    // 获取验证码
    getCaptcha() {
      captchaApi()
        .then(res => {
          // 假设接口返回 { data: '验证码图片Base64或URL', key: '验证码标识' }
          this.captchImg = res.data;
          this.loginForm.key = res.key; // 存储验证码标识（用于后端校验）
        })
        .catch(err => {
          ElMessageBox.alert("验证码获取失败，请重试", "提示", {
            type: "error"
          });
        });
    },
    // 跳转注册页（新窗口打开）
    toRegister() {
      const routeData = this.$router.resolve({ path: "/reg" });
      window.open(routeData.href, "_blank");
    },
    // 跳转忘记密码页（新窗口打开）
    toForgetPwd() {
      const routeData = this.$router.resolve({ path: "/forgetPassword" });
      window.open(routeData.href, "_blank");
    },
    // 提取路由中除redirect外的其他参数
    getOtherQuery(query) {
      const otherQuery = {};
      Object.keys(query).forEach(key => {
        if (key !== "redirect") {
          otherQuery[key] = query[key];
        }
      });
      return otherQuery;
    },
    // 微信公众号登录（按需启用）
    agentWeiXinLogin() {
      const isWechat = this.$wechat && this.$wechat.isWeixin(); // 需确保$wechat挂载到Vue原型
      if (!isWechat) return;

      const code = this.$route.query.code;
      const state = this.$route.query.state;
      const wxAuthPath = `${location.origin}/login`; // 微信授权回调地址

      // 1. 无code：跳转微信授权页获取code
      if (!code) {
        getWXCodeByUrl(wxAuthPath, "step1"); // 调用微信授权工具
        return;
      }

      // 2. 有code且state=step1：尝试微信自动登录
      if (state === "step1") {
        loginByWxCode(code)
          .then(res => {
            setToken(res.token); // 存储Token
            this.$router.push({ path: this.redirect, query: this.otherQuery });
          })
          .catch(() => {
            // 登录失败：重新获取code用于账号绑定
            getWXCodeByUrl(wxAuthPath, "step2");
          });
      } else if (state === "step2") {
        // 存储微信code用于后续绑定
        this.loginForm.wxCode = code;
      }
    },
    // 核心登录逻辑
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          // 调用登录接口
          login(this.loginForm)
            .then(res => {
              setToken(res.token); // 存储Token到Cookie/LocalStorage
              // 跳转首页或目标页面
              log(this.redirect);
              this.$router.push({
                path: this.redirect,
                query: this.otherQuery
              });
            })
            .catch(err => {
              this.loading = false;
              // 登录失败提示（如用户名密码错误、验证码错误）
              ElMessageBox.alert(err.message || "登录失败，请重试", "提示", {
                type: "error"
              });
              // 失败后刷新验证码
              this.getCaptcha();
            });
        }
      });
    }
  }
};
</script>

<style scoped>
/* 全局容器：全屏背景 + 居中布局 */
.pageContainer {
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #f5f7fa; /* 淡蓝灰色背景，贴近企业风格 */
  min-height: 100vh;
  padding: 30px 20px;
  box-sizing: border-box;
}

/* 顶部Logo */
.top-logo {
  margin-bottom: 25px;
}
.logo {
  width: 140px;
  height: auto;
}

/* 主体登录容器：白色背景 + 阴影 */
.loginContainer {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  width: 100%;
  max-width: 1100px; /* 最大宽度限制 */
}

/* 左侧插画：仅大屏显示 */
.loginLeftPic {
  width: 55%;
  height: 580px; /* 固定高度，避免拉伸 */
  object-fit: cover; /* 保持图片比例 */
}

/* 右侧表单容器：响应式适配 */
.loginInputContainer {
  flex: 1;
  padding: 50px 30px;
  box-sizing: border-box;
}
/* 大屏：表单容器限制宽度 */
.container-small {
  max-width: 420px;
  margin: 0 auto;
}
/* 小屏：表单占满宽度 */
.container-big {
  width: 100%;
  max-width: 500px;
}

/* 表单顶部：图标 + 标题 */
.page-account-top {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40px;
}
.company-icon {
  width: 70px;
  height: 70px;
  margin-bottom: 15px;
}
.platform-title {
  font-size: 22px;
  color: #2ec28b; /* 主色调：绿色 */
  margin: 0;
  font-weight: 500;
}
.platform-en {
  font-size: 13px;
  color: #999;
  margin: 8px 0 0;
}

/* 表单输入框组 */
.login-form {
  width: 100%;
}
.login-input {
  position: relative;
  margin-bottom: 22px;
}
.txt1 {
  display: inline-block;
  width: 70px;
  text-align: right;
  margin-right: 12px;
  color: #666;
  font-size: 14px;
  line-height: 40px; /* 与输入框高度对齐 */
}
.label-icon {
  width: 18px;
  height: 18px;
  position: absolute;
  left: 85px;
  top: 11px;
  z-index: 1;
}
/* 用户名/密码输入框 */
.user-input,
.pwd-input {
  padding-left: 45px !important; /* 给图标留空间 */
  height: 40px !important;
  border-radius: 6px !important;
}
/* 密码显示图标 */
.show-pwd {
  cursor: pointer;
  color: #999;
  font-size: 16px;
}

/* 记住我 */
.section6 {
  margin: -10px 0 20px 82px; /* 与输入框对齐 */
}
.section6 .el-checkbox__label {
  color: #666;
  font-size: 13px;
}

/* 验证码 */
.captcha {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
}
.captcha-img {
  flex: 1;
  height: 40px;
  overflow: hidden;
  border-radius: 6px;
  cursor: pointer;
}
.captcha-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 登录按钮 */
.btn-container {
  margin-bottom: 20px;
}
.login-btn {
  width: 100%;
  height: 44px !important;
  background-color: #2ec28b !important;
  border-color: #2ec28b !important;
  border-radius: 6px !important;
  font-size: 16px !important;
}
.login-btn:hover {
  opacity: 0.9 !important;
}

/* 注册/忘记密码链接 */
.link-group {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}
.register-link span {
  color: #666;
  margin-right: 5px;
}
.register-link .el-link--success {
  color: #2ec28b !important;
}
.link-group .el-link--info {
  color: #666 !important;
}
.link-group .el-link {
  text-decoration: none !important;
}
.link-group .el-link:hover {
  text-decoration: underline !important;
}

/* 小屏适配：隐藏左侧插画 + 调整间距 */
@media screen and (max-width: 768px) {
  .loginContainer {
    box-shadow: none;
    background: transparent;
  }
  .page-account-top {
    margin-bottom: 30px;
  }
  .section6 {
    margin-left: 0;
    text-align: center;
  }
}
</style>
