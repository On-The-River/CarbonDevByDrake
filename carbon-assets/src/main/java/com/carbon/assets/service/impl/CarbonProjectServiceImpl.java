package com.carbon.assets.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.bean.copier.ValueProvider;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbon.assets.common.enums.ProjectStatus;
import com.carbon.assets.entity.CarbonProject;
import com.carbon.assets.entity.CarbonProjectContent;
import com.carbon.assets.entity.CarbonResourceFile;
import com.carbon.assets.mapper.CarbonMethodologyMapper;
import com.carbon.assets.mapper.CarbonProjectMapper;
import com.carbon.assets.mapper.DictMapper;
import com.carbon.assets.param.*;
import com.carbon.assets.service.CarbonProjectService;
import com.carbon.assets.service.CarbonResourceFileService;
import com.carbon.assets.util.ProjectAddUtil;
import com.carbon.assets.vo.CarbonDetectionDataVo;
import com.carbon.assets.vo.CarbonProjectListVo;
import com.carbon.assets.vo.CarbonProjectQueryVo;
import com.carbon.common.enums.ExpCodeEnum;
import com.carbon.common.exception.CommonBizException;
import com.carbon.common.feishu.FeiShuAPI;
import com.carbon.common.service.BaseServiceImpl;
import com.carbon.common.api.Paging;
import com.carbon.domain.common.ApiResult;
import com.carbon.domain.common.constant.RocketDelayLevelConstant;
import com.carbon.domain.common.constant.RocketMqName;
import com.carbon.domain.mq.entity.OpenRegisterAccount;
import com.carbon.domain.mq.entity.ProjectApproval;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;


/**
 * <p>
 * 碳减排项目 服务实现类
 * </p>
 *
 * @author Li Jun
 * @since 2022-04-24
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CarbonProjectServiceImpl extends BaseServiceImpl<CarbonProjectMapper, CarbonProject> implements CarbonProjectService {

    @Resource
    private CarbonProjectMapper carbonProjectMapper;

    @Resource
    private CarbonMethodologyMapper carbonMethodologyMapper;
    @Autowired
    private CarbonResourceFileService carbonResourceFileService;
    @Resource
    private RocketMQTemplate mqTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    DictMapper dictMapper;

    @Override
    public void triggerSyncToFeishu(Long projectId) {
//        Message<Long> message = MessageBuilder.withPayload(projectId).build();
//        rocketMQTemplate.send(RocketMqName.DATABASE_TO_FEISHU_SYNC, message);
        FeiShuAPI.sendToMQTemplate("carbon_project");
    }

    @Override
    public Boolean delDataSubmissionPage(String id) {
        return carbonProjectMapper.delDataSubmissionPage(id);
    }

    @Override
    public List<String> updateDataSubmissionPage(String id) {
        return carbonProjectMapper.updateDataSubmissionPage(id);
    }

    @Override
    public List<CarbonDetectionDataVo> getDataSubmissionPageById(String id) {

        return carbonProjectMapper.getDataSubmissionPageById(id);
    }

    @Override
    public boolean insertSubmissionToDB(CarbonDataSubmissionQueryParam param) {

        String factorCode = "208" + "2" + param.getProjectId();
        if (factorCode.length() < 10) {
            while (factorCode.length() < 10) {

                Random random = new Random();
                int number = random.nextInt(10);
                factorCode += String.valueOf(number);
            }
        }
        param.setFactorCode(factorCode);
        carbonProjectMapper.insertSubmissionToFactorProjectTable(param);

        return carbonProjectMapper.insertSubmissionToFactorTable(param);
    }

    @Override
    public Boolean deleteProject(Long projectId) {
        CarbonProject project = getById(projectId);
        if (project == null) {
            throw new CommonBizException("项目不存在");
        }
        boolean removed = removeById(projectId);
        if (removed) {
            carbonResourceFileService.lambdaUpdate()
                    .eq(CarbonResourceFile::getBusinessId, projectId)
                    .remove();
            triggerSyncToFeishu(projectId);
        }
        return removed;
    }

    @Override
    public CarbonProjectQueryVo getCarbonProjectById(Serializable id) {
        System.out.println("id:  "+id);
        CarbonProjectQueryVo vo = carbonProjectMapper.getCarbonProjectById(id);
        Optional.ofNullable(vo.getCarbonMethodology())
                .ifPresent(e->{
                    Optional.ofNullable(carbonMethodologyMapper.getCarbonMethodologyByDictCode(e))
                            .ifPresent(o-> vo.setCarbonMethodologyName(o.getName()==null?"":o.getName()));
        });
        if (vo == null){
            throw new CommonBizException("项目不存在");
        }
        List<CarbonResourceFile> files = carbonResourceFileService.lambdaQuery().eq(CarbonResourceFile::getFileDictCode, "023")
                .eq(CarbonResourceFile::getBusinessId, vo.getId()).list();
        vo.setAnnexList(BeanUtil.copyToList(files, CarbonProjectOwnerDataAnnex.class));
        return vo;
    }

    @Override
    public Paging<CarbonProjectListVo> getCarbonProjectPageList(CarbonProjectQueryParam param) {
        Page<?> page =getPage(param);
        page.addOrder(OrderItem.desc("cp.updated_time"));
        IPage<CarbonProjectListVo> iPage = carbonProjectMapper.getCarbonProjectPageList(page,param);
        return new Paging<>(iPage);
    }

    @Override
    public Paging<CarbonProjectListVo> getDataSubmissionPage(CarbonProjectQueryParam param) {
        Page<?> page =getPage(param);
        page.addOrder(OrderItem.desc("cp.updated_time"));
        IPage<CarbonProjectListVo> iPage = carbonProjectMapper.getDataSubmissionPage(page,param);
        return new Paging<>(iPage);
    }

    @Override
    public List<CarbonProject> selectProjectAll() {
        return carbonProjectMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public CarbonProjectQueryVo addCarbonProject(CarbonProject carbonProject) {
        carbonProject.setProjectStatus(ProjectStatus.STATUS_1.getStatus());
        carbonProject.setTenantId(getCurrentTenantId());
        save(carbonProject);

        triggerSyncToFeishu(carbonProject.getId());
        return getCarbonProjectById(carbonProject.getId());
    }

    private String handleNull(String str){
        return str == null ? "-" : str;
    }

    @Override
    public boolean updateCarbonProject(CarbonProjectAddParam param) {
        CarbonProject carbonProject = this.getById(param.getId());
        if (carbonProject == null){
            throw new CommonBizException("项目不存在");
        }
        BeanUtil.copyProperties(param,carbonProject);

        //更新附件资源
        ArrayList<CarbonResourceFile> files = new ArrayList<>();
        // 附件资源 不是必填项
        if(param.getAnnexList()!= null && param.getAnnexList().size()>0){
            for (CarbonProjectOwnerDataAnnex annex : param.getAnnexList()) {
                CarbonResourceFile file = BeanUtil.copyProperties(annex, CarbonResourceFile.class);
                file.setTenantId(getCurrentTenantId());
                file.setBusinessId(param.getId());
                file.setFileDictCode("023");
                files.add(file);
            }
        }
        carbonResourceFileService.saveBatch(files);
        boolean result = updateById(carbonProject);
        if (result) {
            triggerSyncToFeishu(param.getId());
        }
        return result;
    }

    @Override
    public void uploadOwnerData(CarbonProjectOwnerDataParam param) {
        CarbonProject carbonProject = this.getById(param.getId());
        if (carbonProject == null){
            throw new CommonBizException("项目不存在");
        }
        BeanUtil.copyProperties(param,carbonProject);
        if (!updateById(carbonProject)){
            throw new CommonBizException(ExpCodeEnum.OPERATE_FAIL_ERROR);
        }

        //保存附件资源
        ArrayList<CarbonResourceFile> files = new ArrayList<>();
        // 附件资源 不是必填项
        if(param.getAnnexList()!= null && param.getAnnexList().size()>0){
            for (CarbonProjectOwnerDataAnnex annex : param.getAnnexList()) {
                CarbonResourceFile file = BeanUtil.copyProperties(annex, CarbonResourceFile.class);
                file.setTenantId(getCurrentTenantId());
                file.setBusinessId(param.getId());
                file.setFileDictCode("023");
                files.add(file);
            }
        }
        carbonResourceFileService.saveBatch(files);
        triggerSyncToFeishu(param.getId());
    }

    @Override
    public CarbonProjectContent getCarbonProjectByName(String name) {
        return carbonProjectMapper.getCarbonProjectByName(name);
    }

    @Override
    public ApiResult insertMqForm(CarbonProjectAddParam param) {
        System.out.println("param:"+param);
        return ApiResult.ok();
    }

    @Override
    public ApiResult addFeishuProject(CarbonProjectAddParam param) {
        String method=dictMapper.getDictCh(param.getCarbonMethodology()==null?"0010000000":param.getCarbonMethodology());
        String country=dictMapper.getDictCh(param.getCountry()==null?"0010000000":param.getCountry());
        String province=dictMapper.getDictCh(param.getProvince()==null?"0010000000":param.getProvince());
        String city=dictMapper.getDictCh(param.getCity()==null?"0010000000":param.getCity());
        String json="{\"valueRange\": {\"range\": \"34372d!A1:T1\",\"values\": " +
                "[["  +
                "\""+param.getProjectName()+"\"," +
                "\""+""+"\"," +
                "\""+""+"\"," +
                "\""+param.getPrincipalName()+"\"," +
                "\""+param.getPrincipalPhone()+"\"," +
                "\""+param.getAssetsDevelopAgency()+"\"," +
                "\""+method+"\"," +
                "\""+country+"\"," +
                "\""+province+"\"," +
                "\""+city+"\"," +
                "\""+param.getAddress()+"\"," +
                "\""+param.getProjectIntroduction()+"\"," +
                "\""+""+"\"," +
                "\""+param.getOwnerName()+"\"," +
                "\""+""+"\"," +
                "\""+""+"\"," +
                "\""+param.getDesignDocument()+"\""
                +  "]]}}";
        try
        {
            ProjectAddUtil.projectFormAdd(json);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ApiResult.ok();
    }

}
