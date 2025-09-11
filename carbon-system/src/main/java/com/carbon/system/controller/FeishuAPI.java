package com.carbon.system.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.carbon.common.entity.Message;
import com.carbon.common.enums.ApprovalCodeEnum;
import com.carbon.common.feishu.FeiShuAPI;
import com.carbon.domain.common.ApiResult;
import com.carbon.domain.common.constant.RocketDelayLevelConstant;
import com.carbon.domain.common.constant.RocketMqName;
import com.carbon.domain.mq.entity.AddTradingAccountApproval;
import com.carbon.domain.mq.entity.AssetUploadApproval;
import com.carbon.system.entity.CarbonProjectToken;
import com.carbon.system.entity.FeishuFiletoken;
import com.carbon.system.param.FeiShuEventParam1;
import com.carbon.system.param.FeiShuSheetChangeEvent;
import com.carbon.system.service.CarbonApprovalService;
import com.carbon.system.service.CarbonProjectTokenService;
import com.carbon.system.service.SyncService;
import com.carbon.system.vo.FeiShuEventVo;
import com.carbon.system.vo.FileDate;
import com.carbon.system.service.FeishuFiletokenService;
import com.carbon.system.vo.ProjectFileCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Bae
 * @version 1.0
 * @date 2022/5/19 10:44
 */
@Slf4j
@RestController
@RequestMapping("/feishu")
@Api(value = "飞书API调用 模块", tags = {"飞书API调用 模块"})
public class FeishuAPI {
    @Autowired
    FeishuFiletokenService filetokenService;
    @Autowired
    CarbonApprovalService carbonApprovalService;
    @Resource
    RocketMQTemplate mqTemplate;
    @Autowired
    CarbonProjectTokenService tokenService;
    @Autowired
    private SyncService syncService;


    /**
     * 传入code 获取云文档鉴权信息
     */
    @GetMapping("/information")
    @ApiOperation(value = "获取云文档鉴权信息", notes = "获取云文档鉴权信息")
    public ApiResult<Message> getTextAuthenticationInformation(String code, String templateNum, String refreshToken) {
        String app="1";
        Message message = FeiShuAPI.getTextAuthenticationInformation(code, app,refreshToken);
        if (message == null) {
            return ApiResult.fail("code非法，请确保 code 没有重复消费或过期消费（code只能使用一次且5分钟内有效）");
        }
        QueryWrapper<CarbonProjectToken> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("text_code",templateNum);
        CarbonProjectToken one = tokenService.getOne(queryWrapper);
        String textToken = one.getFileToken();
        String textUrl="https://carbonmsger.feishu.cn/docs/"+textToken;
        message.setTextToken(textToken);
        message.setTextUrl(textUrl);
        return ApiResult.ok(message);
    }

//    /**
//     * 传入code 获取 租户上传文件创建文件夹鉴权所需信息
//     */
//    @GetMapping("/folderInformation")
//    @ApiOperation(value = "获取云文档鉴权信息", notes = "获取云文档鉴权信息")
//    public ApiResult getFolderAuthenticationInformation(String folderName,String code, String refreshToken) {
//        String app="2";
//        Folder folder = FeiShuAPI.createFolder(app, folderName, code, refreshToken);
//        return ApiResult.ok(folder);
//    }

    /**
     * 传入code 获取电子表格鉴权信息
     */
    @GetMapping("/formInformation")
    @ApiOperation(value = "获取电子表格鉴权信息", notes = "获取电子表格鉴权信息")
    public ApiResult<Message> getFormAuthenticationInformation(String code, String templateNum, String refreshToken) {
        FeishuFiletoken one = filetokenService.getOne(templateNum);
        Message message = FeiShuAPI.getFormAuthenticationInformation(code, one.getApp(), one.getWebRedirect() ,refreshToken);
        if (message == null) {
            return ApiResult.fail("code非法，请确保 code 没有重复消费或过期消费（code只能使用一次且5分钟内有效）");
        }

        message.setTextToken(one.getFileToken());
        message.setTextUrl(one.getFileUrl());
        return ApiResult.ok(message);
    }

    @PostMapping("/updateByNum")
    @ApiOperation(value = "根据文档编号和位置修改指定区域内容", notes = "根据文档编号和位置修改指定区域内容")
    public ApiResult<Boolean> updateFileByNum(String templateNum, String position, String replaceText) {
        FeiShuAPI.updateFileByNum(templateNum, position, replaceText);
        //更新文档时间
        filetokenService.updatedTime(templateNum);
        return ApiResult.ok("修改成功");
    }


    @PostMapping("/update")
    @ApiOperation(value = "修改指定内容", notes = "修改指定内容")
    public ApiResult<Boolean> updateFile(String fileToken, String text, String replaceText) {
        FeiShuAPI.updateFileText(fileToken, text, replaceText);
        return ApiResult.ok("修改成功");
    }


    /**
     * 获取指定文档的创建-修改日期
     */
    @GetMapping("/getFileDate/{templateNum}")
    @ApiOperation(value = "获取指定文档的创建-修改日期", notes = "获取指定文档的创建-修改日期")
    public ApiResult<FileDate> getFileDate(@PathVariable("templateNum") String templateNum) {
        QueryWrapper<FeishuFiletoken> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", templateNum);
        FeishuFiletoken one = filetokenService.getOne(queryWrapper);
        FileDate fileDate = new FileDate();
        BeanUtil.copyProperties(one, fileDate);
        return ApiResult.ok(fileDate);
    }

//    @PostMapping("/event")
//    @ApiOperation(value = "审批监听事件", notes = "审批监听事件")
//    public ApiResult<Boolean> testUpdateFile(@RequestBody FeiShuEventParam1 param) {
//        log.info("审批监听事件:{}", JSONUtil.toJsonPrettyStr(param));
//        carbonApprovalService.approvalEventCallback(param);
//
//        return ApiResult.ok();
//    }

    @PostMapping("/event")
    @ApiOperation(value = "测试事件",notes = "测试事件")
    public FeiShuEventVo test(@RequestBody FeiShuEventParam1 param) {
        FeiShuEventVo vo = new FeiShuEventVo();
        vo.setChallenge(param.getChallenge());
        return vo;
    }

    /**
     * 传入项目ID和项目名称，返回对应飞书文档
     *
     * @param projectId 项目编号
     * @return
     */
    @GetMapping("/getProjectFile")
    @ApiOperation(value = "获取项目对应飞书文档", notes = "获取项目对应飞书文档")
    public ApiResult<ProjectFileCode> getProjectFile(
            @RequestParam("projectId") String projectId,@RequestParam("projectName") String projectName) {
        Long code = tokenService.getProjectFileCode(projectId, projectName);
        return ApiResult.ok(new ProjectFileCode(code));
    }

    @PostMapping("/uploadFile")
    @ApiOperation(value = "飞书上传文件", notes = "飞书上传文件")
    public ApiResult uploadFiles(@RequestParam("file") MultipartFile file, @RequestParam("fileName") String fileName) {
        log.info("uploadFile");
        return FeiShuAPI.uploadFile(file, fileName);
    }

    /**
     * 初始化飞书表格与数据库同步
     */
    @PostMapping("/initSync")
    @ApiOperation(value = "初始化表格同步", notes = "建立飞书表格与数据库表的同步关系")
    public ApiResult<Boolean> initSync(
            @RequestParam String feishuFileToken,
            @RequestParam String sheetId,
            @RequestParam String databaseTable,
            @RequestParam String controllerEndpoint,
            @RequestBody LinkedHashMap<String, Object> fieldMapping) {

        syncService.initializeSync(
                feishuFileToken, sheetId, databaseTable, controllerEndpoint, fieldMapping
        );
        return ApiResult.ok("同步初始化成功");
    }


    /**
     * 手动触发数据库到飞书的同步
     */
    @PostMapping("/syncToFeishu/{syncConfigId}")
    @ApiOperation(value = "同步数据库数据到飞书", notes = "将数据库指定表数据同步到飞书表格")
    public ApiResult<Boolean> syncDatabaseToFeishu(@PathVariable String syncConfigId) {
        if (syncService.syncDatabaseToFeishu(syncConfigId))
            return ApiResult.ok("同步到飞书成功");
        else
            return ApiResult.fail("同步失败！");
    }

    /**
     * 手动触发飞书到数据库的同步
     */
    @PostMapping("/syncToDatabase/{syncConfigId}")
    @ApiOperation(value = "同步飞书数据到数据库", notes = "将飞书表格数据同步到数据库")
    public ApiResult<Boolean> syncFeishuToDatabase(@PathVariable String syncConfigId) {
        if (syncService.syncFeishuToDatabase(syncConfigId))
            return ApiResult.ok("同步到数据库成功");
        else
            return ApiResult.fail("同步失败！");
    }


    /**
     * 飞书表格变更webhook
     */
    @PostMapping("/sheetChangeEvent")
    @ApiOperation(value = "飞书表格变更事件", notes = "接收飞书表格变更通知")
    public Object handleSheetChange(@RequestBody FeiShuSheetChangeEvent event) {
        log.info("飞书表格变更事件: {}", JSONUtil.toJsonPrettyStr(event));
        FeiShuEventVo vo = new FeiShuEventVo();

        if (event.getChallenge() != null) {
            vo.setChallenge(event.getChallenge());
            return vo;
        }
        if (event.getEvent() != null) {
            syncService.handleFeishuSheetChange(event);
        }
        return ApiResult.ok(vo);
    }
}
