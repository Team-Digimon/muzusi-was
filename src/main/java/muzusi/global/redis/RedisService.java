package muzusi.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public List<Object> getList(String key) {
        return redisTemplate.opsForList().range(key, 0 , -1);
    }

    public void set(String key, Object value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    public void setList(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    public void del(String key) {
        redisTemplate.delete(key);
    }

    public boolean existed(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}