package com.carbon.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carbon.system.entity.SyncConfig;
import org.springframework.stereotype.Service;

@Service
public interface SyncConfigService extends IService<SyncConfig> {

    /**
     * 根据飞书文件token获取同步配置
     */
    SyncConfig getByFeishuToken(String feishuFileToken);

    /**
     * 根据数据库表名获取同步配置
     */
    SyncConfig getByDatabaseTable(String databaseTable);

    /**
     * 根据项目ID获取同步配置
     */
    SyncConfig getByProjectId(String projectId);

    /**
     * 启用/禁用同步
     */
    void toggleSync(String configId, boolean enabled);
}
