package muzusi.domain.stock.repository;

import lombok.RequiredArgsConstructor;
import muzusi.application.kis.dto.KisDto;
import muzusi.application.stock.dto.StockRankDto;
import muzusi.domain.stock.type.StockRankingType;
import muzusi.infrastructure.redis.RedisService;
import org.springframework.stereotype.Repository;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class StockRankingCacheRepository {
    private final RedisService redisService;
    
    private static final String RISING_RANK_PREFIX = "rising-rank";
    private static final String FALLING_RANK_PREFIX = "falling-rank";
    private static final String VOLUME_RANK_PREFIX = "volume-rank";
    private static final String RISING_FALLING_RANK_TIME_PREFIX = "rising-falling-rank-time";
    private static final String VOLUME_RANK_TIME_PREFIX = "volume-rank-time";
    
    public Map<String, Object> findByStockRankingType(StockRankingType stockRankingType) {
        return switch (stockRankingType) {
            case RISING -> buildStockRankingResponse(RISING_RANK_PREFIX, RISING_FALLING_RANK_TIME_PREFIX);
            case FALLING -> buildStockRankingResponse(FALLING_RANK_PREFIX, RISING_FALLING_RANK_TIME_PREFIX);
            case VOLUME -> buildStockRankingResponse(VOLUME_RANK_PREFIX, VOLUME_RANK_TIME_PREFIX);
        };
    }
    
    private Map<String, Object> buildStockRankingResponse(String rankingKey, String timeKey) {
        String time = ((KisDto.Time) redisService.get(timeKey)).getValue()
                  .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        List<StockRankDto> ranking = redisService.getList(rankingKey, StockRankDto.class);
        
        return Map.of(
                "time", time,
                "rank", ranking
        );
    }
    
    public void setByStockRankingType(StockRankingType stockRankingType, List<StockRankDto> values) {
        switch (stockRankingType) {
            case RISING -> redisService.setList(RISING_RANK_PREFIX, values);
            case FALLING -> redisService.setList(FALLING_RANK_PREFIX, values);
            case VOLUME -> redisService.setList(VOLUME_RANK_PREFIX, values);
        }
    }
    
    public void setTimeByStockRankingType(StockRankingType stockRankingType, KisDto.Time time) {
        switch (stockRankingType) {
            case RISING, FALLING -> redisService.set(RISING_FALLING_RANK_TIME_PREFIX, time);
            case VOLUME -> redisService.set(VOLUME_RANK_TIME_PREFIX, time);
        }
    }
    
    public void deleteByStockRankingType(StockRankingType stockRankingType) {
        switch (stockRankingType) {
            case RISING -> redisService.del(RISING_RANK_PREFIX);
            case FALLING -> redisService.del(FALLING_RANK_PREFIX);
            case VOLUME -> redisService.del(VOLUME_RANK_PREFIX);
        }
    }
}
