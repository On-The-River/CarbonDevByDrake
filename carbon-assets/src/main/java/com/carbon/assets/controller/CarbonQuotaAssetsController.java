package com.carbon.assets.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.carbon.assets.service.CarbonQuotaAssetsService;
import com.carbon.assets.param.CarbonQuotaAssetsQueryParam;
import com.carbon.assets.vo.CarbonAssetsTotalVo;
import com.carbon.assets.vo.CarbonQuotaAssetsQueryVo;
import com.carbon.assets.entity.CarbonQuotaAssets;
import com.carbon.assets.common.BaseController;
import com.carbon.common.exception.CommonBizException;
import com.carbon.domain.chainmaker.api.ChainMakerServiceApi;
import com.carbon.domain.chainmaker.param.CarbonQuotaAssetsParam;
import com.carbon.domain.common.ApiResult;
import com.carbon.common.api.Paging;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

import static com.carbon.common.feishu.FeiShuAPI.handleSheetString;


/**
 * <p>
 * 碳配额资产 前端控制器
 * </p>
 *
 * @author Li Jun
 * @since 2022-04-24
 */
@Slf4j
@RestController
@RequestMapping("/carbonQuotaAssets")
@Api(value = "碳配额资产模块", tags = {"碳配额资产模块"})
public class CarbonQuotaAssetsController extends BaseController {

    @Autowired
    private CarbonQuotaAssetsService carbonQuotaAssetsService;

    @Autowired
    private ChainMakerServiceApi chainMakerServiceApi;

    /**
    * 添加碳配额资产
    */
    @PostMapping("/add")
    @ApiOperation(value = "添加碳配额资产",notes = "添加碳配额资产")
    public ApiResult<Boolean> addCarbonQuotaAssets(@Valid @RequestBody CarbonQuotaAssets carbonQuotaAssets) {
        carbonQuotaAssets.setAvailableAmount(carbonQuotaAssets.getTotal());
        //资产估值=总值*50
        carbonQuotaAssets.setValuation(carbonQuotaAssets.getTotal().multiply(new BigDecimal("50.00")));
        boolean flag = carbonQuotaAssetsService.save(carbonQuotaAssets);
        carbonQuotaAssetsService.triggerSyncToFeishu();
        //发送mq飞书审批消息
//        carbonQuotaAssetsService.SendFeishuApprove(carbonQuotaAssets);
//        try {
//            //长安链-上链
//            chainMakerServiceApi.addQuotaAssets(BeanUtil.copyProperties(carbonQuotaAssets, CarbonQuotaAssetsParam.class));
//        }catch (Exception e){
//            log.error(e.getMessage());
//        }
        return ApiResult.result(flag);
    }

    /**
    * 修改碳配额资产
    */
    @PutMapping("/update")
    @ApiOperation(value = "修改碳配额资产",notes = "修改碳配额资产")
    public ApiResult<Boolean> updateCarbonQuotaAssets(@Valid @RequestBody CarbonQuotaAssets carbonQuotaAssets) {
        boolean flag = carbonQuotaAssetsService.updateById(carbonQuotaAssets);
        carbonQuotaAssetsService.triggerSyncToFeishu();
        return ApiResult.result(flag);
    }

    /**
    * 获取碳配额资产
    */
    @GetMapping("/info/{id}")
    @ApiOperation(value = "碳配额资产-详情",notes = "查看碳配额资产")
    public ApiResult<CarbonQuotaAssetsQueryVo> getCarbonQuotaAssets(@PathVariable String id) {
        CarbonQuotaAssetsQueryVo carbonQuotaAssetsQueryVo = carbonQuotaAssetsService.getCarbonQuotaAssetsById(id);
        return ApiResult.ok(carbonQuotaAssetsQueryVo);
    }

    /**
     * 碳配额资产分页列表
     */
    @PostMapping("/getPageList")
    @ApiOperation(value = "碳配额资产-分页列表",notes = "碳配额资产分页列表")
    public ApiResult<Paging<CarbonQuotaAssetsQueryVo>> getCarbonQuotaAssetsPageList(@Valid @RequestBody(required = false) CarbonQuotaAssetsQueryParam carbonQuotaAssetsQueryParam) {
        Paging<CarbonQuotaAssetsQueryVo> paging = carbonQuotaAssetsService.getCarbonQuotaAssetsPageList(carbonQuotaAssetsQueryParam);
        return ApiResult.ok(paging);
    }

    @GetMapping("/assetsTotal")
    @ApiOperation(value = "碳配额资产-总计",notes = "碳配额资产-总计")
    public ApiResult<CarbonAssetsTotalVo> getCarbonAssetsTotal() {
        CarbonAssetsTotalVo vo = carbonQuotaAssetsService.getCarbonAssetsTotal();
        return ApiResult.ok(vo);
    }

    @GetMapping("/sync")
    public ApiResult<List<CarbonQuotaAssets>> selectAllProject() {
        return ApiResult.ok(carbonQuotaAssetsService.list());
    }

    @GetMapping("/sync/{id}")
    public ApiResult<CarbonQuotaAssets> getProjectForSync(@PathVariable Long id) {
        CarbonQuotaAssets assets = carbonQuotaAssetsService.getById(id);
        if (assets == null) {
            throw new CommonBizException("配额不存在");
        }
        return ApiResult.ok(assets);
    }

    @PostMapping("/sync/batch")
    public ApiResult<Boolean> batchUpdateProjectList(@RequestBody List<CarbonQuotaAssets> assets) {
        boolean allSuccess = true;
        for (CarbonQuotaAssets asset : assets) {
            UpdateWrapper<CarbonQuotaAssets> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", asset.getId());

            updateWrapper.set("agency_name", handleSheetString(asset.getAgencyName()));
            updateWrapper.set("issuing_agency", handleSheetString(asset.getIssuingAgency()));
            updateWrapper.set("issuing_certificates", handleSheetString(asset.getIssuingCertificates()));
            updateWrapper.set("issuing_certificates_file_name", handleSheetString(asset.getIssuingCertificatesFileName()));
            updateWrapper.set("assets_status", handleSheetString(asset.getAssetsStatus()));
            updateWrapper.set("transaction_status", handleSheetString(asset.getTransactionStatus()));
            updateWrapper.set("buy_certificate", handleSheetString(asset.getBuyCertificate()));
            updateWrapper.set("buy_certificate_file_name", handleSheetString(asset.getBuyCertificateFileName()));

            if (asset.getCarbonExchangeId() != null) {
                updateWrapper.set("carbon_exchange_id", asset.getCarbonExchangeId());
            }
            if (asset.getTenantId() != null) {
                updateWrapper.set("tenant_id", asset.getTenantId());
            }

            if (asset.getIssuingDate() != null) {
                updateWrapper.set("issuing_date", asset.getIssuingDate());
            }
            if (asset.getExpiryDate() != null) {
                updateWrapper.set("expiry_date", asset.getExpiryDate());
            }
            if (asset.getBuyDate() != null) {
                updateWrapper.set("buy_date", asset.getBuyDate());
            }

            if (asset.getBuyTotalPrice() != null) {
                updateWrapper.set("buy_total_price", asset.getBuyTotalPrice());
            }
            if (asset.getBuyUnitPrice() != null) {
                updateWrapper.set("buy_unit_price", asset.getBuyUnitPrice());
            }
            if (asset.getTotal() != null) {
                updateWrapper.set("total", asset.getTotal());
            }
            if (asset.getAvailableAmount() != null) {
                updateWrapper.set("available_amount", asset.getAvailableAmount());
            }
            if (asset.getFrozenAmount() != null) {
                updateWrapper.set("frozen_amount", asset.getFrozenAmount());
            }
            if (asset.getLockedAmount() != null) {
                updateWrapper.set("locked_amount", asset.getLockedAmount());
            }
            if (asset.getValuation() != null) {
                updateWrapper.set("valuation", asset.getValuation());
            }
            boolean success = carbonQuotaAssetsService.update(updateWrapper);
            if (!success) {
                allSuccess = false;
            }
        }
        return ApiResult.ok(allSuccess);
    }



    @GetMapping("/sync/rowCount")
    public Long rowCount() {
        return (long)carbonQuotaAssetsService.count();
    }

    @GetMapping("/onAddQuote/{assetId}/{delta}")
    public ApiResult<Boolean> onAddQuote(@PathVariable Integer assetId, @NotNull @PathVariable BigDecimal delta) {
        carbonQuotaAssetsService.onAddQuote(assetId, delta);
        return ApiResult.ok();
    }

    @GetMapping("/onCancelQuote/{assetId}/{delta}")
    public ApiResult<Boolean> onCancelQuote(@PathVariable Integer assetId, @NotNull @PathVariable BigDecimal delta) {
        carbonQuotaAssetsService.onAddQuote(assetId, delta.negate());
        return ApiResult.ok();
    }

    @GetMapping("/toFrozen/{assetId}/{quoteQuantity}/{actualQuantity}")
    public ApiResult<Boolean> toFrozen(@PathVariable Integer assetId
            , @NotNull @PathVariable BigDecimal quoteQuantity
            , @NotNull @PathVariable BigDecimal actualQuantity)
    {
        carbonQuotaAssetsService.toFrozen(assetId, quoteQuantity,actualQuantity);
        return ApiResult.ok();
    }
}

