package com.carbon.mq.feishu;

import com.carbon.domain.common.constant.RocketMqName;
import com.carbon.domain.system.api.SystemServiceApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 飞书变更同步到数据库的消费者
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = RocketMqName.FEISHU_TO_DATABASE_SYNC, consumerGroup = RocketMqName.FEISHU_TO_DATABASE_SYNC)
public class FeishuToDatabaseSyncReceiver implements RocketMQListener<String> {
    @Autowired
    private SystemServiceApi systemServiceApi;

    @Override
    public void onMessage(String syncConfigId) {
        try {
            log.info("收到飞书变更同步请求, syncConfigId={}", syncConfigId);
            systemServiceApi.syncFeishuToDatabase(syncConfigId);
            log.info("飞书到数据库同步完成, syncConfigId={}", syncConfigId);
        } catch (Exception e) {
            log.error("飞书到数据库同步失败, syncConfigId={}", syncConfigId, e);
        }
    }
}