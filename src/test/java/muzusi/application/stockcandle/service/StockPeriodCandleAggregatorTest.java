package muzusi.application.stockcandle.service;

import muzusi.application.stockchart.dto.StockChartDto;
import muzusi.domain.stock.type.StockPeriodType;
import muzusi.domain.stockcandle.entity.StockDailyCandle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockPeriodCandleAggregatorTest {

    private final StockPeriodCandleAggregator stockPeriodCandleAggregator = new StockPeriodCandleAggregator();

    private static final String STOCK_CODE = "005930";

    private StockDailyCandle dailyCandle(LocalDate date,
                                        long open, long high, long low, long close, long volume) {
        return StockDailyCandle.builder()
                .stockCode(STOCK_CODE)
                .date(date)
                .open(open).high(high).low(low).close(close).volume(volume)
                .build();
    }

    @Nested
    @DisplayName("가드 로직")
    class Guard {

        @Test
        @DisplayName("일봉 목록이 비어 있으면 빈 목록을 반환한다")
        void returnsEmptyWhenNoDailyCandle() {
            // given
            List<StockDailyCandle> candles = List.of();

            // when
            List<StockChartDto> result =
                    stockPeriodCandleAggregator.aggregateStockCandlePerPeriod(STOCK_CODE, candles, StockPeriodType.WEEKLY);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("집계 대상이 아닌 기간(DAILY)이면 예외를 던진다")
        void throwsWhenPeriodIsDaily() {
            // given
            StockDailyCandle candle260909 = dailyCandle(LocalDate.of(2026, 9, 9), 100, 110, 95, 105, 10);

            List<StockDailyCandle> candles = List.of(candle260909);

            // when & then
            assertThatThrownBy(() ->
                    stockPeriodCandleAggregator.aggregateStockCandlePerPeriod(STOCK_CODE, candles, StockPeriodType.DAILY))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("집계 대상이 아닌 기간(MINUTES)이면 예외를 던진다")
        void throwsWhenPeriodIsMinutes() {
            // given
            StockDailyCandle candle260909 = dailyCandle(LocalDate.of(2026, 9, 9), 100, 110, 95, 105, 10);

            List<StockDailyCandle> candles = List.of(candle260909);

            // when & then
            assertThatThrownBy(() ->
                    stockPeriodCandleAggregator.aggregateStockCandlePerPeriod(STOCK_CODE, candles, StockPeriodType.MINUTES))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("주봉 집계")
    class Weekly {

        @Test
        @DisplayName("같은 주의 일봉들을 하나의 주봉으로 집계한다 - open=첫 거래일, close=마지막 거래일, high/low/volume=전체")
        void aggregatesOneWeek() {
            // given
            // 2026-09-07(월) ~ 2026-09-13(일) 주
            StockDailyCandle candle260908 = dailyCandle(LocalDate.of(2026, 9, 8), 100, 110, 95, 105, 10);
            StockDailyCandle candle260909 = dailyCandle(LocalDate.of(2026, 9, 9), 105, 130, 100, 120, 20);
            StockDailyCandle candle260911 = dailyCandle(LocalDate.of(2026, 9, 11), 120, 125, 90, 108, 15);

            List<StockDailyCandle> candles = List.of(candle260908, candle260909, candle260911);

            // when
            List<StockChartDto> result =
                    stockPeriodCandleAggregator.aggregateStockCandlePerPeriod(STOCK_CODE, candles, StockPeriodType.WEEKLY);

            // then
            assertThat(result).hasSize(1);

            StockChartDto weekly = result.get(0);
            assertThat(weekly.stockCode()).isEqualTo(STOCK_CODE);
            assertThat(weekly.dateTime()).isEqualTo("2026-09-07 00:00:00");   // 그 주 월요일
            assertThat(weekly.open()).isEqualTo(candle260908.getOpen());     // 가장 빠른 날짜(9/8)의 open
            assertThat(weekly.close()).isEqualTo(candle260911.getClose());   // 가장 느린 날짜(9/11)의 close
            assertThat(weekly.high()).isEqualTo(Math.max(candle260908.getHigh(), Math.max(candle260909.getHigh(), candle260911.getHigh())));
            assertThat(weekly.low()).isEqualTo(Math.min(candle260908.getLow(), Math.min(candle260909.getLow(), candle260911.getLow())));
            assertThat(weekly.volume()).isEqualTo(candle260908.getVolume() + candle260909.getVolume() + candle260911.getVolume());
        }

        @Test
        @DisplayName("여러 주의 일봉을 주별로 나누고, 입력 순서와 무관하게 주 시작일 오름차순으로 반환한다")
        void splitsByWeekAndSortsAscending() {
            // given
            // 9/15(화): 주 시작 9/14(월)
            StockDailyCandle candle260915 = dailyCandle(LocalDate.of(2026, 9, 15), 200, 210, 190, 205, 30);
            // 9/11(금): 주 시작 9/7(월)
            StockDailyCandle candle260911 = dailyCandle(LocalDate.of(2026, 9, 11), 120, 125, 90, 108, 15);
            // 9/8(화): 주 시작 9/7(월)
            StockDailyCandle candle260908 = dailyCandle(LocalDate.of(2026, 9, 8), 100, 110, 95, 105, 10);

            List<StockDailyCandle> candles = List.of(candle260915, candle260911, candle260908);

            // when
            List<StockChartDto> result =
                    stockPeriodCandleAggregator.aggregateStockCandlePerPeriod(STOCK_CODE, candles, StockPeriodType.WEEKLY);

            // then
            // 입력이 뒤섞여 있어도 주 시작일 오름차순으로 반환된다.
            assertThat(result).extracting(StockChartDto::dateTime)
                    .containsExactly("2026-09-07 00:00:00", "2026-09-14 00:00:00");

            // 9/7주 검증
            StockChartDto firstWeek = result.get(0);
            assertThat(firstWeek.open()).isEqualTo(candle260908.getOpen());     // 가장 빠른 날짜(9/8)의 open
            assertThat(firstWeek.close()).isEqualTo(candle260911.getClose());   // 가장 느린 날짜(9/11)의 close
        }

        @Test
        @DisplayName("주가 연말을 걸치면 해당 주 월요일(전년도)을 기준으로 한 버킷에 묶는다")
        void weekCrossingYearBoundary() {
            // given
            // 2025-12-31(수), 2026-01-02(금) → 같은 주, 월요일 2025-12-29
            StockDailyCandle candle251231 = dailyCandle(LocalDate.of(2025, 12, 31), 100, 115, 95, 110, 10);
            StockDailyCandle candle260102 = dailyCandle(LocalDate.of(2026, 1, 2), 110, 120, 105, 118, 20);

            List<StockDailyCandle> candles = List.of(candle251231, candle260102);

            // when
            List<StockChartDto> result =
                    stockPeriodCandleAggregator.aggregateStockCandlePerPeriod(STOCK_CODE, candles, StockPeriodType.WEEKLY);

            // then
            assertThat(result).hasSize(1);

            StockChartDto weekly = result.get(0);
            assertThat(weekly.dateTime()).isEqualTo("2025-12-29 00:00:00");   // 전년도 월요일
            assertThat(weekly.open()).isEqualTo(candle251231.getOpen());      // 가장 빠른 날짜(12/31)의 open
            assertThat(weekly.close()).isEqualTo(candle260102.getClose());    // 가장 느린 날짜(1/2)의 close
        }

        @Test
        @DisplayName("일봉이 1개인 주는 그 일봉 값이 그대로 주봉이 된다")
        void singleDailyCandle() {
            // given
            StockDailyCandle candle260909 = dailyCandle(LocalDate.of(2026, 9, 9), 100, 120, 90, 110, 50);

            List<StockDailyCandle> candles = List.of(candle260909);

            // when
            List<StockChartDto> result =
                    stockPeriodCandleAggregator.aggregateStockCandlePerPeriod(STOCK_CODE, candles, StockPeriodType.WEEKLY);

            // then
            assertThat(result).hasSize(1);

            StockChartDto weekly = result.get(0);
            assertThat(weekly.dateTime()).isEqualTo("2026-09-07 00:00:00");
            assertThat(weekly.open()).isEqualTo(candle260909.getOpen());
            assertThat(weekly.high()).isEqualTo(candle260909.getHigh());
            assertThat(weekly.low()).isEqualTo(candle260909.getLow());
            assertThat(weekly.close()).isEqualTo(candle260909.getClose());
            assertThat(weekly.volume()).isEqualTo(candle260909.getVolume());
        }
    }

    @Nested
    @DisplayName("월봉 집계")
    class Monthly {

        @Test
        @DisplayName("같은 월의 일봉들을 하나의 월봉으로 집계하고 대표일은 해당 월 1일이다")
        void aggregatesByMonth() {
            // given
            StockDailyCandle candle260803 = dailyCandle(LocalDate.of(2026, 8, 3), 100, 110, 95, 105, 10);
            StockDailyCandle candle260828 = dailyCandle(LocalDate.of(2026, 8, 28), 105, 140, 100, 130, 20);
            StockDailyCandle candle260901 = dailyCandle(LocalDate.of(2026, 9, 1), 130, 135, 120, 125, 30);

            List<StockDailyCandle> candles = List.of(candle260803, candle260828, candle260901);

            // when
            List<StockChartDto> result =
                    stockPeriodCandleAggregator.aggregateStockCandlePerPeriod(STOCK_CODE, candles, StockPeriodType.MONTHLY);

            // then
            // 각 월의 시작일이 월봉의 dateTime이 된다.
            assertThat(result).extracting(StockChartDto::dateTime)
                    .containsExactly("2026-08-01 00:00:00", "2026-09-01 00:00:00");

            // 8월봉 검증
            StockChartDto august = result.get(0);
            assertThat(august.open()).isEqualTo(candle260803.getOpen());     // 가장 빠른 날짜(8/3)의 open
            assertThat(august.close()).isEqualTo(candle260828.getClose());   // 가장 느린 날짜(8/28)의 close
            assertThat(august.high()).isEqualTo(Math.max(candle260803.getHigh(), candle260828.getHigh()));
            assertThat(august.low()).isEqualTo(Math.min(candle260803.getLow(), candle260828.getLow()));
            assertThat(august.volume()).isEqualTo(candle260803.getVolume() + candle260828.getVolume());
        }
    }

    @Nested
    @DisplayName("년봉 집계")
    class Yearly {

        @Test
        @DisplayName("같은 년도의 일봉들을 하나의 년봉으로 집계하고 대표일은 해당 년 1월 1일이다")
        void aggregatesByYear() {
            // given
            StockDailyCandle candle240315 = dailyCandle(LocalDate.of(2024, 3, 15), 100, 110, 95, 105, 10);
            StockDailyCandle candle241120 = dailyCandle(LocalDate.of(2024, 11, 20), 105, 150, 90, 140, 20);
            StockDailyCandle candle250701 = dailyCandle(LocalDate.of(2025, 7, 1), 140, 145, 130, 135, 30);

            List<StockDailyCandle> candles = List.of(candle240315, candle241120, candle250701);

            // when
            List<StockChartDto> result =
                    stockPeriodCandleAggregator.aggregateStockCandlePerPeriod(STOCK_CODE, candles, StockPeriodType.YEARLY);

            // then
            // 각 년도의 1월 1일이 년봉의 dateTime이 된다.
            assertThat(result).extracting(StockChartDto::dateTime)
                    .containsExactly("2024-01-01 00:00:00", "2025-01-01 00:00:00");

            // 2024년봉 검증
            StockChartDto year2024 = result.get(0);
            assertThat(year2024.open()).isEqualTo(candle240315.getOpen());     // 가장 빠른 날짜(3/15)의 open
            assertThat(year2024.close()).isEqualTo(candle241120.getClose());   // 가장 느린 날짜(11/20)의 close
            assertThat(year2024.high()).isEqualTo(Math.max(candle240315.getHigh(), candle241120.getHigh()));
            assertThat(year2024.low()).isEqualTo(Math.min(candle240315.getLow(), candle241120.getLow()));
            assertThat(year2024.volume()).isEqualTo(candle240315.getVolume() + candle241120.getVolume());
        }
    }
}
