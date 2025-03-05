package muzusi.infrastructure.kis.websocket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.kis.dto.KisAuthDto;
import muzusi.infrastructure.kis.dto.SessionConnectionDto;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class KisWebSocketSessionManager {
    private final List<Session> sessions = new ArrayList<>();

    public void addSession(WebSocketSession session) {
        sessions.add(Session.builder()
                .session(session)
                .build());
    }

    public void addWebSocketKey(List<KisAuthDto.WebSocketKey> webSocketKeys) {
        for (int idx = 0; idx < Math.min(sessions.size(), webSocketKeys.size()); idx++) {
            sessions.get(idx).setWebSocketKey(webSocketKeys.get(idx));
        }
    }

    public List<WebSocketSession> getWebSocketSessions() {
        return sessions.stream().map(Session::getWebSocketSession).toList();
    }

    public SessionConnectionDto getSessionToSubscribe(String trId, String stockCode) {
        for (Session session : sessions) {
            if (session.subscribe(trId, stockCode))
                return SessionConnectionDto.of(session.getWebSocketSession(), session.getWebSocketKey());
        }
        return null;
    }

    public SessionConnectionDto getSessionToUnsubscribe(String trId, String stockCode) {
        for (Session session : sessions) {
            if (session.unsubscribe(trId, stockCode))
                return SessionConnectionDto.of(session.getWebSocketSession(), session.getWebSocketKey());
        }
        return null;
    }
}