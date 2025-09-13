package com.carbon.system.service;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.carbon.domain.common.ApiResult;
import com.carbon.domain.common.constant.RocketMqName;
import com.carbon.common.entity.SyncConfig;
import com.carbon.common.feishu.FeiShuAPI;
import com.carbon.system.param.FeiShuSheetChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

import static com.carbon.common.feishu.FeiShuAPI.getSpreadsheetToken;

@Service
@Slf4j
public class SyncService {

    @Autowired
    private SyncConfigService syncConfigService;

    @Autowired
    private RestTemplate restTemplate;

    @Resource
    private RocketMQTemplate mqTemplate;

    /**
     * 初始化同步配置
     */
    public void initializeSync(String feishuFileToken, String sheetId, String databaseTable,
                               String controllerEndpoint, LinkedHashMap<String, Object> fieldMapping) {
        SyncConfig config = new SyncConfig();
        config.setFeishuFileToken(getSpreadsheetToken(feishuFileToken));
        config.setSheetId(sheetId);
        config.setDatabaseTable(databaseTable);
        config.setControllerEndpoint(controllerEndpoint);

        JSONObject orderedJson = new JSONObject(true);
        orderedJson.putAll(fieldMapping);
        config.setFieldMapping(orderedJson.toString());

        Map<String, String> realMapping = (Map<String, String>) fieldMapping.get("fieldMapping");
        config.setFieldMappingSize(realMapping.size());

        config.setEnabled(true);
        config.setLastSyncTime(new Date());

        try {
            syncConfigService.save(config);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
        if (!syncDatabaseToFeishu(config)) {
            syncConfigService.remove(
                    new QueryWrapper<SyncConfig>().eq("sheet_id", config.getSheetId())
            );
        }
    }

    public Boolean syncDatabaseToFeishu(String syncConfigId) {
        SyncConfig config = syncConfigService.getById(syncConfigId);
        return syncDatabaseToFeishu(config);
    }

    /**
     * 数据库数据同步到飞书
     */
    public Boolean syncDatabaseToFeishu(SyncConfig config) {
        if (config == null || !config.getEnabled()) {
            log.warn("配置已禁用, config: {}", config);
            return false;
        }

        try {
            List<Map<String, Object>> databaseData = fetchDatabaseData(config.getControllerEndpoint());
            List<List<String>> feishuData = convertToFeishuFormat(databaseData, config.getFieldMapping());

            FeiShuAPI.updateSheetData(config.getFeishuFileToken(), config.getSheetId(), feishuData);
            config.setLastSyncTime(new Date());
            syncConfigService.updateById(config);

            log.info("数据库到飞书同步完成，配置ID: {}", config.getId());
            return true;
        } catch (Exception e) {
            log.error("数据库到飞书同步失败: ", e);
            throw new RuntimeException("同步失败: " + e.getMessage());
        }
    }

    /**
     * 飞书数据同步到数据库
     */
    public Boolean syncFeishuToDatabase(String syncConfigId) {
        SyncConfig config = syncConfigService.getById(syncConfigId);
        if (config == null || !config.getEnabled()) {
            log.warn("配置不存在, configId: {}", syncConfigId);
            return false;
        }

        if (config.getLastSyncTime() != null) {
            long lastSyncMills = config.getLastSyncTime().getTime();
            long currentMills = System.currentTimeMillis();
            long timeDiff = currentMills - lastSyncMills;
            if (timeDiff < 3000) {
                log.info("3秒内已同步，跳过本次同步，configId={}", syncConfigId);
                return true;
            }
        }

        try {
            List<List<String>> feishuData = FeiShuAPI.getSheetData(config);
            int inMetaIdx = feishuData.get(0).indexOf("在meta仓库内");
            List<Map<String, Object>> databaseData = convertToDatabaseFormat(feishuData, config.getFieldMapping());

            if (config.getDatabaseTable().equals("carbon_project")) {
                int idx = 1;
                for (Map<String, Object> map : databaseData) {
                    String status = (String) map.get("projectStatus");
                    String inMeta = (String) map.get("inMeta");
                    if (inMeta == null) {
                        inMeta = "0";
                    }
                    if (status != null && status.equals("0100000013") && inMeta.equals("0")) {
                        Map<String, Object> params = new HashMap<>();
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            switch (entry.getKey()) {
                                case "projectStatus": {
                                    params.put("projectStatusCode", entry.getValue());
                                    break;
                                }
                                case "carbonMethodology": {
                                    params.put("methodologyCode", entry.getValue());
                                    break;
                                }
                                case "country": {
                                    params.put("projectCountryCode", entry.getValue());
                                    break;
                                }
                                case "province": {
                                    params.put("projectProvinceCode", entry.getValue());
                                    break;
                                }
                                case "city": {
                                    params.put("projectCityCode", entry.getValue());
                                    break;
                                }
                                default: {
                                    params.put(entry.getKey(), entry.getValue());
                                }
                            }
                        }
                        ResponseEntity<ApiResult> res = restTemplate.postForEntity(
                                "http://localhost:9003/assets/carbonMetaregistry/add",
                                params, ApiResult.class
                        );
                        if(res.getBody().getCode() == 200){
                            map.put("inMeta", "1");
                            feishuData.get(idx).set(inMetaIdx, "1");
                        }
                    }
                    idx++;
                }
            }

            updateDatabaseData(config.getControllerEndpoint(), databaseData);
            FeiShuAPI.updateSheetData(config.getFeishuFileToken(), config.getSheetId(), feishuData);

            config.setLastSyncTime(new Date());
            syncConfigService.updateById(config);

            log.info("飞书到数据库同步完成，配置ID: {}", syncConfigId);
            return true;
        } catch (Exception e) {
            log.error("飞书到数据库同步失败: ", e);
            throw new RuntimeException("同步失败: " + e.getMessage());
        }
    }

    /**
     * 处理飞书表格变更事件
     */
    public void handleFeishuSheetChange(FeiShuSheetChangeEvent event) {
        String sheetId = event.getEvent().getSheet_id();
        SyncConfig config = syncConfigService.getBySheetId(sheetId);
        if (config != null && config.getEnabled()) {
            Message<String> message = MessageBuilder
                    .withPayload(config.getId())
                    .build();
            mqTemplate.send(RocketMqName.FEISHU_TO_DATABASE_SYNC, message);
        }
    }

    private List<Map<String, Object>> fetchDatabaseData(String endpoint) {
        ResponseEntity<ApiResult> response = restTemplate.getForEntity(endpoint, ApiResult.class);
        return (List<Map<String, Object>>) Objects.requireNonNull(response.getBody()).getData();
    }

    private void updateDatabaseData(String endpoint, List<Map<String, Object>> data) {
        restTemplate.postForEntity(endpoint + "/batch", data, ApiResult.class);
    }

    /**
     * 将数据库数据转换为飞书表格格式（二维列表）
     * @param databaseData 数据库数据，每条记录是一个Map
     * @param fieldMappingJSON 字段映射JSON
     * @return 二维列表
     */
    public List<List<String>> convertToFeishuFormat(List<Map<String, Object>> databaseData, String fieldMappingJSON) {
        JSONConfig config = new JSONConfig();
        config.setOrder(true);
        JSONObject rootJson = JSONUtil.parseObj(fieldMappingJSON, config);
        JSONObject fieldMappingJson = rootJson.getJSONObject("fieldMapping");
        if (fieldMappingJson == null) {
            throw new RuntimeException("fieldMapping 不存在");
        }
        LinkedHashMap<String, String> fieldMapping = fieldMappingJson.toBean (
                new TypeReference<LinkedHashMap<String, String>>() {}
        );
        List<String> headers = new ArrayList<>(fieldMapping.values());
        List<List<String>> sheetData = new ArrayList<>();
        sheetData.add(headers);

        for (Map<String, Object> record : databaseData) {
            List<String> row = new ArrayList<>();
            for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
                String dbField = entry.getKey();
                Object value = record.get(dbField);
                row.add(value == null ? "" : value.toString());
            }
            sheetData.add(row);
        }
        List<String> emptyOne = new ArrayList<>();
        for (int i = 0; i < databaseData.size(); i++) {
            emptyOne.add("");
        }
        sheetData.add(emptyOne);
        return sheetData;
    }

    /**
     * 将飞书表格数据转换为数据库格式（记录列表）
     * @param sheetData 飞书表格数据，第一行为表头
     * @param fieldMappingJson 字段映射JSON字符串
     * @return 数据库记录列表
     */
    public List<Map<String, Object>> convertToDatabaseFormat(List<List<String>> sheetData, String fieldMappingJson) {
        if (sheetData.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Object> fieldMappingRaw = (Map<String, Object>) JSONUtil.toBean(fieldMappingJson, Map.class);
        Map<String, String> fieldMapping = (Map<String, String>) fieldMappingRaw.get("fieldMapping");

        // 反转映射
        Map<String, String> reverseMapping = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
            reverseMapping.put(entry.getValue(), entry.getKey());
        }

        List<String> headers = sheetData.get(0);
        List<Map<String, Object>> databaseData = new ArrayList<>();

        for (int i = 1; i < sheetData.size(); i++) {
            List<String> row = sheetData.get(i);
            Map<String, Object> record = new HashMap<>();
            for (int j = 0; j < headers.size() && j < row.size(); j++) {
                String header = headers.get(j);
                String dbField = reverseMapping.get(header);
                if (dbField != null) {
                    record.put(dbField, row.get(j));
                }
            }
            databaseData.add(record);
        }
        return databaseData;
    }
}
