package muzusi.application.stockquote.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;

class StockQuoteSubscriptionRegistryTest {
    private StockQuoteSubscriptionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new StockQuoteSubscriptionRegistry();
    }
    
    private void initializeRegistry(int capacity, String... sessionIds) {
        ReflectionTestUtils.setField(registry, "capacity", capacity);
        registry.initialize(List.of(sessionIds));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, StockQuoteSubscriptionRegistry.StockQuoteSubscriptionContext> getSubscriptionContextMap() {
        return (Map<String, StockQuoteSubscriptionRegistry.StockQuoteSubscriptionContext>) ReflectionTestUtils
                .getField(registry, "subscriptionContextMap");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getStockSessionIndex() {
        return (Map<String, String>) ReflectionTestUtils.getField(registry, "stockSessionIndex");
    }

    @SuppressWarnings("unchecked")
    private List<String> getSessionIds() {
        return (List<String>) ReflectionTestUtils.getField(registry, "sessionIds");
    }

    private ReentrantLock getLock() {
        return (ReentrantLock) ReflectionTestUtils.getField(registry, "lock");
    }

    @Nested
    @DisplayName("초기화")
    class Initialize {
        @Test
        @DisplayName("세션 목록으로 초기화하면 세션별 구독 컨텍스트와 세션 목록이 생성된다")
        void successInitialize() {
            // given
            String sessionId1 = "session1";
            String sessionId2 = "session2";

            // when
            registry.initialize(List.of(sessionId1, sessionId2));

            // then
            Map<String, StockQuoteSubscriptionRegistry.StockQuoteSubscriptionContext> contextMap = getSubscriptionContextMap();
            assertThat(contextMap).containsOnlyKeys(sessionId1, sessionId2);
            assertThat(contextMap.get(sessionId1).getSessionId()).isEqualTo(sessionId1);
            assertThat(contextMap.get(sessionId2).getSessionId()).isEqualTo(sessionId2);
            assertThat(getSessionIds()).containsExactly(sessionId1, sessionId2);
        }
    }

    @Nested
    @DisplayName("초기화 해제")
    class Reset {
        @Test
        @DisplayName("초기화 해제 시 구독 컨텍스트, 역인덱스, 세션 목록이 모두 제거된다")
        void successReset() throws InterruptedException {
            // given
            initializeRegistry(2, "session1");
            registry.subscribe("000001");

            // when
            registry.reset();

            // then
            assertThat(getSubscriptionContextMap()).isEmpty();
            assertThat(getStockSessionIndex()).isEmpty();
            assertThat(getSessionIds()).isEmpty();
        }
    }

    @Nested
    @DisplayName("구독")
    class Subscribe {
        @Test
        @DisplayName("초기화된 세션이 없으면 구독에 실패한다")
        void failWhenNoSessionExists() throws InterruptedException {
            // when
            StockQuoteSubscriptionResult.Subscription result = registry.subscribe("000001");

            // then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.sessionId()).isNull();
            assertThat(result.isNewSubscription()).isFalse();
        }

        @Test
        @DisplayName("신규 종목을 구독하면 성공하고 신규 구독으로 표시된다")
        void successNewSubscription() throws InterruptedException {
            // given
            initializeRegistry(2, "session1");

            // when
            StockQuoteSubscriptionResult.Subscription result = registry.subscribe("000001");

            // then
            assertThat(result.sessionId()).isEqualTo("session1");
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isNewSubscription()).isTrue();
            assertThat(getStockSessionIndex()).containsEntry("000001", "session1");
        }

        @Test
        @DisplayName("이미 구독 중인 종목을 다시 구독하면 같은 세션을 재사용하고 신규 구독이 아니다")
        void successReSubscriptionOnExistingStock() throws InterruptedException {
            // given
            initializeRegistry(2, "session1");
            StockQuoteSubscriptionResult.Subscription firstSub = registry.subscribe("000001");

            // when
            StockQuoteSubscriptionResult.Subscription secondSub = registry.subscribe("000001");

            // then
            assertThat(secondSub.isSuccess()).isTrue();
            assertThat(secondSub.isNewSubscription()).isFalse();
            assertThat(secondSub.sessionId()).isEqualTo(firstSub.sessionId()); // 기존 구독 시 사용한 세션과 같은 세션을 사용한다.
        }

        @Test
        @DisplayName("여러 세션이 있으면 신규 종목은 라운드로빈 방식으로 공정하게 분배된다")
        void successFairDistributionAcrossSessions() throws InterruptedException {
            // given
            initializeRegistry(5, "session1", "session2");

            // when
            StockQuoteSubscriptionResult.Subscription result1 = registry.subscribe("000001");
            StockQuoteSubscriptionResult.Subscription result2 = registry.subscribe("000002");
            StockQuoteSubscriptionResult.Subscription result3 = registry.subscribe("000003");

            // then: session1 -> session2 -> session1 순으로 순환하며 배정된다.
            assertThat(result1.sessionId()).isEqualTo("session1");
            assertThat(result2.sessionId()).isEqualTo("session2");
            assertThat(result3.sessionId()).isEqualTo("session1");
        }

        @Test
        @DisplayName("세션 정원이 가득 차면 정원이 남은 다음 세션으로 배정된다")
        void successMoveToNextSessionWhenFull() throws InterruptedException {
            // given
            initializeRegistry(1, "session1", "session2");
            registry.subscribe("000001"); // session1 정원 소진

            // when
            StockQuoteSubscriptionResult.Subscription result = registry.subscribe("000002");

            // then: session1의 정원이 가득차서 session2에 구독된다.
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.sessionId()).isEqualTo("session2");
        }

        @Test
        @DisplayName("모든 세션의 정원이 가득 차면 신규 종목 구독은 실패한다")
        void failWhenAllSessionsAreFull() throws InterruptedException {
            // given
            initializeRegistry(1, "session1", "session2");
            registry.subscribe("000001"); // session1 정원 소진
            registry.subscribe("000002"); // session2 정원 소진

            // when
            StockQuoteSubscriptionResult.Subscription result = registry.subscribe("000003");

            // then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.sessionId()).isNull();
        }

        @Test
        @DisplayName("세션 정원이 가득 차 있어도 이미 구독 중인 종목은 재구독에 성공한다")
        void successReSubscriptionEvenIfSessionIsFull() throws InterruptedException {
            // given
            initializeRegistry(1, "session1");
            registry.subscribe("000001"); // session1 정원 소진 & 구독 종목: "000001"

            // when
            StockQuoteSubscriptionResult.Subscription result = registry.subscribe("000001");

            // then: "000001" 종목은 이미 구독 중이었던 종목이기에, 정원이 가득차있더라도 구독이 성공한다.
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isNewSubscription()).isFalse();
        }
    }

    @Nested
    @DisplayName("구독 해제")
    class Unsubscribe {
        @Test
        @DisplayName("구독하지 않은 종목을 해제하면 성공으로 처리되지만 삭제된 것은 아니다")
        void successNoOpWhenNotSubscribed() throws InterruptedException {
            // given
            initializeRegistry(2, "session1");

            // when
            StockQuoteSubscriptionResult.Unsubscription result = registry.unsubscribe("000001");

            // then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isDeleted()).isFalse();
            assertThat(result.sessionId()).isNull();
        }

        @Test
        @DisplayName("한 번만 구독된 종목을 해제하면 완전히 삭제되고 역인덱스에서도 제거된다")
        void successDeleteWhenLastSubscriptionRemoved() throws InterruptedException {
            // given
            initializeRegistry(2, "session1");
            StockQuoteSubscriptionResult.Subscription subscribed = registry.subscribe("000001");

            // when
            StockQuoteSubscriptionResult.Unsubscription result = registry.unsubscribe("000001");

            // then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isDeleted()).isTrue();
            assertThat(result.sessionId()).isEqualTo(subscribed.sessionId());
            assertThat(getStockSessionIndex()).doesNotContainKey("000001");
        }

        @Test
        @DisplayName("중복 구독된 종목은 한 번 해제하더라도 완전히 삭제되지 않는다.")
        void successPartialUnsubscribeKeepsSubscription() throws InterruptedException {
            // given
            initializeRegistry(2, "session1");
            registry.subscribe("000001");
            registry.subscribe("000001"); // 동일 종목 중복 구독 (횟수: 2회)

            // when
            StockQuoteSubscriptionResult.Unsubscription firstUnsubscribe = registry.unsubscribe("000001");

            // then: 중복 구독된 종목이기 때문에 완전히 삭제되지 않는다.
            assertThat(firstUnsubscribe.isDeleted()).isFalse();
            assertThat(getStockSessionIndex()).containsKey("000001");
        }
    }

    @Nested
    @DisplayName("동시성")
    class Concurrency {
        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("동일 종목을 여러 스레드가 동시에 구독해도 구독 횟수가 유실 없이 정확히 집계된다")
        void successConcurrentSubscriptionCountIsAccurate() throws InterruptedException {
            // given
            int threadCount = 20;
            int capacity = 41;
            String stockCode = "000001";
            initializeRegistry(capacity, "session1");

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            // when
            // 동일 종목에 대해 threadCount번의 구독 요청을 동시에 보낸다.
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        registry.subscribe(stockCode);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            executorService.shutdown();
            boolean isCompleted = latch.await(10, TimeUnit.SECONDS);

            // then: 동시에 여러 명이 같은 종목을 구독하더라도 구독 횟수가 정확하게 집계된다.
            assertThat(isCompleted).isTrue();
            StockQuoteSubscriptionRegistry.StockQuoteSubscriptionContext context = getSubscriptionContextMap().get("session1");
            Map<String, Integer> subscriptionMap = (Map<String, Integer>) ReflectionTestUtils.getField(context, "subscriptionMap");
            assertThat(subscriptionMap.get(stockCode)).isEqualTo(threadCount);
        }

        @Test
        @DisplayName("서로 다른 종목을 여러 스레드가 동시에 구독해도 전체 세션 정원을 초과하지 않는다")
        void successConcurrentSubscriptionRespectsCapacity() throws InterruptedException {
            // given
            int threadCount = 10;
            int totalCapacity = 2; // session1(1) + session2(1)
            initializeRegistry(1, "session1", "session2"); // 세션 정원은 1로 제한

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<StockQuoteSubscriptionResult.Subscription> results = Collections.synchronizedList(new ArrayList<>());

            // when: 서로 다른 threadCount개의 종목을 동시에 구독 요청한다.
            for (int i = 0; i < threadCount; i++) {
                String stockCode = String.format("%06d", i);
                executorService.submit(() -> {
                    try {
                        results.add(registry.subscribe(stockCode));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            executorService.shutdown();
            boolean isCompleted = latch.await(10, TimeUnit.SECONDS);

            // then
            assertThat(isCompleted).isTrue();

            long successCount = results.stream()
                    .filter(StockQuoteSubscriptionResult.Subscription::isSuccess)
                    .count();

            // 결과적으로 구독에 성공한 횟수는 총 세션 컨텍스트 정원과 일치한다.
            assertThat(successCount).isEqualTo(totalCapacity);
        }
    }

    @Nested
    @DisplayName("락 타임아웃")
    class LockTimeout {
        private Thread occupyLockInBackground(CountDownLatch lockAcquired, CountDownLatch releaseLock) {
            ReentrantLock lock = getLock();
            Thread lockHolder = new Thread(() -> {
                lock.lock();
                try {
                    lockAcquired.countDown();
                    releaseLock.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                    // 테스트 종료 정리 과정에서 발생 가능, 무시한다.
                } finally {
                    lock.unlock();
                }
            });
            lockHolder.start();
            return lockHolder;
        }

        @Test
        @DisplayName("다른 스레드가 락을 계속 점유하고 있으면 구독은 타임아웃 후 실패로 처리된다")
        void failSubscribeWhenLockAcquisitionTimesOut() throws InterruptedException {
            // given
            initializeRegistry(2, "session1");
            CountDownLatch lockAcquired = new CountDownLatch(1);
            CountDownLatch releaseLock = new CountDownLatch(1);
            Thread lockHolder = occupyLockInBackground(lockAcquired, releaseLock);
            lockAcquired.await();

            try {
                // when
                StockQuoteSubscriptionResult.Subscription result = registry.subscribe("000001");

                // then
                assertThat(result.isSuccess()).isFalse();
                assertThat(result.sessionId()).isNull();
            } finally {
                releaseLock.countDown();
                lockHolder.join();
            }
        }

        @Test
        @DisplayName("다른 스레드가 락을 계속 점유하고 있으면 구독 해제도 타임아웃 후 실패로 처리된다")
        void failUnsubscribeWhenLockAcquisitionTimesOut() throws InterruptedException {
            // given
            initializeRegistry(2, "session1");
            registry.subscribe("000001");

            CountDownLatch lockAcquired = new CountDownLatch(1);
            CountDownLatch releaseLock = new CountDownLatch(1);
            Thread lockHolder = occupyLockInBackground(lockAcquired, releaseLock);
            lockAcquired.await();

            try {
                // when
                StockQuoteSubscriptionResult.Unsubscription result = registry.unsubscribe("000001");

                // then
                assertThat(result.isSuccess()).isFalse();
                assertThat(result.sessionId()).isNull();
            } finally {
                releaseLock.countDown();
                lockHolder.join();
            }
        }
    }
}
