package muzusi.application.stockcandle.service;

import muzusi.application.stockchart.dto.StockChartDto;
import muzusi.domain.stock.type.StockPeriodType;
import muzusi.domain.stockcandle.entity.StockDailyCandle;
import muzusi.global.util.datetime.DateTimeFormatterUtil;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StockPeriodCandleAggregator {
    /**
     * 일봉 목록을 조회 기간 단위(주/월/년)로 집계하여 차트 데이터로 반환하는 메서드
     *
     * <p> 일봉 목록이 비어 있는 경우, 빈 목록을 반환한다.
     *
     * <p> 각 주기별 주기의 시작 날짜를 버킷의 키(Key)로 사용한다.
     * <ul>
     *     <li>주({@code StockPeriodType.WEEKLY}): 월요일</li>
     *     <li>월({@code StockPeriodType.MONTHLY}): 1일</li>
     *     <li>년({@code StockPeriodType.YEARLY}): 1월 1일</li>
     * </ul>
     *
     * <p> 같은 버킷 키를 가진 일봉끼리 묶는다.
     * <p> 버킷별 일봉 묶음을 하나의 봉으로 집계한 뒤, 버킷 시작일 오름차순으로 정렬해 반환한다.
     */
    public List<StockChartDto> aggregateStockCandlePerPeriod(String stockCode, List<StockDailyCandle> stockDailyCandles, StockPeriodType period) {
        if (stockDailyCandles.isEmpty()) return Collections.emptyList();
        
        Map<LocalDate, List<StockDailyCandle>> candleMap = new HashMap<>();
        
        for (StockDailyCandle candle : stockDailyCandles) {
            candleMap.computeIfAbsent(getBucketKey(candle.getId().getDate(), period), key -> new ArrayList<>()).add(candle);
        }
        
        return candleMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> aggregateBucket(stockCode, entry.getKey(), entry.getValue()))
                .toList();
    }
    
    /**
     * 주어진 날짜를 해당 기간의 시작일로 변환해 버킷 키를 구하는 메서드
     *
     * <ul>
     *     <li>주({@code StockPeriodType.WEEKLY}): 월요일</li>
     *      <li>월({@code StockPeriodType.MONTHLY}): 1일</li>
     *      <li>년({@code StockPeriodType.YEARLY}): 1월 1일</li>
     * </ul>
     *
     * @throws IllegalArgumentException 집계 대상으로 지원하지 않는 기간(분봉/일봉)이 전달된 경우
     */
    private LocalDate getBucketKey(LocalDate date, StockPeriodType period) {
        return switch (period) {
            case WEEKLY -> date.with(DayOfWeek.MONDAY);
            case MONTHLY -> date.withDayOfMonth(1);
            case YEARLY -> date.withDayOfYear(1);
            default -> throw new IllegalArgumentException(period + "는 지원하지 않는 집계 대상 기간입니다.");
        };
    }
    
    /**
     * 한 버킷에 속한 일봉 묶음을 하나의 봉으로 집계하는 메서드
     *
     * <ul>
     *     <li>시가({@code open}): 버킷 내 가장 빠른 날짜의 일봉 시가</li>
     *     <li>종가({@code close}): 버킷 내 가장 느린 날짜의 일봉 종가</li>
     *     <li>고가({@code high}): 전체 일봉 중 고가의 최댓값</li>
     *     <li>저가({@code low}): 전체 일봉 중 저가의 최솟값</li>
     *     <li>거래량({@code volume}): 전체 일봉 거래량의 합</li>
     *     <li>대표 시각({@code dateTime}): 버킷 시작일의 자정</li>
     * </ul>
     */
    private StockChartDto aggregateBucket(String stockCode, LocalDate start, List<StockDailyCandle> stockDailyCandles) {
        StockDailyCandle firstCandle = stockDailyCandles.get(0);
        StockDailyCandle lastCandle = stockDailyCandles.get(0);
        long high = Long.MIN_VALUE;
        long low = Long.MAX_VALUE;
        long volume = 0;
        
        for (StockDailyCandle candle : stockDailyCandles) {
            if (candle.getId().getDate().isBefore(firstCandle.getId().getDate())) firstCandle = candle;
            if (candle.getId().getDate().isAfter(lastCandle.getId().getDate())) lastCandle = candle;
            high = Math.max(high, candle.getHigh());
            low = Math.min(low, candle.getLow());
            volume += candle.getVolume();
        }
        
        return StockChartDto.builder()
                .stockCode(stockCode)
                .dateTime(DateTimeFormatterUtil.parseToString(start.atStartOfDay()))
                .open(firstCandle.getOpen())
                .high(high)
                .low(low)
                .close(lastCandle.getClose())
                .volume(volume)
                .build();
    }
}
