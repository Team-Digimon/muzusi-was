package muzusi.application.trade.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.trade.service.TradeReservationCleaner;
import muzusi.application.trade.service.TradeReservationTrigger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeScheduler {
    private final TradeReservationCleaner tradeReservationCleaner;
    private final TradeReservationTrigger tradeReservationTrigger;

    @Scheduled(cron = "0 30 15 * * 1-5")
    public void runReservationDeleteJob() {
        tradeReservationCleaner.clearReservedOrdersAtMarketClose();
    }

    @Schedules({
            @Scheduled(cron = "0 15,25,35,45,55 9 * * 1-5"),
            @Scheduled(cron = "0 5,15,25,35,45,55 10-14 * * 1-5"),
            @Scheduled(cron = "0 5,15,25 15 * * 1-5")
    })
    public void runReservationTriggerJob() {
        tradeReservationTrigger.triggerTradeReservations();
    }
}
