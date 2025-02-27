package muzusi.application.websocket.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.kis.KisRealTimeTradeHandler;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketConnectionManager;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Component
@DependsOn("kisWebSocketConfig")
@RequiredArgsConstructor
public class WebSocketConnectionScheduler {
    private final WebSocketConnectionManager connection;
    private final KisRealTimeTradeHandler kisRealTimeTradeHandler;

    @PostConstruct
    public void init() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        boolean isWeek = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        boolean isEnabled = ((now.getHour() > 8) || (now.getHour() == 8 && now.getMinute() >= 55))
                            && ((now.getHour() < 15) || (now.getHour() == 15 && now.getMinute() < 30));

        if (!isWeek && isEnabled && !connection.isConnected())
            connection.start();
    }

    @Scheduled(cron = "0 59 8 * * 1-5")
    public void runConnectWebSocketSessionJob() {
        connection.start();
    }

    @Scheduled(cron = "0 30 15 * * 1-5")
    public void runDisconnectWebSocketToKis() {
        kisRealTimeTradeHandler.getConnectingStockCodes()
                .forEach(kisRealTimeTradeHandler::disconnect);

        if (connection.isConnected())
            connection.stop();
    }

    @Schedules({
            @Scheduled(cron = "0 * 9-14 * * 1-5"),
            @Scheduled(cron = "0 0-29 15 * * 1-5")
    })
    public void runKeepConnectionJob() {
        kisRealTimeTradeHandler.keepConnection();
    }
}