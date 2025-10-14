package muzusi.infrastructure.kis.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.infrastructure.kis.auth.KisAuthService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisWebSocketSessionManager {
    private final Map<String, KisWebSocketSession> sessions = new HashMap<>();
    private final KisWebSocketConnector kisWebSocketConnector;
    private final KisAuthService kisAuthService;
    
    /**
     * 웹소켓 세션을 초기화하는 메서드
     *
     * <p> {@link KisWebSocketConnector}를 통하여 한국투자증권 웹소켓과 연결 후 반환된 세션을 저장
     *
     * @return 한국투자증권 웹소켓과 연결된 세션의 아이디 목록
     */
    public List<String> initializeSessions() {
        List<String> webSocketKeys = kisAuthService.getWebSocketKeys();
        
        for (String webSocketKey : webSocketKeys) {
            WebSocketSession webSocketSession = kisWebSocketConnector.connect();
            
            if (webSocketSession != null) {
                sessions.put(webSocketSession.getId(), new KisWebSocketSession(webSocketSession, webSocketKey));
            }
        }
        
        return sessions.keySet().stream().toList();
    }
    
    /**
     * 웹소켓 세션 종료 후, 삭제하는 메서드
     */
    public void closeSessions() {
        for (KisWebSocketSession kisWebSocketSession : sessions.values()) {
            try {
                kisWebSocketSession.getWebSocketSession().close();
            } catch (IOException e) {
                log.error("[Error] Failed to close Websocket session - {}", kisWebSocketSession.getWebSocketSession().getId());
            }
        }
        
        sessions.clear();
    }
    
    /**
     * 특정 한국투자증권 웹소켓 세션 정보 객체를 조회하는 메서드
     *
     * @param sessionId 조회할 웹소켓 세션 ID
     * @return          한국투자증권 웹소켓 세션 정보 객체
     */
    public KisWebSocketSession getKisWebSocketSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * 한국투자증권과 연결된 웹소켓 세션과 해당 세션의 웹소켓 접속키를 저장하는 클래스
     */
    public static class KisWebSocketSession {
        private final WebSocketSession webSocketSession;
        private final String webSocketKey;
        
        public KisWebSocketSession(WebSocketSession webSocketSession, String webSocketKey) {
            this.webSocketSession = webSocketSession;
            this.webSocketKey = webSocketKey;
        }
        
        public WebSocketSession getWebSocketSession() {
            return this.webSocketSession;
        }
        
        public String getWebSocketKey() {
            return this.webSocketKey;
        }
    }
}
