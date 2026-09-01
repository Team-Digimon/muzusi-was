package muzusi.application.stockcandle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.domain.stockcandle.service.StockMinuteCandleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockMinuteCandleCleaner {
    private final StockMinuteCandleService stockMinuteCandleService;
    
    private static final int STOCK_MINUTE_RETENTION_DAYS = 7;
    
    /**
     * 보관 기간({@value STOCK_MINUTE_RETENTION_DAYS}일)이 지난 주식 분봉 데이터를 삭제하는 메서드
     *
     * <p> 기준 시각 자정 기준 이전 분봉을 삭제하고, 삭제된 행 수를 로깅한다.
     */
    @Transactional
    public void deleteOutdatedStockMinuteCandle() {
        LocalDateTime threshold = LocalDate.now().minusDays(STOCK_MINUTE_RETENTION_DAYS).atStartOfDay();
        
        int deleted = stockMinuteCandleService.deleteByDateTimeBefore(threshold);
        log.info("보관 기간이 지난 주식 분봉 제거: {}", deleted);
    }
}
