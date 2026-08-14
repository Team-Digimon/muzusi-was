package muzusi.application.stockquote.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.stockquote.exception.StockQuoteException;
import muzusi.application.stockquote.port.StockQuotePort;
import muzusi.application.stockquote.registry.StockQuoteSubscriptionRegistry;
import muzusi.application.stockquote.registry.StockQuoteSubscriptionResult;
import muzusi.domain.stockquote.exception.StockQuoteErrorType;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockQuoteSubscriptionService {
    private final StockQuotePort stockQuotePort;
    private final StockQuoteSubscriptionRegistry registry;
    
    /**
     * 주식 시세 연동 포트를 통해 연결(Connection)을 받아와 구독 저장소를 초기화하는 메서드
     */
    public void setupSubscription() {
        List<String> connectionIds = stockQuotePort.connect();
        registry.initialize(connectionIds);
    }
    
    /**
     * 구독 저장소 초기화를 해제하는 메서드
     */
    public void resetSubscription() {
        registry.reset();
    }
    
    /**
     * 특정 주식 종목을 구독하는 메서드
     *
     * <p> 구독 저장소에 해당 종목 구독을 등록한다.
     * <p> 신규 구독인 경우에는 외부 주식 시세 연동 포트를 통해 구독을 요청한다.
     *
     * @param stockCode                 구독할 주식 종목 코드
     * @throws InterruptedException     구독 저장소 락 획득 대기 중 인터럽트가 발생한 경우
     * @throws StockQuoteException      구독 처리를 실패한 경우
     */
    public void subscribe(String stockCode) throws InterruptedException {
        StockQuoteSubscriptionResult.Subscription result = registry.subscribe(stockCode);

        if (!result.isSuccess()) {
            log.error("Failed to subscribe stock - {}", stockCode);
            throw new StockQuoteException(stockCode, StockQuoteErrorType.FAIL_SUBSCRIPTION);
        }

        log.info("Success to subscribe stock - session: {} / stockCode: {}", result.sessionId(), stockCode);

        if (result.isNewSubscription()) {
            stockQuotePort.subscribe(result.sessionId(), stockCode);
            log.info("Try a new subscription connection - session: {} / stockCode: {}", result.sessionId(), stockCode);
        }
    }
    
    /**
     * 특정 주식 종목 구독을 해제하는 메서드
     *
     * <p> 구독 저장소에 해당 종목 구독을 해제한다.
     * <p> 해당 종목에 대한 더 이상의 구독이 존재하지 않는 경우 외부 주식 시세 연동 포트를 통해 구독 해제를 요청한다.
     *
     * @param stockCode                 구독 해제할 주식 종목 코드
     * @throws InterruptedException     구독 저장소 락 획득 대기 중 인터럽트가 발생한 경우
     * @throws StockQuoteException      구독 해제 처리를 실패한 경우
     */
    public void unsubscribe(String stockCode) throws InterruptedException {
        StockQuoteSubscriptionResult.Unsubscription result = registry.unsubscribe(stockCode);
        
        if (!result.isSuccess()) {
            log.error("Failed to unsubscribe stock - {}", stockCode);
            throw new StockQuoteException(stockCode, StockQuoteErrorType.FAIL_UNSUBSCRIPTION);
        }
        
        log.info("Success to unsubscribe stock - session: {} / stockCode: {}", result.sessionId(), stockCode);
        
        if (result.isDeleted()) {
            stockQuotePort.unsubscribe(result.sessionId(), stockCode);
            log.info("Try a disconnect subscription connection - session: {} / stockCode: {}", result.sessionId(), stockCode);
        }
    }
}
