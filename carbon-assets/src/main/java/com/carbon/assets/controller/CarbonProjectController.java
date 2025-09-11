package com.carbon.assets.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.carbon.assets.entity.CarbonProject;
import com.carbon.assets.param.CarbonDataSubmissionQueryParam;
import com.carbon.assets.param.CarbonProjectAddParam;
import com.carbon.assets.param.CarbonProjectOwnerDataParam;
import com.carbon.assets.service.CarbonProjectService;
import com.carbon.assets.param.CarbonProjectQueryParam;
import com.carbon.assets.vo.CarbonDetectionDataVo;
import com.carbon.assets.vo.CarbonProjectListVo;
import com.carbon.assets.vo.CarbonProjectQueryVo;
import com.carbon.assets.common.BaseController;
import com.carbon.common.exception.CommonBizException;
import com.carbon.domain.common.ApiResult;
import com.carbon.common.api.Paging;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.carbon.common.feishu.FeiShuAPI.handleSheetString;


/**
 * <p>
 * 碳减排项目 前端控制器
 * </p>
 *
 * @author Li Jun
 * @since 2021-07-31
 */
@Slf4j
@RestController
@RequestMapping("/carbonProject")
@Api(value = "碳减排项目模块", tags = {"碳减排项目模块"})
public class CarbonProjectController extends BaseController {

    @Autowired
    private CarbonProjectService carbonProjectService;
    /**
    * 添加碳减排项目
    */
    @PostMapping("/add")
    @ApiOperation(value = "添加碳减排项目",notes = "添加碳减排项目")
    public ApiResult<CarbonProjectQueryVo> addCarbonProject(@Valid @RequestBody CarbonProjectAddParam param) {
        return ApiResult.ok(carbonProjectService.addCarbonProject(param));
    }

    /**
    * 修改碳减排项目
    */
    @PutMapping("/update")
    @ApiOperation(value = "修改碳减排项目",notes = "修改碳减排项目")
    public ApiResult<Boolean> updateCarbonProject(@Valid @RequestBody CarbonProjectAddParam param) {
        boolean flag = carbonProjectService.updateCarbonProject(param);
        ApiResult<Boolean> ok=ApiResult.result(flag);
        return ok;
    }

    /**
     * 修改碳减排项目
     */
    @PostMapping("/uploadOwnerData")
    @ApiOperation(value = "上传业主资料",notes = "上传业主资料")
    public ApiResult<Boolean> uploadOwnerData(@Valid @RequestBody CarbonProjectOwnerDataParam param) {
        carbonProjectService.uploadOwnerData(param);
        return ApiResult.ok();
    }

    /**
    * 删除碳减排项目
    */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除碳减排项目",notes = "删除碳减排项目")
    public ApiResult<Boolean> deleteCarbonProject(@PathVariable String id) {
        boolean flag = carbonProjectService.deleteProject(Long.parseLong(id));
        return ApiResult.result(flag);
    }

    /**
    * 获取碳减排项目
    */
    @GetMapping("/info/{id}")
    public ApiResult<CarbonProjectQueryVo> selectProject(@PathVariable("id") Long id) {
        System.out.println("Project info Called");
        return ApiResult.ok(carbonProjectService.getCarbonProjectById(id));
    }

    @PostMapping("/sync/batch")
    public ApiResult<Boolean> batchUpdateProjectList(@RequestBody List<CarbonProject> projects) {
        boolean allSuccess = true;
        for (CarbonProject project : projects) {
            UpdateWrapper<CarbonProject> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", project.getId());

            updateWrapper.set("project_name", handleSheetString(project.getProjectName()));
            updateWrapper.set("project_scope", handleSheetString(project.getProjectScope()));
            updateWrapper.set("project_scope_code", handleSheetString(project.getProjectScopeCode()));
            updateWrapper.set("owner_name", handleSheetString(project.getOwnerName()));
            updateWrapper.set("project_introduction", handleSheetString(project.getProjectIntroduction()));
            updateWrapper.set("design_document", handleSheetString(project.getDesignDocument()));
            updateWrapper.set("principal_name", handleSheetString(project.getPrincipalName()));
            updateWrapper.set("principal_phone", handleSheetString(project.getPrincipalPhone()));
            updateWrapper.set("legal_person_name", handleSheetString(project.getLegalPersonName()));
            updateWrapper.set("legal_person_phone", handleSheetString(project.getLegalPersonPhone()));
            updateWrapper.set("project_type", handleSheetString(project.getProjectType()));
            updateWrapper.set("project_type_code", handleSheetString(project.getProjectTypeCode()));
            updateWrapper.set("project_status", handleSheetString(project.getProjectStatus()));
            updateWrapper.set("data_submission_status", handleSheetString(project.getDataSubmissionStatus()));
            updateWrapper.set("country", handleSheetString(project.getCountry()));
            updateWrapper.set("province", handleSheetString(project.getProvince()));
            updateWrapper.set("city", handleSheetString(project.getCity()));
            updateWrapper.set("address", handleSheetString(project.getAddress()));
            updateWrapper.set("assets_develop_agency", handleSheetString(project.getAssetsDevelopAgency()));
            updateWrapper.set("carbon_methodology", handleSheetString(project.getCarbonMethodology()));
            updateWrapper.set("ref_id", handleSheetString(project.getRefId()));
            updateWrapper.set("development_follower", handleSheetString(project.getDevelopmentFollower()));
            updateWrapper.set("project_msg", handleSheetString(project.getProjectMsg()));
            updateWrapper.set("remarks", handleSheetString(project.getRemarks()));
            updateWrapper.set("estimated_reduction", handleSheetString(project.getEstimatedReduction()));

            if (project.getTenantId() != null) {
                updateWrapper.set("tenant_id", project.getTenantId());
            }

            if (project.getInitiationDate() != null) {
                updateWrapper.set("initiation_date", project.getInitiationDate());
            }
            if (project.getApprovalDate() != null) {
                updateWrapper.set("approval_date", project.getApprovalDate());
            }
            if (project.getRecordFilingDate() != null) {
                updateWrapper.set("record_filing_date", project.getRecordFilingDate());
            }
            if (project.getCertifiedDate() != null) {
                updateWrapper.set("certified_date", project.getCertifiedDate());
            }
            if (project.getIssuingDate() != null) {
                updateWrapper.set("issuing_date", project.getIssuingDate());
            }

            boolean success = carbonProjectService.update(updateWrapper);
            if (!success) {
                allSuccess = false;
            }
        }
        return ApiResult.ok(allSuccess);
    }

    @GetMapping("/sync")
    public ApiResult<List<CarbonProject>> selectAllProject() {
        System.out.println("Project info Called");
        return ApiResult.ok(carbonProjectService.selectProjectAll());
    }

    @GetMapping("/sync/{id}")
    public ApiResult<CarbonProject> getProjectForSync(@PathVariable Long id) {
        CarbonProject project = carbonProjectService.getById(id);
        if (project == null) {
            throw new CommonBizException("项目不存在");
        }
        return ApiResult.ok(project);
    }

    @PostMapping("/dataSubmissionPageList")
    public ApiResult<Paging<CarbonProjectListVo>> submissionPagesListPost(
            @Valid @RequestBody(required = false) CarbonProjectQueryParam param) {
        System.out.println("Project dataSubmissionPageList Called");
        return ApiResult.ok(carbonProjectService.getDataSubmissionPage(param));
    }



    @GetMapping("/dataSubmissionPage/{id}")
    @ApiOperation(value = "项目-数据报送-填写数据", notes = "填写数据")
    public ApiResult<List<CarbonDetectionDataVo>>dataSubmissionPage(@PathVariable("id")String id) {

        List<CarbonDetectionDataVo> paging = carbonProjectService.getDataSubmissionPageById(id);
        return ApiResult.ok(paging);
    }

    @PostMapping("/dataSubmission")
    @ApiOperation(value = "项目-数据报送-填写数据-报送", notes = "报送")
    public ApiResult<Boolean>dataSubmission(CarbonDataSubmissionQueryParam carbonDataSubmissionQueryParam) {

        boolean flag = carbonProjectService.insertSubmissionToDB(carbonDataSubmissionQueryParam);
        return ApiResult.result(flag);
    }


    @PostMapping("/updateDataSubmissionPage/{id}")
    @ApiOperation(value = "项目-数据报送-修改", notes = "修改数据")
    public ApiResult<List<String>>updateDataSubmissionPage(@PathVariable("id")String id) {

        List<String> carbonDetectionDataVos = carbonProjectService.updateDataSubmissionPage(id);


        return ApiResult.ok(carbonDetectionDataVos);
    }

    @PostMapping("/delDataSubmissionPage/{id}")
    @ApiOperation(value = "项目-数据报送-删除", notes = "删除")
    public ApiResult<Boolean> delDataSubmissionPage(@PathVariable("id")String id) {

        carbonProjectService.updateDataSubmissionPage(id);

        return ApiResult.result(false);
    }



    /**
     * 项目立项分页列表
     */
    @PostMapping("/getPageList")
    @ApiOperation(value = "项目-立项分页列表",notes = "立项分页列表")
    public ApiResult<Paging<CarbonProjectListVo>> getCarbonProjectPageList(@Valid @RequestBody(required = false) CarbonProjectQueryParam carbonProjectQueryParam) {
        Paging<CarbonProjectListVo> paging = carbonProjectService.getCarbonProjectPageList(carbonProjectQueryParam);
        return ApiResult.ok(paging);
    }

    /**
     * 项目开发实施分页列表
     */
    @PostMapping("/getDevelopPageList")
    @ApiOperation(value = "项目-开发实施分页列表",notes = "开发实施分页列表")
    public ApiResult<Paging<CarbonProjectListVo>> getDevelopPageList(@Valid @RequestBody(required = false) CarbonProjectQueryParam carbonProjectQueryParam) {
        Paging<CarbonProjectListVo> paging = carbonProjectService.getCarbonProjectPageList(carbonProjectQueryParam);
        return ApiResult.ok(paging);
    }

    @PostMapping("/addFeishuProject")
    public ApiResult addFeishuProject(@Valid @RequestBody(required = false) CarbonProjectAddParam param) {
        System.out.println("---param:"+param);
        carbonProjectService.addFeishuProject(param);
        return ApiResult.ok();
    }

    @GetMapping("/sync/rowCount")
    public Long rowCount() {
        System.out.println("Project rowCount Called");
        return (long) carbonProjectService.count();
    }
}

