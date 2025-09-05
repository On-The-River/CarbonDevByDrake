package com.carbon.system.service;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.carbon.common.feishu.FeiShuAPI;
import com.carbon.domain.common.ApiResult;
import com.carbon.domain.common.constant.RocketMqName;
import com.carbon.system.entity.SyncConfig;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    public void initializeSync(String feishuFileToken, String databaseTable,
                               String controllerEndpoint, LinkedHashMap<String, Object> fieldMapping) {
        SyncConfig config = new SyncConfig();
        config.setFeishuFileToken(getSpreadsheetToken(feishuFileToken));
        config.setDatabaseTable(databaseTable);
        config.setControllerEndpoint(controllerEndpoint);
        JSONObject orderedJson = new JSONObject(true);
        orderedJson.putAll(fieldMapping);
        String fieldMappingJSON = orderedJson.toString();
        config.setFieldMapping(fieldMappingJSON);
        config.setEnabled(true);
        config.setLastSyncTime(new Date());
        
        try {
            syncConfigService.save(config);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
        syncDatabaseToFeishu(config.getId());
    }

    public void syncProjectToFeishu(Long projectId) {
        List<SyncConfig> configs = syncConfigService.list();

        for (SyncConfig config : configs) {
            if (config.getSyncDirection() == 1 || config.getSyncDirection() == 2) {
                syncDatabaseToFeishu(config.getId());
            }
        }
    }

    /**
     * 获取项目数据的端点
     */
    private List<Map<String, Object>> fetchProjectData(String endpoint, Long projectId) {
        try {
            String url = "http://localhost:8080" + endpoint;
            if (projectId != null) {
                url += "/" + projectId;
            }

            ResponseEntity<ApiResult> response = restTemplate.getForEntity(url, ApiResult.class);
            ApiResult result = response.getBody();

            if (result != null && result.getCode() == 200) {
                Object data = result.getData();
                if (data instanceof List) {
                    return (List<Map<String, Object>>) data;
                } else {
                    // 单个对象包装成列表
                    return Collections.singletonList((Map<String, Object>) data);
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取项目数据失败: ", e);
            throw new RuntimeException("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 数据库数据同步到飞书
     */
    public void syncDatabaseToFeishu(String syncConfigId) {
        SyncConfig config = syncConfigService.getById(syncConfigId);
        if (!config.getEnabled()) return;

        try {
            List<Map<String, Object>> databaseData = fetchDatabaseData(config.getControllerEndpoint());
            List<List<String>> feishuData = convertToFeishuFormat(databaseData, config.getFieldMapping());

            FeiShuAPI.updateSheetData(config.getFeishuFileToken(), feishuData);
            config.setLastSyncTime(new Date());
            syncConfigService.updateById(config);

            log.info("数据库到飞书同步完成，配置ID: {}", syncConfigId);
        } catch (Exception e) {
            log.error("数据库到飞书同步失败: ", e);
            throw new RuntimeException("同步失败: " + e.getMessage());
        }
    }

    /**
     * 飞书数据同步到数据库
     */
    public void syncFeishuToDatabase(String syncConfigId) {
        SyncConfig config = syncConfigService.getById(syncConfigId);
        if (config == null || !config.getEnabled()) {
            log.warn("配置不存在, configId: {}", syncConfigId);
            return;
        }

        if (config.getLastSyncTime() != null) {
            long lastSyncMills = config.getLastSyncTime().getTime();
            long currentMills = System.currentTimeMillis();
            long timeDiff = currentMills - lastSyncMills;
            if (timeDiff < 3000) {
                log.info("3秒内已同步，跳过本次同步，configId={}", syncConfigId);
                return;
            }
        }

        try {
            List<List<String>> feishuData = FeiShuAPI.getSheetData(config.getFeishuFileToken());
            List<Map<String, Object>> databaseData = convertToDatabaseFormat(feishuData, config.getFieldMapping());
            updateDatabaseData(config.getControllerEndpoint(), databaseData);

            config.setLastSyncTime(new Date());
            syncConfigService.updateById(config);


            log.info("飞书到数据库同步完成，配置ID: {}", syncConfigId);

        } catch (Exception e) {
            log.error("飞书到数据库同步失败: ", e);
            throw new RuntimeException("同步失败: " + e.getMessage());
        }
    }

    /**
     * 处理飞书表格变更事件
     */
    public void handleFeishuSheetChange(FeiShuSheetChangeEvent event) {
        String token = event.getEvent().getFile_token();
        SyncConfig config = syncConfigService.getByFeishuToken(token);
        if (config != null && config.getEnabled()) {
            Message<String> message = MessageBuilder
                    .withPayload(config.getId())
                    .build();
            mqTemplate.send(RocketMqName.FEISHU_TO_DATABASE_SYNC, message);
        }
    }

    private List<Map<String, Object>> fetchDatabaseData(String endpoint) {
        ResponseEntity<ApiResult> response = restTemplate.getForEntity(endpoint, ApiResult.class);
        return (List<Map<String, Object>>) response.getBody().getData();
    }

    private void updateDatabaseData(String endpoint, List<Map<String, Object>> data) {
        restTemplate.postForEntity(endpoint + "/batch", data, ApiResult.class);
    }

    public void syncByProjectId(String projectId) {
        SyncConfig config = syncConfigService.getByProjectId(projectId);
        if (config != null && config.getEnabled()) {
            switch (config.getSyncDirection()) {
                case 1: // 双向同步，这里选了数据库到飞书
                case 2: // 数据库到飞书
                    syncDatabaseToFeishu(config.getId());
                    break;
                case 3: // 飞书到数据库
                    syncFeishuToDatabase(config.getId());
                    break;
            }
        }
    }

//    public List<List<String>> convertToFeishuFormat(List<CarbonProject> databaseData, String fieldMappingJson) {
//        Map<String, String> fieldMapping = JSONUtil.toBean(fieldMappingJson, Map.class);
//        List<String> headers = new ArrayList<>(fieldMapping.values());
//        List<List<String>> sheetData = new ArrayList<>();
//        sheetData.add(headers);
//
//        for (CarbonProject record : databaseData) {
//            List<String> row = new ArrayList<>();
//            for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
//                String dbField = entry.getKey();
//                Object value = record.getFromField(dbField);
//                row.add(value == null ? "" : value.toString());
//            }
//            sheetData.add(row);
//        }
//
//        return sheetData;
//    }

    private static <K, V> V getValueByKey(Set<Map.Entry<K, V>> entrySet, K targetKey) {
        if (entrySet == null || entrySet.isEmpty()) {
            return null;
        }
        for (Map.Entry<K, V> entry : entrySet) {
            if (targetKey.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
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
        return sheetData;
    }

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            // 如果直接字段访问失败，尝试getter方法
            try {
                String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                Method getter = obj.getClass().getMethod(getterName);
                return getter.invoke(obj);
            } catch (Exception ex) {
                log.warn("无法获取字段值: {} from {}", fieldName, obj.getClass().getSimpleName());
                return null;
            }
        }
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

        Map<String, Object> fieldMappingRaw = JSONUtil.toBean(fieldMappingJson, Map.class);
        Map<String, String> fieldMapping = (Map<String, String>) fieldMappingRaw.get("fieldMapping");

        // 反转映射：飞书表头 -> 数据库字段
        Map<String, String> reverseMapping = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
            reverseMapping.put(entry.getValue(), entry.getKey());
        }

        // 提取表头（第一行）
        List<String> headers = sheetData.get(0);

        // 构建数据库记录列表
        List<Map<String, Object>> databaseData = new ArrayList<>();
        // 从第二行开始是数据
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
