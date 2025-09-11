package com.carbon.trade.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbon.common.enums.ExpCodeEnum;
import com.carbon.common.exception.CommonBizException;
import com.carbon.domain.assets.api.AssetsServiceApi;
import com.carbon.domain.assets.vo.CarbonExchangeQueryVo;
import com.carbon.domain.chainmaker.api.ChainMakerServiceApi;
import com.carbon.domain.chainmaker.param.CarbonTradeContractParam;
import com.carbon.domain.common.ApiResult;
import com.carbon.domain.system.api.SystemServiceApi;
import com.carbon.trade.common.enums.TradeRoleEnum;
import com.carbon.trade.common.enums.TradeStatusEnum;
import com.carbon.trade.entity.CarbonTradeContract;
import com.carbon.trade.entity.CarbonTradeQuote;
import com.carbon.trade.mapper.CarbonTradeContractMapper;
import com.carbon.trade.service.CarbonTradeContractService;
import com.carbon.trade.param.CarbonTradeContractQueryParam;
import com.carbon.trade.service.CarbonTradeQuoteService;
import com.carbon.trade.vo.CarbonTradeContractQueryVo;
import com.carbon.common.service.BaseServiceImpl;
import com.carbon.common.api.Paging;
import com.carbon.trade.vo.CarbonTradeQuoteQueryVo;
import com.carbon.trade.vo.TradeContractPerformanceVo;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


/**
 * <p>
 * 碳交易履约 服务实现类
 * </p>
 *
 * @author lin rizhao
 * @since 2022-05-21
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CarbonTradeContractServiceImpl extends BaseServiceImpl<CarbonTradeContractMapper, CarbonTradeContract> implements CarbonTradeContractService {

    @Autowired
    private CarbonTradeQuoteService carbonTradeQuoteService;

    @Resource
    private CarbonTradeContractMapper carbonTradeContractMapper;
    @Autowired
    private AssetsServiceApi assetsServiceApi;

    @Autowired
    private ChainMakerServiceApi chainMakerServiceApi;

    private RestTemplate restTemplate = new RestTemplate();


    @Override
    public void addTradeContract(CarbonTradeContract tradeContract) {
        tradeContract.setStatus(TradeStatusEnum.IN_TRADE.getStatus());
        this.save(tradeContract);
    }

    @Override
    public CarbonTradeContractQueryVo getCarbonTradeContractById(Serializable id) {
        return carbonTradeContractMapper.getCarbonTradeContractById(id);
    }

    @Override
    public Paging<CarbonTradeContractQueryVo> getCarbonTradeContractPageList(CarbonTradeContractQueryParam param) {
        Long tenantId = getCurrentTenantId();
        //分页列表按更新时间降序
        Page<?> page = getPage(param);
        page.addOrder(OrderItem.desc("updated_time"));
        IPage<CarbonTradeContractQueryVo> iPage = carbonTradeContractMapper.getCarbonTradeContractPageList(page,param,tenantId);
        for (CarbonTradeContractQueryVo record : iPage.getRecords()) {
            if (tenantId.equals(record.getBuyerId())){
                record.setTradeRole(TradeRoleEnum.BUYER.getStatus());
                record.setCounterparty(record.getSellerName());
            }else {
                record.setTradeRole(TradeRoleEnum.SELLER.getStatus());
                record.setCounterparty(record.getBuyerName());
            }
        }
        return new Paging<>(iPage);
    }

    @Override
    public TradeContractPerformanceVo performance(Long tradeContractId) {


        CarbonTradeContract tradeContract = this.getById(tradeContractId);
        if (tradeContract == null){
            throw new CommonBizException("履约记录不存在");
        }

        CarbonTradeQuote targetQuote = carbonTradeQuoteService.getById(tradeContract.getTradeQuoteId());
        if(targetQuote == null){
            throw new CommonBizException("履约记录对应的交易行情不存在");
        }
        if(!TradeStatusEnum.INTENDED_TRADE.getStatus().equals(targetQuote.getStatus())){
            throw new CommonBizException("履约记录对应的交易行情状态不正确");
        }

        TradeContractPerformanceVo spawnedPfmVo = new TradeContractPerformanceVo();


        boolean update = this.lambdaUpdate().eq(CarbonTradeContract::getId, tradeContractId)
                .set(CarbonTradeContract::getStatus, TradeStatusEnum.TRADED.getStatus())
                .update();
        if (!update){
            throw new CommonBizException(ExpCodeEnum.OPERATE_FAIL_ERROR);
        }

        //update quote status
        targetQuote.setStatus(TradeStatusEnum.TRADED.getStatus());
        carbonTradeQuoteService.updateById(targetQuote);
        postQuotePerformance(targetQuote.getId().intValue(),tradeContract);


        CarbonExchangeQueryVo exchange = assetsServiceApi.getExchangeInfoByDict(tradeContract.getDeliveryExchange()).getData();
        spawnedPfmVo.setTradeContractId(tradeContractId);
        spawnedPfmVo.setExchangeWebsite(exchange == null ? "" : exchange.getWebsite());


        //长安链-上链
//        try {
//            chainMakerServiceApi.addContract(BeanUtil.copyProperties(tradeContract, CarbonTradeContractParam.class));
//        }catch (Exception e){
//            log.error("调用区块链异常！！");
//            log.error(e.getMessage());
//        }
        return spawnedPfmVo;
    }

    private void postQuotePerformance(Integer quoteId,CarbonTradeContract tradeContract)
    {
        CarbonTradeQuote targetQuote = carbonTradeQuoteService.getById(quoteId);
        if(targetQuote == null){
            throw new CommonBizException("供需行情不存在");
        }

        String assetType=targetQuote.getAssetType();
        Integer assetId=targetQuote.getAssetId();
        BigDecimal quoteQuantity=targetQuote.getTradeQuantity();
        BigDecimal actualQuantity=tradeContract.getTradeQuantity();

        String url="";
        if(assetType.equals("0140000001"))
        {
            url="http://localhost:9003/assets/carbonCreditAssets/toFrozen/"+assetId+"/"+ quoteQuantity +"/"+ actualQuantity;
        }
        else if(assetType.equals("0140000002"))
        {
            url="http://localhost:9003/assets/carbonQuotaAssets/toFrozen/"+assetId+"/"+ quoteQuantity +"/"+ actualQuantity;
        }
        restTemplate.getForEntity(url,ApiResult.class);

    }

}
