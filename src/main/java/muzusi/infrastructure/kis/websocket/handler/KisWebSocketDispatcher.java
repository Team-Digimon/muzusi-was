package muzusi.infrastructure.kis.websocket.handler;

import lombok.extern.slf4j.Slf4j;
import muzusi.infrastructure.kis.websocket.KisWebSocketMessageParser;
import muzusi.infrastructure.kis.websocket.dto.KisWebSocketMetaResponseDto;
import muzusi.infrastructure.kis.websocket.dto.KisWebSocketResponseDto;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class KisWebSocketDispatcher extends TextWebSocketHandler {
    private final KisWebSocketMessageParser kisWebSocketMessageParser;
    private final Map<String, KisWebSocketHandler> kisWebSocketHandlerMap;
    
    private static final String PING_PONG_TR_ID = "PINGPONG";
    
    /**
     * 스프링 빈으로 등록된 모든 {@link KisWebSocketHandler} 구현체를 TR_ID 기준으로 매핑하는 생성자
     *
     * @param kisWebSocketMessageParser    한국투자증권 웹소켓 메시지 파서
     * @param kisWebSocketHandlers         스프링 빈으로 등록된 {@link KisWebSocketHandler} 구현체 목록
     */
    public KisWebSocketDispatcher(KisWebSocketMessageParser kisWebSocketMessageParser, List<KisWebSocketHandler> kisWebSocketHandlers) {
        this.kisWebSocketMessageParser = kisWebSocketMessageParser;
        this.kisWebSocketHandlerMap = new HashMap<>();
        
        for (KisWebSocketHandler kisWebSocketHandler : kisWebSocketHandlers) {
            kisWebSocketHandlerMap.put(kisWebSocketHandler.getTrId(), kisWebSocketHandler);
        }
    }
    
    /**
     * 한국투자증권 웹소켓 서버와의 연결이 수립된 직후 호출되는 메서드
     *
     * @param session   한국투자증권 웹소켓 서버와 연결된 세션
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("KIS Websocket connected - session id: {}", session.getId());
        super.afterConnectionEstablished(session);
    }
    
    /**
     * 한국투자증권 웹소켓 세션을 통해 수신되는 텍스트 메시지({@code TextMessage})를 처리하는 메서드
     *
     * <p> 메타 메시지는 메시지 정보에 맞게 처리 후, 해당 메서드를 종료한다.
     *
     * <p> 실시간 응답은 응답의 TR_ID({@link KisWebSocketResponseDto#trId})를 통해 해당 응답을 처리 가능한 핸들러에게 처리를 위임한다.
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        if (isMetaMessage(payload)) {
            handleMetaMessage(session, payload);
            return;
        }
        
        KisWebSocketResponseDto response = kisWebSocketMessageParser.parse(payload);
        KisWebSocketHandler kisWebSocketHandler = kisWebSocketHandlerMap.get(response.trId());
        
        if (kisWebSocketHandler == null) {
            log.error("[Error] No KIS websocket handler registered for TR_ID: {}", response.trId());
            return;
        }
        
        kisWebSocketHandler.handle(response);
    }
    
    /**
     * 한국투자증권 웹소켓 세션에서 전송 계층 오류가 발생했을 때 호출되는 메서드
     *
     * @param session   한국투자증권 웹소켓 서버와 연결된 세션
     * @param exception 발생한 예외
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[Error] KIS Websocket transport error - session id: {}, message: {}", session.getId(), exception.getMessage());
        super.handleTransportError(session, exception);
    }

    /**
     * 한국투자증권 웹소켓 서버와의 연결이 종료된 직후 호출되는 메서드
     *
     * @param session   한국투자증권 웹소켓 서버와 연결된 세션
     * @param status    연결 종료 상태
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("KIS Websocket disconnected - session id: {}", session.getId());
        super.afterConnectionClosed(session, status);
    }
    
    /**
     * 한국투자증권 메타 메시지를 처리하는 메서드
     *
     * @param session       한국투자증권 웹소켓 서버와 연결된 세션
     * @param payload       페이로드
     */
    private void handleMetaMessage(WebSocketSession session, String payload) throws IOException {
        KisWebSocketMetaResponseDto metaMessage = kisWebSocketMessageParser.parseMeta(payload);
        
        if (isPingPong(metaMessage)) {
            session.sendMessage(new TextMessage(payload));
            return;
        }
        
        if (metaMessage.body() != null && metaMessage.body().msg1() != null) {
            log.info("[KIS Websocket] tr id: {} / msg: {}", metaMessage.header().trId(), metaMessage.body().msg1());
        }
    }
    
    /**
     * 한국투자증권 메타 메시지 응답의 핑퐁 메시지 여부를 반환하는 메서드
     *
     * <p> 한국투자증권 웹소켓은 세션 내 데이터 교환이 없을 경우, 세션 유지를 위해 주기적으로 PingPong 메시지를 보낸다.
     * <p> PingPong 메시지는 응답 메시지 헤더의 TR_ID({@code header.tr_id}) 부분에 {@link #PING_PONG_TR_ID} 값을 포함한다.
     *
     * @param response  한국투자증권 메타 메시지 응답 DTO
     * @return          핑퐁 메시지 여부
     */
    private boolean isPingPong(KisWebSocketMetaResponseDto response) {
        return response.header().trId().equals(PING_PONG_TR_ID);
    }
    
    /**
     * 한국투자증권 웹소켓 요청에 대한 메타 메시지 여부를 반환하는 메서드
     *
     * <p> 한국투자증권 웹소켓은 정상 등록 여부(메타 정보), 핑퐁 메시지를 JSON 형태로 전달한다.
     * <p> 따라서, 이를 판별하기 위해 중괄호를 통해 메타 메시지 여부를 식별한다.
     *
     * @param payload   페이로드
     * @return          메타 메시지 여부
     */
    private boolean isMetaMessage(String payload) {
        return payload.startsWith("{") || payload.endsWith("}");
    }
}
