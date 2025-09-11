package com.carbon.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carbon.common.entity.SyncConfig;

public interface SyncConfigService extends IService<SyncConfig> {

    /**
     * 根据飞书文件token获取同步配置
     */
    SyncConfig getBySheetId(String sheetId);

    /**
     * 根据数据库表名获取同步配置
     */
    SyncConfig getByDatabaseTable(String databaseTable);

    /**
     * 启用/禁用同步
     */
    void toggleSync(String configId, boolean enabled);
}
