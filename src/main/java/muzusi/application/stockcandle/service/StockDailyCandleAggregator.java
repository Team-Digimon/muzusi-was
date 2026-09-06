package muzusi.application.stockcandle.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stockcandle.dto.StockMinuteCandleDto;
import muzusi.domain.stockcandle.entity.StockDailyCandle;
import muzusi.domain.stockcandle.service.StockDailyCandleService;
import muzusi.domain.stockcandle.service.StockMinuteCandleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockDailyCandleAggregator {
    private final StockMinuteCandleService stockMinuteCandleService;
    private final StockDailyCandleService stockDailyCandleService;
    
    /**
     * 당일 수집된 분봉 데이터를 종목별로 집계하여 일봉 데이터로 저장하는 메서드
     *
     * <p> 이미 당일 일봉이 저장되어 있는 경우, 중복 저장을 막기 위해 종료한다.
     * <p> 당일 분봉 데이터가 하나도 없는 경우(휴장일 등), 아무 작업 없이 종료한다.
     *
     * <p> 조회한 분봉을 종목 코드 기준으로 그룹핑한다.
     * <p> 종목별 분봉 묶음을 하나의 일봉으로 집계하여 일괄 저장한다.
     */
    @Transactional
    public void aggregateAllStockMinuteCandleToStockDailyCandle() {
        LocalDate today = LocalDate.now();
        
        if (stockDailyCandleService.existsByDate(today)) {
            return;
        }
        
        List<StockMinuteCandleDto> stockMinuteCandleDtos = stockMinuteCandleService.readStockMinuteCandleDtoGreaterThanEqualDateTime(today.atStartOfDay());
        
        if (stockMinuteCandleDtos.isEmpty()) {
            return;
        }
        
        Map<String, List<StockMinuteCandleDto>> stockMinuteCandleDtoMap = stockMinuteCandleDtos.stream().collect(
                Collectors.groupingBy(smc -> smc.stockCode())
        );
        
        List<StockDailyCandle> stockDailyCandles = stockMinuteCandleDtoMap.entrySet().stream()
                .map(entry -> toStockDailyCandle(entry.getKey(), entry.getValue(), today)).toList();
        
        stockDailyCandleService.saveAll(stockDailyCandles);
    }
    
    /**
     * 한 종목의 분봉 목록을 하나의 일봉으로 집계하는 메서드
     *
     * <ul>
     *     <li>시가({@code open}): 당일 가장 빠른 분봉의 시가</li>
     *     <li>종가({@code close}): 당일 가장 느린 분봉의 종가</li>
     *     <li>고가({@code high}): 전체 분봉 중 고가의 최댓값</li>
     *     <li>저가({@code low}): 전체 분봉 중 저가의 최솟값</li>
     *     <li>거래량({@code volume}): 전체 분봉 거래량의 합</li>
     *     <li>대표 시각({@code date}): 현재 날짜(당일)</li>
     * </ul>
     */
    private StockDailyCandle toStockDailyCandle(
            String stockCode,
            List<StockMinuteCandleDto> stockMinuteCandleDtos,
            LocalDate date
    ) {
        StockMinuteCandleDto firstCandle = stockMinuteCandleDtos.get(0);
        StockMinuteCandleDto lastCandle = stockMinuteCandleDtos.get(0);
        
        long high = Long.MIN_VALUE;
        long low = Long.MAX_VALUE;
        long volume = 0L;
        
        for (StockMinuteCandleDto candle : stockMinuteCandleDtos) {
            if (candle.dateTime().isBefore(firstCandle.dateTime())) firstCandle = candle;
            if (candle.dateTime().isAfter(lastCandle.dateTime())) lastCandle = candle;
            high = Math.max(high, candle.high());
            low = Math.min(low, candle.low());
            volume += candle.volume();
        }
        
        return StockDailyCandle.builder()
                .stockCode(stockCode)
                .date(date)
                .open(firstCandle.open())
                .high(high)
                .low(low)
                .close(lastCandle.close())
                .volume(volume)
                .build();
    }
}
