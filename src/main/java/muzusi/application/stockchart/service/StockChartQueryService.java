package muzusi.application.stockchart.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stockcandle.service.StockPeriodCandleAggregator;
import muzusi.application.stockchart.dto.StockChartDto;
import muzusi.domain.stock.type.StockPeriodType;
import muzusi.domain.stockcandle.entity.StockDailyCandle;
import muzusi.domain.stockcandle.service.StockDailyCandleService;
import muzusi.domain.stockcandle.service.StockMinuteCandleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockChartQueryService {
    private final StockMinuteCandleService stockMinuteCandleService;
    private final StockDailyCandleService stockDailyCandleService;
    private final StockPeriodCandleAggregator stockPeriodCandleAggregator;
    
    private static final int DAILY_LOOKBACK_YEARS = 1;
    private static final int WEEKLY_LOOKBACK_YEARS = 3;
    private static final int MONTHLY_LOOKBACK_YEARS = 5;
    private static final int YEARLY_LOOKBACK_YEARS = 10;
    
    /**
     * 주식 차트 정보를 조회 기간과 조회 주기에 맞게 조회하여 반환하는 메서드
     *
     * <p> {@code period}가 {@code MINUTES}인 경우, 해당 종목에 대한 분봉을 모두 조회하여 반환한다.
     * <p> 그 외의 일/주/년/월봉 데이터는 일봉 데이터를 조회 및 집계하여 반환한다.
     *
     * <p> {@code from}이 없는 경우, {@code period}별 기본 조회 기간만큼 이전 날짜로 대체한다.
     * <p> {@code to}가 없는 경우, 현재 날짜로 대체한다.
     *
     * <p> {@code WEEKLY}/{@code MONTHLY}/{@code YEARLY}인 경우, 조회 기간의 경계에 걸친 봉도 온전히 집계될 수 있도록
     * {@link #toBucketStart(LocalDate, StockPeriodType)}를 호출하여 해당 주기의 시작일,
     * {@link #toBucketEnd(LocalDate, StockPeriodType)}를 호출하여 해당 주기의 종료일로 확장한다.
     *
     * <p> 확장된 기간으로 일봉을 조회한 뒤, {@code DAILY}는 그대로 반환하고
     * {@code WEEKLY}/{@code MONTHLY}/{@code YEARLY}는 집계하여 반환한다.
     */
    @Transactional(readOnly = true)
    public List<StockChartDto> getStockChartByType(String stockCode, LocalDate from, LocalDate to, StockPeriodType period) {
        if (period == StockPeriodType.MINUTES) {
            return stockMinuteCandleService.readByStockCode(stockCode).stream()
                    .map(StockChartDto::from).toList();
        }
        
        LocalDate resolvedFrom = (from == null) ? resolveFrom(period) : from;
        LocalDate resolvedTo = (to == null) ? LocalDate.now() : to;
        
        LocalDate bucketStart = toBucketStart(resolvedFrom, period);
        LocalDate bucketEnd = toBucketEnd(resolvedTo, period);
        
        List<StockDailyCandle> stockDailyCandles = stockDailyCandleService.readByStockCodeBetween(stockCode, bucketStart, bucketEnd);
        
        return switch (period) {
            case DAILY -> stockDailyCandles.stream().map(StockChartDto::from).toList();
            case WEEKLY, MONTHLY, YEARLY -> stockPeriodCandleAggregator.aggregateStockCandlePerPeriod(stockCode, stockDailyCandles, period);
            default -> throw new IllegalArgumentException(period + "는 지원하지 않는 집계대상 기간입니다.");
        };
    }
    
    /**
     * {@code from}이 주어지지 않은 경우 사용할 기본 조회 시작일을 구하는 메서드
     *
     * <p> 오늘로부터 {@code period}별 기본 조회 기간만큼 이전 날짜를 반환한다.
     */
    private LocalDate resolveFrom(StockPeriodType period) {
        LocalDate now = LocalDate.now();
        
        return switch (period) {
            case DAILY -> now.minusYears(DAILY_LOOKBACK_YEARS);
            case WEEKLY -> now.minusYears(WEEKLY_LOOKBACK_YEARS);
            case MONTHLY -> now.minusYears(MONTHLY_LOOKBACK_YEARS);
            case YEARLY -> now.minusYears(YEARLY_LOOKBACK_YEARS);
            default -> now;
        };
    }
    
    /**
     * 주어진 날짜가 속한 기간의 시작일을 구하는 메서드
     *
     * <ul>
     *     <li>주({@code WEEKLY}): 월요일</li>
     *     <li>월({@code MONTHLY}): 1일</li>
     *     <li>년({@code YEARLY}): 1월 1일</li>
     *     <li>그 외({@code DAILY} 포함): 주어진 날짜</li>
     * </ul>
     */
    private LocalDate toBucketStart(LocalDate date, StockPeriodType period) {
        return switch (period) {
            case WEEKLY -> date.with(DayOfWeek.MONDAY);
            case MONTHLY -> date.withDayOfMonth(1);
            case YEARLY -> date.withDayOfYear(1);
            default -> date;
        };
    }
    
    /**
     * 주어진 날짜가 속한 기간의 종료일을 구하는 메서드
     *
     * <ul>
     *     <li>주({@code WEEKLY}): 일요일</li>
     *     <li>월({@code MONTHLY}): 해당 월의 마지막 날</li>
     *     <li>년({@code YEARLY}): 해당 년의 마지막 날</li>
     *     <li>그 외({@code DAILY} 포함): 주어진 날짜</li>
     * </ul>
     */
    private LocalDate toBucketEnd(LocalDate date, StockPeriodType period) {
        return switch (period) {
            case WEEKLY -> date.with(DayOfWeek.SUNDAY);
            case MONTHLY -> date.with(TemporalAdjusters.lastDayOfMonth());
            case YEARLY -> date.with(TemporalAdjusters.lastDayOfYear());
            default -> date;
        };
    }
}
