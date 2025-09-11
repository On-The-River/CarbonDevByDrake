package com.carbon.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carbon.common.entity.SyncConfig;
import com.carbon.system.mapper.SyncConfigMapper;
import com.carbon.system.service.SyncConfigService;
import org.springframework.stereotype.Service;

@Service
public class SyncConfigServiceImpl extends ServiceImpl<SyncConfigMapper, SyncConfig>
        implements SyncConfigService {

    @Override
    public SyncConfig getBySheetId(String sheetId) {
        QueryWrapper<SyncConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sheet_id", sheetId);
        queryWrapper.eq("enabled", true);
        return this.getOne(queryWrapper);
    }

    @Override
    public SyncConfig getByDatabaseTable(String databaseTable) {
        QueryWrapper<SyncConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("database_table", databaseTable);
        queryWrapper.eq("enabled", true);
        return this.getOne(queryWrapper);
    }

    @Override
    public void toggleSync(String configId, boolean enabled) {
        SyncConfig config = this.getById(configId);
        if (config != null) {
            config.setEnabled(enabled);
            this.updateById(config);
        }
    }
}
