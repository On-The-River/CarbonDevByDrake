package com.carbon.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@TableName("sync_config")
public class SyncConfig {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 飞书表格token
     */
    private String feishuFileToken;

    /**
     * 数据库表名
     */
    private String databaseTable;

    /**
     * 对应的controller端点
     */
    private String controllerEndpoint;

    /**
     * 字段映射JSON字符串
     */
    private String fieldMapping;

    /**
     * 项目ID（可选，用于关联具体项目）
     */
    private String projectId;

    /**
     * 是否启用同步
     */
    private Boolean enabled;

    /**
     * 最后同步时间
     */
    private Date lastSyncTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 同步方向：1-双向，2-仅数据库到飞书，3-仅飞书到数据库
     */
    private Integer syncDirection;

    /**
     * 备注
     */
    private String remark;
}
