package muzusi.application.trade.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.trade.service.TradeReservationCleaner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeScheduler {
    private final TradeReservationCleaner tradeReservationCleaner;

    @Scheduled(cron = "0 30 15 * * MON-FRI")
    public void runReservationDeleteJob() {
        tradeReservationCleaner.clearReservedOrdersAtMarketClose();
    }
}
