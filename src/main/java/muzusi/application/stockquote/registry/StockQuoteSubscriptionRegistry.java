package muzusi.application.stockquote.registry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class StockQuoteSubscriptionRegistry {
    private final Map<String, StockQuoteSubscriptionContext> subscriptionContextMap = new HashMap<>();
    private final Map<String, String> stockSessionIndex = new HashMap<>();
    private final List<String> sessionIds = new ArrayList<>();
    private int sessionCursor = 0;

    private final ReentrantLock lock = new ReentrantLock(true);

    @Value("${kis.websocket-subscription-limit}")
    private int capacity;

    /**
     * 외부 웹소켓 세션 목록을 통해 구독 레지스트리를 초기화하는 메서드
     *
     * 각 웹소켓 세션 아이디 당 컨텍스트({@link StockQuoteSubscriptionContext})를 할당한다.
     * 향후 세션 공정 분배를 위해 세션 아이디 목록 컬렉션을 저장한다.
     *
     * @param connectedSessionIds 연결에 성공한 웹소켓 세션 ID 목록
     */
    public void initialize(List<String> connectedSessionIds) {
        lock.lock();

        try {
            for (String sessionId : connectedSessionIds) {
                subscriptionContextMap.put(sessionId, new StockQuoteSubscriptionContext(sessionId, capacity));
            }
            sessionIds.addAll(connectedSessionIds);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 레지스트리 내 모든 구독 관련 상태(컨텍스트, 역인덱스, 세션 목록)를 초기화 해제하는 메서드
     */
    public void reset() {
        lock.lock();

        try {
            subscriptionContextMap.clear();
            stockSessionIndex.clear();
            sessionIds.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 특정 주식 종목에 대한 구독 메서드
     *
     * 이미 구독 중인 종목이면 할당된 세션의 컨텍스트, 신규 구독 종목이면 공정 분배 정책으로 특정 세션 컨텍스트를 가져온다.
     * 선정된 세션 컨텍스트를 활용해 구독을 진행한다.
     *
     * @param stockCode 구독할 주식 종목 코드
     * @return 구독 처리 결과 DTO(세션 ID, 성공 여부, 신규 구독 여부)
     * @throws InterruptedException 락 획득 대기 중 인터럽트가 발생한 경우
     */
    public StockQuoteSubscriptionResult.Subscription subscribe(String stockCode) throws InterruptedException {
        if (lock.tryLock(5, TimeUnit.SECONDS)) {
            try {
                StockQuoteSubscriptionContext context = getContext(stockCode);
                
                if (context == null) {
                    return new StockQuoteSubscriptionResult.Subscription(null, false, false);
                }
                
                boolean isNewSubscription = isNewSubscription(stockCode);

                if (!context.subscribe(stockCode)) {
                    return new StockQuoteSubscriptionResult.Subscription(context.getSessionId(), false, false);
                }
                
                stockSessionIndex.put(stockCode, context.getSessionId());
                return new StockQuoteSubscriptionResult.Subscription(context.getSessionId(), true, isNewSubscription);
            } finally {
                lock.unlock();
            }
        }

        return new StockQuoteSubscriptionResult.Subscription(null, false, false);
    }

    /**
     * 특정 주식 종목에 대한 구독 해제 메서드
     *
     * 해당 종목을 구독 중인 세션 컨텍서스트에서 구독 횟수를 감소시키고, 횟수가 0이 되면 역인덱스에서도 제거한다.
     *
     * @param stockCode 구독 해제할 주식 종목 코드
     * @return 구독 해제 처리 결과(세션 ID, 성공 여부, 완전 삭제 여부)
     * @throws InterruptedException 락 획득 대기 중 인터럽트가 발생한 경우
     */
    public StockQuoteSubscriptionResult.Unsubscription unsubscribe(String stockCode) throws InterruptedException {
        if (lock.tryLock(5, TimeUnit.SECONDS)) {
            try {
                String sessionId = stockSessionIndex.get(stockCode);

                if (sessionId == null) {
                    return new StockQuoteSubscriptionResult.Unsubscription(null, true, false);
                }

                StockQuoteSubscriptionContext context = subscriptionContextMap.get(sessionId);
                int count = context.unsubscribe(stockCode);
                boolean isDeleted = (count == 0);

                if (isDeleted) {
                    stockSessionIndex.remove(stockCode);
                }

                return new StockQuoteSubscriptionResult.Unsubscription(sessionId, true, isDeleted);
            } finally {
                lock.unlock();
            }
        }

        return new StockQuoteSubscriptionResult.Unsubscription(null, false, false);
    }
    
    /**
     * 해당 주식 종목이 새로 구독을 진행하는 종목인지 여부를 확인하는 메서드
     *
     * 해도 주식 종목 코드가 역인덱스 맵에 존재하는지 여부를 기반으로 판단한다.
     *
     * @param stockCode 확인할 주식 종목 코드
     * @return 신규 구독 대상 여부
     */
    private boolean isNewSubscription(String stockCode) {
        return !stockSessionIndex.containsKey(stockCode);
    }
    
    /**
     * 특정 주식 종목 코드를 구독 중인/구독할 세션 컨텍스트를 조회하는 메서드
     *
     * 이미 구독 중인 종목이면 역인덱스를 통해 해당 세션의 컨텍스트를 반환한다.
     * 신규 종목이면 공정 분배 정책({@link #getFairContext()})에 따라 컨텍스트를 새로 선정한다.
     *
     * @param stockCode 조회할 종목 코드
     * @return 대응하는 구독 컨텍스트 (신규로 배정 가능한 세션이 없으면 {@code null}을 반환)
     */
    private StockQuoteSubscriptionContext getContext(String stockCode) {
        String sessionId = stockSessionIndex.get(stockCode);

        return sessionId == null ? getFairContext() : subscriptionContextMap.get(sessionId);
    }

    /**
     * 공정 분배 정책에 따라 구독 가능한 세션 컨텍스트를 찾는 메서드
     *
     * 라운드로빈 방식으로 정원이 차지 않은 다음 세션의 구독 컨텍스트를 찾는다.
     * 현재 커서 위치({@link #sessionCursor})부터 순회하며 정원이 남은 첫 세션을 반환하고, 다음 호출을 위해 커서를 이동시킨다.
     *
     * @return 구독 가능한 세션 컨텍스트 (세션이 없거나 모든 세션이 가득 찬 경우 {@code null})
     */
    private StockQuoteSubscriptionContext getFairContext() {
        int originalCursor = sessionCursor;

        if (sessionIds.isEmpty()) return null;

        StockQuoteSubscriptionContext context = subscriptionContextMap.get(sessionIds.get(originalCursor));

        // 이 시점에서는 이미 인덱스 내에는 존재하지 않는(기존에 없었다는 이야기)이기 때문에 무조건 41개 다 차있으면 더 이상 못 들어간다.
        while (context.isFull()) {
            sessionCursor = getNextSessionCursor();

            // 원래대로 돌아왔다면 모두 full아리는 것이니 null 리턴
            if (sessionCursor == originalCursor) {
                return null;
            }

            context = subscriptionContextMap.get(sessionIds.get(sessionCursor));
        }

        sessionCursor = getNextSessionCursor(); // 성공 시에도 다음 커서를 가르킬 수 있도록
        return context;
    }

    /**
     * 세션 목록에서 현재 커서의 다음 인덱스를 계산하는 메서드
     *
     * @return 다음 세션 커서 인덱스
     */
    private int getNextSessionCursor() {
        return (sessionCursor + 1) % sessionIds.size();
    }
    
    static class StockQuoteSubscriptionContext {
        private String sessionId;
        private final int capacity;
        private Map<String, Integer> subscriptionMap;

        public StockQuoteSubscriptionContext(String sessionId, int capacity) {
            this.sessionId = sessionId;
            this.capacity = capacity;
            this.subscriptionMap = new HashMap<>(capacity);
        }

        /**
         * 세션 컨텍스트에 주식 종목을 구독하는 메서드
         *
         * 주식 종목에 대한 구독 횟수를 1 증가시킨다.
         * 아직 구독 중이지 않은 종목이면서 정원이 가득 찬 경우에는 더 이상 구독을 진행할 수 없기 때문에 실패한다.
         *
         * @param stockCode 구독할 주식 종목 코드
         * @return 구독 성공 여부
         */
        public boolean subscribe(String stockCode) {
            if (!subscriptionMap.containsKey(stockCode) && subscriptionMap.size() >= capacity) return false;

            subscriptionMap.put(stockCode, subscriptionMap.getOrDefault(stockCode, 0) + 1);
            return true;
        }

        /**
         * 세션 컨텍스트에 주식 종목의 구독을 해제하는 메서드
         *
         * 종목 코드에 대한 구독 횟수를 1 감소시킨다.
         * 감소 후 횟수가 0 이하가 되면 구독 목록에서 완전히 제거한다.
         *
         * @param stockCode 구독 해제할 주식 종목 코드
         * @return 구독 해제후 남은 구독 횟수 ({@code 0}이면 완전히 삭제되었음을 의미)
         */
        public int unsubscribe(String stockCode) {
            Integer count = subscriptionMap.get(stockCode);

            if (count == null) {
                return 0;
            }

            if (count - 1 <= 0) {
                subscriptionMap.remove(stockCode);
                return 0;
            }

            subscriptionMap.put(stockCode, count - 1);
            return count - 1;
        }

        /**
         * 세션 컨텍스트가 담당하는 구독 종목 수가 정원에 도달했는지 확인하는 메서드
         *
         * @return 정원이 가득 찼으면 {@code true}
         */
        public boolean isFull() {
            return subscriptionMap.size() == capacity;
        }
        
        public String getSessionId() {
            return sessionId;
        }
    }
}
