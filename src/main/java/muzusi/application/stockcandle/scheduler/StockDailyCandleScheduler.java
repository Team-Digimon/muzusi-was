package muzusi.application.stockcandle.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.market.service.MarketService;
import muzusi.application.stockcandle.service.StockDailyCandleAggregator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockDailyCandleScheduler {
    private final StockDailyCandleAggregator stockDailyCandleAggregator;
    private final MarketService marketService;
    
    @Scheduled(cron = "0 50 15 * * 1-5")
    public void runAggregateStockDailyCandleJob() {
        if (marketService.isMarketOpen()) {
            stockDailyCandleAggregator.aggregateAllStockMinuteCandleToStockDailyCandle();
        }
    }
}
