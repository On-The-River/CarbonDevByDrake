package com.carbon.auth.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.carbon.auth.service.SmsService;
import com.carbon.common.exception.CommonBizException;
import com.carbon.common.redis.RedisService;
import com.carbon.domain.common.constant.RedisKeyName;
import com.carbon.domain.common.constant.SmsConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService{

    @Autowired
    private RedisService redisService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private JavaMailSender javaMailSender; // 注入 JavaMailSender

    private static final String REGION_ID = "cn-hangzhou";
    private static final String SYS_DOMAIN = "dysmsapi.aliyuncs.com";
    private static final String SYS_VERSION = "2017-05-25";
    private static final String SYS_ACTION = "SendSms";

    // 签名
    @Value("${sms.sign}")
    private String smsSignContent;
    @Value("${aliyun.accesskey-id}")
    private String accesskeyId;
    @Value("${aliyun.accesskey-secret}")
    private String accesskeySecret;

    @Override
    public void sendMsg(String phone, String templateCode, String content)  {

        String code="123546";

        String url = String.format("https://push.spug.cc/send/%s?code=%s&targets=%s",
                templateCode, code, phone);
        BufferedReader in=null;

        URL urlObj = null;
        try {
            urlObj = new URL(url);
            HttpURLConnection requestConnection = (HttpURLConnection) urlObj.openConnection();
            requestConnection.setRequestMethod("GET");

            in = new BufferedReader(new InputStreamReader(requestConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }



        ///----starter code----
//        DefaultProfile profile = DefaultProfile.getProfile(REGION_ID, accesskeyId, accesskeySecret);
//        IAcsClient client = new DefaultAcsClient(profile);
//        CommonRequest request = new CommonRequest();
//        request.setSysMethod(MethodType.POST);
//        request.setSysDomain(SYS_DOMAIN);
//        request.setSysVersion(SYS_VERSION);
//        request.setSysAction(SYS_ACTION);
//        request.putQueryParameter("RegionId", REGION_ID);
//        request.putQueryParameter("PhoneNumbers", phone);
//        request.putQueryParameter("SignName", smsSignContent);
//        request.putQueryParameter("TemplateCode", templateCode);
//        request.putQueryParameter("TemplateParam", content);
//        try {
//            CommonResponse response = client.getCommonResponse(request);
//            log.info("调用发送短信返回结果，{}", JSONUtil.toJsonPrettyStr(response.getData()));
//
//            JSONObject resultJson= JSON.parseObject(response.getData());
//            if (!"OK".equals(resultJson.get("Message"))) {
//                throw new CommonBizException("发送失败");
//            }
//        } catch (ClientException e) {
//            e.printStackTrace();
//            throw new CommonBizException("发送失败");
//        }
        ///-END----starter code----
    }

    @Override
    public void sendRegisterCode(String phone) {
        String key = RedisKeyName.SMS_REGISTER_KEY + phone;
        String validCode = "123546";
        HashMap<String, Object> param = new HashMap<>();
        param.put("code", validCode);

        redisService.setEx(key, validCode, 60, TimeUnit.SECONDS);
        sendMsg(phone, SmsConstant.SMS_TEMPLATE_REGISTER, JSONUtil.toJsonStr(param));
    }

    @Override
    public void sendEmail(String email, String templateCode, String content) {
        try {
            // 解析验证码
            JSONObject contentJson = JSON.parseObject(content);
            String code = contentJson.getString("code");

            // 构造邮件内容
            String subject = "验证码";
            String text = "您的验证码为：" + code + "，有效期5分钟。";

            // 这里需要一个邮件服务来发送邮件
            // 可以使用 JavaMailSender 或其他邮件服务
            // 示例代码：
            // mailService.simple(email, subject, text);
            // 临时使用 HTTP 请求方式发送邮件（根据您的实际邮件服务接口调整）
//            String url = String.format("https://push.spug.cc/send/%s?code=%s&targets=%s",
//                    templateCode, "your_email_code", email);
//            URL urlObj = new URL(url);
//            HttpURLConnection requestConnection = (HttpURLConnection) urlObj.openConnection();
//            requestConnection.setRequestMethod("GET");
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(requestConnection.getInputStream()));
//            String inputLine;
//            StringBuilder response = new StringBuilder();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
            // 创建邮件消息
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject(subject);
            message.setText(text);

            // 发送邮件
            javaMailSender.send(message);

            log.info("邮件发送结果: {}", email);
        } catch (Exception e) {
            log.error("发送邮件失败，收件人地址可能无效: {}", e.getMessage());
            throw new CommonBizException("邮件发送失败，收件人地址可能无效");
        }
    }

    //    @Override
//    public void sendForgotPasswordCode(String phone) {
//        String key = RedisKeyName.SMS_FORGOT_PASSWORD_KEY + phone;
//        String validCode = "123546";
//        HashMap<String, Object> param = new HashMap<>();
//        param.put("code", validCode);
//
//        redisService.setEx(key, validCode, 60000, TimeUnit.SECONDS);
////        sendMsg(phone, SmsConstant.SMS_TEMPLATE_REGISTER, JSONUtil.toJsonStr(param));
//    }
//
//    @Override
//    public void checkValidateCode(String phone, String code,String type) {
//        if (StrUtil.isEmpty(phone) || StrUtil.isEmpty(code)) {
//            throw new CommonBizException("手机号或验证码为空");
//        }
//        String redisKey = type + phone;
//        String redisMsgCode = redisService.get(redisKey);
//        log.info("redisMsgCode：{}",redisMsgCode);
//        if (StrUtil.isEmpty(redisMsgCode)) {
//            throw new CommonBizException("验证码已过期");
//        }
//        if (!code.equals(redisMsgCode)) {
//            throw new CommonBizException("请输入正确的验证码");
//        }
//        //验证码消费后失效
//        redisService.delete(CollUtil.newArrayList(redisKey));
//    }
    @Override
    public void sendForgotPasswordCode(String email) {
        String key = RedisKeyName.SMS_FORGOT_PASSWORD_EMAIL_CODE_KEY + email;
        String validCode = RandomUtil.randomNumbers(6);
        HashMap<String, Object> param = new HashMap<>();
        param.put("code", validCode);

        redisService.setEx(key, validCode, 300, TimeUnit.SECONDS);
//        sendMsg(phone, SmsConstant.SMS_TEMPLATE_REGISTER, JSONUtil.toJsonStr(param));
        sendEmail(email, SmsConstant.SMS_FORGOT_PASSWORD_EMAIL_CODE_KEY, JSONUtil.toJsonStr(param));
    }

    @Override
    public void checkValidateCode(String email, String code,String type) {
        if (StrUtil.isEmpty(email) || StrUtil.isEmpty(code)) {
            throw new CommonBizException("邮箱或验证码为空");
        }
        String redisKey = type + email;
        String redisCode = redisService.get(redisKey);
        log.info("redisCode：{}",redisCode);
        if (StrUtil.isEmpty(redisCode)) {
            log.warn("验证码已过期或不存在: redisKey={}", redisKey);
            throw new CommonBizException("验证码已过期");
        }
        if (!code.equals(redisCode)) {
            throw new CommonBizException("请输入正确的验证码");
        }
        //验证码消费后失效
        redisService.delete(CollUtil.newArrayList(redisKey));
    }



//    @Override
//    public void sendMsg(String target, String templateCode, String content) {
//        // 判断是手机号还是邮箱
//        if (target.contains("@")) {
//            // 是邮箱，调用邮件发送服务
//            sendEmail(target, templateCode, content);
//        } else {
//            // 是手机号，保持原来的短信发送逻辑
//            sendMsg(target, templateCode, content);
//        }
//    }



    // 发送邮箱验证码
    @Override
    public void sendEmailCode(String email) {
        String key = RedisKeyName.SMS_EMAIL_CODE_KEY + email;
        String validCode = RandomUtil.randomNumbers(6); // 生成6位随机数
        HashMap<String, Object> param = new HashMap<>();
        param.put("code", validCode);

        // 存储到Redis，设置过期时间5分钟
        redisService.setEx(key, validCode, 300, TimeUnit.SECONDS);

        // 调用邮件服务发送验证码
        sendEmail(email,  SmsConstant.SMS_TEMPLATE_REGISTER, JSONUtil.toJsonStr(param));
    }

    @Override
    // 添加邮箱验证码校验方法和手机验证码一样
    public void checkEmailCode(String email, String code,String type) {
        if (StrUtil.isEmpty(email) || StrUtil.isEmpty(code)) {
            throw new CommonBizException("邮箱或验证码为空");
        }
        String redisKey = type + email;
        String redisCode = redisService.get(redisKey);
        log.info("redisCode：{}", redisCode);
        if (StrUtil.isEmpty(redisCode)) {
            throw new CommonBizException("验证码已过期");
        }
        if (!code.equals(redisCode)) {
            throw new CommonBizException("请输入正确的验证码");
        }
        // 验证码消费后失效
        redisService.delete(CollUtil.newArrayList(redisKey));
    }








}
