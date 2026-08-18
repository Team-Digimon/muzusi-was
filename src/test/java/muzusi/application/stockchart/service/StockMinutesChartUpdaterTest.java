package muzusi.application.stockchart.service;

import muzusi.application.stockchart.dto.StockChartDto;
import muzusi.application.stockchart.port.FetchStockChartPort;
import muzusi.application.stockcode.port.StockCodePort;
import muzusi.domain.stock.entity.StockMinutes;
import muzusi.domain.stock.service.StockMinutesService;
import muzusi.domain.stock.service.StockPriceService;
import muzusi.global.exception.ExternalApiRateLimitExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockMinutesChartUpdaterTest {

    @Mock
    private StockCodePort stockCodePort;

    @Mock
    private FetchStockChartPort fetchStockChartPort;

    @Mock
    private StockMinutesService stockMinutesService;

    @Mock
    private StockPriceService stockPriceService;

    @InjectMocks
    private StockMinutesChartUpdater stockMinutesChartUpdater;

    private StockChartDto stockChartDto(String stockCode) {
        return StockChartDto.builder()
                .stockCode(stockCode)
                .dateTime("20250630 120000")
                .open(10000L)
                .high(11000L)
                .low(9000L)
                .close(10500L)
                .volume(1000L)
                .build();
    }

    @Test
    @DisplayName("주식 분봉 차트 갱신 - 성공")
    void successToUpdateStockMinutesChart() throws InterruptedException {
        // given
        given(stockCodePort.getAllStockCodes()).willReturn(List.of("005930", "000660"));

        StockChartDto samsungChart = stockChartDto("005930");
        StockChartDto skHynixChart = stockChartDto("000660");

        try (MockedStatic<LocalDateTime> mockedLocalDateTime
                     = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            LocalDateTime fixedNow = LocalDateTime.of(2025, 6, 30, 12, 0, 0);
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedNow);

            given(fetchStockChartPort.getStockMinutesChart(eq("005930"), eq(fixedNow), eq(10)))
                    .willReturn(samsungChart);
            given(fetchStockChartPort.getStockMinutesChart(eq("000660"), eq(fixedNow), eq(10)))
                    .willReturn(skHynixChart);

            // when
            stockMinutesChartUpdater.updateStockMinutesChart();
        }

        // then
        verify(stockMinutesService).saveAllInCache(argThat(charts ->
                charts.size() == 2 && charts.containsAll(List.of(samsungChart, skHynixChart))));
        verify(stockPriceService).saveAllInCache(argThat((Map<String, Object> priceMap) ->
                priceMap.size() == 2
                        && priceMap.get("005930").equals(samsungChart.toStockPriceDto())
                        && priceMap.get("000660").equals(skHynixChart.toStockPriceDto())));
    }

    @Test
    @DisplayName("주식 분봉 차트 갱신 - 유량 초과 시 1회 재시도 후 성공")
    void successToUpdateStockMinutesChartAfterRetry() throws InterruptedException {
        // given
        given(stockCodePort.getAllStockCodes()).willReturn(List.of("005930"));

        StockChartDto samsungChart = stockChartDto("005930");

        given(fetchStockChartPort.getStockMinutesChart(eq("005930"), any(), eq(10)))
                .willThrow(new ExternalApiRateLimitExceededException("유량 초과"))
                .willReturn(samsungChart);

        // when
        stockMinutesChartUpdater.updateStockMinutesChart();

        // then
        verify(fetchStockChartPort, times(2))
                .getStockMinutesChart(eq("005930"), any(), eq(10));
        verify(stockMinutesService).saveAllInCache(argThat(charts ->
                charts.size() == 1 && charts.contains(samsungChart)));
    }

    @Test
    @DisplayName("주식 분봉 차트 갱신 - 재시도까지 실패한 종목은 스킵하고 다음 종목을 진행")
    void skipStockCodeWhenFetchFailsPersistently() throws InterruptedException {
        // given
        given(stockCodePort.getAllStockCodes()).willReturn(List.of("005930", "000660"));

        StockChartDto skHynixChart = stockChartDto("000660");

        willThrow(new RuntimeException("조회 실패"))
                .given(fetchStockChartPort).getStockMinutesChart(eq("005930"), any(), eq(10));
        given(fetchStockChartPort.getStockMinutesChart(eq("000660"), any(), eq(10)))
                .willReturn(skHynixChart);

        // when
        stockMinutesChartUpdater.updateStockMinutesChart();

        // then
        verify(stockMinutesService).saveAllInCache(argThat(charts ->
                charts.size() == 1 && charts.contains(skHynixChart)));
    }

    @Test
    @DisplayName("당일 분봉 차트 이관 - 성공")
    void successToFlushStockMinutesChart() {
        // given
        given(stockCodePort.getAllStockCodes()).willReturn(List.of("005930", "000660"));

        StockChartDto samsungChart = stockChartDto("005930");
        StockChartDto skHynixChart = stockChartDto("000660");
        given(stockMinutesService.readAllInCache("005930")).willReturn(List.of(samsungChart));
        given(stockMinutesService.readAllInCache("000660")).willReturn(List.of(skHynixChart));

        try (MockedStatic<LocalDate> mockedLocalDate
                     = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            LocalDate fixedToday = LocalDate.of(2025, 6, 30);
            mockedLocalDate.when(() -> LocalDate.now(ZoneId.of("Asia/Seoul"))).thenReturn(fixedToday);

            // when
            stockMinutesChartUpdater.flushStockMinutesChart();

            // then
            verify(stockMinutesService).deleteInCache("005930");
            verify(stockMinutesService).deleteInCache("000660");
            verify(stockMinutesService).saveAll(argThat((List<StockMinutes> stockMinutesList) ->
                    stockMinutesList.size() == 2
                            && stockMinutesList.stream().anyMatch(stockMinutes ->
                            stockMinutes.getStockCode().equals("005930")
                                    && stockMinutes.getDate().equals(fixedToday)
                                    && stockMinutes.getMinutesChart().equals(List.of(samsungChart)))
                            && stockMinutesList.stream().anyMatch(stockMinutes ->
                            stockMinutes.getStockCode().equals("000660")
                                    && stockMinutes.getDate().equals(fixedToday)
                                    && stockMinutes.getMinutesChart().equals(List.of(skHynixChart)))));
            verify(stockMinutesService).deleteByDateBefore(fixedToday.minusDays(6));
        }
    }
}
