package com.carbon.system.service.impl;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.carbon.common.service.BaseServiceImpl;
import com.carbon.system.entity.DataPanel;
import com.carbon.system.entity.SysAccount;
import com.carbon.system.entity.SysTenant;
import com.carbon.system.service.*;
import com.carbon.system.vo.*;
import com.carbon.system.mapper.DataPanelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;


/**
 * <p>
 * 数据面板 服务实现类
 * </p>
 *
 * @author Li Jun
 * @since 2021-07-20
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DataPanelServiceImpl extends BaseServiceImpl<DataPanelMapper, DataPanel> implements DataPanelService {

    @Resource
    private DataPanelMapper dataPanelMapper;
    @Autowired
    private SysAccountService sysAccountService;
    @Autowired
    private SysTenantService sysTenantService;
    @Autowired
    private SysAccountRoleService sysAccountRoleService;
    @Autowired
    private SysRoleService sysRoleService;

    @Override
    public StatHomeDataVo getHomeData() {
        StatHomeDataVo vo;

        DataPanel dataPanel = this.getOne(Wrappers.<DataPanel>lambdaQuery().eq(DataPanel::getTenantId,getCurrentTenantId()));
        if (dataPanel == null){
            vo =  new StatHomeDataVo();
        }else {
            vo = dataPanel.getHomeData();
        }

        SysAccount account = sysAccountService.getById(getCurrentAccountId());
        SysTenant tenant = sysTenantService.getById(getCurrentTenantId());
        SysAccountRoleVo accountRole = sysRoleService.getAccountRole(account.getId());

        StatAccountVo accountVo = new StatAccountVo();
        accountVo.setAccountName(account.getAccountName());
        accountVo.setAccountValidity(account.getValidityPeriod());
        accountVo.setExpired(DateUtil.compare(DateUtil.date(),accountVo.getAccountValidity()) > 0);
        accountVo.setAvatar(account.getAvatar());
        accountVo.setAccountType(account.getAccountType());
        accountVo.setProductVersion(account.getProductVersion());
        accountVo.setEnterpriseName(tenant==null?"null":tenant.getTenantName());
        accountVo.setRoleNames(accountRole.getRoleNames());

        //上个月
        DateTime lastMonth = DateUtil.offsetMonth(DateUtil.date(), -1);
        DateTime statDate = DateUtil.date();
        DateTime lastYear = DateUtil.offsetMonth(DateUtil.date(),-12);

        BigDecimal carbonCreditTotal = dataPanelMapper.getCarbonCreditTotal(null);
        BigDecimal carbonQuotaTotal = dataPanelMapper.getCarbonQuotaTotal(null);
        BigDecimal carbonCreditLastMonth = dataPanelMapper.getCarbonCreditTotal(lastMonth);
        BigDecimal carbonQuotaLastMonth = dataPanelMapper.getCarbonQuotaTotal(lastMonth);
        BigDecimal carbonCreditCurrentMonth = dataPanelMapper.getCarbonCreditTotal(statDate);
        BigDecimal carbonQuotaCurrentMonth = dataPanelMapper.getCarbonQuotaTotal(statDate);
        BigDecimal carbonCreditLastYear = dataPanelMapper.getCarbonCreditTotal(lastYear);
        BigDecimal carbonQuotaLastYear = dataPanelMapper.getCarbonQuotaTotal(lastYear);
        double currentTotal=carbonCreditCurrentMonth.add(carbonQuotaCurrentMonth).doubleValue();


        //资产收入
        StatCarbonIncomeVo assetsIncome = new StatCarbonIncomeVo();
        assetsIncome.setStatDate(statDate);
        assetsIncome.setMonthIncome(carbonCreditLastMonth.add(carbonQuotaLastMonth));
        assetsIncome.setTotalIncome(carbonCreditTotal.add(carbonQuotaTotal));
        {
            double assetsTotalLastMonth=carbonCreditLastMonth.add(carbonQuotaLastMonth).doubleValue();
            BigDecimal monthOnMonthRatio=BigDecimal.ZERO;
            if(currentTotal!=0)
            {
                monthOnMonthRatio=BigDecimal.valueOf((assetsTotalLastMonth-currentTotal)/currentTotal);
            }

            assetsIncome.setMonthOnMonthRatio(monthOnMonthRatio);
        }
        {
            BigDecimal assetsLastYearTotal=carbonCreditLastYear.add(carbonQuotaLastYear);
            BigDecimal yearOnYearRatio=BigDecimal.ZERO;
            if(assetsLastYearTotal.doubleValue()!=0)
            {
                yearOnYearRatio=BigDecimal.valueOf(currentTotal).subtract(assetsLastYearTotal).divide(assetsLastYearTotal,2,BigDecimal.ROUND_HALF_UP);
            }
            assetsIncome.setYearOnYearRatio(yearOnYearRatio);
        }
        vo.setAssetsIncome(assetsIncome);

        //资金收入
        double performanceCurrent=dataPanelMapper.getPerformancePrice(statDate);
        double performanceLastMonth=dataPanelMapper.getPerformancePrice(lastMonth);
        double performanceLastYear=dataPanelMapper.getPerformancePrice(lastYear);
        double performanceTotal=dataPanelMapper.getPerformancePrice(null);

        StatCarbonIncomeVo fundIncome = new StatCarbonIncomeVo();
        fundIncome.setStatDate(statDate);
        fundIncome.setMonthIncome(BigDecimal.valueOf(performanceLastMonth));
        fundIncome.setTotalIncome(BigDecimal.valueOf(performanceTotal));

        double monthOnMonth=0.0;
        if(performanceCurrent!=0)
        {
            monthOnMonth=(performanceCurrent-performanceLastMonth)/performanceLastMonth;
        }
        fundIncome.setMonthOnMonthRatio(BigDecimal.valueOf(monthOnMonth));

        double yearOnYear=0.0;
        if(performanceLastYear!=0)
        {
            yearOnYear=(performanceCurrent-performanceLastYear)/performanceLastYear;
        }
        fundIncome.setYearOnYearRatio(BigDecimal.valueOf(yearOnYear));


        vo.setFundIncome(fundIncome);
        vo.setCarbonCredit(carbonCreditTotal);
        vo.setCarbonQuota(carbonQuotaTotal);
        vo.setGreenScore(BigDecimal.ZERO);

        //供应量
        StatCarbonMonthVo monthSupply = new StatCarbonMonthVo();
        monthSupply.setStatDate(statDate);
        monthSupply.setCarbonCredit(carbonCreditLastMonth);
        monthSupply.setCarbonQuota(carbonQuotaLastMonth);
        monthSupply.setGreenScore(BigDecimal.ZERO);
        monthSupply.setCarbonSupply(carbonCreditLastMonth.add(carbonQuotaLastMonth));
        monthSupply.setCarbonSupplyTotal(carbonCreditTotal.add(carbonQuotaTotal));
        monthSupply.setCarbonValuation(getAmount(monthSupply.getCarbonSupplyTotal()));
        vo.setMonthSupply(monthSupply);
        //开发量
        vo.setMonthDevelopment(monthSupply);

        //销售量
        double pfmAmountCurrent=dataPanelMapper.getPerformanceAmount(statDate,null);
        double pfmCreditAmountLastMonth=dataPanelMapper.getPerformanceAmount(lastMonth,"0140000001");
        double pfmQuotaAmountLastMonth=dataPanelMapper.getPerformanceAmount(lastMonth,"0140000002");
        double pfmAmountTotal=dataPanelMapper.getPerformanceAmount(null,null);

        StatCarbonMonthVo monthSales = new StatCarbonMonthVo();
        monthSales.setStatDate(statDate);
        monthSales.setCarbonCredit(BigDecimal.valueOf(pfmCreditAmountLastMonth));
        monthSales.setCarbonQuota(BigDecimal.valueOf(pfmQuotaAmountLastMonth));
        monthSales.setGreenScore(BigDecimal.ZERO);
        monthSales.setCarbonSupply(BigDecimal.valueOf(pfmCreditAmountLastMonth+pfmQuotaAmountLastMonth));
        monthSales.setCarbonSupplyTotal(BigDecimal.valueOf(pfmAmountTotal));
        monthSales.setCarbonValuation(BigDecimal.ZERO);
        vo.setMonthSales(monthSales);

        //项目统计
        vo.setProjectStat(getCarbonProjectVo());

        vo.setAccountVo(accountVo);
        return vo;
    }

    private StatCarbonProjectVo getCarbonProjectVo() {
        StatCarbonProjectVo vo = dataPanelMapper.getProjectCountStat();

        List<StatCarbonProject> projectList  = dataPanelMapper.getProjectList();
        for (StatCarbonProject project : projectList) {
            long reduction = 0;
            try {
                reduction = Long.parseLong(StrUtil.trim(project.getReduction()));
            }catch (NumberFormatException e){
                log.error("首页-数据面板，reduction 转换异常");
            }
            project.setCarbonValuation(getAmount(BigDecimal.valueOf(reduction)));
        }
        vo.setProjectList(projectList);
        return vo;
    }

    private BigDecimal getAmount(BigDecimal carbonEmission){
        return carbonEmission.multiply(BigDecimal.valueOf(50));
    }

    @Override
    public EnterpriseGreennessVo getGreenness() {
        EnterpriseGreennessVo greennessVo = new EnterpriseGreennessVo();
        greennessVo.setCarbonEmissionRate(30.0);
        greennessVo.setEnergyEfficiencyRate(20.0);
        greennessVo.setEnvironmentRate(50.0);
        return greennessVo;
    }

}
