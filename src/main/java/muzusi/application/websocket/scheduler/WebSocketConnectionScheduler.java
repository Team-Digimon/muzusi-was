package muzusi.application.websocket.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.kis.KisAuthService;
import muzusi.infrastructure.kis.websocket.KisWebSocketConnector;
import muzusi.infrastructure.kis.websocket.KisWebSocketSessionManager;
import muzusi.infrastructure.kis.websocket.Session;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Component
@DependsOn("kisWebSocketConfig")
@RequiredArgsConstructor
public class WebSocketConnectionScheduler {
    private final List<WebSocketConnectionManager> connections;
    private final KisWebSocketSessionManager kisWebSocketSessionManager;
    private final KisWebSocketConnector kisWebSocketConnector;
    private final KisAuthService kisAuthService;

    @PostConstruct
    public void init() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        boolean isWeek = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        boolean isEnabled = ((now.getHour() > 8) || (now.getHour() == 8 && now.getMinute() >= 55))
                            && ((now.getHour() < 15) || (now.getHour() == 15 && now.getMinute() < 30));

        for (WebSocketConnectionManager connection : connections) {
            if (!isWeek && isEnabled && !connection.isConnected())
                connection.start();
        }
    }

    @Scheduled(cron = "30 58 8 * * 1-5")
    public void runConnectWebSocketSessionJob() {
        for (WebSocketConnectionManager connection : connections)
            connection.start();
    }

    @Scheduled(cron = "0 59 8 * * 1-5")
    public void runSaveWebSocketKeyJob() {
        kisWebSocketSessionManager.addWebSocketKey(kisAuthService.getWebSocketKey());
    }

    @Scheduled(cron = "0 30 15 * * 1-5")
    public void runDisconnectWebSocketToKis() {
        for (Session session : kisWebSocketSessionManager.getSessions()) {
            session.clearSubscription();
        }

        for (WebSocketConnectionManager connection : connections) {
            if (connection.isConnected())
                connection.stop();
        }
    }

    @Schedules({
            @Scheduled(cron = "0 * 9-14 * * 1-5"),
            @Scheduled(cron = "0 0-29 15 * * 1-5")
    })
    public void runKeepConnectionJob() {
        for (WebSocketSession session : kisWebSocketSessionManager.getWebSocketSessions()) {
            kisWebSocketConnector.keepConnection(session);
        }
    }
}