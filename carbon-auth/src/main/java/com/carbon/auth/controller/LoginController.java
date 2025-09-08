package com.carbon.auth.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.carbon.auth.entity.SysAccount;
import com.carbon.auth.service.LoginService;
import com.carbon.auth.service.SmsService;
import com.carbon.common.exception.CommonBizException;
import com.carbon.common.utils.HttpContextUtils;
import com.carbon.domain.auth.param.ForgotPasswordParam;
import com.carbon.domain.auth.param.LoginParam;
import com.carbon.domain.auth.param.RegisterParam;
import com.carbon.domain.auth.vo.LoginInfoVo;
import com.carbon.domain.common.ApiCode;
import com.carbon.domain.common.ApiResult;
import com.carbon.domain.common.constant.SmsConstant;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;


/**
 * <p>
 * 帐号  前端控制器
 * </p>
 *
 * @author Li Jun
 * @since 2021-06-11
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@Api(value = "登录 模块", tags = {"登录 模块"})
public class LoginController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private SmsService smsService;

    @PostMapping("/login")
    @ApiOperation(value = "登录", notes = "登录", response = ApiResult.class)
    public ApiResult<LoginInfoVo> login(@Validated @RequestBody LoginParam loginParam, HttpServletRequest request) {
        return ApiResult.ok(loginService.byLoginName(loginParam));
    }

    @GetMapping("/logout")
    @ApiOperation(value = "退出登录", notes = "退出登录")
    public ApiResult<Boolean> logout() {
        loginService.logout(HttpContextUtils.getAccountId());
        return ApiResult.ok();
    }


    @GetMapping("/register/code/{phone}")
    @ApiOperation(value = "验证码-注册", notes = "获取注册验证码")
    public ApiResult<Boolean> sendRegisterCode(@PathVariable String phone) {
        smsService.sendRegisterCode(phone);
        return ApiResult.ok("发送成功");
    }
    @GetMapping("/register/emailCode/{email}")
    @ApiOperation(value = "验证码-邮箱注册", notes = "获取邮箱注册验证码")
    public ApiResult<Boolean> sendRegisterEmailCode(@PathVariable String email) {
        smsService.sendEmailCode(email);
        return ApiResult.ok("发送成功");
    }


    @PostMapping("/register")
    @ApiOperation(value = "注册", notes = "注册", response = ApiResult.class)
    public ApiResult register(@Validated @RequestBody RegisterParam param) {
        try {
            loginService.register(param);
            return ApiResult.ok();
        } catch (CommonBizException e) {
            log.error("注册失败: ", e);
            return ApiResult.fail(e.getMessage());
        } catch (Exception e) {
            log.error("注册异常: ", e);
            return ApiResult.fail("注册失败: " + e.getMessage());
        }
    }

    @GetMapping("/forgotPassword/code/{email}")
    @ApiOperation(value = "验证码-忘记密码", notes = "获取验证码")
    public ApiResult<Boolean> sendForgotPasswordCode(@PathVariable String email) {
        smsService.sendForgotPasswordCode(email);
        return ApiResult.ok("发送成功");
    }

    @PostMapping("/forgotPassword")
    @ApiOperation(value = "忘记密码", notes = "忘记密码，找回密码", response = ApiResult.class)
    public ApiResult forgotPassword(@Validated @RequestBody ForgotPasswordParam param) {
        loginService.forgotPassword(param);  //登进去之后跳转修改密码页面也改成邮箱
        return ApiResult.ok();
    }

    @GetMapping("/verify/{accountName}")
    @ApiOperation(value = "验证账户是否存在", notes = "验证账户是否存在,存在返回false")
    public ApiResult<Boolean> verifyAccountName(@PathVariable String accountName) {
        return ApiResult.ok(loginService.verify(accountName));
    }

    @GetMapping("/verifyEmail/{email}")
    @ApiOperation(value = "验证邮箱是否存在", notes = "验证邮箱是否存在,存在返回false")
    public ApiResult<Boolean> verifyEmail(@PathVariable String email) {

        return ApiResult.ok(loginService.verifyEmail(email));
    }


}

