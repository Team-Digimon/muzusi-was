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

    /**
     * 웹소켓 세션을 관리 세션 목록에 추가하는 메서드
     *
     * @param session : 웹소켓 세션
     */
    public void addSession(WebSocketSession session) {
        sessions.add(Session.builder()
                .session(session)
                .build());
    }

    /**
     * 웹소켓 세션을 관리 세션 목록에서 삭제하는 메서드
     *
     * @param webSocketSession : 웹소켓 세션
     */
    public void deleteSession(WebSocketSession webSocketSession) {
        for (Session session : sessions) {
            if (session.getWebSocketSession().getId().equals(webSocketSession.getId())) {
                sessions.remove(session);
            }
        }
    }

    /**
     * 웹소켓 세션 별 웹소켓 접속키를 추가하는 메서드
     * 
     * @param webSocketKeys : 웹소켓 접속키 리스트
     */
    public void addWebSocketKey(List<KisAuthDto.WebSocketKey> webSocketKeys) {
        for (int idx = 0; idx < Math.min(sessions.size(), webSocketKeys.size()); idx++) {
            sessions.get(idx).setWebSocketKey(webSocketKeys.get(idx));
        }
    }

    /**
     * 모든 웹소켓 세션 리스트를 반환하는 메서드
     *
     * @return 웹소켓 세션 리스트
     */
    public List<WebSocketSession> getWebSocketSessions() {
        return sessions.stream().map(Session::getWebSocketSession).toList();
    }

    /**
     * 웹소켓 특정 종목 구독 가능 여부에 따른 세션 반환 메서드
     * 
     * @param trId : 트랜잭션 아이디
     * @param stockCode : 특정 주식 종목 코드
     * @return : 세션 연결 정보 (웹소켓 세션, 웹소켓 접속키)
     */
    public SessionConnectionDto getSessionToSubscribe(String trId, String stockCode) {
        for (Session session : sessions) {
            if (session.subscribe(trId, stockCode))
                return SessionConnectionDto.of(session.getWebSocketSession(), session.getWebSocketKey());
        }
        return null;
    }

    /**
     * 웹소켓 특정 종목 구독 해제 가능 여부에 따른 세션 반환 메서드
     *
     * @param trId : 트랜잭션 아이디
     * @param stockCode : 특정 주식 종목 코드
     * @return : 세션 연결 정보 (웹소켓 세션, 웹소켓 접속키)
     */
    public SessionConnectionDto getSessionToUnsubscribe(String trId, String stockCode) {
        for (Session session : sessions) {
            if (session.unsubscribe(trId, stockCode))
                return SessionConnectionDto.of(session.getWebSocketSession(), session.getWebSocketKey());
        }
        return null;
    }
}