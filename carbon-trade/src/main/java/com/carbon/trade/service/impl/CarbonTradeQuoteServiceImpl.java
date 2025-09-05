package com.carbon.trade.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbon.common.exception.CommonBizException;
import com.carbon.domain.common.constant.RocketDelayLevelConstant;
import com.carbon.domain.common.constant.RocketMqName;
import com.carbon.domain.trade.vo.MqCarbonTradeQuote;
import com.carbon.trade.common.enums.TradeRoleEnum;
import com.carbon.trade.common.enums.TradeStatusEnum;
import com.carbon.trade.entity.CarbonTradePrice;
import com.carbon.trade.entity.CarbonTradeQuote;
import com.carbon.trade.mapper.CarbonTradeQuoteMapper;
import com.carbon.trade.param.StartTradingParam;
import com.carbon.trade.repository.EsCarbonTradeQuoteRepository;
import com.carbon.trade.service.CarbonTradePriceService;
import com.carbon.trade.service.CarbonTradeQuoteService;
import com.carbon.trade.param.CarbonTradeQuoteQueryParam;
import com.carbon.trade.vo.CarbonTradeQuoteQueryVo;
import com.carbon.common.service.BaseServiceImpl;
import com.carbon.common.api.Paging;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * <p>
 * 碳交易供需行情 服务实现类
 * </p>
 *
 * @author lin rizhao
 * @since 2022-05-21
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CarbonTradeQuoteServiceImpl extends BaseServiceImpl<CarbonTradeQuoteMapper, CarbonTradeQuote> implements CarbonTradeQuoteService {

    @Resource
    private CarbonTradeQuoteMapper carbonTradeQuoteMapper;

    @Autowired
    private CarbonTradePriceService carbonTradePriceService;
    @Autowired
    private RocketMQTemplate mqTemplate;
    @Autowired
    private EsCarbonTradeQuoteRepository carbonTradeQuoteRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public void addTradeQuote(CarbonTradeQuote tradeQuote) {

        // TODO 这里是从 项目库里面拿出 项目类型和项目领域信息
        Long projectId = tradeQuote.getProjectId();
        Optional.ofNullable(projectId).ifPresent(e->{
            com.carbon.trade.vo.MetaregistryDataVo map =  carbonTradeQuoteMapper.getCarbonMetaregistryByCarbonMetaregistryProjectId(e);
            if(map==null)
            {
                return;
            }
            tradeQuote.setProjectType(map.getProjectScopeTypeCode());
            tradeQuote.setProjectScope(map.getProjectScope());
            tradeQuote.setProjectScopeCode(map.getProjectScopeCode());
            log.info("{}",JSONUtil.toJsonStr(map));
        });


        this.save(tradeQuote);


        //发送Mq 消息
//        Message<MqCarbonTradeQuote> message = MessageBuilder
//                .withPayload(BeanUtil.copyProperties(tradeQuote, MqCarbonTradeQuote.class)).build();
//        mqTemplate.syncSend(RocketMqName.FS_TEST,message,3000,RocketDelayLevelConstant.SECOND10);
    }

    @Override
    public CarbonTradeQuoteQueryVo getCarbonTradeQuoteById(Serializable id) {
        return carbonTradeQuoteMapper.getCarbonTradeQuoteById(id);
    }

    @Override
    public Paging<CarbonTradeQuoteQueryVo> getCarbonTradeQuotePageList(CarbonTradeQuoteQueryParam param) {

//        if(param!=null)
//        {
//            if(param.getProjectType().substring(8).equals("000"))
//            {
//                param.setProjectType(null);
//            }
//
//            if(param.getProjectScope().substring(8).equals("000"))
//            {
//                param.setProjectScope(null);
//            }
//
//        }
        //分页列表按更新时间降序
        Page<?> page = getPage(param);
        page.addOrder(OrderItem.desc("ctq.updated_time"));
        IPage<CarbonTradeQuoteQueryVo> iPage = carbonTradeQuoteMapper.getCarbonTradeQuotePageList(page,param);
        return new Paging<>(iPage);
    }

    @Override
    public void startTrading(StartTradingParam param) {
        Long currentTenantId = getCurrentTenantId();

        //get the Quote will be trade.
        String tradingQuoteId = param.getTradeQuoteId();
        CarbonTradeQuote targetQuote = getById(tradingQuoteId);

        if (targetQuote == null){
            throw new CommonBizException("交易行情不存在");
        }
        if (targetQuote.getPublisherId().equals(currentTenantId)){
            //the quote is belong to your Tenant.
            throw new CommonBizException("无法询报价自己发布的行情");
        }
        if(!(TradeStatusEnum.OFFER.getStatus().equals(targetQuote.getStatus()) || TradeStatusEnum.PRE_OFFER.getStatus().equals(targetQuote.getStatus())))
        {
            throw new CommonBizException("询报价状态不正确!(仅能为状态为询报价的供需要求发起询报价)");
        }

        //on: write targetQuote's status
        if(TradeStatusEnum.PRE_OFFER.getStatus().equals(targetQuote.getStatus())){
            targetQuote.setStatus(TradeStatusEnum.OFFER.getStatus());
            updateById(targetQuote);
        }


        //生成询报价信息(spawn the price)
        CarbonTradePrice spawnedPrice = new CarbonTradePrice();
        //link the quote and price(1 to n)
        spawnedPrice.setTradeQuoteId(targetQuote.getId());
        if (TradeRoleEnum.SELLER.getStatus().equals(targetQuote.getTradeRole()))
        {
            //发布行情为卖方,当前人为买方
            spawnedPrice.setSellerId(targetQuote.getPublisherId());
            spawnedPrice.setSellerTradeQuantity(targetQuote.getTradeQuantity());
            spawnedPrice.setSellerUnitPrice(targetQuote.getAssetUnitPrice());
            spawnedPrice.setSellerDeliveryExchange(targetQuote.getDeliveryExchange());
            spawnedPrice.setSellerDeliveryMethod(targetQuote.getDeliveryMethod());
            spawnedPrice.setSellerDeliveryTime(targetQuote.getDeliveryTime());

            spawnedPrice.setBuyerId(currentTenantId);
            spawnedPrice.setProjectScope(param.getProjectScope());
            spawnedPrice.setProjectScopeCode(param.getProjectScopeCode());
            spawnedPrice.setBuyerTradeQuantity(param.getTradeQuantity());
            spawnedPrice.setBuyerUnitPrice(param.getAssetUnitPrice());
            spawnedPrice.setBuyerDeliveryExchange(param.getDeliveryExchange());
            spawnedPrice.setBuyerDeliveryMethod(param.getDeliveryMethod());
            spawnedPrice.setBuyerDeliveryTime(param.getDeliveryTime());
        }
        else if (TradeRoleEnum.BUYER.getStatus().equals(targetQuote.getTradeRole()))
        {
            //发布行情为买方,当前人为卖
            spawnedPrice.setSellerId(currentTenantId);
            spawnedPrice.setSellerTradeQuantity(param.getTradeQuantity());
            spawnedPrice.setSellerUnitPrice(param.getAssetUnitPrice());
            spawnedPrice.setSellerDeliveryExchange(param.getDeliveryExchange());
            spawnedPrice.setSellerDeliveryMethod(param.getDeliveryMethod());
            spawnedPrice.setSellerDeliveryTime(param.getDeliveryTime());

            spawnedPrice.setBuyerId(targetQuote.getPublisherId());
            spawnedPrice.setBuyerTradeQuantity(targetQuote.getTradeQuantity());
            spawnedPrice.setBuyerUnitPrice(targetQuote.getAssetUnitPrice());
            spawnedPrice.setBuyerDeliveryExchange(targetQuote.getDeliveryExchange());
            spawnedPrice.setBuyerDeliveryMethod(targetQuote.getDeliveryMethod());
            spawnedPrice.setBuyerDeliveryTime(targetQuote.getDeliveryTime());
        }
        else
        {
            throw new CommonBizException("交易行情，交易角色不匹配");
        }
        carbonTradePriceService.addTradePrice(spawnedPrice);
    }

    @Override
    public Paging<CarbonTradeQuoteQueryVo> searchByEs(String keyword, Integer pageNum, Integer pageSize) {

//        org.springframework.data.domain.Page<CarbonTradeQuoteQueryVo> page = carbonTradeQuoteRepository.findByInstitutionName( keyword, pageable);


        Pageable pageable = PageRequest.of(pageNum, pageSize);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //分页
        nativeSearchQueryBuilder.withPageable(pageable);
        if (StringUtils.isEmpty(keyword)) {
            nativeSearchQueryBuilder.withQuery(QueryBuilders.matchAllQuery());
        } else {
            List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<>();
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("institutionName", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(10)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("status", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(5)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("projectType", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(2)));
            FunctionScoreQueryBuilder.FilterFunctionBuilder[] builders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
            filterFunctionBuilders.toArray(builders);
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(builders)
                    .scoreMode(FunctionScoreQuery.ScoreMode.SUM)
                    .setMinScore(2);

            nativeSearchQueryBuilder.withQuery(functionScoreQueryBuilder);


            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.fuzzyQuery("institutionName",keyword))
                    .must(QueryBuilders.fuzzyQuery("institutionName",keyword))
                    .must(QueryBuilders.fuzzyQuery("institutionName",keyword));

        }

        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();
        SearchHits<CarbonTradeQuoteQueryVo> searchHits = elasticsearchRestTemplate.search(searchQuery, CarbonTradeQuoteQueryVo.class);


        if(searchHits.getTotalHits()<=0){
            return new Paging<>(0,pageNum,new ArrayList<CarbonTradeQuoteQueryVo>());
        }

        List<CarbonTradeQuoteQueryVo> searchProductList = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return new Paging<>(searchHits.getTotalHits(),pageNum,searchProductList);
    }

    @Override
    public Integer importAll() {
        CarbonTradeQuoteQueryParam queryParam = new CarbonTradeQuoteQueryParam();
        queryParam.setSize(Integer.MAX_VALUE);
        List<CarbonTradeQuoteQueryVo> records = this.getCarbonTradeQuotePageList(queryParam).getRecords();

        //保存到ES
        carbonTradeQuoteRepository.saveAll(records);
        return records.size();
    }

}
