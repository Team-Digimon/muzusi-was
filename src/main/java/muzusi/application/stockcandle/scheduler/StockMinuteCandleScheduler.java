package muzusi.application.stockcandle.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.market.service.MarketService;
import muzusi.application.stockcandle.service.StockMinuteCandleCollector;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockMinuteCandleScheduler {
    private final MarketService marketService;
    private final StockMinuteCandleCollector stockMinuteCandleCollector;
    
    @Schedules({
            @Scheduled(cron = "0 10,20,30,40,50 9 * * 1-5"),
            @Scheduled(cron = "0 0/10 10-14 * * 1-5"),
            @Scheduled(cron = "0 0,10,20,30 15 * * 1-5")
    })
    public void runCollectStockMinuteCandleJob() throws InterruptedException {
        if (marketService.isMarketOpen()) {
            stockMinuteCandleCollector.collectAllStockMinuteCandle();
        }
    }
}