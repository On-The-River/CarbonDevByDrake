package com.carbon.common.feishu;

import cn.hutool.core.io.resource.InputStreamResource;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.carbon.common.entity.AddResponse;
import com.carbon.common.entity.Folder;
import com.carbon.common.entity.Message;
import com.carbon.common.entity.Test;
import com.carbon.common.enums.ApprovalCodeEnum;
import com.carbon.domain.common.ApiResult;
import com.carbon.domain.common.constant.RocketMqName;
import com.carbon.domain.mq.entity.AddTradingAccountApproval;
import com.carbon.domain.mq.entity.AssetUploadApproval;
import com.carbon.domain.mq.entity.ProjectApproval;
import com.carbon.domain.mq.entity.QuotaApproval;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.carbon.common.entity.SyncConfig;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Bae
 * @version 1.0
 * @date 2022/4/29 10:27
 */
@Service
@Slf4j
public class FeiShuAPI {
    private static RestTemplate restTemplate = new RestTemplate();

    private static String cachedTenantToken = null;
    private static Long tokenExpireTime = 0L;

    public static final String app_id = "cli_a826d0701cb8100d";
    public static final String app_secret = "v7w02GXyirLViZn8djEscdgZIper1lHE";
    public static final String open_id = "ou_77eeb6ad144d6e8b4f652866a351bfd4";
    public static final String parent_node = "TNFWfyZbPlM9KedSbnyc8hBYnmd";

    @Autowired
    private RocketMQTemplate rocketMQTemplateInstance;

    private static RocketMQTemplate rocketMQTemplate;

    @PostConstruct
    public void initStaticTemplate() {
        rocketMQTemplate = this.rocketMQTemplateInstance;
    }


    /**
     * 跟进服务表
     */
    public static final String spreadsheet_token1 = "BzUew6hCDiePr4kBaM6ckNjJnOc";

    /**
     * 开发业务跟进
     */
    public static final String sheet_id_1_1 = "18105b";
    /**
     * 注册开户跟进
     */
    public static final String sheet_id_1_2 = "Uno7Rh";
    /**
     * 交易开户跟进
     */
    public static final String sheet_id_1_3 = "4h4YBu";
    /**
     * 采购客户跟进
     */
    public static final String sheet_id_1_4 = "ijLngq";
    /**
     * 出售客户跟进
     */
    public static final String sheet_id_1_5 = "yWAwGB";
    /**
     * 交易履约跟进
     */
    public static final String sheet_id_1_6 = "4jluvv";


    /**
     * 资产开发进度表
     */
    public static final String spreadsheet_token2 = "IyTEw27c5iptASkRTpTcLO9Xn0s";

    /**
     * 交易履约跟进
     */
    public static final String sheet_id_2_1 = "75eb32";



    /**
     * 获取企业自建应用app_id与app_secret
     */
    public static Map<String, Object> getEnterpriseInformation(){
        return getTenantInformation();
    }

    /**
     * 获取租户应用app_id与app_secret
     */
    public static Map<String, Object> getTenantInformation(){
        Map<String, Object> map = new HashMap<>();
        map.put("app_id", app_id);
        map.put("app_secret", app_secret);
        return map;
    }

    /**
     * 获取tenant_access_token
     */
    public static String getTenantToken(){
        if (System.currentTimeMillis() < tokenExpireTime) {
            return cachedTenantToken;
        }
        // 缓存过期，重新获取并更新缓存
        Map<String, Object> map = getTenantInformation();
        String tokenStr = HttpUtil.post("https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal", map);
        JSONObject object = JSON.parseObject(tokenStr);
        cachedTenantToken = "Bearer " + object.getString("tenant_access_token");
        tokenExpireTime = System.currentTimeMillis() + (70 * 60 * 1000); // 70分钟后过期
        return cachedTenantToken;
    }

    /**
     * 获取app信息
     */
    public static Map<String, Object> getAppInfo(String app){
        Map<String, Object> map = null;
        if ("1".equals(app)){
            map = getEnterpriseInformation();
        } else if ("2".equals(app)){
            map = getTenantInformation();
        }
        return map;
    }

    /**
     * 获取app_access_token
     */
    public static String getAppToken(String app){
        Map<String, Object> map = getAppInfo(app);
        String url = "https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal";
        String tokenStr = HttpUtil.post(url, map);
        JSONObject object = JSON.parseObject(tokenStr);
        String token = object.getString("app_access_token");
        return "Bearer " + token;
    }

    /**
     * 刷新user_access_token
     */
    public static JSONObject refreshUserToken(String refreshToken,String app){
        String appToken = getAppToken(app);
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.set("grant_type", "refresh_token");
        param.set("refresh_token", refreshToken);
        String json = param.toString();

        String url = "https://open.feishu.cn/open-apis/authen/v1/refresh_access_token";
        String result = HttpRequest.post(url).header(Header.AUTHORIZATION, appToken).body(json).execute().body();
        JSONObject object = JSON.parseObject(result);
        return JSONObject.parseObject(object.getString("data"));
    }

    /**
     * 获取用户信息
     * @param code  登录预授权码 code
     */
    public static JSONObject getUser(String code,String app){
        String appToken = getAppToken(app);
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.set("code", code);
        param.set("grant_type", "authorization_code");
        String json=param.toString();

        String url="https://open.feishu.cn/open-apis/authen/v1/access_token";
        String result = HttpRequest.post(url).header(Header.AUTHORIZATION, appToken).body(json).execute().body();
        JSONObject object= JSON.parseObject(result);
        return JSONObject.parseObject(object.getString("data"));
    }


    /**
     * 获取 云文档鉴权所需信息
     * window.webComponent.config({
     *   openId,    // 当前登录用户的open id，要确保与生成 signature 使用的 user_access_token 相对应，使用 app_access_token 时此项不填。注意：仅云文档组件可使用app_access_token
     *   signature, // 签名
     *   appId,     // 应用 appId
     *   timestamp, // 时间戳（毫秒）
     *   nonceStr,  // 随机字符串
     *   url,       // 第3步参与加密计算的url
     *   jsApiList, // 指定要使用的组件列表，请根据对应组件的开发文档填写。如云文档组件，填写['DocsComponent']
     *   locale,    // 指定组件的国际化语言：en-US-英文、zh-CN-中文、ja-JP-日文
     * }).then(res=>{
     *   // 可以在这里进行组件动态渲染
     * })
     * @param  code  登录预授权码 code
     */
    public static Message getTextAuthenticationInformation(String code, String app, String refreshToken){
        JSONObject data;
        if (refreshToken == null || "".equals(refreshToken)) {
            data = getUser(code,app);
        } else {
            data = refreshUserToken(refreshToken,app);
        }
        Message res = new Message();

        if (data == null) {
            return null;
        }
        res.setRefreshToken(data.getString("refresh_token"));

        String userToken = "Bearer "+data.getString("access_token");
        res.setOpenId(data.getString("open_id"));

        String requestUrl="https://open.feishu.cn/open-apis/jssdk/ticket/get";
        String result = HttpUtil.createRequest(Method.POST, requestUrl).header(Header.AUTHORIZATION, userToken).execute().body();
        JSONObject object= JSON.parseObject(result);
        data = JSONObject.parseObject(object.getString("data"));
        String jsapi_ticket = data.getString("ticket");

        String noncestr= RandomUtil.randomString(16);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String url="http://saas.xcarbon.cc/";

        return getMessage(app, res, jsapi_ticket, noncestr, timestamp, url);
    }

    private static Message getMessage(String app, Message res, String jsapi_ticket, String noncestr, String timestamp, String url) {
        String string1 =
                "jsapi_ticket=" + jsapi_ticket +
                        "&noncestr=" + noncestr +
                        "&timestamp="  +timestamp +
                        "&url=" + url;

        String signature = SecureUtil.sha1(string1);

        res.setSignature(signature);
        Map<String, Object> appInfo = getAppInfo(app);
        res.setAppId((String) appInfo.get("app_id"));
        res.setTimestamp(timestamp);
        res.setNonceStr(noncestr);
        res.setUrl(url);
        res.setJsApiList("DocsComponent");
        res.setLocale("zh");

        return res;
    }


    /**
     * 获取 电子表格鉴权所需信息
     * window.webComponent.config({
     *   openId,    // 当前登录用户的open id，要确保与生成 signature 使用的 user_access_token 相对应，使用 app_access_token 时此项不填。注意：仅云文档组件可使用app_access_token
     *   signature, // 签名
     *   appId,     // 应用 appId
     *   timestamp, // 时间戳（毫秒）
     *   nonceStr,  // 随机字符串
     *   url,       // 第3步参与加密计算的url
     *   jsApiList, // 指定要使用的组件列表，请根据对应组件的开发文档填写。如云文档组件，填写['DocsComponent']
     *   locale,    // 指定组件的国际化语言：en-US-英文、zh-CN-中文、ja-JP-日文
     * }).then(res=>{
     *   // 可以在这里进行组件动态渲染
     * })
     *
     * @param code   用于获取user_token
     * @param app    判定需获取文档在哪个应用
     * @param url    鉴权后重定向url
     * @param refreshToken    刷新user_token
     */
    public static Message getFormAuthenticationInformation(String code, String app, String url, String refreshToken){
        JSONObject data;
        if(refreshToken == null || refreshToken.isEmpty()){
            data = getUser(code,app);
        }else {
            data = refreshUserToken(refreshToken,app);
        }
        Message res = new Message();

        if(data==null){
            return null;
        }
        res.setRefreshToken(data.getString("refresh_token"));

        //获取user_token
        String userToken = "Bearer " + data.getString("access_token");

        //获取open_id
        res.setOpenId(data.getString("open_id"));

        //获取jsapi_ticket
        String requestUrl="https://open.feishu.cn/open-apis/jssdk/ticket/get";
        String result = HttpUtil.createRequest(Method.POST, requestUrl).header(Header.AUTHORIZATION, userToken).execute().body();
        JSONObject object= JSON.parseObject(result);
        data = JSONObject.parseObject(object.getString("data"));
        String jsapi_ticket = data.getString("ticket");

        String noncestr= RandomUtil.randomString(16);
        String timestamp = String.valueOf(System.currentTimeMillis());
        return getMessage(app, res, jsapi_ticket, noncestr, timestamp, url);
    }


    /**
     * 传入 code或refreshToken 为该用户创建文件夹 并返回链接
     */
    public static Folder createFolder(String app, String folderName, String code, String refreshToken){
        JSONObject data;
        if(refreshToken==null|| refreshToken.isEmpty()){
            data = getUser(code,app);
        }else {
            data=refreshUserToken(refreshToken,app);
        }
        Folder folder=new Folder();
        folder.setRefreshToken(data.getString("refresh_token"));
        String user_token = "Bearer "+data.getString("access_token");
        String requestUrl="https://open.feishu.cn/open-apis/drive/v1/files/create_folder";
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("name", folderName);
        param.put("folder_token", "xxxxxxxx");   //folder_token 可通过学习文档 飞书部分获取
        String json=param.toString();
        String result = HttpRequest.post(requestUrl).header(Header.AUTHORIZATION, user_token).body(json).execute().body();
        JSONObject object= JSON.parseObject(result);
        data = JSONObject.parseObject(object.getString("data"));
        folder.setUrl(data.getString("url"));
        return folder;
    }



    /**
     * 根据文档编号返回文档token
     */
    public static String getTextToken(String templateNum){
        if("0420000001".equals(templateNum)){
            return "doccnN9C9AChwRryhlXW32yI0nf";
        }else if("0420000002".equals(templateNum)){
            return "doccnsNgbESPsVgINde5qmTqwBe";
        }else if("0420000003".equals(templateNum)){
            return "doccn1rf9EqaXZcJw57zAlxDZKg";
        }else if("0420000004".equals(templateNum)){
            return "shtcnwqyEqtaFuSch4ZsE4E6RMS";
        }
        return null;
    }

    /**
     * 新增空白文章
     *
     */
    public static AddResponse addArticle(){
        String tenantToken = getTenantToken();
        String url="https://open.feishu.cn/open-apis/doc/v2/create";
        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).execute().body();
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));

        String objToken = data.getString("objToken");
        modifyPermissions(tenantToken,objToken);
        AddResponse response = new AddResponse();
        response.setObjToken(objToken);
        response.setUrl(data.getString("url"));
        return response;
    }

    /**
     * 修改文档权限
     * @param tenantToken
     * @param fileToken  文档token
     */
    public static void modifyPermissions(String tenantToken,String fileToken){
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.set("external_access", true);
        param.set("security_entity", "anyone_can_edit");
        param.set("comment_entity", "anyone_can_edit");
        param.set("share_entity", "anyone");
        param.set("link_share_entity", "anyone_editable");
        param.set("invite_external", true);
        String json=param.toString();

        String url="https://open.feishu.cn/open-apis/drive/v1/permissions/"+fileToken+"/public?type=doc";
        HttpUtil.createRequest(Method.PATCH, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();
    }

    /**
     * 获取元空间token
     * test
     */
    public static String getMyMetaSpaceToken(){
        String tenantToken = getTenantToken();
        String url = "https://open.feishu.cn/open-apis/drive/explorer/v2/folder/:folderToken/meta";
        String result = HttpUtil.createRequest(Method.GET,url).header(Header.AUTHORIZATION, tenantToken).execute().body();
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));
        return data.getString("token");
    }

    /**
     * 获取空间中文件清单
     * @return
     */
    public static String getFileUnderFolder(){
        String tenantToken = getTenantToken();
        String myMetaSpaceToken = getMyMetaSpaceToken();

        String url = "https://open.feishu.cn/open-apis/drive/explorer/v2/folder/"+myMetaSpaceToken+"/children";
        String result = HttpUtil.createRequest(Method.GET, url).header(Header.AUTHORIZATION, tenantToken).execute().body();
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));

        return data.toString();
    }

    /**
     * 获取云文档版本
     * @return
     */
    public static String getTextReversion(String fileToken){
        String tenantToken = getTenantToken();

        String url="https://open.feishu.cn/open-apis/doc/v2/"+fileToken+"/content";
        String result = HttpUtil.createRequest(Method.GET, url).header(Header.AUTHORIZATION, tenantToken).execute().body();
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));

        return data.getString("revision");
    }

    /**
     * 修改文章内容
     * @param fileToken
     * @param text
     * @param replaceText
     */
    public static void updateFileText(String fileToken,String text,String replaceText){
        String tenantToken = getTenantToken();

        if (StrUtil.isBlank(fileToken)) {
            fileToken="xxxxxx";  //folder_token 可通过学习文档 飞书部分获取
        }
        String url="https://open.feishu.cn/open-apis/doc/v2/"+fileToken+"/batch_update";

        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.set("Revision", getTextReversion(fileToken));
        JSONArray array=new JSONArray();
        String requests="{\"requestType\":\"ReplaceAllTextRequestType\",\"replaceAllTextRequest\":{\"containsText\":{\"text\":\""+text+"\",\"matchCase\":false},\"replaceText\": \""+replaceText+"\"}}";
        array.add(requests);
        param.set("Requests", array);

        String json=param.toString();
        HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();
    }

    /**
     * 修改文章内容(测试)
     * @param fileToken
     * @param text
     * @param replaceText
     */
    public static Test testUpdateFileText(String fileToken, String text, String replaceText){
        String tenantToken = getTenantToken();

        if (StrUtil.isBlank(fileToken)) {
            fileToken="xxxxxx";   //folder_token 可通过学习文档 飞书部分获取
        }
        String url="https://open.feishu.cn/open-apis/doc/v2/"+fileToken+"/batch_update";

        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.set("Revision", getTextReversion(fileToken));
        JSONArray array=new JSONArray();
        String requests="{\"requestType\":" +
                "\"ReplaceAllTextRequestType\",\"replaceAllTextRequest\"" +
                ":{\"containsText\":" +
                "{\"text\":\""+text+"\",\"matchCase\":false}," +
                "\"replaceText\": \""+replaceText+"\"" +
                "}" +
                "}";
        array.add(requests);
        param.set("Requests", array);

        String json=param.toString();
        HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();
        return new Test(fileToken,text,replaceText);
    }

    /**
     * 根据文本编号和位置修改指定区域内容
     * @param templateNum
     * @param position
     * @param replaceText
     */
    public static void updateFileByNum(String templateNum,String position,String replaceText){
        String tenantToken = getTenantToken();

        String fileToken = getTextToken(templateNum);
        String text = getTextByTemplateAndPosition(templateNum, position);

        String url="https://open.feishu.cn/open-apis/doc/v2/"+fileToken+"/batch_update";

        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.set("Revision", getTextReversion(fileToken));
        JSONArray array=new JSONArray();
        String requests="{\"requestType\":\"ReplaceAllTextRequestType\",\"replaceAllTextRequest\":{\"containsText\":{\"text\":\""+text+"\",\"matchCase\":false},\"replaceText\": \""+replaceText+"\"}}";
        array.add(requests);
        param.set("Requests", array);

        String json=param.toString();
        HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();
    }

    /**
     * 根据文档原内容修改指定区域内容
     */
    public static void updateFile(String fileToken, String text,String replaceText){
        String tenantToken = getTenantToken();
        String url="https://open.feishu.cn/open-apis/doc/v2/"+fileToken+"/batch_update";

        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.set("Revision", getTextReversion(fileToken));
        JSONArray array=new JSONArray();
        String requests="{\"requestType\":\"ReplaceAllTextRequestType\",\"replaceAllTextRequest\":{\"containsText\":{\"text\":\""+text+"\",\"matchCase\":false},\"replaceText\": \""+replaceText+"\"}}";
        array.add(requests);
        param.set("Requests", array);

        String json=param.toString();
        HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();
    }

    /**
     * 根据文章编号和位置获取初始内容
     * @param templateNum
     * @param position
     * @return
     */
    public static String getTextByTemplateAndPosition(String templateNum,String position){
        if("0420000001".equals(templateNum)){
            switch (position){
                case "0430000001":
                    return "初始项目名称";
                case "0430000002":
                    return "初始项目类别";
            }
        }else if("0420000002".equals(templateNum)){
            switch (position){
                case "0440000001":
                    return "初始准线";
                case "0440000002":
                    return "初始排放";
            }
        }else if("0420000003".equals(templateNum)){
            switch (position){
                case "0450000001":
                    return "初始单位";
                case "0450000002":
                    return "初始描述";
            }
        }
        return null;
    }


    /**
     * 更新指定工作表
     * @param spreadsheetToken
     * @param sheetId
     * @param idField     唯一确定一条数据的字段内容
     * @param data        数据
     * @param rightRange  工作表右边界字母
     */
    public static void updateForm(String spreadsheetToken,String sheetId,String idField,Object data,String rightRange){
        String tenantToken = getTenantToken();
        String updateContent = appendString(data);
        String s = searchForm(spreadsheetToken, sheetId, idField,rightRange);
        String url;
        String location1;
        String location2;
        if(s == null) {
            url="https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/"+spreadsheetToken+"/values_append";
            location1="1";
            location2="5000";
        } else {
            url="https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/"+spreadsheetToken+"/values";
            location1 = location2 = new StringBuilder(s).deleteCharAt(0).toString();
        }
        String json = "{\n" +
                "    \"valueRange\":{\n" +
                "        \"range\": \""+sheetId+"!A"+location1+":"+rightRange+location2+"\",\n" +
                "        \"values\": [\n" +
                "        [\n" +
                updateContent +
                "        ]\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();
    }


    /**
     * 查找指定内容在工作表中的位置
     * @param spreadsheetToken  表格唯一标识
     * @param sheetId  工作表唯一标识
     * @param content  查找内容
     * @return  返回指定内容在工作表的坐标
     */
    public static String searchForm(String spreadsheetToken,String sheetId,String content,String rightRange){
        String tenantToken = getTenantToken();
        String url="https://open.feishu.cn/open-apis/sheets/v3/spreadsheets/"+spreadsheetToken+"/sheets/"+sheetId+"/find";

        String json="{\n" +
                "    \"find\": \""+content+"\",\n" +
                "    \"find_condition\": {\n" +
                "        \"include_formulas\": false,\n" +
                "        \"match_case\": false,\n" +
                "        \"match_entire_cell\": true,\n" +
                "        \"range\": \""+sheetId+"!A1:"+rightRange+"200\",\n" +
                "        \"search_by_regex\": false\n" +
                "    }\n" +
                "}\n";
        log.info("searchForm-param:{}",json);

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();
        log.info("searchForm-result:{}",result);
        JSONObject object= JSON.parseObject(result);

        if (object.getInteger("code") != 0){
            return null;
        }

        JSONObject data = JSONObject.parseObject(object.getString("data"));
        JSONObject find_result = JSONObject.parseObject(data.getString("find_result"));
        com.alibaba.fastjson.JSONArray matched_cells = find_result.getJSONArray("matched_cells");

        if(matched_cells.isEmpty()) {
            return null;
        }
        return (String) matched_cells.get(0);
    }

    /**
     * 将对象中所有属性拼接为字符串返回
     */
    public static String appendString(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        StringBuilder sb=new StringBuilder();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if(field.get(o)!=null){
                    sb.append("\"");
                    sb.append(field.get(o).toString());
                    sb.append("\"");
                    sb.append(",");
                }
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
            }
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }


    /**
     * 立项审批请求
     * @param project  项目立项审批表单MQ实体类
     */
    public static String projectApproval(ProjectApproval project){
        log.info("----------发送项目立项审批----------\n{}", project);
        String tenantToken = getTenantToken();
        String url="https://www.feishu.cn/approval/openapi/v2/instance/create";
//        project.setMethodologyName("方法学");
        String form="[{\"id\":\"legalOwnerName\", \"type\": \"input\", \"value\":\""+project.getLegalPersonName()
                +"\"},{\"id\":\"legalOwnerContactNumber\", \"type\": \"input\", \"value\":\""+project.getLegalPersonPhone()
                +"\"},{\"id\":\"OwnerName\", \"type\": \"input\", \"value\":\""+project.getOwnerName()
                +"\"},{\"id\":\"projectName\", \"type\": \"input\", \"value\":\""+project.getProjectName()
                +"\"},{\"id\":\"methodologyName\", \"type\": \"input\", \"value\":\""+project.getCarbonMethodology()
                +"\"},{\"id\":\"nation\", \"type\": \"input\", \"value\":\""+project.getCountry()
                +"\"},{\"id\":\"province\", \"type\": \"input\", \"value\":\""+project.getProvince()
                +"\"},{\"id\":\"city\", \"type\": \"input\", \"value\":\""+project.getCity()
                +"\"},{\"id\":\"projectLocation\", \"type\": \"input\", \"value\":\""+project.getAddress()
                +"\"},{\"id\":\"developAgencies\", \"type\": \"input\", \"value\":\""+project.getDevelopAgencies()
                +"\"},{\"id\":\"developPrincipal\", \"type\": \"input\", \"value\":\""+project.getPrincipalName()
                +"\"},{\"id\":\"developPrincipalContactNumber\", \"type\": \"input\", \"value\":\""+project.getPrincipalPhone()
                +"\"},{\"id\":\"projectInformation\", \"type\": \"input\", \"value\":\""+project.getProjectIntroduction()+"\"}]";
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.set("approval_code", ApprovalCodeEnum.PROJECT_INITIATION_APPROVAL.getCode());
        param.set("open_id", open_id);
        param.set("form", form);

        String json=param.toString();
        log.info("projectApproval-param:{}",json);

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

        log.info("projectApproval-result:{}",result);
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));
        return data.getString("instance_code");
    }


    /**
     * 资产上传审批请求
     * @param asset  资产上传审批表单MQ实体类
     */
    public static String assetUploadApproval(AssetUploadApproval asset){
        String tenantToken = getTenantToken();

        String url="https://www.feishu.cn/approval/openapi/v2/instance/create";
        String form="[{\"id\":\"userName\", \"type\": \"input\", \"value\":\""+asset.getUserName()
                +"\"},{\"id\":\"agenciesName\", \"type\": \"input\", \"value\":\""+asset.getAgenciesName()
                +"\"},{\"id\":\"contactNumber\", \"type\": \"input\", \"value\":\""+asset.getContactNumber()
                +"\"},{\"id\":\"assetType\", \"type\": \"input\", \"value\":\""+asset.getAssetType()
                +"\"},{\"id\":\"primaryMarketHoldingInstitutions\", \"type\": \"input\", \"value\":\""+asset.getPrimaryMarketHoldingInstitutions()+
                "\"},{\"id\":\"shareholding\", \"type\": \"input\", \"value\":\""+asset.getShareholding()
                +"\"},{\"id\":\"proofOfPosition\", \"type\": \"input\", \"value\":\""+asset.getProofOfPosition()
                +"\"},{\"id\":\"issuingAgency\", \"type\": \"input\", \"value\":\""+asset.getIssuingAgency()+"\"}]";
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("approval_code", ApprovalCodeEnum.ASSETS_APPROVAL.getCode());
        param.put("open_id", open_id);
        param.put("form", form);

        String json=param.toString();
        log.info("assetUploadApproval-param:{}",json);

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

        log.info("assetUploadApproval-result:{}",result);
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));
        return data.getString("instance_code");
    }

    /**
     * 资产上传审批请求
     * @param quota  配额审批表单MQ实体类
     */
    public static String quotaApproval(QuotaApproval quota){
        String tenantToken = getTenantToken();

        String url="https://www.feishu.cn/approval/openapi/v2/instance/create";
        String form="[{\"id\":\"userName\", \"type\": \"input\", \"value\":\""+quota.getUserName()
                +"\"},{\"id\":\"agenciesName\", \"type\": \"input\", \"value\":\""+quota.getAgenciesName()
                +"\"},{\"id\":\"contactNumber\", \"type\": \"input\", \"value\":\""+quota.getContactNumber()
                +"\"},{\"id\":\"assetType\", \"type\": \"input\", \"value\":\""+quota.getAssetType()
                +"\"},{\"id\":\"primaryMarketHoldingInstitutions\", \"type\": \"input\", \"value\":\""+quota.getPrimaryMarketHoldingInstitutions()+
                "\"},{\"id\":\"shareholding\", \"type\": \"input\", \"value\":\""+quota.getShareholding()
                +"\"},{\"id\":\"proofOfPosition\", \"type\": \"input\", \"value\":\""+quota.getProofOfPosition()
                +"\"},{\"id\":\"issuingAgency\", \"type\": \"input\", \"value\":\""+quota.getIssuingAgency()+"\"}]";
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("approval_code",ApprovalCodeEnum.QUOTA_APPROVAL.getCode());
        param.put("open_id", open_id);
        param.put("form", form);

        String json=param.toString();
        log.info("quotaApproval-param:{}",json);

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

        log.info("quotaApproval-result:{}",result);
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));
        return data.getString("instance_code");
    }

    /**
     * 添加交易账户审批请求
     * @param account  添加交易账户审批表单MQ实体类
     */
    public static String addTradingAccountApproval(AddTradingAccountApproval account){
        String tenantToken = getTenantToken();

        String url="https://www.feishu.cn/approval/openapi/v2/instance/create";
        String form="[{\"id\":\"userName\", \"type\": \"input\", \"value\":\""+account.getUserName()+"\"}," +
                "{\"id\":\"agenciesName\", \"type\": \"input\", \"value\":\""+account.getAgenciesName()+"\"}," +
                "{\"id\":\"contactNumber\", \"type\": \"input\", \"value\":\""+account.getContactNumber()+"\"}," +
                "{\"id\":\"exchangeName\", \"type\": \"input\", \"value\":\""+account.getExchangeName()+"\"}," +
                "{\"id\":\"tradeAccount\", \"type\": \"input\", \"value\":\""+account.getTradeAccount()+"\"}," +
                "{\"id\":\"widget16583974324200001\", \"type\": \"input\", \"value\":\""+account.getRemark()+"\"}," +
                "{\"id\":\"widget16587490813080001\", \"type\":\"input\", \"value\":\""+account.getAccountProof()+"\"}]";
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("approval_code", ApprovalCodeEnum.TRADE_ACCOUNT_APPROVAL.getCode());
        param.put("open_id", open_id);
        param.put("form", form);

        String json=param.toString();
        log.info("addTradingAccountApproval-param:{}",json);

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

        log.info("addTradingAccountApproval-result:{}",result);
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));
        return data.getString("instance_code");
    }

    /*阿帕奇HttpClient测试*/
    public static String addProjectFile(String projectId, String projectName){
        String tenantToken = getTenantToken();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String url="https://open.feishu.cn/open-apis/drive/explorer/v2/file/copy/files/doccnX6eWrZtQtCvvGLCNtZVRlh";
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            uriBuilder.addParameter("type", "doc");
            uriBuilder.addParameter("dstFolderToken", "fldcnSjlFG6i10m4fNip8zPIcSe");
            uriBuilder.addParameter("dstName", "PDD文档模板");
            uriBuilder.addParameter("commentNeeded", "false");
            HttpPost httpPost=new HttpPost(uriBuilder.build());
            httpPost.setHeader("Authorization",tenantToken);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            String result= EntityUtils.toString(responseEntity);
            JSONObject object= JSON.parseObject(result);
            JSONObject data = JSONObject.parseObject(object.getString("data"));
            String fileToken = data.getString("token");
            modifyPermissions(tenantToken,fileToken);
            System.out.println("初始化项目------");
            updateFile(fileToken,"初始项目名称",projectName);
            return fileToken;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 上传文件到飞书root文件夹
     * @param file 文件
     * @param fileName 文件名
     * @return 文件token
     */
    public static ApiResult uploadFile(MultipartFile file, String fileName) {
        String url="https://open.feishu.cn/open-apis/drive/v1/files/upload_all";
        try {
            InputStreamResource isr = new InputStreamResource(file.getInputStream(),file.getOriginalFilename());
            String result = HttpUtil.createRequest(Method.POST, url)
                    .header(Header.AUTHORIZATION, getTenantToken())
                    .contentType("multipart/form-data")
                    .form("file_name", fileName)
                    .form("size", file.getSize())
                    .form("file", isr)
                    .form("parent_type", "explorer")
                    .form("parent_node", parent_node)
                    .execute().body();

            return JSONUtil.toBean(result, ApiResult.class);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

//    public static List<List<String>> getSheetData(String spreadsheetToken) {
//        return getSheetData(spreadsheetToken, sheet_id_2_1);
//    }

    /**
     * 获取指定工作表的数据
     * @param syncConfig 飞书表格的设置
     * @return 二维列表，每一行是一个字符串列表
     */
    public static List<List<String>> getSheetData(final SyncConfig syncConfig) {
        String sheetId = syncConfig.getSheetId();
        String spreadsheetToken = syncConfig.getFeishuFileToken();
        String endpoint = syncConfig.getControllerEndpoint();
        List<List<String>> dataList = new ArrayList<>();

        String tenantToken = getTenantToken();
        if (sheetId == null) {
            sheetId = getFirstSheetId(spreadsheetToken);
        }

        Long rowCount = restTemplate.getForObject(endpoint + "/rowCount", Long.class);
        Integer columnCount = syncConfig.getFieldMappingSize();

        if (rowCount == null) {
            log.warn("表无数据, config: {}", syncConfig);
            return dataList;
        }
        rowCount += 2;
        String url = "https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/" +
                spreadsheetToken + "/values/" + sheetId + "!A1:" + getColumnLetter(columnCount) + rowCount;

        String result = HttpUtil.createRequest(Method.GET, url)
                .header(Header.AUTHORIZATION, tenantToken)
                .execute().body();

        JSONObject object = JSON.parseObject(result);
        if (object.getInteger("code") != 0) {
            log.error("获取表格数据失败: {}", object.getString("msg"));
            return dataList;
        }

        JSONObject data = object.getJSONObject("data");
        JSONObject valueRange = data.getJSONObject("valueRange");
        com.alibaba.fastjson.JSONArray values = valueRange.getJSONArray("values");

        if (values == null) {
            log.warn("values在JSON中不存在");
            return dataList;
        }

        // int buyCertificateColumnIndex = -1;
        for (int i = 0; i < values.size(); i++) {
            com.alibaba.fastjson.JSONArray row = values.getJSONArray(i);
            List<String> rowList = new ArrayList<>();
            if (row == null) continue;
            for (int j = 0; j < row.size(); j++) {
                String str = row.getString(j);
                if (str != null && str.startsWith("[{\"link\":")) {
                    com.alibaba.fastjson.JSONArray richTextArray = row.getJSONArray(j);
                    if (richTextArray != null) {
                        StringBuilder originalJson = new StringBuilder();
                        for (int k = 0; k < richTextArray.size(); k++) {
                            JSONObject fragment = richTextArray.getJSONObject(k);
                            String fragmentText = fragment.getString("text");
                            if (fragmentText != null) {
                                originalJson.append(fragmentText);
                            }
                        }
                        str = originalJson.toString();
                    }
                }
//                else if (i == 0) {
//                    if (Objects.equals(str, "购入凭证")) {
//                        buyCertificateColumnIndex = j;
//                    }
//                }
                rowList.add(str);
            }
            dataList.add(rowList);
        }
        return dataList;
    }

    /**
     * 更新整个飞书表格的数据
     * @param spreadsheetToken 飞书表格的token
     * @param data 二维列表，每一行是一个字符串列表
     * @return 是否成功
     */
//    public static Boolean updateSheetData(String spreadsheetToken, List<List<String>> data) {
//        return updateSheetData(spreadsheetToken, sheet_id_2_1, data);
//    }

    /**
     * 更新指定工作表的数据
     * @param spreadsheetToken 飞书表格的token
     * @param sheetId 工作表的id，如果为null则使用第一个工作表
     * @param data 二维列表，每一行是一个字符串列表
     * @return 是否成功
     */
    public static void updateSheetData(String spreadsheetToken, String sheetId, List<List<String>> data) {
        String tenantToken = getTenantToken();

        // 如果未指定sheetId，获取第一个工作表
        if (sheetId == null) {
            sheetId = getFirstSheetId(spreadsheetToken);
        }

        Integer maxCols = data.stream().mapToInt(List::size).max().orElse(0);
        String range = sheetId + "!A1:" + getColumnLetter(maxCols) + data.size();

        String url = "https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/"
                + spreadsheetToken + "/values_batch_update";

        cn.hutool.json.JSONObject body = new cn.hutool.json.JSONObject();
        JSONArray valueRanges = new JSONArray();

        cn.hutool.json.JSONObject valueRange = new cn.hutool.json.JSONObject();
        valueRange.set("range", range);
        valueRange.set("values", data);
        valueRanges.add(valueRange);

        body.set("valueRanges", valueRanges);
        String rawJSON = body.toString();

        String result = HttpUtil.createRequest(Method.POST, url)
                .header("Authorization", tenantToken)
                .header(Header.CONTENT_TYPE, "application/json")
                .body(rawJSON)
                .execute().body();

        JSONObject object = JSON.parseObject(result);
        if (object.getInteger("code") != 0) {
            log.error("更新表格数据失败: {}", object.getString("msg"));
        }
    }

    public static String getSpreadsheetToken(String nodeToken) {
        String tenantToken = getTenantToken();
        String url = "https://open.feishu.cn/open-apis/wiki/v2/spaces/get_node?token=" + nodeToken;

        String result = HttpUtil.createRequest(Method.GET, url)
                .header(Header.AUTHORIZATION, tenantToken)
                .header("Content-Type", "application/json")
                .execute().body();

        JSONObject object = JSON.parseObject(result);
        if (object.getInteger("code") == 0) {
            JSONObject node = object.getJSONObject("data").getJSONObject("node");
            if (node != null && !node.isEmpty()) {
                return node.getString("obj_token");
            }
        }
        return null;
    }

    /**
     * 获取表格的第一个工作表ID
     */
    private static String getFirstSheetId(String spreadsheetToken) {
        String tenantToken = getTenantToken();
        String url = "https://open.feishu.cn/open-apis/sheets/v3/spreadsheets/" + spreadsheetToken + "/sheets/query";

        String result = HttpUtil.createRequest(Method.GET, url)
                .header(Header.AUTHORIZATION, tenantToken)
                .execute().body();

        JSONObject object = JSON.parseObject(result);
        if (object.getInteger("code") == 0) {
            JSONObject data = object.getJSONObject("data");
            com.alibaba.fastjson.JSONArray sheets = data.getJSONArray("sheets");
            if (sheets != null && !sheets.isEmpty()) {
                JSONObject firstSheet = sheets.getJSONObject(0);
                return firstSheet.getString("sheet_id");
            }
        }
        return null;
    }

    /**
     * 将列数转换为列字母（A, B, C, ..., AA, AB, ...）
     */
    private static String getColumnLetter(Integer columnNumber) {
        StringBuilder result = new StringBuilder();
        while (columnNumber > 0) {
            columnNumber--;
            result.insert(0, (char) ('A' + columnNumber % 26));
            columnNumber /= 26;
        }
        return result.toString();
    }

    public static void sendToMQTemplate(String dbName) {
        String configId = ((ApiResult<String>) Objects.requireNonNull(restTemplate.getForObject(
                "http://localhost:9002/system/syncConfig/dbToId/" + dbName, ApiResult.class
        ))).getData();
        if (configId == null) return;
        org.springframework.messaging.Message<String> message = MessageBuilder.withPayload(configId).build();
        rocketMQTemplate.syncSend(RocketMqName.DATABASE_TO_FEISHU_SYNC, message, 5000L);
    }

    public static String handleSheetString(String string) {
        return string == null ? "" : string;
    }
}
