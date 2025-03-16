package muzusi.domain.stock.repository;

import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class StockPriceCacheRepository {
    private final RedisService redisService;
    private final static String KEY = KisConstant.INQUIRE_PRICE_PREFIX.getValue();
    
    public void saveAll(Map<String, Object> stockPriceMap) {
        redisService.addToHash(KEY, stockPriceMap);
    }
}