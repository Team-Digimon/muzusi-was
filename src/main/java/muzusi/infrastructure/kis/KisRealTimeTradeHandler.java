package muzusi.infrastructure.kis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.kis.dto.KisAuthDto;
import muzusi.application.trade.dto.TradeNotificationDto;
import muzusi.application.websocket.service.TradeNotificationPublisher;
import muzusi.domain.stock.exception.StockErrorType;
import muzusi.domain.trade.type.TradeType;
import muzusi.global.exception.CustomException;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class KisRealTimeTradeHandler extends KisWebSocketHandler {
    private final TradeNotificationPublisher tradeNotificationPublisher;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    private static final String TR_ID = "H0STCNT0";
    private static final int MAX_CONNECTION = 41;
    private final ConcurrentHashMap<String, Integer> subscribedStocks = new ConcurrentHashMap<>(MAX_CONNECTION);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        this.session = session;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        if(!payload.startsWith("{") && !payload.contains("PINGPONG")){
            String[] parts = payload.split("\\^");
            String[] metas = parts[0].split("\\|");
            int tradeCount = Integer.parseInt(metas[2]);
            String stockCode = metas[3];

            for (int idx = 0, i = 0 ; i < tradeCount ; i++) {
                tradeNotificationPublisher.publishTradeNotification(TradeNotificationDto.builder()
                        .stockCode(stockCode)
                        .time(convertTime(parts[idx+1]))
                        .price(Long.valueOf(parts[idx+2]))
                        .stockCount(Long.valueOf(parts[idx+12]))
                        .volume(Long.valueOf(parts[idx+13]))
                        .tradeType((parts[idx+21].equals("1")) ? TradeType.BUY : TradeType.SELL)
                        .changeRate(Double.valueOf(parts[idx+5]))
                        .build());
                idx += 46;
            }
        }
    }

    private String convertTime(String time) {
        return time.substring(0,2) + ":" + time.substring(2,4) + ":" + time.substring(4);
    }

    public void connect(String stockCode) {
        if (subscribedStocks.size() >= MAX_CONNECTION)
            throw new CustomException(StockErrorType.MAX_REQUEST_WEB_SOCKET);
        subscribedStocks.compute(stockCode, (k, v) -> v == null ? 1 : v + 1);
        request(stockCode, "1");
    }

    public void disconnect(String stockCode) {
        subscribedStocks.compute(stockCode, (k, v) -> {
            if (v == null)
                return null;
            else if (v > 1)
                return v-1;
            else {
                request(stockCode, "2");
                return null;
            }
        });
    }

    private void request(String trKey, String trType){
        Map<String, String> header = new HashMap<>();
        KisAuthDto.WebSocketKey webSocketKey = (KisAuthDto.WebSocketKey) redisService.get(KisConstant.WEBSOCKET_KEY_PREFIX.getValue());
        header.put("approval_key", webSocketKey.getValue());
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