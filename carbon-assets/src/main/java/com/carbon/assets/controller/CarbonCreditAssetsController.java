package com.carbon.assets.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.carbon.assets.param.CarbonCreditAssetsAddParam;
import com.carbon.assets.service.CarbonCreditAssetsService;
import com.carbon.assets.param.CarbonCreditAssetsQueryParam;
import com.carbon.assets.vo.CarbonAssetsTotalVo;
import com.carbon.assets.vo.CarbonCreditAssetsQueryVo;
import com.carbon.assets.entity.CarbonCreditAssets;
import com.carbon.assets.common.BaseController;
import com.carbon.common.exception.CommonBizException;
import com.carbon.domain.common.ApiResult;
import com.carbon.common.api.Paging;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

import static com.carbon.common.feishu.FeiShuAPI.handleSheetString;


/**
 * <p>
 * 碳信用资产 前端控制器
 * </p>
 *
 * @author Li Jun
 * @since 2022-04-24
 */
@Slf4j
@RestController
@RequestMapping("/carbonCreditAssets")
@Api(value = "碳信用资产模块", tags = {"碳信用资产模块"})
public class CarbonCreditAssetsController extends BaseController {

    @Autowired
    private CarbonCreditAssetsService carbonCreditAssetsService;

    /**
    * 添加碳信用资产
    */
    @PostMapping("/add")
    @ApiOperation(value = "添加碳信用资产",notes = "添加碳信用资产")
    public ApiResult<Boolean> addCarbonCreditAssets(@Valid @RequestBody CarbonCreditAssetsAddParam param) {
        carbonCreditAssetsService.addCarbonCreditAssets(param);
        return ApiResult.ok();
    }

    /**
    * 修改碳信用资产
    */
    @PutMapping("/update")
    @ApiOperation(value = "修改碳信用资产",notes = "修改碳信用资产")
    public ApiResult<Boolean> updateCarbonCreditAssets(@Valid @RequestBody CarbonCreditAssets carbonCreditAssets) {
        boolean flag = carbonCreditAssetsService.updateById(carbonCreditAssets);
        carbonCreditAssetsService.triggerSyncToFeishu();
        return ApiResult.result(flag);
    }


    /**
    * 获取碳信用资产
    */
    @GetMapping("/info/{id}")
    @ApiOperation(value = "碳信用资产-详情",notes = "查看碳信用资产")
    public ApiResult<CarbonCreditAssetsQueryVo> getCarbonCreditAssets(@PathVariable String id) {
        CarbonCreditAssetsQueryVo carbonCreditAssetsQueryVo = carbonCreditAssetsService.getCarbonCreditAssetsById(id);
        return ApiResult.ok(carbonCreditAssetsQueryVo);
    }

    /**
     * 碳信用资产分页列表
     */
    @PostMapping("/getPageList")
    @ApiOperation(value = "碳信用资产-分页列表",notes = "碳信用资产分页列表")
    public ApiResult<Paging<CarbonCreditAssetsQueryVo>> getCarbonCreditAssetsPageList(@Valid @RequestBody(required = false) CarbonCreditAssetsQueryParam carbonCreditAssetsQueryParam) {
        Paging<CarbonCreditAssetsQueryVo> paging = carbonCreditAssetsService.getCarbonCreditAssetsPageList(carbonCreditAssetsQueryParam);
        return ApiResult.ok(paging);
    }

    @GetMapping("/assetsTotal")
    @ApiOperation(value = "碳信用资产-总计",notes = "碳信用资产-总计")
    public ApiResult<CarbonAssetsTotalVo> getCarbonAssetsTotal() {
        CarbonAssetsTotalVo vo = carbonCreditAssetsService.getCarbonAssetsTotal();
        return ApiResult.ok(vo);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除碳信用对象",notes = "删除")
    public ApiResult<Boolean> deleteCarbonCreditAsset(String id) {
        boolean flag = carbonCreditAssetsService.removeById(id);
        carbonCreditAssetsService.triggerSyncToFeishu();
        return ApiResult.result(flag);
    }

    @GetMapping("/sync")
    public ApiResult<List<CarbonCreditAssets>> selectAllProject() {
        return ApiResult.ok(carbonCreditAssetsService.list());
    }

    @GetMapping("/sync/{id}")
    public ApiResult<CarbonCreditAssets> getProjectForSync(@PathVariable Long id) {
        CarbonCreditAssets assets = carbonCreditAssetsService.getById(id);
        if (assets == null) {
            throw new CommonBizException("信用不存在");
        }
        return ApiResult.ok(assets);
    }

    @PostMapping("/sync/batch")
    public ApiResult<Boolean> batchUpdateProjectList(@RequestBody List<CarbonCreditAssets> assets) {
        boolean allSuccess = true;
        for (CarbonCreditAssets asset : assets) {
            UpdateWrapper<CarbonCreditAssets> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", asset.getId());

            updateWrapper.set("certified_agency", handleSheetString(asset.getCertifiedAgency()));
            updateWrapper.set("issuing_agency", handleSheetString(asset.getIssuingAgency()));
            updateWrapper.set("issuing_certificates", handleSheetString(asset.getIssuingCertificates()));
            updateWrapper.set("issuing_certificates_file_name", handleSheetString(asset.getIssuingCertificatesFileName()));
            updateWrapper.set("assets_status", handleSheetString(asset.getAssetsStatus()));
            updateWrapper.set("transaction_status", handleSheetString(asset.getTransactionStatus()));
            updateWrapper.set("buy_certificate", handleSheetString(asset.getBuyCertificate()));
            updateWrapper.set("buy_certificate_file_name", handleSheetString(asset.getBuyCertificateFileName()));

            if (asset.getCarbonProjectId() != null) {
                updateWrapper.set("carbon_project_id", asset.getCarbonProjectId());
            }
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

            boolean success = carbonCreditAssetsService.update(updateWrapper);
            if (!success) {
                allSuccess = false;
            }
        }
        return ApiResult.ok(allSuccess);
    }


    @GetMapping("/sync/rowCount")
    public Long rowCount() {
        return (long)carbonCreditAssetsService.count();
    }

    @GetMapping("/onAddQuote/{assetId}/{delta}")
    public ApiResult<Boolean> onAddQuote(@PathVariable Integer assetId, @NotNull @PathVariable BigDecimal delta) {
        carbonCreditAssetsService.onAddQuote(assetId, delta);
        return ApiResult.ok();
    }

    @GetMapping("/onCancelQuote/{assetId}/{delta}")
    public ApiResult<Boolean> onCancelQuote(@PathVariable Integer assetId, @NotNull @PathVariable BigDecimal delta) {
        carbonCreditAssetsService.onAddQuote(assetId, delta.negate());
        return ApiResult.ok();
    }

    @GetMapping("/toFrozen/{assetId}/{quoteQuantity}/{actualQuantity}")
    public ApiResult<Boolean> toFrozen(@PathVariable Integer assetId
            , @NotNull @PathVariable BigDecimal quoteQuantity
            , @NotNull @PathVariable BigDecimal actualQuantity)
    {
        carbonCreditAssetsService.toFrozen(assetId, quoteQuantity,actualQuantity);
        return ApiResult.ok();
    }




}

