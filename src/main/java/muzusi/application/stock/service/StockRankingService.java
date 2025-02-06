package muzusi.application.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.kis.dto.KisDto;
import muzusi.application.stock.dto.StockRankDto;
import muzusi.domain.stock.type.StockRankingType;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockRankingService {
    private final RedisService redisService;

    public Map<String, Object> getStockRanking(StockRankingType stockRankingType) {
        return switch (stockRankingType) {
            case RISING -> getStockRankingByType(KisConstant.RISING_RANK_PREFIX, KisConstant.FLUCTUATION_RANK_TIME_PREFIX);
            case FALLING -> getStockRankingByType(KisConstant.FALLING_RANK_PREFIX, KisConstant.FLUCTUATION_RANK_TIME_PREFIX);
            case VOLUME -> getStockRankingByType(KisConstant.VOLUME_RANK_PREFIX, KisConstant.VOLUME_RANK_TIME_PREFIX);
        };
    }

    private Map<String, Object> getStockRankingByType(KisConstant rank, KisConstant time) {
        Map<String, Object> content = new HashMap<>();
        content.put("time", ((KisDto.Time) redisService.get(time.getValue())).getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        content.put("rank", redisService.getList(rank.getValue()).stream().map(obj -> (StockRankDto) obj).toList());
        return content;
    }
}