package muzusi.domain.stock.repository;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.StockChartInfoDto;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockMinutesCacheRepository {
    private final RedisService redisService;
    private final static String KEY_PREFIX = KisConstant.MINUTES_CHART_PREFIX.getValue();

    public void saveAll(Collection<StockChartInfoDto> stockChartInfoList) {
        stockChartInfoList.forEach(stockChartInfo -> {
            redisService.setList(KEY_PREFIX + ":" + stockChartInfo.stockCode(), stockChartInfo);
        });
    }

    public List<StockChartInfoDto> findAll(String stockCode) {
        return redisService.getList(KEY_PREFIX + ":" + stockCode).stream()
                .map(stockCharInfo -> (StockChartInfoDto) stockCharInfo)
                .toList();
    }

    public void delete(String stockCode) {
        redisService.del(KEY_PREFIX + ":" + stockCode);
    }
}