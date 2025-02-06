package muzusi.application.kis.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.global.redis.RedisService;
import muzusi.infrastructure.data.StockCodeProvider;
import muzusi.infrastructure.kis.KisConstant;
import muzusi.infrastructure.kis.KisStockClient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisStockService {
    private final StockCodeProvider stockCodeProvider;
    private final RedisService redisService;
    private final KisStockClient kisStockClient;

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
}