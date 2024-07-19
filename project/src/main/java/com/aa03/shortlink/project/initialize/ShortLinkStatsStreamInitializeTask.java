package com.aa03.shortlink.project.initialize;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.aa03.shortlink.project.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_GROUP_KEY;
import static com.aa03.shortlink.project.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_TOPIC_KEY;

/**
 * 初始化短链接监控消息队列消费者组
 */
@Component
@RequiredArgsConstructor
public class ShortLinkStatsStreamInitializeTask implements InitializingBean {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!isStreamGroupExists(SHORT_LINK_STATS_STREAM_TOPIC_KEY, SHORT_LINK_STATS_STREAM_GROUP_KEY)) {
            stringRedisTemplate.opsForStream().createGroup(SHORT_LINK_STATS_STREAM_TOPIC_KEY, SHORT_LINK_STATS_STREAM_GROUP_KEY);
        }
    }

    private boolean isStreamGroupExists(String streamKey, String groupName) {
        RedisStreamCommands commands = redisConnectionFactory.getConnection().streamCommands();

        //首先检查Stream Key是否存在，否则下面代码可能会因为尝试检查不存在的Stream Key而导致异常
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(streamKey))){
            return false;
        }

        //获取streamKey下的所有groups
        StreamInfo.XInfoGroups xInfoGroups = commands.xInfoGroups(streamKey.getBytes());
        AtomicBoolean exists= new AtomicBoolean(false);
        xInfoGroups.forEach(xInfoGroup -> {
            if (xInfoGroup.groupName().equals(groupName)){
                exists.set(true);
            }
        });

        return exists.get();
    }

}
