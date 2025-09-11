package com.carbon.assets.controller;

import com.alibaba.druid.support.json.JSONUtils;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.carbon.assets.param.ExchangeAccountBindingParam;
import com.carbon.assets.service.CarbonExchangeService;
import com.carbon.assets.service.ExchangeAccountService;
import com.carbon.assets.param.ExchangeAccountQueryParam;
import com.carbon.assets.vo.ExchangeAccountQueryVo;
import com.carbon.assets.entity.ExchangeAccount;
import com.carbon.assets.common.BaseController;
import com.carbon.common.api.Paging;
import com.carbon.common.exception.CommonBizException;
import com.carbon.common.feishu.FeiShuAPI;
import com.carbon.domain.common.ApiResult;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

import static com.carbon.common.feishu.FeiShuAPI.handleSheetString;


/**
 * <p>
 * 交易账户  前端控制器
 * </p>
 *
 * @author Li Jun
 * @since 2021-08-16
 */
@Slf4j
@RestController
@RequestMapping("/exchangeAccount")
@Api(value = "交易账户 模块", tags = {"交易账户 模块"})
public class ExchangeAccountController extends BaseController {

    @Autowired
    private ExchangeAccountService exchangeAccountService;

    /**
    * 添加交易账户
    */
    @PostMapping("/add")
    @ApiOperation(value = "添加交易账户",notes = "添加交易账户 ")
    public ApiResult<Boolean> addExchangeAccount(@Valid @RequestBody ExchangeAccount exchangeAccount) {
        exchangeAccountService.addExchangeAccount(exchangeAccount);
        return ApiResult.ok();
    }

    /**
    * 修改交易账户
    */
    @PutMapping("/update")
    @ApiOperation(value = "修改交易账户",notes = "修改交易账户 ")
    public ApiResult<Boolean> updateExchangeAccount(@Valid @RequestBody ExchangeAccount exchangeAccount) {
        boolean flag = exchangeAccountService.updateById(exchangeAccount);
        exchangeAccountService.triggerSyncToFeishu();
        return ApiResult.result(flag);
    }

    /**
    * 删除交易账户
    */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除交易账户",notes = "删除交易账户 ")
    public ApiResult<Boolean> deleteExchangeAccount(@PathVariable String id) {
        boolean flag = exchangeAccountService.removeById(id);
        exchangeAccountService.triggerSyncToFeishu();
        return ApiResult.result(flag);
    }


    @PutMapping("/binding")
    @ApiOperation(value = "交易账户-绑定",notes = "绑定交易账户 ")
    public ApiResult<Boolean> binding(@Valid @RequestBody ExchangeAccountBindingParam param) {
        exchangeAccountService.binding(param);
        return ApiResult.ok();
    }

    @PutMapping("/unbind/{id}")
    @ApiOperation(value = "交易账户-解绑",notes = "解绑交易账户 ")
    public ApiResult<Boolean> unbind(@PathVariable Long id) {
        exchangeAccountService.unbind(id);
        return ApiResult.ok();
    }

    /**
    * 获取交易账户
    */
    @GetMapping("/info/{id}")
    @ApiOperation(value = "查看交易账户",notes = "查看交易账户 ")
    public ApiResult<ExchangeAccountQueryVo> getExchangeAccount(@PathVariable String id) {
        ExchangeAccountQueryVo exchangeAccountQueryVo = exchangeAccountService.getExchangeAccountById(id);
        return ApiResult.ok(exchangeAccountQueryVo);
    }

    /**
     * 交易账户 分页列表
     */
    @PostMapping("/getPageList")
    @ApiOperation(value = "（带关键词）获取交易账户分页列表",notes = "（带关键词）交易账户分页列表")
    public ApiResult<Paging<ExchangeAccountQueryVo>> getExchangeAccountPageList(@RequestBody ExchangeAccountQueryParam exchangeAccountQueryParam) {
        Paging<ExchangeAccountQueryVo> paging = exchangeAccountService.getExchangeAccountPageList(exchangeAccountQueryParam);
        return ApiResult.ok(paging);
    }

    /**
     * 上传项目管理的文档
     */
    @PostMapping("/upload")
    @ApiOperation(value = "上传项目管理的文档",notes = "上传项目管理的文档")
    public ApiResult upLoadCarbonDoc(@RequestParam("file") MultipartFile file, @RequestParam("fileName") String fileName) {
        return FeiShuAPI.uploadFile(file, fileName);
    }

    @PostMapping("/uploadCredential")
    @ApiOperation(value = "飞书查看用户凭证", notes = "飞书查看用户凭证")
    public ApiResult uploadCredential(@RequestParam("file") MultipartFile file) {

        String originalFilename = file.getOriginalFilename();
        ApiResult apiResult = FeiShuAPI.uploadFile(file, originalFilename);

        JsonObject jsonObject = (JsonObject) new JsonParser().parse(apiResult.getData().toString());

        String url = "https://carbonmsger.feishu.cn/file/" + jsonObject.get("file_token").getAsString();

        return ApiResult.ok(url);
    }

    @GetMapping("/sync")
    public ApiResult<List<ExchangeAccount>> selectAllProject() {
        return ApiResult.ok(exchangeAccountService.list());
    }

    @GetMapping("/sync/{id}")
    public ApiResult<ExchangeAccount> getProjectForSync(@PathVariable Long id) {
        ExchangeAccount account = exchangeAccountService.getById(id);
        if (account == null) {
            throw new CommonBizException("账户不存在");
        }
        return ApiResult.ok(account);
    }

    @PostMapping("/sync/batch")
    public ApiResult<Boolean> batchUpdateAccountList(@RequestBody List<ExchangeAccount> accounts) {
        boolean allSuccess = true;
        for (ExchangeAccount account : accounts) {
            UpdateWrapper<ExchangeAccount> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", account.getId());

            updateWrapper.set("account_name", handleSheetString(account.getAccountName()));
            updateWrapper.set("password", handleSheetString(account.getPassword()));
            updateWrapper.set("account_credentials", handleSheetString(account.getAccountCredentials()));
            updateWrapper.set("account_status", handleSheetString(account.getAccountStatus()));
            updateWrapper.set("remarks", handleSheetString(account.getRemarks()));
            updateWrapper.set("cookie", handleSheetString(account.getCookie()));

            if (account.getAccountType() != null) {
                updateWrapper.set("account_type", account.getAccountType());
            }

            if (account.getBrokerId() != null) {
                updateWrapper.set("broker_id", account.getBrokerId());
            }
            if (account.getCarbonExchangeId() != null) {
                updateWrapper.set("carbon_exchange_id", account.getCarbonExchangeId());
            }
            if (account.getTenantId() != null) {
                updateWrapper.set("tenant_id", account.getTenantId());
            }

            if (account.getCarbonAmount() != null) {
                updateWrapper.set("carbon_amount", account.getCarbonAmount());
            }
            if (account.getCcerAmount() != null) {
                updateWrapper.set("ccer_amount", account.getCcerAmount());
            }

            if (account.getLoginTime() != null) {
                updateWrapper.set("login_time", account.getLoginTime());
            }
            if (account.getBindingTime() != null) {
                updateWrapper.set("binding_time", account.getBindingTime());
            }

            boolean success = exchangeAccountService.update(updateWrapper);
            if (!success) {
                allSuccess = false;
            }
        }
        return ApiResult.ok(allSuccess);
    }

    @GetMapping("/sync/rowCount")
    public Long rowCount() {
        return (long)exchangeAccountService.count();
    }

//    /**
//     * 跳转到root文件夹下
//     */
//    @GetMapping("/jump/root")
//    @ApiOperation(value = "跳转到root文件夹的url",notes = "跳转到root文件夹的url")
//    public String jumpRootFolder() {
//         return FeiShuAPI.jumpRootFolder();
//    }
}

