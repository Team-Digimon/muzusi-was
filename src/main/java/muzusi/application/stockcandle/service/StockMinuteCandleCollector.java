package muzusi.application.stockcandle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.stockcandle.dto.StockMinuteCandleDto;
import muzusi.application.stockchart.port.FetchStockChartPort;
import muzusi.application.stockcode.port.StockCodePort;
import muzusi.domain.stockcandle.entity.StockMinuteCandle;
import muzusi.domain.stockcandle.service.StockMinuteCandleService;
import muzusi.global.exception.ExternalApiRateLimitExceededException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockMinuteCandleCollector {
    private final StockCodePort stockCodePort;
    private final FetchStockChartPort fetchStockChartPort;
    private final StockMinuteCandleService stockMinuteCandleService;
    
    private static final int CHART_MINUTE_GAP = 10;
    private static final int BATCH_SIZE = 500;
    
    /**
     * 모든 주식 종목에 대하여 분봉 데이터를 수집하는 메서드
     *
     * <p> 분봉 데이터는 {@value CHART_MINUTE_GAP}분 단위로 수집한다.
     * <p> 외부 주식 분봉 데이터 수집 포트를 통해 데이터를 수집하고 이를 {@value BATCH_SIZE} 단위로 저장한다.
     *
     * @throws InterruptedException 유량 초과 재시도 대기({@code Thread.sleep}) 중 인터럽트된 경우
     */
    public void collectAllStockMinuteCandle() throws InterruptedException {
        Map<String, StockMinuteCandleDto> stockMinuteCandleDtoMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        int count = 0;
        
        for (String stockCode : stockCodePort.getAllStockCodes()) {
            try {
                StockMinuteCandleDto stockMinuteCandleDto = fetchStockChartPort.getStockMinuteCandle(stockCode, now, CHART_MINUTE_GAP);
                stockMinuteCandleDtoMap.put(stockCode, stockMinuteCandleDto);
            } catch (Exception exception) {
                if (exception instanceof ExternalApiRateLimitExceededException e) {
                    Thread.sleep(1000L);
                    StockMinuteCandleDto stockMinuteCandle = fetchStockChartPort.getStockMinuteCandle(stockCode, now, CHART_MINUTE_GAP);
                    stockMinuteCandleDtoMap.put(stockCode, stockMinuteCandle);
                } else {
                    log.error("[Error] Failed to fetch '{}' StockMinuteCandle - {}", stockCode, exception.getMessage());
                }
            }
            
            if (++count >= BATCH_SIZE) {
                flush(stockMinuteCandleDtoMap);
                count = 0;
            }
        }
        
        if (!stockMinuteCandleDtoMap.isEmpty()) {
            flush(stockMinuteCandleDtoMap);
        }
    }
    
    /**
     * 분봉 DTO를 엔티티로 변환 후 일괄 저장하는 메서드
     *
     * <p> 일괄 저장 후, 분봉 DTO Map은 초기화한다.
     *
     * @param stockMinuteCandleDtoMap 종목 코드별 분봉 DTO 맵
     */
    private void flush(Map<String, StockMinuteCandleDto> stockMinuteCandleDtoMap) {
        List<StockMinuteCandle> stockMinuteCandles = stockMinuteCandleDtoMap.values().stream().map(StockMinuteCandleDto::toEntity).toList();
        stockMinuteCandleService.saveAll(stockMinuteCandles);
        stockMinuteCandleDtoMap.clear();
    }
}
