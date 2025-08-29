package com.carbon.common.enums;

/**
 * 飞书事件审批定义枚举
 * @author Li Jun
 * @since 2021-06-11
 */
public enum ApprovalCodeEnum {

    ASSETS_APPROVAL("3E72423D-229A-4A19-96B0-D18D963857E1", "资产上传审批"),  //在飞书网站中创建审批后 可以将对应的审批code替换为左侧声明的code
    TRADE_ACCOUNT_APPROVAL("739FA2FD-0B8F-4271-A482-00A6D3129E08", "添加交易账户审批"),
    PROJECT_INITIATION_APPROVAL("3352885F-2AB8-4D5D-AD2B-4AB5827D84BB", "项目立项审批"),
    QUOTA_APPROVAL("E323C2DA-4A18-42D3-A596-202BBB5DDC07","碳配额审批");
    ;

    private String code;
    private String title;

    private ApprovalCodeEnum(String code, String title) {
        this.code = code;
        this.title = title;
    }

    ApprovalCodeEnum() {
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return title;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.title = message;
    }
}
