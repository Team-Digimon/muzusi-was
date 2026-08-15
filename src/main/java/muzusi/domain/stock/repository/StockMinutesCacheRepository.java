package muzusi.domain.stock.repository;

import lombok.RequiredArgsConstructor;
import muzusi.application.stockchart.dto.StockChartDto;
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

    public void saveAll(Collection<StockChartDto> stockChartInfoList) {
        stockChartInfoList.forEach(stockChart -> {
            redisService.setList(KEY_PREFIX + ":" + stockChart.stockCode(), stockChart);
        });
    }

    public List<StockChartDto> findAll(String stockCode) {
        return redisService.getList(KEY_PREFIX + ":" + stockCode, StockChartDto.class);
    }

    public void delete(String stockCode) {
        redisService.del(KEY_PREFIX + ":" + stockCode);
    }
}