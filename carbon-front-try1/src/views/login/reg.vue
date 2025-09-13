<template>
  <div class="login-container">
    <div class="video-overlay-container">
      <video autoplay loop muted class="background-video">
        <source src="@/assets/videos/shu1.mp4" type="video/mp4">
        您的浏览器不支持视频播放。
      </video>
      <div class="video-overlay"></div>
    </div>
    <div>
      <img class="logo" :src="loginLogo" />
    </div>
    <div class="loginContiner">
      <div class="loginInputContainer" :class="[ fullWidth > 768 ? 'containerSamll':'containerBig']">
        <div class="login-form">
          <div v-show="regShow">
            <el-form :model="regForm" ref="regForm" label-position="left">
              <div class="login-input" style="margin-top: 0px;">
                <span class="txt1">用&nbsp;户&nbsp;名</span>
                <img class="label1" referrerpolicy="no-referrer" src="@/assets/imgs/label1.jpg" />
                <input class="user-input" ref="accountName" v-model="regForm.accountName" name="accountName" type="text"
                       placeholder="请输入用户名" />
              </div>

              <div class="login-input">
                <span class="txt1">手&nbsp;机&nbsp;号</span>
                <img class="label1" referrerpolicy="no-referrer" src="@/assets/imgs/label1.jpg" />
                <input class="user-input" ref="mobile" v-model="regForm.mobile" name="mobile" type="number"
                       placeholder="请输入11位手机号" @mouseover="getIsAccountName" />
              </div>

              <div class="login-input">
                <span class="txt1">邮&nbsp;箱</span>
                <img class="label1" referrerpolicy="no-referrer" src="@/assets/imgs/label1.jpg" />
                <input class="user-input" ref="email" v-model="regForm.email" name="email" type="text"
                       placeholder="请输入邮箱地址" />
              </div>


              <div class="login-input">
                <span class="txt1">验&nbsp;证&nbsp;码</span>
                <img class="label1" referrerpolicy="no-referrer" src="@/assets/imgs/label1.jpg" />
                <input class="user-input" ref="verificationCode" v-model="regForm.verificationCode"
                       name="verificationCode" type="text" placeholder="请输入验证码" />
                <button v-show="sendAuthCode" style="cursor:pointer" class="codeBtn"
                        @click.prevent="handlecode" @mouseover="verifyDefaultEmailFormat">获取验证码</button>
                <button v-show="!sendAuthCode" class="codeBtn" disabled>{{auth_time}}s</button>
              </div>



              <div class="login-input">
                <span class="txt1">设置密码</span>
                <img class="label1" referrerpolicy="no-referrer" src="@/assets/imgs/label1.jpg" />
                <input class="pwd-input" :key="passwordType" :type="passwordType" ref="password" clearable
                       v-model="regForm.password" placeholder="请输入6-16位密码" name="password" />
              </div>

              <div class="login-input">
                <span class="txt1">确认密码</span>
                <img class="label1" referrerpolicy="no-referrer" src="@/assets/imgs/label1.jpg" />
                <input class="pwd-input" :key="passwordType" ref="confirmPassword" :type="passwordType" clearable
                       v-model="regForm.confirmPassword" placeholder="请输入确认密码" name="confirmPassword" />
              </div>

              <div class="btn_reg">
                <el-button :class="setButtonClass()" type="success"
                           style="width:100%;margin-top:30px;background:#2EC28B;height:50px;font-size: 15px;" :loading="loading"
                           @click.prevent="handleNext">注册</el-button>
              </div>
              <!-- <div>
            <el-link @click="clickLogin">已有账号</el-link>
          </div> -->
            </el-form>
          </div>
          <div class="hasaccount">
            <a class="acss">已有账户，</a>
            <el-link type="success" href="/" @click="handleLogin">去登陆 ></el-link>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
// import { checkPhone, checkPassword, checkEmail } from './../../filters/index'
import { register, login, logout,regEmailCode, regCode ,getAuthCenterAuthVerify,verifyEmail} from '@/api/user'
import { MessageBox } from 'element-ui'
import md5 from 'js-md5'
// import shajs from 'sha.js'

export default {
  components: {},
  name: 'reg',
  data() {
    // let verificationCode = (rule, value, callback) => {
    //   if (!value) {
    //     return callback(new Error('验证码不能为空'))
    //   } else if (value.length > 6) {
    //     return callback(new Error('请输入六位验证码'))
    //   } else {
    //     callback()
    //   }
    // }
    // const companyName = (rule, value, callback) => {
    //   if (!value) {
    //     return callback(new Error('公司名不能为空'))
    //   } else {
    //     callback()
    //   }
    // }
    // const userName = (rule, value, callback) => {
    //   if (!value) {
    //     return callback(new Error('名称不能为空'))
    //   } else {
    //     callback()
    //   }
    // }
    // const confirmPassword = (rule, value, callback) => {
    //   if (!value) {
    //     callback(new Error('密码不能为空'))
    //   } else if (value !== this.regForm.password) {
    //     callback(new Error('两次输入的密码不一致'))
    //   } else {
    //     callback()
    //   }
    // }

    return {
      regForm: {
        mobile: '',
        verificationCode: '',
        password: '',
        confirmPassword: '',
        company: '',
        accountName: '',
        email: '',
        verifyAccountName:'',
        verifyEmailName:''
      },
      loginRules: {
        // mobile: [{ required: true, trigger: 'blur', validator: checkPhone }],
        // verificationCode: [
        //   { required: true, trigger: 'blur', validator: verificationCode }
        // ],
        // password: [
        //   { required: true, trigger: 'blur', validator: checkPassword }
        // ],
        // confirmPassword: [
        //   { required: true, trigger: 'blur', validator: confirmPassword }
        // ]
      },
      regFormInfo: {
        // email: [{ required: true, trigger: 'blur', validator: checkEmail }],
        // company: [{ required: true, trigger: 'blur', validator: companyName }],
        // username: [{ required: true, trigger: 'blur', validator: userName }]
      },

      passwordType: 'password',
      loading: false,
      showDialog: false,
      regShow: true,
      nextShow: false,
      sendAuthCode: true,
      auth_time: 60,
      loginLogo: require('@/assets/videos/logol.png'),
    }
  },
  methods: {

    getAuthCode: function() {
      this.sendAuthCode = false
      this.auth_time = 60
      var timetimer = setInterval(() => {
        this.auth_time--
        if (this.auth_time <= 0) {
          this.sendAuthCode = true
          clearInterval(timetimer)
        }
      }, 1000)
    },
    // showPwd() {
    //   if (this.passwordType === 'password') {
    //     this.passwordType = ''
    //   } else {
    //     this.passwordType = 'password'
    //   }
    // },
    // 手机号格式
    verifyPhoneNumberFormat(str){
      var myreg = /^1[3-9]\d{9}$/;   //1开头，第二位3-9，供11位
      return myreg.test(str);
    },
    // 验证邮箱格式
    verifyEmailFormat(email) {
      const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      return emailReg.test(email);
    },
    // verifyDefaultEmailFormat() {
    //   return this.verifyEmailFormat(this.regForm.email);
    // },
    verifyDefaultEmailFormat() {
      // 验证邮箱的
      if(!this.regForm.email || (this.regForm.email == this.verifyEmailName)){
        return
      }
      let param = this.regForm.email;
      verifyEmail(param).then((res)=>{
        if(res && res.code == 200){
          this.$message.error("邮箱已被注册，请重新输入邮箱")
        }else{
          this.verifyEmailName = this.regForm.email
        }
      })
    },
    getIsAccountName(){
      // 验证用户名的
      if(!this.regForm.accountName || (this.regForm.accountName == this.verifyAccountName)){
        return
      }
      let param = this.regForm.accountName;
      getAuthCenterAuthVerify(param).then((res)=>{
        if(res && res.code == 200){
          this.$message.error("用户名已被注册，请重新输入用户名")
        }else{
          this.verifyAccountName = this.regForm.accountName
        }
      })
    },
    handlecode() {

      if (this.regForm.mobile !== '') {
        let is_phone = this.verifyPhoneNumberFormat(this.regForm.mobile);
        let is_email = this.verifyEmailFormat(this.regForm.email);
        if (!is_phone) {
          return this.$message.error('手机格式错误！')
        }
        if (!is_email) {
          return this.$message.error('邮箱格式错误！')
        }
        this.getAuthCode()
        //   regCode(this.regForm.email).then((res, err) => {
        //     debugger
        //     if (err) {
        //        this.$message.error(error)
        //     } else {
        //       this.$message.success(res.msg)
        //     }
        //   }).catch(err => {
        //     this.$message.error(err.msg)
        //   })
        // } else {
        //   return this.$message.error('请输入手机号')
        // }
        regEmailCode(this.regForm.email).then((res) => {
          if (res && res.code === 200) {
            this.$message.success('验证码发送成功，请查收邮箱');
            this.getAuthCode(); // 启动倒计时
            // this.$message.success(res.msg || '验证码已发送至您的邮箱')
          } else {
            this.$message.error(res.msg || '验证码发送失败')
            // 发送失败时重置按钮状态
            this.sendAuthCode = true
            this.auth_time = 0
          }
        }).catch(err => {
          this.$message.error(err.msg || '验证码发送失败')
          // 发送失败时重置按钮状态
          this.sendAuthCode = true
          this.auth_time = 0
        })
      }else{
        return this.$message.error('请输入邮箱')
      }
    },
    // 下一步
    handleNext() {

      this.$refs.regForm.validate(valid => {
        if (valid) {
          if (this.regForm.password !== this.regForm.confirmPassword) {
            this.$message.error("两次输入的密码不一致，请重新输入！")
            return
          }
          var data = {
            "accountName": this.regForm.accountName,
            "code":this.regForm.verificationCode,
            "password": this.regForm.password,
            "confirmPassword": this.regForm.confirmPassword,
            "phone": this.regForm.mobile,
            "email":this.regForm.email,
          }
          register(data)
            .then((data, err) => {
              if (err) {
                this.$message.error(error)
                return false
              } else {
                MessageBox.alert('', '提示', {
                  message: '恭喜您注册成功，请直接登录。',
                  confirmButtonText: '确定',
                }).then(() => {
                  this.$router.push({ path: '/login' })
                })
                // this.regShow = false
                // this.nextShow = true
              }
            })
            .catch(() => {})
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    setButtonClass(){
      if(this.regForm.accountName && this.regForm.confirmPassword && this.regForm.password && this.regForm.verificationCode && this.regForm.mobile){
        return 'register-text'
      }else{
        return 'no-register-text'
      }
    },
    fullWidth(){},

    handleLogin(callback) {
      this.$router.push({ path: '/login' })
    }
  },
  created() {
    // window.addEventListener('hashchange', this.afterQRScan)
  },
  destroyed() {
    // window.removeEventListener('hashchange', this.afterQRScan)
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
$bg: #2d3a4b;
$dark_gray: #889aa4;
$light_gray: #eee;

// 页面外层容器：改为全屏 flex 居中，且背景透明，让视频背景完全透出
.pageContainer {
  position: relative;
  z-index: 1;
  background-color: transparent !important;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center; /* 水平居中 */
  justify-content: flex-start; /* 垂直从顶部开始布局 */
  padding-top: 30px; /* 给 logo 等顶部元素留间距 */
}

// 登录容器：作为.pageContainer 的子容器，继承居中特性
.login-container {
  position: static;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center; /* 内部元素水平居中 */
}

// 表单容器：核心是透明背景 + 居中，且不被其他层遮挡
.login-form {
  position: static;
  width: 520px;
  min-height: 400px;
  padding: 35px;
  margin: 20px auto; /* 水平居中 + 顶部留间距 */
  background-color: rgba(255, 255, 255, 0.3) !important;
  backdrop-filter: blur(5px);
  border-radius: 8px;
  z-index: 2; /* 确保在视频遮罩之上 */
}

// logo 样式：保持显示，调整位置
.logo {
  background: transparent;
  width: 134px;
  height: 36px;
  margin: 30px 0 20px; /* 上下间距，取消左右浮动 */
  filter: brightness(1.2);
  z-index: 3; /* 层级高于视频遮罩 */
}

// 登录输入容器：跟随表单居中
.loginInputContainer {
  margin: 0;
  height: auto !important; /* 由内容撑开高度 */
  padding: 0 !important;
  border-radius: 6px;
  z-index: 2;
  display: flex;
  background-color: rgba(255, 255, 255, 0.3) !important;
  backdrop-filter: blur(5px);
}

// 视频背景容器：全屏覆盖，层级放最底层
.video-overlay-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1; /* 层级低于表单和 logo */
}

.background-video {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(rgba(0, 128, 0, 0.3), rgba(0, 64, 0, 0.5));
  pointer-events: none;
}

// 其他原有样式（保持不变，如输入框、按钮等）
.tips {
  font-size: 14px;
  color: #000000;
  margin-bottom: 10px;
  span:first-of-type {
    margin-right: 16px;
  }
}

.svg-container {
  padding: 6px 5px 6px 15px;
  color: $dark_gray;
  vertical-align: middle;
  width: 30px;
  display: inline-block;
  &_login {
    font-size: 20px;
  }
}

.title-container {
  position: relative;
  .title {
    font-size: 26px;
    font-weight: bold;
    color: #fff;
    margin: 0 auto 40px;
    text-align: center;
  }
  .set-language {
    color: #fff;
    position: absolute;
    top: 5px;
    right: 0;
  }
}

.show-pwd {
  position: absolute;
  right: 10px;
  top: 7px;
  font-size: 16px;
  color: $dark_gray;
  cursor: pointer;
  user-select: none;
}

.thirdparty-button {
  position: absolute;
  right: 35px;
  bottom: 28px;
}

.loginBtn {
  background: #407ffe;
  height: 64px;
  line-height: 32px;
  font-size: 24px;
}

.el-form-item {
  margin-bottom: 20px;
}

.regInfo {
  text-align: center;
  color: #000000;
  font-size: 16px;
}

.codeBtn {
  background: rgba(255, 255, 255, 0.3);
  color: #092ef8;
  border: 1px solid rgba(255, 255, 255, 0.3);
  align-items: center;
  font-size: 15px;
  margin-right: 16px;
  font-family: PingFangSC-Regular, PingFang SC;
  font-weight: 400;
  line-height: 15px;
  cursor: pointer;
  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }
  &:disabled {
    background: rgba(255, 255, 255, 0.3);
    color: #092ef8;
    cursor: not-allowed;
  }
}

.login-input {
  margin-top: 32px;
  height: 50px;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  flex-direction: row;
  font-family: PingFangSC-Medium, PingFang SC;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  &:hover {
    background: rgba(255, 255, 255, 0.5);
  }
}

.txt1 {
  width: 64px;
  height: 15px;
  overflow-wrap: break-word;
  text-align: left;
  white-space: nowrap;
  line-height: 15px;
  display: flex;
  align-self: center;
  margin-left: 16px;
  font-size: 16px;
  font-family: PingFangSC-Medium, PingFang SC;
  font-weight: 500;
  color: #000000;
  letter-spacing: 1px;
}

.label1 {
  width: 1px;
  height: 16px;
  display: flex;
  align-self: center;
  margin: 0 9px;
  background: rgba(255, 255, 255, 0.3);
}

.user-input {
  padding: 5px;
  display: flex;
  align-self: center;
  margin-right: 25px;
  font-size: 15px;
  line-height: 15px;
  flex-grow: 1;
  background-color: transparent;
  border: 0;
  outline: none;
  font-family: PingFangSC-Regular, PingFang SC;
  font-weight: 400;
  color: #000000;
  &::placeholder {
    color: rgba(255, 255, 255, 0.6);
  }
}

.verificationCodeContainer {
  display: flex;
}

.pwd-input {
  padding: 5px;
  display: flex;
  align-self: center;
  margin-right: 5px;
  font-size: 15px;
  line-height: 15px;
  flex-grow: 1;
  background-color: transparent;
  border: 0;
  outline: none;
  font-family: PingFangSC-Regular, PingFang SC;
  color: #fff;
  &::placeholder {
    color: rgba(255, 255, 255, 0.6);
  }
}

.loginContiner {
  width: 100%;
  display: flex;
  flex-flow: row;
  justify-content: center;
  align-items: flex-start;
  margin-top: 20px;
}

.loginLeftPic {
  display: none;
  flex-grow: 1;
  height: 705px;
  z-index: 3;
}

.hasaccount {
  width: 150px;
  margin-top: 15px;
  display: flex;
  flex-direction: row;
  margin: auto;
  padding-top: 20px;
}

.hasaccountbtn {
  display: flex;
  color: #4caf50;
  font-size: 14px;
  font-family: PingFangSC-Regular, PingFang SC;
}

.acss {
  font-size: 14px;
  font-family: PingFangSC-Regular, PingFang SC;
  font-weight: 400;
  color: #ccc;
  line-height: 14px;
}

.register-text {
  font-size: 18px;
  background: #4caf50;
  color: #fff;
  font-family: PingFangSC-Medium, PingFang SC;
  font-weight: 500;
  line-height: 18px;
  transition: background-color 0.3s ease-in;
  border: none;
  &:hover {
    background: #43a047;
  }
}

.no-register-text {
  background: #76dfb7;
  color: #fff;
  font-size: 18px;
  font-family: PingFangSC-Medium, PingFang SC;
  font-weight: 500;
  line-height: 18px;
  width: 100%;
  margin-top: 30px;
  height: 50px;
  border: none;
  pointer-events: none;
  transition: background-color 0.3s ease-in;
}

// 响应式调整
@media (max-width: 768px) {
  .login-form {
    width: 90%;
    padding: 25px 20px;
  }
  .loginInputContainer {
    margin: 20px;
    width: calc(100% - 40px);
  }
  .logo {
    margin: 20px 0;
  }
}
</style>
