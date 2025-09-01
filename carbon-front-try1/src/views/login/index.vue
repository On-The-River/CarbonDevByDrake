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
import {captchaApi, getLoginPicApi, login} from "@/api/user"; // 登录/验证码接口
import { getWXCodeByUrl, loginByWxCode } from "@/libs/wechat"; // 微信登录工具
import { getToken, setToken } from "@/utils/auth"; // Token存储工具
import Cookies from "js-cookie";
import { MessageBox } from "element-ui";
import LoginLogo from '../../assets/imgs/login_logo.png'
import CaptchaImg from '../../assets/imgs/no.png'
import LoginLeftPic from  '../../assets/imgs/login_left_pic.png'
import LoginCompanyIcon from  '../../assets/imgs/icon_login_company.png'
import SvgIcon from "@/components/FormGenerator/components/SvgIcon/index.vue";

export default {
  name: "Login",
  components: {SvgIcon},
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!validUsername(value)) {
        callback(new Error('请输入正确的用户名'));
      } else {
        callback();
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6 || value.length > 12) {
        callback(new Error('密码为6-12位'));
      } else {
        callback();
      }
    }
    return {
      loginLogo: LoginLogo,
      captchaImg: CaptchaImg,
      loginLeftPic: LoginLeftPic,
      loginCompanyIcon: LoginCompanyIcon,
      swiperList: [],
      fullWidth: document.body.clientWidth,
      swiperOption: {
        pagination: {
          el: ".pagination",
        },
        autoplay: {
          enabled: true,
          disableOnInteraction: false,
          delay: 3000
        }
      },
      loginForm: {
        account: '',
        pwd: '',
        key: '',
        code: '',
        wxCode: '',
        rememberMe: false
      },
      loginRules: {
        account: [{ required: true, trigger: 'blur' }],
        pwd: [{ required: true, trigger: 'blur', validator: validatePassword }],
        code: [{ required: true, trigger: 'blur', message: '请输入正确的验证码' }]
      },
      passwordType: "password",
      capsTooltip: false,
      loading: false,
      showDialog: false,
      redirect: undefined,
      otherQuery: {},
      keydownHandler: null
    };
  },
  watch: {
    fullWidth(val) {
      if (!this.timer) {
        this.screenWidth = val;
        this.timer = true;
        const that = this;
        setTimeout(function () {
          that.timer = false;
        }, 400)
      }
    },
    $route: {
      handle: function (route) {
        const query = route.query;
        if (query) {
          this.redirect = query.redirect;
          this.otherQuery = query.otherQuery;
        }
      },
      immediate: true
    }
  },
  created() {
    const _this = this;
    this.keydownHandler = function (event) {
      if (_this.$route && _this.$route.path && _this.$route.path.indexOf("login") !== -1) {
        const key = event.key;
        if (key === "13" || key === 'Enter') {
          _this.handleLogin();
        }
      }
    };
    document.onkeydown = this.keydownHandler;
    window.addEventListener("resize", this.handleResize)
  },
  mounted() {
    if (this.loginForm.account === '') {
      this.$refs.account.focus();
    } else if (this.loginForm.pwd === '') {
      this.$refs.pwd.focus();
    }
  },
  beforeUnmount() {
    if (this.keydownHandler) {
      document.onkeydown = null;
    }
    window.removeEventListener('resize', this.handleResize);
  },
  destroyed() {},
  beforeDestroy() {
    this.beforeUnmount();
  },
  methods: {
    handleChecked() {
      this.rememberMe = !this.rememberMe;
    },
    clickReg() {
      let routeData = this.$route.resolve({ path: 'register' });
      window.open(routeData.href, '_blank');
    },
    clickForgetPassword() {
      let routeData = this.$route.resolve({ path: 'forgetPassWord' });
      window.open(routeData.href, '_blank');
    },
    agentWeiXinLogin() {
      /*const _isWechat = this.$wechat.isWeixin();
      if (_isWechat) {
        let code = this.$route?.query?.code;
        let state = this.$route?.query?.state;
        let wxAuthPath = location.origin + '/login';
        if (code === null) {
          getWXCodeByUrl(wxAuthPath, 'step1');
        }
        if (state === 'step1') {
          loginByWxCode(code).then(res => {
            sessionStorage.setItem('token', res.token);
            this.$router.push({
              path: this.redirect || '/',
              query: this.otherQuery
            });
          }).catch(() => {
            getWXCodeByUrl(wxAuthPath, 'step2');
          });
        } else if (state === 'step2') {
          this.loginForm.wxCode = code;
        }
      }*/
    },
    onWechat() {
      /*let url = this.$router.query.redirect ? this.$router.query.redirect : '/dashboard';
      this.$wechat.onAuth(url, 'login');*/
    },
    handleResize() {
      this.fullWidth = document.body.clientWidth;
    },
    getInfo() {
      getLoginPicApi().then(res => {
        this.swiperList = res.banner;
        this.loginLogo = res.logo;
        Cookies.set('MerInfo', JSON.stringify(res));
      })
    },
    clickCapslock(e) {
      const { key } = e;
      this.capsTooltip = key && key.length === 1 && key >= 'A' && key <= 'Z';
    },
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = '';
      } else {
        this.passwordType = 'password';
      }
      this.$nextTick(() => {
        this.$refs.pwd.focus();
      })
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          this.$store.dispatch('user/login', this.loginForm).then(() => {
            console.log("login succeed");
            this.$router.push({ path: this.redirect || '/', query: this.otherQuery });
            if (this.loginForm.rememberMe) {
              localStorage.setItem('login-form', JSON.stringify(this.loginForm));
            }
            this.loading = false;
          }).catch((error) => {
            console.log("login failed");
            this.loading = false;
            if (error && error.status === 40006) {
              this.$router.push({ path: '/loginErrSwiper' });
            } else if (error && error.status === 40005) {
              this.$router.push({ path: '/loginErrSwiper' });
            } else {
              this.$router.push({ path: this.redirect || '/', query: this.otherQuery });
            }
          })
        } else {
          return false;
        }
      })
    },
    getCaptcha() {
      captchaApi().then(data => {
        this.captchaImg = data.code;
        this.loginForm.key = data.key;
      }).catch(message => {
        this.$message.info(message);
      });
    },
    getOtherQuery(query) {
      return Object.keys(query).reduce((acc, cur) => {
        if (cur === 'redirect') {
          acc[cur] = query[cur];
        }
        return acc;
      }, {});
    }
  }
}
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
