package com.carbon.domain.system.api.hystrix;

import cn.hutool.json.JSONObject;
import com.carbon.domain.common.ApiResult;
import com.carbon.domain.mq.entity.AddTradingAccountApproval;
import com.carbon.domain.mq.entity.AssetUploadApproval;
import com.carbon.domain.mq.entity.ProjectApproval;
import com.carbon.domain.mq.entity.QuotaApproval;
import com.carbon.domain.system.api.SystemServiceApi;
import com.carbon.domain.system.param.ChangePasswordParam;
import com.carbon.domain.system.param.SysAccountParam;
import com.carbon.domain.system.vo.SysAccountModelVo;
import com.carbon.domain.system.vo.SysTenantModelVo;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Li Jun
 */
@Component
public class SystemServiceApiFallback implements FallbackFactory<SystemServiceApi> {
	private final RestTemplate restTemplate = new RestTemplate();
	private final String systemURL = "http://localhost:9002/system";

	@Override
	public SystemServiceApi create(Throwable arg0) {

		ApiResult result = ApiResult.fail("系统服务不可用");

		return new SystemServiceApi() {


			@Override
			public ApiResult syncDatabaseToFeishu(@Valid @PathVariable String configId) {
				System.out.println("syncProjectToFeishu Dispatched");
				return restTemplate.postForObject(
						systemURL + "/feishu/syncToFeishu/" + configId, null, ApiResult.class
				);
			}

            @Override
			public ApiResult syncFeishuToDatabase(@Valid @PathVariable  String syncConfigId) {
				System.out.println("syncFeishuToDatabase Dispatched");
				return restTemplate.postForObject(
						systemURL + "/feishu/syncToDatabase/" + syncConfigId, null, ApiResult.class
				);
			}

			@Override
			public ApiResult<Boolean> addSysAccount(@Valid SysAccountParam param) {
				System.out.println("addSysAccount Dispatched");
				return restTemplate.postForObject(systemURL + "/sysAccount/add", param, ApiResult.class);
			}

			@Override
			public ApiResult<Boolean> updatePassword(@Valid ChangePasswordParam param) {
				System.out.println("updatePassword Dispatched");
				return restTemplate.postForObject(systemURL + "/sysAccount/update/password", param, ApiResult.class);
			}

			@Override
			public ApiResult<List<SysAccountModelVo>> getAccountList() {
				return result;
			}

            @Override
            public ApiResult<List<SysTenantModelVo>> getTenantList() {
				return result;
            }

			@Override
			public ApiResult<Boolean> addAssetsApproval(@Valid AssetUploadApproval approval) {
				System.out.println("addAssetsApproval Dispatched");
				return restTemplate.postForObject(systemURL + "/approval/addAssetsApproval", approval, ApiResult.class);
			}

			@Override
			public ApiResult<Boolean> addQuotaApproval(QuotaApproval approval) {
				System.out.println("addQuotaApproval Dispatched");
				return restTemplate.postForObject(systemURL + "/approval/addQuotaApproval", approval, ApiResult.class);
			}

			@Override
			public ApiResult<Boolean> addTradeAccountApproval(@Valid AddTradingAccountApproval approval) {
				System.out.println("addTradeAccountApproval Dispatched");
				return restTemplate.postForObject(systemURL + "/approval/addTradeAccountApproval", approval, ApiResult.class);
			}

			@Override
			public ApiResult<Boolean> addProjectApproval(@Valid ProjectApproval approval) {
				System.out.println("addProjectApproval Dispatched");
				return restTemplate.postForObject(systemURL + "/approval/addProjectApproval", approval, ApiResult.class);
			}

			@Override
			public ApiResult pushArticle(JSONObject jsonObject) {
				System.out.println("addProjectApproval Dispatched");
				return restTemplate.postForObject(systemURL + "/carbonArticle/push", jsonObject, ApiResult.class);
			}
		};
	}
}
