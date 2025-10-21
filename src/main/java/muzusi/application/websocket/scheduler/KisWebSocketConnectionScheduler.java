package muzusi.application.websocket.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import muzusi.application.websocket.service.KisSubscriptionManager;
import muzusi.infrastructure.kis.websocket.KisWebSocketSessionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KisWebSocketConnectionScheduler {
    private final KisWebSocketSessionManager kisWebSocketSessionManager;
    private final KisSubscriptionManager kisSubscriptionManager;

    @PostConstruct
    public void init() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        int hour = now.getHour();
        int minute = now.getMinute();
        
        boolean isWeekend = (dayOfWeek == DayOfWeek.SATURDAY) || (dayOfWeek == DayOfWeek.SUNDAY);
        boolean isMarketOpened = (hour > 8 || (hour == 8 && minute >= 55))
                                    && (hour < 15 || (hour == 15 && minute < 30));
        
        if (!isWeekend && isMarketOpened) {
            List<String> connectedSessionIds = kisWebSocketSessionManager.initializeSessions();
            kisSubscriptionManager.initialize(connectedSessionIds);
        }
    }

    @Scheduled(cron = "0 59 8 * * 1-5")
    public void runConnectKisWebSocketSessionJob() {
        List<String> connectedSessionIds = kisWebSocketSessionManager.initializeSessions();
        kisSubscriptionManager.initialize(connectedSessionIds);
    }

    @Scheduled(cron = "0 30 15 * * 1-5")
    public void runDisconnectKisWebSocketJob() {
        kisWebSocketSessionManager.closeSessions();
        kisSubscriptionManager.clearSubscriptions();
    }
}