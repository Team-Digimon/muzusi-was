package muzusi.infrastructure.kis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.trade.dto.TradeNotificationDto;
import muzusi.application.websocket.service.TradeNotificationPublisher;
import muzusi.domain.stock.exception.StockErrorType;
import muzusi.domain.trade.type.TradeType;
import muzusi.global.exception.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class KisRealTimeTradeHandler extends KisWebSocketHandler {
    private final TradeNotificationPublisher tradeNotificationPublisher;
    private final ObjectMapper objectMapper;
    private final KisAuthProvider kisAuthProvider;

    private static final String TR_ID = "H0STCNT0";
    private static final int MAX_CONNECTION = 41;
    private final ConcurrentHashMap<String, Integer> subscribedStocks = new ConcurrentHashMap<>(MAX_CONNECTION);

    /**
     * 현재 구독 중인 주식 종목 코드 목록을 반환하는 메서드
     * 
     * @return : 현재 구독 주식 종목 코드 목록
     */
    public List<String> getConnectingStockCodes() {
        return subscribedStocks.keySet().stream().toList();
    }

    /**
     * 한국투자증권 주식 체결가 메시지 수신 및 처리 메서드
     *
     * @param session : 연결 세션
     * @param message : 수신 메시지
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        if (!payload.startsWith("{") && !payload.contains("PINGPONG")) {
            String[] parts = payload.split("\\^");
            String[] metas = parts[0].split("\\|");
            int tradeCount = Integer.parseInt(metas[2]);
            String stockCode = metas[3];

            for (int idx = 0, i = 0; i < tradeCount; i++) {
                tradeNotificationPublisher.publishTradeNotification(TradeNotificationDto.builder()
                        .stockCode(stockCode)
                        .time(convertTime(parts[idx + 1]))
                        .price(Long.valueOf(parts[idx + 2]))
                        .stockCount(Long.valueOf(parts[idx + 12]))
                        .volume(Long.valueOf(parts[idx + 13]))
                        .tradeType((parts[idx + 21].equals("1")) ? TradeType.BUY : TradeType.SELL)
                        .changeRate(Double.valueOf(parts[idx + 5]))
                        .build());
                idx += 46;
            }
        }
    }


    /**
     * 주식 체결가 데이터 내 시간 형식 변환 메서드
     *
     * @param time : HHMMSS
     * @return     : HH:MM:SS
     */
    private String convertTime(String time) {
        return time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);
    }

    /**
     * 특정 종목 체결가 웹소켓 구독 등록 요청 및 요청량 관리 메서드
     *
     * - 호출 유량 초과 시 연결 미진행
     * - 종목 별 호출량 증가
     * - 한국투자증권 주식 체결가 구독 등록 요청
     *
     * @param stockCode : 주식 종목 코드
     */
    public void connect(String stockCode) {
        subscribedStocks.compute(stockCode, (k, v) -> {
            if (v == null) {
                if (subscribedStocks.size() >= MAX_CONNECTION)
                    throw new CustomException(StockErrorType.MAX_REQUEST_WEB_SOCKET);
                request(stockCode, "1");
                return 1;
            } else {
                return v + 1;
            }
        });
    }

    /**
     * 특정 종목 체결가 웹소켓 구독 해체 요청 및 요청량 관리 메서드
     *
     * - 종목 별 호출량 감소
     * - 호출량이 0이 되면, 한국투자증권 주식 체결가 구독 해제 요청
     *
     * @param stockCode : 주식 종목 코드
     */
    public void disconnect(String stockCode) {
        subscribedStocks.compute(stockCode, (k, v) -> {
            if (v == null)
                return null;
            else if (v > 1)
                return v - 1;
            else {
                request(stockCode, "2");
                return null;
            }
        });
    }



    /**
     * 특정 종목 체결가 웹소켓 연결 요청 메서드
     *
     * @param trKey  : 주식 종목 코드
     * @param trType : 요청 타입(1: 등록, 2: 해제)
     */
    private void request(String trKey, String trType) {

        if (session == null || !session.isOpen()) {
            log.error("KIS session closed in request");
            return;
        }

        Map<String, String> header = new HashMap<>();
        header.put("approval_key", kisAuthProvider.getWebSocketKey().getValue());
        header.put("custtype", "P");
        header.put("tr_type", trType);
        header.put("content-type", "utf-8");

        Map<String, Object> body = new HashMap<>();
        Map<String, String> input = new HashMap<>();
        input.put("tr_id", TR_ID);
        input.put("tr_key", trKey);
        body.put("input", input);

        Map<String, Object> request = new HashMap<>();
        request.put("header", header);
        request.put("body", body);


        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(request)));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}