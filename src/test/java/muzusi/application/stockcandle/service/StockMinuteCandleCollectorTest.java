package muzusi.application.stockcandle.service;

import muzusi.application.stockcandle.dto.StockMinuteCandleDto;
import muzusi.application.stockchart.port.FetchStockChartPort;
import muzusi.application.stockcode.port.StockCodePort;
import muzusi.domain.stockcandle.entity.StockMinuteCandle;
import muzusi.domain.stockcandle.service.StockMinuteCandleService;
import muzusi.global.exception.ExternalApiRateLimitExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockMinuteCandleCollectorTest {
    @Mock
    private StockCodePort stockCodePort;

    @Mock
    private FetchStockChartPort fetchStockChartPort;

    @Mock
    private StockMinuteCandleService stockMinuteCandleService;

    @InjectMocks
    private StockMinuteCandleCollector stockMinuteCandleCollector;

    private static final int CHART_MINUTE_GAP = 10;

    private StockMinuteCandleDto candleDto(String stockCode) {
        return StockMinuteCandleDto.builder()
                .stockCode(stockCode)
                .dateTime(LocalDateTime.of(2025, 6, 30, 11, 50, 0))
                .open(10000L)
                .high(11000L)
                .low(9000L)
                .close(10500L)
                .volume(1000L)
                .build();
    }

    @Test
    @DisplayName("10분봉 수집 - 전 종목 조회 후 한 번에 저장한다")
    void successToCollectAllStockMinuteCandle() throws InterruptedException {
        // given
        given(stockCodePort.getAllStockCodes()).willReturn(List.of("005930", "000660"));

        LocalDateTime fixedNow = LocalDateTime.of(2025, 6, 30, 12, 0, 0);
        StockMinuteCandleDto samsungCandle = candleDto("005930");
        StockMinuteCandleDto skHynixCandle = candleDto("000660");

        try (MockedStatic<LocalDateTime> mockedLocalDateTime
                     = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedNow);

            given(fetchStockChartPort.getStockMinuteCandle(eq("005930"), eq(fixedNow), eq(CHART_MINUTE_GAP)))
                    .willReturn(samsungCandle);
            given(fetchStockChartPort.getStockMinuteCandle(eq("000660"), eq(fixedNow), eq(CHART_MINUTE_GAP)))
                    .willReturn(skHynixCandle);

            // when
            stockMinuteCandleCollector.collectAllStockMinuteCandle();
        }

        // then
        verify(stockMinuteCandleService, times(1)).saveAll(argThat(candles ->
                candles.size() == 2
                        && containsStockCode(candles, "005930")
                        && containsStockCode(candles, "000660")));
    }

    @Test
    @DisplayName("10분봉 수집 - 유량 초과 시 1초 대기 후 1회 재시도하여 성공한다")
    void retryOnceWhenRateLimitExceeded() throws InterruptedException {
        // given
        given(stockCodePort.getAllStockCodes()).willReturn(List.of("005930"));

        given(fetchStockChartPort.getStockMinuteCandle(eq("005930"), any(), eq(CHART_MINUTE_GAP)))
                .willThrow(new ExternalApiRateLimitExceededException("유량 초과"))
                .willReturn(candleDto("005930"));

        // when
        stockMinuteCandleCollector.collectAllStockMinuteCandle();

        // then
        verify(fetchStockChartPort, times(2))
                .getStockMinuteCandle(eq("005930"), any(), eq(CHART_MINUTE_GAP));
        verify(stockMinuteCandleService).saveAll(argThat(candles ->
                candles.size() == 1 && containsStockCode(candles, "005930")));
    }

    @Test
    @DisplayName("10분봉 수집 - 유량 초과 재시도까지 실패하면 예외가 전파된다")
    void propagateWhenRetryAlsoFails() {
        // given
        given(stockCodePort.getAllStockCodes()).willReturn(List.of("005930"));

        given(fetchStockChartPort.getStockMinuteCandle(eq("005930"), any(), eq(CHART_MINUTE_GAP)))
                .willThrow(new ExternalApiRateLimitExceededException("유량 초과"));

        // when & then
        assertThatThrownBy(() -> stockMinuteCandleCollector.collectAllStockMinuteCandle())
                .isInstanceOf(ExternalApiRateLimitExceededException.class);

        verify(stockMinuteCandleService, never()).saveAll(any());
    }

    @Test
    @DisplayName("10분봉 수집 - 조회에 실패한 종목은 건너뛰고 나머지 종목을 계속 수집한다")
    void skipFailedStockCodeAndContinue() throws InterruptedException {
        // given
        given(stockCodePort.getAllStockCodes()).willReturn(List.of("005930", "000660"));

        willThrow(new RuntimeException("조회 실패"))
                .given(fetchStockChartPort).getStockMinuteCandle(eq("005930"), any(), eq(CHART_MINUTE_GAP));
        given(fetchStockChartPort.getStockMinuteCandle(eq("000660"), any(), eq(CHART_MINUTE_GAP)))
                .willReturn(candleDto("000660"));

        // when
        stockMinuteCandleCollector.collectAllStockMinuteCandle();

        // then
        verify(stockMinuteCandleService).saveAll(argThat(candles ->
                candles.size() == 1 && containsStockCode(candles, "000660")));
    }

    @Test
    @DisplayName("10분봉 수집 - 조회 결과가 배치 크기를 넘으면 중간에 나눠 저장한다")
    void flushInBatchesWhenExceedingBatchSize() throws InterruptedException {
        // given
        List<String> stockCodes = IntStream.rangeClosed(1, 501)
                .mapToObj(i -> String.format("%06d", i))
                .toList();
        given(stockCodePort.getAllStockCodes()).willReturn(stockCodes);
        stockCodes.forEach(code ->
                given(fetchStockChartPort.getStockMinuteCandle(eq(code), any(), eq(CHART_MINUTE_GAP)))
                        .willReturn(candleDto(code)));

        // when
        stockMinuteCandleCollector.collectAllStockMinuteCandle();

        // then
        verify(stockMinuteCandleService, times(2)).saveAll(any());
    }

    private boolean containsStockCode(List<StockMinuteCandle> candles, String stockCode) {
        return candles.stream().anyMatch(candle -> candle.getStockCode().equals(stockCode));
    }
}
