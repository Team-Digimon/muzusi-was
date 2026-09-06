package muzusi.application.stockcandle.service;

import muzusi.domain.stockcandle.dto.StockMinuteCandleDto;
import muzusi.domain.stockcandle.entity.StockDailyCandle;
import muzusi.domain.stockcandle.service.StockDailyCandleService;
import muzusi.domain.stockcandle.service.StockMinuteCandleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockDailyCandleAggregatorTest {
    @Mock
    private StockMinuteCandleService stockMinuteCandleService;

    @Mock
    private StockDailyCandleService stockDailyCandleService;

    @InjectMocks
    private StockDailyCandleAggregator stockDailyCandleAggregator;

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 4);

    /**
     * 테스트 가독성을 위해 dateTime 오름차순으로 분봉을 만든다.
     *
     * <p> 집계 로직은 시각을 직접 비교해 첫/마지막 분봉을 판별하므로, 입력 순서와 무관하게 동작한다.
     */
    private StockMinuteCandleDto minuteCandle(String stockCode, int hour, int minute,
                                              long open, long high, long low, long close, long volume) {
        return new StockMinuteCandleDto(
                stockCode,
                LocalDateTime.of(2026, 9, 4, hour, minute),
                open, high, low, close, volume
        );
    }

    private List<StockDailyCandle> captureSaved() {
        ArgumentCaptor<List<StockDailyCandle>> captor = ArgumentCaptor.forClass(List.class);
        verify(stockDailyCandleService).saveAll(captor.capture());
        return captor.getValue();
    }

    private Map<String, StockDailyCandle> byStockCode(List<StockDailyCandle> candles) {
        return candles.stream().collect(
                java.util.stream.Collectors.toMap(c -> c.getId().getStockCode(), Function.identity())
        );
    }

    @Nested
    @DisplayName("가드 로직")
    class Guard {
        @Test
        @DisplayName("이미 당일 일봉이 존재하면 분봉 조회 없이 종료한다")
        void skipWhenDailyCandleAlreadyExists() {
            given(stockDailyCandleService.existsByDate(TODAY)).willReturn(true);

            try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
                mocked.when(LocalDate::now).thenReturn(TODAY);

                stockDailyCandleAggregator.aggregateAllStockMinuteCandleToStockDailyCandle();
            }

            verify(stockMinuteCandleService, never()).readStockMinuteCandleDtoGreaterThanEqualDateTime(any());
            verify(stockDailyCandleService, never()).saveAll(any());
        }

        @Test
        @DisplayName("당일 분봉이 하나도 없으면(휴장일) 저장하지 않는다")
        void skipWhenNoMinuteCandle() {
            given(stockDailyCandleService.existsByDate(TODAY)).willReturn(false);
            given(stockMinuteCandleService.readStockMinuteCandleDtoGreaterThanEqualDateTime(TODAY.atStartOfDay()))
                    .willReturn(List.of());

            try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
                mocked.when(LocalDate::now).thenReturn(TODAY);

                stockDailyCandleAggregator.aggregateAllStockMinuteCandleToStockDailyCandle();
            }

            verify(stockDailyCandleService, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("집계 로직")
    class Aggregation {

        @Test
        @DisplayName("종목별로 그룹핑하여 각각의 일봉을 생성한다")
        void aggregatesPerStockCode() {
            given(stockDailyCandleService.existsByDate(TODAY)).willReturn(false);
            given(stockMinuteCandleService.readStockMinuteCandleDtoGreaterThanEqualDateTime(TODAY.atStartOfDay()))
                    .willReturn(List.of(
                            minuteCandle("005930", 9, 0, 100, 110, 95, 105, 10),
                            minuteCandle("005930", 9, 10, 105, 130, 100, 120, 20),
                            minuteCandle("000660", 9, 0, 200, 205, 190, 195, 30)
                    ));

            try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
                mocked.when(LocalDate::now).thenReturn(TODAY);

                stockDailyCandleAggregator.aggregateAllStockMinuteCandleToStockDailyCandle();
            }

            Map<String, StockDailyCandle> saved = byStockCode(captureSaved());
            assertThat(saved).containsOnlyKeys("005930", "000660");
        }

        @Test
        @DisplayName("open은 첫 분봉, close는 마지막 분봉, high/low/volume은 전체 집계값이다")
        void computesOhlcv() {
            given(stockDailyCandleService.existsByDate(TODAY)).willReturn(false);
            given(stockMinuteCandleService.readStockMinuteCandleDtoGreaterThanEqualDateTime(TODAY.atStartOfDay()))
                    .willReturn(List.of(
                            minuteCandle("005930", 9, 0, 100, 110, 95, 105, 10),
                            minuteCandle("005930", 9, 10, 105, 130, 100, 120, 20),
                            minuteCandle("005930", 9, 20, 120, 115, 90, 108, 15)
                    ));

            try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
                mocked.when(LocalDate::now).thenReturn(TODAY);

                stockDailyCandleAggregator.aggregateAllStockMinuteCandleToStockDailyCandle();
            }

            StockDailyCandle candle = byStockCode(captureSaved()).get("005930");
            assertThat(candle.getId().getDate()).isEqualTo(TODAY);
            assertThat(candle.getOpen()).isEqualTo(100L);   // 첫 분봉 open
            assertThat(candle.getClose()).isEqualTo(108L);  // 마지막 분봉 close
            assertThat(candle.getHigh()).isEqualTo(130L);   // max(110, 130, 115)
            assertThat(candle.getLow()).isEqualTo(90L);     // min(95, 100, 90)
            assertThat(candle.getVolume()).isEqualTo(45L);  // 10 + 20 + 15
        }

        @Test
        @DisplayName("분봉이 시간 역순으로 주어져도 open/close는 실제 시각 기준으로 정확히 집계된다")
        void computesOhlcvRegardlessOfInputOrder() {
            given(stockDailyCandleService.existsByDate(TODAY)).willReturn(false);
            given(stockMinuteCandleService.readStockMinuteCandleDtoGreaterThanEqualDateTime(TODAY.atStartOfDay()))
                    .willReturn(List.of(
                            minuteCandle("005930", 9, 20, 120, 115, 90, 108, 15),  // 가장 늦은 시각 → close
                            minuteCandle("005930", 9, 10, 105, 130, 100, 120, 20),
                            minuteCandle("005930", 9, 0, 100, 110, 95, 105, 10)    // 가장 빠른 시각 → open
                    ));

            try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
                mocked.when(LocalDate::now).thenReturn(TODAY);

                stockDailyCandleAggregator.aggregateAllStockMinuteCandleToStockDailyCandle();
            }

            StockDailyCandle candle = byStockCode(captureSaved()).get("005930");
            assertThat(candle.getOpen()).isEqualTo(100L);    // 9:00 분봉의 open
            assertThat(candle.getClose()).isEqualTo(108L);   // 9:20 분봉의 close
            assertThat(candle.getHigh()).isEqualTo(130L);
            assertThat(candle.getLow()).isEqualTo(90L);
            assertThat(candle.getVolume()).isEqualTo(45L);
        }

        @Test
        @DisplayName("분봉이 1개인 종목은 그 분봉의 값이 그대로 일봉이 된다")
        void singleMinuteCandle() {
            given(stockDailyCandleService.existsByDate(TODAY)).willReturn(false);
            given(stockMinuteCandleService.readStockMinuteCandleDtoGreaterThanEqualDateTime(TODAY.atStartOfDay()))
                    .willReturn(List.of(
                            minuteCandle("005930", 15, 20, 100, 120, 90, 110, 50)
                    ));

            try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
                mocked.when(LocalDate::now).thenReturn(TODAY);

                stockDailyCandleAggregator.aggregateAllStockMinuteCandleToStockDailyCandle();
            }

            StockDailyCandle candle = byStockCode(captureSaved()).get("005930");
            assertThat(candle.getOpen()).isEqualTo(100L);
            assertThat(candle.getClose()).isEqualTo(110L);
            assertThat(candle.getHigh()).isEqualTo(120L);
            assertThat(candle.getLow()).isEqualTo(90L);
            assertThat(candle.getVolume()).isEqualTo(50L);
        }

        @Test
        @DisplayName("한 번의 saveAll 호출로 전 종목 일봉을 일괄 저장한다")
        void savesInSingleBatch() {
            given(stockDailyCandleService.existsByDate(TODAY)).willReturn(false);
            given(stockMinuteCandleService.readStockMinuteCandleDtoGreaterThanEqualDateTime(TODAY.atStartOfDay()))
                    .willReturn(List.of(
                            minuteCandle("005930", 9, 0, 100, 110, 95, 105, 10),
                            minuteCandle("000660", 9, 0, 200, 205, 190, 195, 30)
                    ));

            try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
                mocked.when(LocalDate::now).thenReturn(TODAY);

                stockDailyCandleAggregator.aggregateAllStockMinuteCandleToStockDailyCandle();
            }

            verify(stockDailyCandleService, times(1)).saveAll(any());
            assertThat(captureSaved()).hasSize(2);
        }
    }
}
