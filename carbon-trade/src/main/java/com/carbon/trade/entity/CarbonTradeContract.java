package com.carbon.trade.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.carbon.domain.common.BaseEntity;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * <p>
 * 碳交易履约
 * </p>
 *
 * @author lin rizhao
 * @since 2022-05-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="CarbonTradeContract对象", description="碳交易履约")
public class CarbonTradeContract extends BaseEntity {

    private static final long serialVersionUID = 1L;

    //auto
    @ApiModelProperty(value = "主键",hidden = true)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //c
    @ApiModelProperty(value = "交易行情ID")
    private Long tradeQuoteId;

    //c-r
    @ApiModelProperty(value = "卖方-租户ID")
    private Long sellerId;

    //c-r
    @ApiModelProperty(value = "卖方机构名称")
    private String sellerName;

    //c-r
    @ApiModelProperty(value = "卖方联系人")
    private String sellerContacts;

    //c-r
    @ApiModelProperty(value = "卖方联系人电话")
    private String sellerPhone;

    //c-r
    @ApiModelProperty(value = "卖方邮箱")
    private String sellerEmail;

    //c-r
    @ApiModelProperty(value = "买方-租户ID")
    private Long buyerId;

    //c-r
    @ApiModelProperty(value = "买方机构名称")
    private String buyerName;

    //c-r
    @ApiModelProperty(value = "买方联系人")
    private String buyerContacts;

    //c-r
    @ApiModelProperty(value = "买方联系人电话")
    private String buyerPhone;

    //c-r
    @ApiModelProperty(value = "买方邮箱")
    private String buyerEmail;

    //c-r
    @ApiModelProperty(value = "资产类型（字典：014）")
    private String assetType;

    //c-r
    @ApiModelProperty(value = "项目类型（字典：004）")
    private String projectType;

    //c-r
    @ApiModelProperty(value = "交易量")
    private BigDecimal tradeQuantity;

    //c-r
    @ApiModelProperty(value = "资产单位（字典：015）")
    private String assetUnit;

    //c-r
    @ApiModelProperty(value = "资产单价")
    private BigDecimal assetUnitPrice;

    //c
    @ApiModelProperty(value = "交易状态（字典：016）")
    private String status;

    //i
    @ApiModelProperty(value = "备注")
    private String remark;

    //c-r
    @ApiModelProperty(value = "交割方式（字典：019）")
    private String deliveryMethod;

    //c-r
    @ApiModelProperty(value = "交割场所，交易所（字典：017）")
    private String deliveryExchange;

    //c-r
    @ApiModelProperty(value = "交割时间")
    private Date deliveryTime;

    //c-r
    @ApiModelProperty(value = "交易截止日期")
    private Date expirationDate;

}
