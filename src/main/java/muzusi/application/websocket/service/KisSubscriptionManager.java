package muzusi.application.websocket.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.exception.StockErrorType;
import muzusi.global.exception.CustomException;
import muzusi.infrastructure.kis.websocket.KisRealTimeTradeWebSocketClient;
import muzusi.infrastructure.kis.websocket.KisWebSocketSessionManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
public class KisSubscriptionManager {
    private final KisWebSocketSessionManager kisWebSocketSessionManager;
    private final KisRealTimeTradeWebSocketClient kisRealTimeTradeWebSocketClient;
    
    /**
     * 웹 소켓 세션 ID를 Key로 사용하여, 세션 별 구독 종목 정보({@link StockSubscriptionContext})를 저장하는 Map
     */
    private final Map<String, StockSubscriptionContext> stockSubscriptionContextBySession = new LinkedHashMap<>();
    
    /**
     * 종목 코드(StockCode)를 Key로 사용하여, 특정 종목 코드의 구독을 담당하는 웹 소켓 세션 ID를 저장하는 Map
     *
     * <p>해당 주식 종목 코드를 구독하고 있는 웹소켓 세션 ID를 바로 알아내기 위한 역인덱싱 목적
     */
    private final Map<String, String> stockSessionIndex = new HashMap<>();
    
    private final ReentrantLock lock = new ReentrantLock();
    
    /**
     * 웹소켓 세션 ID 목록을 인자로 받아, 웹소켓 세션 ID 별 구독 목록 Map을 초기화하는 메서드
     *
     * @param sessionIds    초기화할 웹소켓 세션 ID 목록
     */
    public void initialize(List<String> sessionIds) {
        for (String sessionId : sessionIds) {
            stockSubscriptionContextBySession.put(sessionId, new StockSubscriptionContext());
        }
    }
    
    /**
     * 웹소켓 세션 ID 별 구독 목록 Map과 주식 종목 코드 별 할당된 웹소켓 세션 ID Map을 비우는 메서드
     */
    public void clearSubscriptions() {
        stockSubscriptionContextBySession.clear();
        stockSessionIndex.clear();
    }
    
    /**
     * 주식 종목을 구독하는 메서드
     *
     * <p> 주식 종목 구독 정보를 관리하고, 초기 구독 요청이 온 종목의 경우에는
     * {@link muzusi.infrastructure.kis.websocket.KisWebSocketSessionManager}에 요청을 위임하여 구독 요청
     *
     * @param stockCode 주식 종목 코드
     */
    public void subscribe(String stockCode) {
        lock.lock();
        
        try {
            String sessionId = stockSessionIndex.get(stockCode);
            
            if (sessionId == null) { // 해당 주식 종목을 처음 구독하는 경우, 한국투자증권 국내주식 실시간 체결가 구독 요청
                sessionId = getAvailableSessionId();
                
                KisWebSocketSessionManager.KisWebSocketSession kisWebSocketSession = kisWebSocketSessionManager.getKisWebSocketSession(sessionId);
                
                kisRealTimeTradeWebSocketClient.subscribe(
                        kisWebSocketSession.getWebSocketSession(),
                        kisWebSocketSession.getWebSocketKey(),
                        stockCode
                );
                stockSessionIndex.put(stockCode, sessionId);
            }
            
            StockSubscriptionContext context = stockSubscriptionContextBySession.get(sessionId);
            context.add(stockCode);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 구독 가능한 웹소켓 세션의 아이디를 반환하는 메서드
     *
     * @return 사용 가능한 웹소켓 세션 ID
     * @throws CustomException StockErrorType.MAX_REQUEST_WEB_SOCKET - 더 이상 구독 가능한 세션이 없는 경우
     */
    private String getAvailableSessionId() {
        return stockSubscriptionContextBySession.entrySet().stream()
                .filter(entry -> entry.getValue().isAvailable())
                .map(entry -> entry.getKey())
                .findFirst()
                .orElseThrow(() -> new CustomException(StockErrorType.MAX_REQUEST_WEB_SOCKET));
    }
    
    /**
     * 구독 중인 주식 종목에 대한 구독을 해제하는 메서드
     *
     * <p> 주식 종목 구독 해제를 담당하고, 구독 해제 후 해당 종목에 대한 더 이상 구독이 없는 경우에는
     * {@link muzusi.infrastructure.kis.websocket.KisWebSocketSessionManager}에 요청을 위임하여 구독 해제 요청
     *
     * @param stockCode 주식 종목 코드
     */
    public void unsubscribe(String stockCode) {
        lock.lock();
        
        try {
            String sessionId = stockSessionIndex.get(stockCode);
            
            if (sessionId == null || sessionId.isBlank()) {
                throw new CustomException(StockErrorType.NOT_SUBSCRIBED_STOCK);
            }
            
            StockSubscriptionContext context = stockSubscriptionContextBySession.get(sessionId);
            int subscriptionCount = context.getSubscriptionCount(stockCode);
            
            if (subscriptionCount == 1) { // 구독 해제 이후 더 이상 구독 수가 없다면, 해당 구독 종목을 삭제
                KisWebSocketSessionManager.KisWebSocketSession kisWebSocketSession = kisWebSocketSessionManager.getKisWebSocketSession(sessionId);
                kisRealTimeTradeWebSocketClient.unsubscribe(
                        kisWebSocketSession.getWebSocketSession(),
                        kisWebSocketSession.getWebSocketKey(),
                        stockCode
                );
                
                stockSessionIndex.remove(stockCode);
            }
            
            context.remove(stockCode);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 주식 종목 코드와 해당 주식 종목에 대한 구독 수를 저장하는 컨텍스트 클래스
     *
     * <p>한국투자증권 웹소켓 호출 유량 제한을 충족하는 만큼의 구독 수를 관리
     */
    public static class StockSubscriptionContext {
        private static final int MAX_SUBSCRIPTION = 41;
        private final Map<String, Integer> subscribedStocks = new HashMap<>(MAX_SUBSCRIPTION);
        
        /**
         * 특정 주식 종목 구독 메서드
         *
         * <p> 처음 구독한 경우, 해당 종목에 대한 구독 수는 1로 설정
         *
         * @param stockCode 구독할 주식 종목 코드
         * @throws CustomException StockErrorType.MAX_REQUEST_WEB_SOCKET - 해당 세션을 통해 더 이상 새로운 종목을 구독할 수가 없는 경우
         */
        public int add(String stockCode) {
            return subscribedStocks.compute(
                    stockCode,
                    (stock, subscriptionCount) -> {
                        if (subscriptionCount == null) {
                            if (!isAvailable()) {
                                throw new CustomException(StockErrorType.MAX_REQUEST_WEB_SOCKET);
                            }
                            return 1;
                        }
                        
                        return subscriptionCount + 1;
                    }
            );
        }
        
        /**
         * 특정 주식 종목 구독 해제 메서드
         *
         * @param stockCode 구독 해제할 주식 종목 코드
         * @return          구독 해제 후 해당 주식 종목 구독 수
         */
        public int remove(String stockCode) {
            Integer afterSubscriptionCount = subscribedStocks.compute(
                    stockCode,
                    (stock, subscriptionCount) -> {
                        if (subscriptionCount == null) {
                            return null;
                        }
                        
                        if (subscriptionCount == 1) {
                            return null;
                        }
                        
                        return subscriptionCount - 1;
                    }
            );
            
            return afterSubscriptionCount == null ? 0 : afterSubscriptionCount;
        }
        
        /**
         * 특정 주식 종목 구독 수 반환 메서드
         *
         * @param stockCode 주식 종목 코드
         * @return          해당 주식 종목 구독 수
         */
        public int getSubscriptionCount(String stockCode) {
            return subscribedStocks.get(stockCode);
        }
        
        /**
         * 현재 겍체가 더 이상 구독이 가능한지 여부를 반환하는 메서드
         *
         * @return 최대 구독 가능 수보다 작을 경우 true, 최대 구독 수보다 같거나 클 경우 false
         */
        public boolean isAvailable() {
            return this.subscribedStocks.size() < MAX_SUBSCRIPTION;
        }
    }
}
