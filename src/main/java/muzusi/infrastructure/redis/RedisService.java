package muzusi.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public List<Object> getList(String key) {
        return redisTemplate.opsForList().range(key, 0 , -1);
    }
    
    public <T> List<T> getList(String key, Class<T> clazz) {
        List<Object> list = redisTemplate.opsForList().range(key, 0 , -1);
        
        return list.stream()
                .map(obj -> objectMapper.convertValue(obj, clazz))
                .toList();
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
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

    public void addToSet(String key, Object value) {
        redisTemplate.opsForSet().add(key, value);
    }

    public void removeFromSet(String key, Object value) {
        redisTemplate.opsForSet().remove(key, value);
    }

    public Set<Object> getSetMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public void addToHash(String key, Map<String, Object> value) {
        redisTemplate.opsForHash().putAll(key, value);
    }

    public Object getHash(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public Map<String, Object> getHashMultiple(String key, List<String> fields) {
        if (fields.isEmpty()) return Collections.emptyMap();

        List<Object> values = redisTemplate.opsForHash().multiGet(key, new ArrayList<>(fields));

        return IntStream.range(0, fields.size())
                .filter(i -> values.get(i) != null)
                .boxed()
                .collect(Collectors.toMap(fields::get, values::get));
    }
}