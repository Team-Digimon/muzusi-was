package muzusi.application.stock.service;

import muzusi.application.stockcandle.service.StockPeriodCandleAggregator;
import muzusi.application.stockchart.dto.StockChartDto;
import muzusi.application.stockchart.service.StockChartQueryService;
import muzusi.domain.stock.type.StockPeriodType;
import muzusi.domain.stockcandle.entity.StockDailyCandle;
import muzusi.domain.stockcandle.entity.StockMinuteCandle;
import muzusi.domain.stockcandle.service.StockDailyCandleService;
import muzusi.domain.stockcandle.service.StockMinuteCandleService;
import muzusi.global.util.datetime.DateTimeFormatterUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StockChartQueryServiceTest {
    @Mock
    private StockMinuteCandleService stockMinuteCandleService;

    @Mock
    private StockDailyCandleService stockDailyCandleService;

    @Mock
    private StockPeriodCandleAggregator stockPeriodCandleAggregator;

    @InjectMocks
    private StockChartQueryService stockChartQueryService;

    private static final String STOCK_CODE = "000001";

    private StockMinuteCandle minuteCandle(LocalDateTime dateTime,
                                           long open, long high, long low, long close, long volume) {
        return StockMinuteCandle.builder()
                .stockCode(STOCK_CODE)
                .dateTime(dateTime)
                .open(open).high(high).low(low).close(close).volume(volume)
                .build();
    }

    private StockDailyCandle dailyCandle(LocalDate date,
                                        long open, long high, long low, long close, long volume) {
        return StockDailyCandle.builder()
                .stockCode(STOCK_CODE)
                .date(date)
                .open(open).high(high).low(low).close(close).volume(volume)
                .build();
    }

    @Nested
    @DisplayName("분봉 차트 조회")
    class Minutes {

        @Test
        @DisplayName("StockMinuteCandle을 조회해 시각 오름차순으로 반환하며 from/to는 무시한다")
        void getMinutesChart() {
            // given
            StockMinuteCandle candle250630_0900 = minuteCandle(LocalDateTime.of(2025, 6, 30, 9, 0, 0), 10000, 10500, 9800, 10200, 1000);
            StockMinuteCandle candle250630_0910 = minuteCandle(LocalDateTime.of(2025, 6, 30, 9, 10, 0), 10200, 10800, 10100, 10700, 1500);

            given(stockMinuteCandleService.readByStockCode(STOCK_CODE))
                    .willReturn(List.of(candle250630_0900, candle250630_0910));

            // when
            List<StockChartDto> result = stockChartQueryService.getStockChartByType(
                    STOCK_CODE, LocalDate.of(2020, 1, 1), LocalDate.of(2025, 1, 1), StockPeriodType.MINUTES);

            // then
            assertThat(result).extracting(StockChartDto::dateTime)
                    .containsExactly(
                            DateTimeFormatterUtil.parseToString(candle250630_0900.getDateTime()),
                            DateTimeFormatterUtil.parseToString(candle250630_0910.getDateTime())
                    );
            assertThat(result.get(0).close()).isEqualTo(candle250630_0900.getClose());
            verifyNoInteractions(stockDailyCandleService, stockPeriodCandleAggregator);
        }
    }

    @Nested
    @DisplayName("일봉 차트 조회")
    class Daily {

        @Test
        @DisplayName("요청 기간 그대로 일봉을 조회해 StockChartDto로 반환한다")
        void getDailyChart() {
            // given
            LocalDate from = LocalDate.of(2026, 9, 9);   // 수요일
            LocalDate to = LocalDate.of(2026, 9, 11);    // 금요일
            StockDailyCandle candle260910 = dailyCandle(LocalDate.of(2026, 9, 10), 10000, 11000, 9000, 10500, 30000);

            given(stockDailyCandleService.readByStockCodeBetween(STOCK_CODE, from, to))
                    .willReturn(List.of(candle260910));

            // when
            List<StockChartDto> result = stockChartQueryService.getStockChartByType(
                    STOCK_CODE, from, to, StockPeriodType.DAILY);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).stockCode()).isEqualTo(STOCK_CODE);
            assertThat(result.get(0).dateTime()).isEqualTo(DateTimeFormatterUtil.parseToString(candle260910.getId().getDate().atStartOfDay()));
            assertThat(result.get(0).close()).isEqualTo(candle260910.getClose());
            verifyNoInteractions(stockMinuteCandleService, stockPeriodCandleAggregator);
        }

        @Test
        @DisplayName("from이 없으면 오늘로부터 1년 전 ~ 오늘 범위로 조회한다")
        void getDailyChartWithDefaultRange() {
            // given
            LocalDate today = LocalDate.now();
            given(stockDailyCandleService.readByStockCodeBetween(eq(STOCK_CODE), any(), any()))
                    .willReturn(List.of());

            // when
            stockChartQueryService.getStockChartByType(STOCK_CODE, null, null, StockPeriodType.DAILY);

            // then
            ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
            ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
            verify(stockDailyCandleService).readByStockCodeBetween(eq(STOCK_CODE), fromCaptor.capture(), toCaptor.capture());
            assertThat(fromCaptor.getValue()).isEqualTo(today.minusYears(1));
            assertThat(toCaptor.getValue()).isEqualTo(today);
        }
    }

    @Nested
    @DisplayName("주/월/년봉 차트 조회")
    class PeriodAggregation {
        @Test
        @DisplayName("주봉 조회 시 요청 기간을 주 경계로 확장해 일봉을 조회하고 집계 결과를 그대로 반환한다")
        void getWeeklyChart() {
            // given
            LocalDate from = LocalDate.of(2026, 9, 9);   // 수요일 → 주 시작 9/7(월)
            LocalDate to = LocalDate.of(2026, 9, 9);     // 수요일 → 주 끝 9/13(일)
            StockDailyCandle candle260908 = dailyCandle(LocalDate.of(2026, 9, 8), 100, 110, 95, 105, 10);
            List<StockDailyCandle> dailyCandles = List.of(candle260908);

            StockChartDto weeklyChartDto260907 = StockChartDto.builder()
                    .stockCode(STOCK_CODE).dateTime("2026-09-07 00:00:00")
                    .open(candle260908.getOpen()).high(candle260908.getHigh())
                    .low(candle260908.getLow()).close(candle260908.getClose())
                    .volume(candle260908.getVolume())
                    .build();
            List<StockChartDto> aggregated = List.of(weeklyChartDto260907);

            given(stockDailyCandleService.readByStockCodeBetween(
                    STOCK_CODE, LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 13)))
                    .willReturn(dailyCandles);
            given(stockPeriodCandleAggregator.aggregateStockCandlePerPeriod(STOCK_CODE, dailyCandles, StockPeriodType.WEEKLY))
                    .willReturn(aggregated);

            // when
            List<StockChartDto> result = stockChartQueryService.getStockChartByType(
                    STOCK_CODE, from, to, StockPeriodType.WEEKLY);

            // then
            assertThat(result).isEqualTo(aggregated);
            verifyNoInteractions(stockMinuteCandleService);
        }

        @Test
        @DisplayName("월봉 조회 시 요청 기간을 월 경계로 확장해 일봉을 조회한다")
        void getMonthlyChartSnapsToMonthBoundary() {
            // given
            LocalDate from = LocalDate.of(2026, 3, 15);
            LocalDate to = LocalDate.of(2026, 5, 20);
            given(stockDailyCandleService.readByStockCodeBetween(
                    STOCK_CODE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 31)))
                    .willReturn(List.of());
            given(stockPeriodCandleAggregator.aggregateStockCandlePerPeriod(any(), any(), eq(StockPeriodType.MONTHLY)))
                    .willReturn(List.of());

            // when
            stockChartQueryService.getStockChartByType(STOCK_CODE, from, to, StockPeriodType.MONTHLY);

            // then
            verify(stockDailyCandleService).readByStockCodeBetween(
                    STOCK_CODE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 31));
        }
    }
}
