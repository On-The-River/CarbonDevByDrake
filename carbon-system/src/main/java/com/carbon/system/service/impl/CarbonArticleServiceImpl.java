package com.carbon.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbon.common.entity.AddResponse;
import com.carbon.common.exception.CommonBizException;
import com.carbon.common.feishu.FeiShuAPI;
import com.carbon.common.redis.RedisService;
import com.carbon.domain.common.constant.RedisKeyName;
import com.carbon.system.config.OkHttpCli;
import com.carbon.system.entity.CarbonArticle;
import com.carbon.system.entity.CarbonArticleComment;
import com.carbon.system.mapper.CarbonArticleCommentMapper;
import com.carbon.system.mapper.CarbonArticleMapper;
import com.carbon.system.param.CarbonArticleStatuParam;
import com.carbon.system.service.CarbonArticleService;
import com.carbon.system.param.CarbonArticleQueryParam;
import com.carbon.system.service.FeishuFiletokenService;
import com.carbon.system.util.CommonUtil;
import com.carbon.system.vo.CarbonArticleAddVo;
import com.carbon.system.vo.CarbonArticleQueryVo;
import com.carbon.common.service.BaseServiceImpl;
import com.carbon.common.api.Paging;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.*;
import org.apache.http.client.methods.HttpHead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * 碳文章 服务实现类
 * </p>
 *
 * @author Li Jun
 * @since 2021-08-01
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CarbonArticleServiceImpl extends BaseServiceImpl<CarbonArticleMapper, CarbonArticle> implements CarbonArticleService {

    @Resource
    private CarbonArticleMapper carbonArticleMapper;
    @Resource
    CarbonArticleCommentMapper commentMapper;

    @Resource
    private RedisService redisService;

    @Resource
    private FeishuFiletokenService feishuFiletokenService;

    @Autowired
    RestTemplate restTemplate;



    @Override
    public CarbonArticleQueryVo getCarbonArticleById(Serializable id) {
        CarbonArticle article = carbonArticleMapper.selectById(id);
        CarbonArticleQueryVo articleQueryVo =new CarbonArticleQueryVo();
        BeanUtil.copyProperties(article,articleQueryVo);
        return articleQueryVo;
    }

    @Override
    public Paging<CarbonArticleQueryVo> getCarbonArticlePageList(CarbonArticleQueryParam param) {
        IPage<CarbonArticleQueryVo> iPage = carbonArticleMapper.getCarbonArticlePageList(getPage(param),param);
        iPage.getRecords().stream().forEach(e->{
            LambdaQueryWrapper<CarbonArticleComment> commentLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commentLambdaQueryWrapper.eq(CarbonArticleComment::getArticleId,e.getId());
            e.setCommentNumber( commentMapper.selectCount(commentLambdaQueryWrapper));
        });
        return new Paging<>(iPage);
    }

    @Override
    public Paging<CarbonArticleQueryVo> getCarbonArticlePageList2(CarbonArticleQueryParam param) {

        //pre
//        if(param!=null && param.getStatus().equals("0")){}

        //先查redis
        String key = RedisKeyName.CA_ARTICLE_LIST_KEY;
        String result = redisService.get(RedisKeyName.CA_ARTICLE_LIST_KEY);
        //如果不空
        //不存在查数据库
        IPage<CarbonArticleQueryVo> iPage = carbonArticleMapper.getCarbonArticlePageList(getPage(param),param);
        //写入redis
        Paging<CarbonArticleQueryVo> voPaging = new Paging<>(iPage);
        redisService.setEx(key, JSONUtil.toJsonStr(voPaging),1L, TimeUnit.DAYS);//过期1天
        return voPaging;
    }

    @Override
    public Paging<CarbonArticleQueryVo> getCarbonArticlePageList3(CarbonArticleQueryParam param) {
        //根据字段降序（暂时没用，后序再看）
        Page<?> page = getPage(param);
        page.addOrder(OrderItem.desc("updated_time"));
        IPage<CarbonArticleQueryVo> iPage = carbonArticleMapper.getCarbonArticlePageList3(getPage(param),param);
        return new Paging<>(iPage);
    }

    @Override
    public boolean updateArticleStatu(CarbonArticleStatuParam param) {
        //先根据id查询文章
        CarbonArticle article = carbonArticleMapper.selectById(param.getId());

        article.setStatus(param.getStatus());
        carbonArticleMapper.updateById(article);
        //更新完数据库后删除redis缓存
        redisService.delete(Collections.singleton(RedisKeyName.CA_ARTICLE_LIST_KEY));
        return true;
    }

    @Override
    public CarbonArticleAddVo testFeishu() {
        AddResponse addResponse = FeiShuAPI.addArticle();
        String token = addResponse.getObjToken();
        String url = addResponse.getUrl();
        CarbonArticleAddVo vo=new CarbonArticleAddVo();
        vo.setUrl(url);
        vo.setToken(token);
        return vo;
    }



    //这里本应该用上面的飞书接口，但是部署上线后会有调不通的情况，故先写死接口，后期可以用上面的
    @Override
    public CarbonArticleAddVo testFeishu2() {
        String tenantToken = getTenantToken();
        String url="https://open.feishu.cn/open-apis/doc/v2/create";
        String result = HttpUtil.createRequest(Method.POST, url).
                header(Header.AUTHORIZATION, tenantToken).execute().body();
        JSONObject object2= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object2.getString("data"));
        String objToken = data.getString("objToken");
        modifyPermissions(tenantToken,objToken);
        String url2 = data.getString("url");
        CarbonArticleAddVo vo=new CarbonArticleAddVo();
        vo.setUrl(url2);
        vo.setToken(objToken);

        CarbonArticle carbonArticle = new CarbonArticle();
        carbonArticle.setUrl(url2);
        this.save(carbonArticle);

        return vo;
    }

    /**
     * 将文章保存到飞书文档
     */
    @Override
    public CarbonArticleAddVo pushFeishu(CarbonArticle carbonArticle) {
        log.info("===> push carbonArticle:{}", JSONUtil.toJsonStr(carbonArticle));
        String title = carbonArticle.getTitle();
        String tenantToken = FeiShuAPI.getTenantToken();

        String createUrl = "https://open.feishu.cn/open-apis/docx/v1/documents";
        JSONObject createBody = new JSONObject();
        createBody.put("title", title);
        createBody.put("folder_token", FeiShuAPI.parent_node);

        try {
            RequestEntity<String> request = RequestEntity.post(new URI(createUrl))
                    .header(Header.AUTHORIZATION.getValue(), tenantToken)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8")
                    .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(createBody.toString());

            ResponseEntity<JSONObject> response = restTemplate.exchange(request, JSONObject.class);
            log.info("===> 创建文档响应:{}", response.getBody());
            Integer code = Objects.requireNonNull(response.getBody()).getInteger("code");
            String msg = response.getBody().getString("msg");
            if (code != 0) {
                throw new CommonBizException("调用飞书创建文档API失败,msg:" + msg);
            }

            JSONObject data = response.getBody().getJSONObject("data");
            JSONObject document = data.getJSONObject("document");
            String documentId = document.getString("document_id");

            modifyPermissions(tenantToken, documentId);

            String shareUrl = createDocumentShareLink(tenantToken, documentId);
            carbonArticle.setUrl(shareUrl);
            // carbonArticle.setFeishuDocId(documentId);
            carbonArticle.setStatus("0260000003");
            this.save(carbonArticle);

            CarbonArticleAddVo vo = new CarbonArticleAddVo();
            vo.setUrl(shareUrl);
            vo.setToken(documentId);
            return vo;
        } catch (URISyntaxException e) {
            throw new RuntimeException("URL格式错误", e);
        }
    }

    private String createDocumentShareLink(String tenantToken, String documentId) throws URISyntaxException {
        String permissionUrl = "https://open.feishu.cn/open-apis/drive/v1/metas/batch_query";

        JSONObject permissionBody = new JSONObject();
        JSONArray docs = new JSONArray();
        JSONObject document = new JSONObject();
        document.put("doc_token", documentId);
        document.put("doc_type", "docx");
        docs.add(document);
        permissionBody.put("request_docs", docs);
        permissionBody.put("with_url", true);

        ResponseEntity<JSONObject> response = restTemplate.exchange(
                RequestEntity.post(new URI(permissionUrl))
                        .header(Header.AUTHORIZATION.getValue(), tenantToken)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8")
                        .body(permissionBody.toString()),
                JSONObject.class
        );

        JSONObject result = response.getBody();
        if (result != null && result.getInteger("code") == 0) {
            log.info("分享启用成功");
        } else {
            throw new CommonBizException(result.getString("msg"));
        }

        return (String) ((Map<String, Object>) result.getObject("data", JSONObject.class).getJSONArray("metas").get(0)).get("url");
    }

    /**
     * 监听content变化更新飞书文档
     */
    @Override
    public CarbonArticleAddVo updateFeishu(CarbonArticle article) {
        return null;
    }

    /*
    飞书文章同步
     */
    @Override
    public void SyncArticle(List<Map<String, Object>> articleUrlList)
    {
        //循环拉取飞书数据到本地数据库
        for(int i=0;i<articleUrlList.size();i++)
        {
            String tenantToken = CarbonArticleServiceImpl.getTenantToken();
            String url="https://open.feishu.cn/open-apis/doc/v2/meta/"+CommonUtil.getArticleToken(articleUrlList.get(i).get("url").toString());
            String result = HttpUtil.createRequest(Method.GET, url).header(Header.AUTHORIZATION, tenantToken)
                    .execute().body();
            System.out.println(result+url+articleUrlList.get(i).get("id"));
            JSONObject object1= JSON.parseObject(result);
            //元数据结果集
            JSONObject data = JSONObject.parseObject(object1.getString("data"));

            String tenantToken2 = CarbonArticleServiceImpl.getTenantToken();
            String contenturl="https://open.feishu.cn/open-apis/doc/v2/"+CommonUtil.getArticleToken(articleUrlList.get(i).get("url").toString())+"/raw_content";
            String result2 = HttpUtil.createRequest(Method.GET, contenturl).header(Header.AUTHORIZATION, tenantToken2)
                    .execute().body();
            JSONObject object2= JSON.parseObject(result2);

            //文章信息结果集
            JSONObject data2 = JSONObject.parseObject(object2.getString("data"));
            Long id=(Long) articleUrlList.get(i).get("id");
            String title=data.getString("title");
            String author=data.getString("edit_user_name");
            String content=data2.getString("content");
            String status="0260000001";
            String category_id="0180000002"; //根据文章标题判断类型
            if(-1!=title.indexOf("产品更新")||-1!=title.indexOf("平台动态")){
                category_id="0180000003"; //平台公告
            } else if (-1!=title.indexOf("《")||-1!=title.indexOf("【")) {
                category_id="0180000001";//行业动态
            } else if (-1!=title.indexOf("?")||-1!=title.indexOf("？")) {
                category_id="0180000004";//常见问题
            } else {
                category_id="0180000002";//行业知识库
            }

            //todo

            String updated_time=CommonUtil.getTime(Long.parseLong(data.getString("edit_time")));
            //排除非更新项
            if(!articleUrlList.get(i).get("updated_time").toString().replace(".0","").equals(updated_time))
            {
                carbonArticleMapper.SyncArticle(id,title,author,content,status,category_id,updated_time);
            }
        }
    }

    /**
     * 获取tenant_access_token
     * @return
     */
    public static String getTenantToken(){
        Map map=new HashMap();
        map.put("app_id","cli_a3c8c08a7fb05013");
        map.put("app_secret","b7MktQdNpROIqi8sIeyNPWLfcK366Lp6");
        String url="https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";
        String tokenStr = HttpUtil.post(url, map);
        JSONObject object= JSON.parseObject(tokenStr);
        String token = object.getString("tenant_access_token");
        return "Bearer "+token;
    }





    /**
     * 修改文档权限
     * @param tenantToken
     * @param fileToken  文档token
     */
    public static void modifyPermissions(String tenantToken,String fileToken){
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("external_access", true);
        param.put("security_entity", "anyone_can_edit");
        param.put("comment_entity", "anyone_can_edit");
        param.put("share_entity", "anyone");
        param.put("link_share_entity", "anyone_editable");
        param.put("invite_external", true);
        String json=param.toString();



//        String url="https://open.feishu.cn/open-apis/drive/v1/permissions/"+fileToken+"/public?type=doc";
//        HttpUtil.createRequest(Method.PATCH, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

        //使用okhttp替换httpClient
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {

                    String url="https://open.feishu.cn/open-apis/drive/v1/permissions/"+fileToken+"/public?type=doc";
                    patchUrl(url, tenantToken, json);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
    static void patchUrl(String url, String tenantToken, String json) throws IOException {

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        OkHttpClient client = new OkHttpClient();//创建一个OkHttpClient实例
        Request request=new Request.Builder()
                .url(url)
                .patch(body)
                .addHeader("Authorization", tenantToken)
                .build();
        Response response=client.newCall(request).execute();
        String responseData=response.body().string();
        System.out.println(responseData);
    }


}
