package muzusi.application.kis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.stock.dto.StockMinutesChartInfoDto;
import muzusi.domain.stock.entity.StockMinutes;
import muzusi.domain.stock.service.StockMinutesService;
import muzusi.infrastructure.data.StockCodeProvider;
import muzusi.infrastructure.kis.KisStockClient;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisStockService {
    private final StockCodeProvider stockCodeProvider;
    private final RedisService redisService;
    private final KisStockClient kisStockClient;
    private final StockMinutesService stockMinutesService;

    public void saveStockMinutesChart() throws InterruptedException {
        int count = 0;
        LocalDateTime now = LocalDateTime.now();
        for (String code : stockCodeProvider.getAllStockCodes()) {
            if (++count % 20 == 0) {
                Thread.sleep(1000L);
            }
            redisService.setList(KisConstant.MINUTES_CHART_PREFIX.getValue() + ":" + code, kisStockClient.getStockMinutesChartInfo(code, now));
        }
    }

    public void saveDailyStockMinutesChart() {
        for (String code : stockCodeProvider.getAllStockCodes()) {
            List<StockMinutesChartInfoDto> minutesChars = redisService.getList(KisConstant.MINUTES_CHART_PREFIX.getValue() + ":" + code).stream()
                            .map(stockMinutesChart -> (StockMinutesChartInfoDto) stockMinutesChart)
                            .toList();

            stockMinutesService.save(StockMinutes.builder()
                    .stockCode(code)
                    .date(LocalDate.now(ZoneId.of("Asia/Seoul")))
                    .minutesChart(minutesChars)
                    .build());
        }
    }
}