package muzusi.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.entity.StockItem;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StockInMemoryRepository {
    private static final String STOCK_ZSET_KEY = "stock:zset";
    private static final String STOCK_DATA_KEY = "stock:data";
    private static final String STOCK_SEARCH_COUNT_KEY = "stock:search:count";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void saveAll(List<StockItem> stocks) {
        for (StockItem stock : stocks) {
            try {
                String stockJson = objectMapper.writeValueAsString(stock);
                redisTemplate.opsForZSet().add(STOCK_ZSET_KEY, stock.getStockName(), 0);
                redisTemplate.opsForHash().put(STOCK_DATA_KEY, stock.getStockName(), stockJson);
            } catch (JsonProcessingException ignored) { }
        }
    }

    public List<StockItem> findByKeyword(String keyword) {
        Cursor<ZSetOperations.TypedTuple<String>> stockCursor =
                redisTemplate.opsForZSet().scan(STOCK_ZSET_KEY, ScanOptions.scanOptions().match("*" + keyword + "*").build());

        List<StockItem> matchingStocks = new ArrayList<>();
        int count = 0;

        while (stockCursor.hasNext() && count < 20) {
            String stockName = stockCursor.next().getValue();
            String stockJson = (String) redisTemplate.opsForHash().get(STOCK_DATA_KEY, stockName);
            try {
                matchingStocks.add(objectMapper.readValue(stockJson, StockItem.class));
                count++;
            } catch (JsonProcessingException ignored) { }
        }

        return matchingStocks;
    }

    public int findSearchCountByStockName(String stockName) {
        return Optional.ofNullable((String) redisTemplate.opsForHash().get(STOCK_SEARCH_COUNT_KEY, stockName))
                .map(Integer::parseInt)
                .orElse(0);
    }

    public void incrementSearchCount(String stockName) {
        redisTemplate.opsForHash().increment(STOCK_SEARCH_COUNT_KEY, stockName, 1);
    }
}
