package muzusi.application.stockquote.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import muzusi.application.market.port.service.MarketService;
import muzusi.application.stockquote.service.StockQuoteSubscriptionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class StockQuoteConnectionScheduler {
    private final StockQuoteSubscriptionService stockQuoteSubscriptionService;
    private final MarketService marketService;
    
    private final LocalTime marketStart = LocalTime.of(8, 55);
    private final LocalTime marketFinish = LocalTime.of(15, 35);

    @PostConstruct
    public void init() {
        DayOfWeek day = LocalDate.now().getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return;
        
        LocalTime time = LocalTime.now();
        if (time.isBefore(marketStart) || time.isAfter(marketFinish)) return;
        
        if (marketService.isMarketOpen()) {
            stockQuoteSubscriptionService.setupSubscription();
        }
    }

    @Scheduled(cron = "0 55 8 * * 1-5")
    public void runConnectKisWebSocketSessionJob() {
        if (marketService.isMarketOpen()) {
            stockQuoteSubscriptionService.setupSubscription();
        }
    }

    @Scheduled(cron = "0 35 15 * * 1-5")
    public void runDisconnectKisWebSocketJob() {
        stockQuoteSubscriptionService.resetSubscription();
    }
}