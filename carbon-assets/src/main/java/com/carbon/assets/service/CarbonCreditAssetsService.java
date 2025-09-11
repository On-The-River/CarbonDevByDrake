package com.carbon.assets.service;

import com.carbon.assets.entity.CarbonCreditAssets;
import com.carbon.assets.param.CarbonCreditAssetsAddParam;
import com.carbon.assets.vo.CarbonAssetsTotalVo;
import com.carbon.common.service.BaseService;
import com.carbon.assets.param.CarbonCreditAssetsQueryParam;
import com.carbon.assets.vo.CarbonCreditAssetsQueryVo;
import com.carbon.common.api.Paging;
import io.swagger.models.auth.In;
import org.springframework.web.bind.annotation.PathVariable;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 碳信用资产 服务类
 * </p>
 *
 * @author Li Jun
 * @since 2022-04-24
 */
public interface CarbonCreditAssetsService extends BaseService<CarbonCreditAssets> {

    void triggerSyncToFeishu();
    /**
     * 根据ID获取查询对象
     * @param id 主键id
     * @return CarbonCreditAssetsQueryVo
     */
    CarbonCreditAssetsQueryVo getCarbonCreditAssetsById(Serializable id);

    /**
     * 获取分页对象
     * @param param CarbonCreditAssetsQueryParam
     * @return Paging<CarbonCreditAssetsQueryVo>
     */
    Paging<CarbonCreditAssetsQueryVo> getCarbonCreditAssetsPageList(CarbonCreditAssetsQueryParam param);

    /**
     * 添加碳信用资产
     * @param carbonCreditAssets 资产
     */
    void addCarbonCreditAssets(CarbonCreditAssetsAddParam carbonCreditAssets);

    CarbonAssetsTotalVo getCarbonAssetsTotal();

    @Override
    boolean removeById(Serializable id) ;

    void onAddQuote(Integer assetId, BigDecimal delta);

    void toFrozen(Integer assetId ,BigDecimal quoteQuantity ,BigDecimal actualQuantity);


}
