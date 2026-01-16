package com.fzg.script;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class LikeLuaScript {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> likeScript;
    
    public LikeLuaScript(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        // 定义Lua脚本
        String script = 
            "local userId = KEYS[1]\n" +
            "local articleId = KEYS[2]\n" +
            "local action = ARGV[1]  -- '1'点赞, '0'取消\n" +
            "local timestamp = ARGV[2]\n" +
            "\n" +
            "-- 用户点赞状态key\n" +
            "local statusKey = 'like:status:' .. userId .. ':' .. articleId\n" +
            "-- 文章点赞数key\n" +
            "local countKey = 'like:count:' .. articleId\n" +
            "-- 脏数据集合key\n" +
            "local dirtySetKey = 'like:dirty:set'\n" +
            "-- 同步记录集合key\n" +
            "local syncRecordKey = 'like:sync:record:set'\n" +
            "\n" +
            "-- 获取当前状态\n" +
            "local currentStatus = redis.call('GET', statusKey)\n" +
            "\n" +
            "-- 幂等检查：如果状态相同，直接返回\n" +
            "if currentStatus == action then\n" +
            "    return 1\n" +
            "end\n" +
            "\n" +
            "-- 更新状态\n" +
            "redis.call('SET', statusKey, action, 'EX', 86400)\n" +
            "\n" +
            "-- 确保计数器存在\n" +
            "if redis.call('EXISTS', countKey) == 0 then\n" +
            "    redis.call('SET', countKey, '0')\n" +
            "end\n" +
            "\n" +
            "-- 更新计数器\n" +
            "if action == '1' then\n" +
            "    redis.call('INCR', countKey)\n" +
            "else\n" +
            "    -- 防止减到负数\n" +
            "    local count = tonumber(redis.call('GET', countKey))\n" +
            "    if count > 0 then\n" +
            "        redis.call('DECR', countKey)\n" +
            "    end\n" +
            "end\n" +
            "\n" +
            "-- 添加到脏数据集合\n" +
            "redis.call('SADD', dirtySetKey, articleId)\n" +
            "\n" +
            "-- 添加到同步记录集合\n" +
            "local record = userId .. ':' .. articleId .. ':' .. action .. ':' .. timestamp\n" +
            "redis.call('SADD', syncRecordKey, record)\n" +
            "\n" +
            "-- 设置过期时间\n" +
            "redis.call('EXPIRE', dirtySetKey, 3600)\n" +
            "redis.call('EXPIRE', syncRecordKey, 3600)\n" +
            "\n" +
            "return 0";
        
        likeScript = new DefaultRedisScript<>();
        likeScript.setScriptText(script);
        likeScript.setResultType(Long.class);
    }
    
    /**
     * 执行点赞/取消点赞的原子操作
     * @return 0=成功, 1=幂等（状态未变）
     */
    public Long executeLike(Long userId, Long articleId, Integer action) {
        List<String> keys = Arrays.asList(
            userId.toString(),
            articleId.toString()
        );
        
        String actionStr = action.toString();
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        return redisTemplate.execute(
            likeScript,
            keys,
            actionStr,
            timestamp
        );
    }
}