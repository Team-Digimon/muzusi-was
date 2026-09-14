package muzusi.infrastructure.kis.websocket;

import muzusi.infrastructure.kis.exception.KisApiException;
import muzusi.infrastructure.kis.websocket.handler.KisWebSocketDispatcher;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;

@Component
public class KisWebSocketConnector {
    private final WebSocketClient webSocketClient = new StandardWebSocketClient();
    private final KisWebSocketDispatcher kisWebSocketDispatcher;
    private final String webSocketDomain;
    
    public KisWebSocketConnector(
            KisProperties kisProperties,
            KisWebSocketDispatcher kisWebSocketDispatcher
    ) {
        this.webSocketDomain = kisProperties.getWebSocketDomain();
        this.kisWebSocketDispatcher = kisWebSocketDispatcher;
    }
    
    /**
     * 한국투자증권 웹소켓 세션 연결 메서드
     *
     * <p> 세션 연결 시, 한국투자증권 웹소켓 공통 응답 처리 및 위임 핸들러 {@link KisWebSocketDispatcher}를 핸들러로 설정한다.
     *
     * @return  한국투자증권 웹소켓과 연결된 세션
     */
    public WebSocketSession connect() {
        try {
            WebSocketSession session = webSocketClient
                    .execute(kisWebSocketDispatcher, webSocketDomain).join();
            
            return session;
        } catch (Exception e) {
            throw new KisApiException("한국투자증권 웹소켓 세션 연결에 실패하였습니다.", e);
        }
    }
    
    /**
     * 한국투자증권 웹소켓 세션 연결 종료 메서드
     *
     * @param session 웹소켓 세션
     * @throws KisApiException 웹소켓 세션 연결 종료에 실패한 경우
     */
    public void close(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException e) {
            throw new KisApiException("한국투자증권 웹소켓 세션 연결 종료에 실패하였습니다.", e);
        }
    }
}
