package com.carbon.system.controller;

import cn.hutool.core.bean.BeanUtil;
import com.carbon.common.api.Paging;
import com.carbon.domain.common.ApiResult;
import com.carbon.domain.system.param.ChangePasswordParam;
import com.carbon.domain.system.param.SysAccountParam;
import com.carbon.domain.system.vo.SysAccountModelVo;
import com.carbon.system.entity.SysAccount;
import com.carbon.system.param.*;
import com.carbon.common.utils.HttpContextUtils;
import com.carbon.system.vo.SysAccountQueryVo;
import com.carbon.system.common.BaseController;
import com.carbon.system.service.SysAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


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
@RequestMapping("/sysAccount")
@Api(value = "账户 模块", tags = {"帐户 模块"})
public class SysAccountController extends BaseController {

    @Autowired
    private SysAccountService sysAccountService;


    @GetMapping("/info/{id}")
    @ApiOperation(value = "账户-信息",notes = "账户信息")
    public ApiResult<SysAccountQueryVo> getAccount(@PathVariable Long id) {
        SysAccountQueryVo sysAccountQueryVo = sysAccountService.getSysAccountById(id);
        return ApiResult.ok(sysAccountQueryVo);
    }

    @PostMapping("/add")
    @ApiOperation(value = "账户-新增",notes = "新增账户")
    public ApiResult<Boolean>  addAccount(@Valid @RequestBody SysAccountParam param) {
        sysAccountService.addAccount(param);
        return ApiResult.ok();
    }

    @PostMapping("/update")
    @ApiOperation(value = "账户-更新",notes = "修改账户信息")
    public ApiResult<Boolean>  updateAccount(@RequestBody SysAccountParam param) {
        sysAccountService.updateAccount(param);
        return ApiResult.ok();
    }

    @GetMapping("/list")
    @ApiOperation(value = "账户列表",notes = "账户列表")
    public ApiResult<List<SysAccountModelVo>> getList() {
        return ApiResult.ok(BeanUtil.copyToList(sysAccountService.list(), SysAccountModelVo.class));
    }

    @PostMapping("/getPageList")
    @ApiOperation(value = "账户-分页列表",notes = "获取账户列表")
    public ApiResult<Paging<SysAccountQueryVo>> getSysAccountPageList(@Valid @RequestBody(required = false) SysAccountQueryParam sysAccountQueryParam) {
        Paging<SysAccountQueryVo> paging = sysAccountService.getSysAccountPageList(sysAccountQueryParam);
        return ApiResult.ok(paging);
    }

    @DeleteMapping("delete/{id}")
    @ApiOperation(value = "删除账户",notes = "删除账户")
    public ApiResult<Boolean> deleteAccountById(@PathVariable Long id) {
        sysAccountService.deleteAccountById(id);
        return ApiResult.ok("删除成功");
    }

//    @PutMapping("/update/baseInfo")
//    @ApiOperation(value = "账户-基本信息更新",notes = "修改基本信息")
//    public ApiResult<Boolean> updateAccountBaseInfo(@Valid @RequestBody AccountBaseInfoUpdateParam param) {
//        sysAccountService.updateAccountBaseInfo(param);
//        return ApiResult.ok();
//    }

    @PostMapping("/update/password")
    @ApiOperation(value = "账户-更新密码",notes = "修改密码")
    public ApiResult<Boolean>  updatePassword(@Valid @RequestBody ChangePasswordParam param) {
        sysAccountService.updatePassword(param);
        return ApiResult.ok();
    }

    @PutMapping("/update/avatar")
    @ApiOperation(value = "账户-头像信息更新",notes = "修改头像信息")
    public ApiResult<Boolean> updateAvatar(@Valid @RequestBody ChangeAvatarParam param) {
        sysAccountService.updateAccountAvatar(param);
        return ApiResult.ok();
    }


    @PutMapping("/update/phone")
    @ApiOperation(value = "账户-修改手机号",notes = "修改手机号")
    public ApiResult<Boolean> updatePhone(@Valid @RequestBody ChangePhoneParam param) {
//        log.info("开始处理修改手机号请求，参数: {}", JSONUtil.toJsonStr(param));
        // 自动填充当前用户ID（如果未提供）
        if (param.getId() == null) {
            Long currentAccountId = getCurrentAccountId();
            log.info("参数中未提供账户ID，自动获取当前用户ID: {}", currentAccountId);
            if (currentAccountId == null) {
                log.warn("无法获取当前用户ID，用户可能未登录");
                return ApiResult.fail("用户未登录");
            }
            param.setId(currentAccountId);
        }
        sysAccountService.updatePhone(param);
        return ApiResult.ok();
    }

    // 在 SysAccountController 类中添加获取账户id方法
    private Long getCurrentAccountId() {
        try {
            // 使用 HttpContextUtils 获取当前账户ID
            Long accountId = HttpContextUtils.getAccountId();
            log.info("通过HttpContextUtils获取到的accountId: {}", accountId);
            return accountId;
        } catch (Exception e) {
            log.error("获取当前账户ID异常: ", e);
            return null;
        }
    }
    @GetMapping("/update/code")
    @ApiOperation(value = "验证码-获取修改手机号验证码", notes = "获取修改验证码")
    public ApiResult<Boolean> sendUpdatePhoneCodeByEmail() {
        log.info("开始处理发送验证码请求");
        Long currentAccountId = getCurrentAccountId();
        log.info("当前用户ID: {}", currentAccountId);
        if (currentAccountId == null) {
            return ApiResult.fail("用户未登录");
        }
        sysAccountService.sendUpdatePhoneCodeByEmail(currentAccountId);
        return ApiResult.ok("发送成功，验证码已发送到您的邮箱");
    }

    @PostMapping("/send/email")
    @ApiOperation(value = "发送绑定链接邮件到客户邮箱",notes = "发送邮件到客户邮箱")
    public ApiResult<Boolean>  sendEmail(@Valid @RequestBody SendEmailParam param) {
        sysAccountService.sendEmail(param);
        return ApiResult.ok("发送成功，等待用户邮箱验证即可绑定成功");
    }

    @GetMapping("/renew/email/{id}/{email}/{value}")
    @ApiOperation(value = "通过点击链接 绑定/修改 邮箱",notes = "绑定/修改邮箱")
    public ApiResult<Boolean>  bindEmail2(@PathVariable Long id,@PathVariable String email,@PathVariable String value) {
        sysAccountService.bindEmail2(id,email,value);
        return ApiResult.ok("绑定成功");
    }

}

