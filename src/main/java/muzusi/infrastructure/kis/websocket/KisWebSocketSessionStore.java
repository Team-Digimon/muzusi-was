package muzusi.infrastructure.kis.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

@Component
public class KisWebSocketSessionStore {
    private final Map<String, KisWebSocketSession> sessionMap = new HashMap<>();
    
    /**
     * 웹소켓 세션과 해당 세션이 사용할 웹소켓 접속키를 저장소에 저장하는 메서드
     *
     * @param session       저장할 웹소켓 세션
     * @param webSocketKey  세션과 함께 저장할 웹소켓 접속키
     * @return              저장된 웹소켓 세션의 ID
     */
    public String save(WebSocketSession session, String webSocketKey) {
        sessionMap.put(session.getId(), new KisWebSocketSession(webSocketKey, session));

        return session.getId();
    }

    /**
     * 저장소에 저장된 모든 웹소켓 세션을 삭제하는 메서드
     */
    public void deleteAll() {
        sessionMap.clear();
    }

    /**
     * 특정 웹소켓 세션 ID로 저장된 세션 정보를 조회하는 메서드
     *
     * @param sessionId 조회할 웹소켓 세션 ID
     * @return          해당 세션 ID로 저장된 웹소켓 세션 정보
     */
    public KisWebSocketSession findBySessionId(String sessionId) {
        return sessionMap.get(sessionId);
    }
    
    public static class KisWebSocketSession {
        private final String webSocketKey;
        private final WebSocketSession session;
        
        public KisWebSocketSession(String webSocketKey, WebSocketSession session) {
            this.webSocketKey = webSocketKey;
            this.session = session;
        }
        
        public String getWebSocketKey() {
            return webSocketKey;
        }
        
        public WebSocketSession getWebSocketSession() {
            return session;
        }
    }
}
