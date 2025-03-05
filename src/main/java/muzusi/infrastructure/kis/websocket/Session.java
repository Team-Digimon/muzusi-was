package muzusi.infrastructure.kis.websocket;

import lombok.Builder;
import muzusi.application.kis.dto.KisAuthDto;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

public class Session {
    private WebSocketSession session;
    private int subCount;
    private String webSocketKey;
    private Map<String, Map<String, Integer>> subscription;
    private static final int MAX_COUNT = 41;

    @Builder
    public Session(WebSocketSession session) {
        this.session = session;
        this.subCount = 0;
        this.subscription = new HashMap<>();
    }

    public WebSocketSession getWebSocketSession() {
        return session;
    }

    public String getWebSocketKey() {
        return webSocketKey;
    }

    public void setWebSocketKey(KisAuthDto.WebSocketKey webSocketKey) {
        this.webSocketKey = webSocketKey.getValue();
    }

    public void clearSubscription() {
        this.subscription.clear();
        subCount = 0;
    }

    private boolean isFull() {
        return subCount >= MAX_COUNT;
    }

    public boolean subscribe(String trId, String stockCode) {
        boolean isContainsTrId = subscription.containsKey(trId);

        if ((isContainsTrId && !subscription.get(trId).containsKey(stockCode)) && isFull()) return false;
        if (!isContainsTrId) {
            subscription.put(trId, new HashMap<>(Map.of(stockCode, 1)));
            subCount++;
            return true;
        }

        Map<String, Integer> stocks = subscription.get(trId);
        if (stocks.containsKey(stockCode))
            stocks.put(stockCode, stocks.get(stockCode) + 1);
        else {
            stocks.put(stockCode, 1);
            subCount++;
        }
        return true;
    }

    public boolean unsubscribe(String trId, String stockCode) {
        Map<String, Integer> stocks = subscription.get(trId);

        if (!subscription.containsKey(trId) || !stocks.containsKey(stockCode)) return false;
        stocks.compute(stockCode, (k, v) -> (v - 1 == 0) ? null : v - 1);
        return true;
    }
}