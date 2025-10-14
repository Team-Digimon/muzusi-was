package muzusi.application.websocket.service;

import muzusi.infrastructure.kis.websocket.KisRealTimeTradeWebSocketClient;
import muzusi.infrastructure.kis.websocket.KisWebSocketSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.WebSocketSession;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class KisSubscriptionMangerTest {

    @Mock
    private KisWebSocketSessionManager kisWebSocketSessionManager;
    
    @Mock
    private KisRealTimeTradeWebSocketClient kisRealTimeTradeWebSocketClient;
    
    @InjectMocks
    private KisSubscriptionManager kisSubscriptionManager;
    
    @Nested
    @DisplayName("구독")
    class Subscribe {
        private final String sessionId1 = "sessionId1";
        private final String sessionId2 = "sessionId2";
        private final String webSocketKey1 = "webSocketKey1";
        private final String webSocketKey2 = "webSocketKey2";
        private KisWebSocketSessionManager.KisWebSocketSession kisWebSocketSession1;
        private KisWebSocketSessionManager.KisWebSocketSession kisWebSocketSession2;
        
        @Captor
        private ArgumentCaptor<WebSocketSession> sessionCaptor;
        
        @Captor
        private ArgumentCaptor<String> webSocketKeyCaptor;
        
        @Captor
        private ArgumentCaptor<String> stockCodeCaptor;
        
        @BeforeEach
        void setUp() {
            WebSocketSession session1 = mock(WebSocketSession.class);
            WebSocketSession session2 = mock(WebSocketSession.class);
            kisWebSocketSession1 = new KisWebSocketSessionManager.KisWebSocketSession(session1, webSocketKey1);
            kisWebSocketSession2 = new KisWebSocketSessionManager.KisWebSocketSession(session2, webSocketKey2);
            lenient().when(session1.getId()).thenReturn(sessionId1);
            lenient().when(session2.getId()).thenReturn(sessionId2);
            lenient().when(kisWebSocketSessionManager.getKisWebSocketSession(sessionId1)).thenReturn(kisWebSocketSession1);
            lenient().when(kisWebSocketSessionManager.getKisWebSocketSession(sessionId2)).thenReturn(kisWebSocketSession2);
            
            Map<String, KisSubscriptionManager.StockSubscriptionContext> mockStockSubContextBySession = new LinkedHashMap<>();
            KisSubscriptionManager.StockSubscriptionContext mockStockSubscriptionContext1 = new KisSubscriptionManager.StockSubscriptionContext();
            KisSubscriptionManager.StockSubscriptionContext mockStockSubscriptionContext2 = new KisSubscriptionManager.StockSubscriptionContext();
            mockStockSubContextBySession.put(sessionId1, mockStockSubscriptionContext1);
            mockStockSubContextBySession.put(sessionId2, mockStockSubscriptionContext2);
            
            ReflectionTestUtils.setField(kisSubscriptionManager, "stockSubscriptionContextBySession", mockStockSubContextBySession);
        }

        @SuppressWarnings("unchecked")
        private Map<String, String> getStockSessionIndex() {
            return (Map<String, String>) ReflectionTestUtils.getField(kisSubscriptionManager, "stockSessionIndex");
        }
        
        @SuppressWarnings("unchecked")
        private Map<String, KisSubscriptionManager.StockSubscriptionContext> getStockSubscriptionContextBySession() {
            return (Map<String, KisSubscriptionManager.StockSubscriptionContext>) ReflectionTestUtils.getField(
                    kisSubscriptionManager,
                    "stockSubscriptionContextBySession"
            );
        }
        
        @Test
        @DisplayName("주식 종목을 처음 구독을 하는 경우")
        void successIfFirstSubscription() {
            // given
            String stockCode = "000001";
            doNothing().when(kisRealTimeTradeWebSocketClient).subscribe(any(WebSocketSession.class), anyString(), anyString());
            
            // when
            kisSubscriptionManager.subscribe(stockCode);
            
            // then
            // 1. 처음 구독한 종목은 한국투자증권 실시간 체결가 웹소켓 구독 요청 메서드를 호출한다.
            verify(kisRealTimeTradeWebSocketClient, times(1)).subscribe(
                    sessionCaptor.capture(),
                    webSocketKeyCaptor.capture(),
                    stockCodeCaptor.capture()
            );
            
            String sessionId = sessionCaptor.getValue().getId();
            
            Map<String, String> stockSessionIndex = getStockSessionIndex();
            Map<String, KisSubscriptionManager.StockSubscriptionContext> stockSubscriptionContextBySession = getStockSubscriptionContextBySession();
            
            // 2. 해당 주식 종목이 연결되고 있는 웹소켓 세션이 역인덱싱이 생성된다.
            assertEquals(sessionId, stockSessionIndex.get(stockCode));
            
            // 3. 해당 주식 종목의 구독 수는 1이다.
            assertEquals(1, stockSubscriptionContextBySession.get(sessionId).getSubscriptionCount(stockCode));
        }
        
        @Test
        @DisplayName("41개 이상의 종목을 구독하는 경우")
        void subscribeMoreThan41Stocks() throws InterruptedException {
            // given
            // 000001, 000045 종목은 구독 수가 2
            String[] stockCodes = {
                    "000001", "000001", "000002", "000003", "000004",
                    "000005", "000006", "000007", "000008", "000009",
                    "000010", "000011", "000012", "000013", "000014",
                    "000015", "000016", "000017", "000018", "000019",
                    "000020", "000021", "000022", "000023", "000024",
                    "000025", "000026", "000027", "000028", "000029",
                    "000030", "000031", "000032", "000033", "000034",
                    "000035", "000036", "000037", "000038", "000039",
                    "000040", "000041", "000042", "000043", "000044",
                    "000045", "000045"
            };
            
            int threadCount = stockCodes.length;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            
            doNothing().when(kisRealTimeTradeWebSocketClient).subscribe(any(WebSocketSession.class), anyString(), anyString());
            
            // when
            for (int i = 0; i < threadCount; i++) {
                final int finalI = i;
                executorService.submit(() -> {
                    try {
                        kisSubscriptionManager.subscribe(stockCodes[finalI]);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            executorService.shutdown();
            boolean isTaskCompleted = latch.await(10, TimeUnit.SECONDS);
            
            // then
            // 1. 작업 시간 내 모든 작업이 완료되었는지 확인한다.
            assertTrue(isTaskCompleted, "작업 시간 내 모든 작업이 완료되지 않았습니다.");
            
            // 2. 000001, 000045 종목은 중복 구독이기 때문에 한국투자증권 구독 요청 메서드 호출 횟수는 45회이다.
            verify(kisRealTimeTradeWebSocketClient, times(45)).subscribe(
                    sessionCaptor.capture(),
                    webSocketKeyCaptor.capture(),
                    stockCodeCaptor.capture()
            );
            
            // 3. 역인덱스에는 중복 종목을 제외한 총 45개의 종목이 등록되어 있어야한다.
            Map<String, String> stockSessionIndex = getStockSessionIndex();
            assertThat(stockSessionIndex).hasSize(45);
            
            // 4. 세션 별 역인덱스 사이즈는 41 또는 4이다.
            int session1IndexCount = (int) stockSessionIndex.values().stream()
                    .filter(id -> id.equals(sessionId1))
                    .count();
            int session2IndexCount = (int) stockSessionIndex.values().stream()
                    .filter(id -> id.equals(sessionId2))
                    .count();
            
            assertThat(session1IndexCount).isIn(41, 4);
            assertThat(session2IndexCount).isIn(41, 4);
            
            // 5. 각 세션 별 구독 종목 수는 41 또는 4이다.
            Map<String, KisSubscriptionManager.StockSubscriptionContext> stockSubscriptionContextBySession = getStockSubscriptionContextBySession();
            KisSubscriptionManager.StockSubscriptionContext context1 = stockSubscriptionContextBySession.get(sessionId1);
            Map<String, Integer> context1Subscription = (Map<String, Integer>) ReflectionTestUtils.getField(context1, "subscribedStocks");
            KisSubscriptionManager.StockSubscriptionContext context2 = stockSubscriptionContextBySession.get(sessionId2);
            Map<String, Integer> context2Subscription = (Map<String, Integer>) ReflectionTestUtils.getField(context2, "subscribedStocks");
            
            assertThat(context1Subscription.values().size()).isIn(41, 4);
            assertThat(context2Subscription.values().size()).isIn(41, 4);
        }
    }
}
