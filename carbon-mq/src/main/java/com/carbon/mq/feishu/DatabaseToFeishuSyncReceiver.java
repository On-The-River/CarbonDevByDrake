package com.carbon.mq.feishu;

import com.carbon.domain.common.constant.RocketMqName;
import com.carbon.domain.system.api.SystemServiceApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 数据库变更同步到飞书的消费者
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = RocketMqName.DATABASE_TO_FEISHU_SYNC, consumerGroup = RocketMqName.DATABASE_TO_FEISHU_SYNC)
public class DatabaseToFeishuSyncReceiver implements RocketMQListener<String> {
    @Autowired
    private SystemServiceApi systemServiceApi;

    @Override
    public void onMessage(String configId) {
        try {
            log.info("收到数据库变更同步请求, configId={}", configId);
            systemServiceApi.syncDatabaseToFeishu(configId);
            log.info("数据库到飞书同步完成, configId={}", configId);
        } catch (Exception e) {
            log.error("数据库到飞书同步失败, configId={}", configId, e);
        }
    }
}
