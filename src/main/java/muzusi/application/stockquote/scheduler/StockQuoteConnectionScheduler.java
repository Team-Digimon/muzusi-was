package muzusi.application.stockquote.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.market.service.MarketService;
import muzusi.application.stockquote.service.StockQuoteSubscriptionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
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
        
        try {
            if (marketService.isMarketOpen()) {
                stockQuoteSubscriptionService.setupSubscription();
            }
        } catch (RuntimeException e) {
            log.error("한국투자증권 웹소켓 초기화(세션 연결) 중 실패가 발생하였습니다.", e);
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