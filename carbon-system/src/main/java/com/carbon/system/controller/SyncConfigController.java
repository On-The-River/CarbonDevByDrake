package com.carbon.system.controller;

import com.carbon.common.entity.SyncConfig;
import com.carbon.domain.common.ApiResult;
import com.carbon.system.service.SyncConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/syncConfig")
public class SyncConfigController {
    @Autowired
    private SyncConfigService syncConfigService;

    @GetMapping("/id/{id}")
    public ApiResult<SyncConfig> getConfig(@PathVariable String id) {
        return ApiResult.ok(syncConfigService.getById(id));
    }

    @GetMapping("/sheet/{sheetId}")
    public ApiResult<SyncConfig> getConfigByFileToken(@PathVariable String sheetId) {
        return ApiResult.ok(syncConfigService.getBySheetId(sheetId));
    }

    @GetMapping("/db/{dbName}")
    public ApiResult<SyncConfig> getConfigByDBName(@PathVariable String dbName) {
        return ApiResult.ok(syncConfigService.getByDatabaseTable(dbName));
    }

    @GetMapping("/dbToId/{dbName}")
    public ApiResult<String> getConfigIdByDBName(@PathVariable String dbName) {
        SyncConfig config = syncConfigService.getByDatabaseTable(dbName);
        return new ApiResult<>(200, config.getId(), "", new Date());
    }

    @PutMapping("/syncEnable/{configId}")
    public ApiResult<Boolean> syncEnable(@PathVariable String configId, @RequestParam Boolean enable) {
        syncConfigService.toggleSync(configId, enable);
        return ApiResult.ok();
    }
}
