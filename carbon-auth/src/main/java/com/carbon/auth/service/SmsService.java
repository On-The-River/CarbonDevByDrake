package com.carbon.auth.service;

import java.io.IOException;

public interface SmsService {

    /**
     * 发送短信
     * @param phone 手机号
     * @param templateCode 短信模板
     * @param content 发送内容
     */
    void sendMsg(String phone, String templateCode, String content) throws IOException;

    /**
     * 发送注册验证码
     * @param phone 手机号
     */
    void sendRegisterCode(String phone);

    /**
     * 发送忘记密码 验证码
     * @param email 邮箱
     */
    void sendForgotPasswordCode(String email);

    /**
     * 校验验证码
     * @param email 邮箱
     * @param code 验证码
     * @param type 验证码类型 RedisKeyName.SMS_FORGOT_PASSWORD_EMAIL_CODE_KEY
     */
    void checkValidateCode(String email, String code,String type);


    //    @Override
    //    public void sendMsgy(String target, String templateCode, String content) {
    //        // 判断是手机号还是邮箱
    //        if (target.contains("@")) {
    //            // 是邮箱，调用邮件发送服务
    //            sendEmail(target, templateCode, content);
    //        } else {
    //            // 是手机号，保持原来的短信发送逻辑
    //            sendMsg(target, templateCode, content);
    //        }
    //    }

    /**
     * 发送邮件验证码
     * @param email 邮箱
     * @param templateCode 邮件模板
     * @param content 邮件内容
     */
    void sendEmail(String email, String templateCode, String content);

    /**
     * 发送邮件验证码
     * @param email 邮箱
     */
    void sendEmailCode(String email);
    /**
     * 校验验证码
     * @param email 邮箱
     * @param code 验证码
     * @param type 验证码类型 RedisKeyName.SMS_EMAIL_CODE_KEY
     */
    void checkEmailCode(String email, String code, String type);



}
