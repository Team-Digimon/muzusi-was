package muzusi.application.stock.service;

import muzusi.application.stockchart.dto.StockChartDto;
import muzusi.application.stockchart.service.StockChartQueryService;
import muzusi.domain.stock.entity.StockDaily;
import muzusi.domain.stock.service.StockDailyService;
import muzusi.domain.stock.service.StockMonthlyService;
import muzusi.domain.stock.service.StockWeeklyService;
import muzusi.domain.stock.service.StockYearlyService;
import muzusi.domain.stock.type.StockPeriodType;
import muzusi.domain.stockcandle.entity.StockMinuteCandle;
import muzusi.domain.stockcandle.service.StockMinuteCandleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StockChartQueryServiceTest {
    @Mock
    private StockMinuteCandleService stockMinuteCandleService;

    @Mock
    private StockDailyService stockDailyService;

    @Mock
    private StockWeeklyService stockWeeklyService;

    @Mock
    private StockMonthlyService stockMonthlyService;

    @Mock
    private StockYearlyService stockYearlyService;

    @InjectMocks
    private StockChartQueryService stockChartQueryService;

    private final String stockCode = "000001";

    @Test
    @DisplayName("분봉 차트 조회 - StockMinuteCandle을 조회해 시각 오름차순으로 반환한다")
    void getMinutesChart() {
        // given
        StockMinuteCandle first = StockMinuteCandle.builder()
                .stockCode(stockCode)
                .dateTime(LocalDateTime.of(2025, 6, 30, 9, 0, 0))
                .open(10000L).high(10500L).low(9800L).close(10200L).volume(1000L)
                .build();
        StockMinuteCandle second = StockMinuteCandle.builder()
                .stockCode(stockCode)
                .dateTime(LocalDateTime.of(2025, 6, 30, 9, 10, 0))
                .open(10200L).high(10800L).low(10100L).close(10700L).volume(1500L)
                .build();
        given(stockMinuteCandleService.readByStockCode(stockCode))
                .willReturn(List.of(first, second));

        // when
        List<StockChartDto> result = stockChartQueryService
                .getStockHistoryByType(stockCode, StockPeriodType.MINUTES);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).dateTime()).isEqualTo("2025-06-30 09:00:00");
        assertThat(result.get(0).close()).isEqualTo(10200L);
        assertThat(result.get(1).dateTime()).isEqualTo("2025-06-30 09:10:00");
        verifyNoInteractions(stockDailyService, stockWeeklyService, stockMonthlyService, stockYearlyService);
    }

    @Test
    @DisplayName("일봉 차트 조회 - StockDaily를 조회해 반환한다")
    void getDailyChart() {
        // given
        StockDaily stockDaily = StockDaily.builder()
                .stockCode(stockCode)
                .date(LocalDateTime.of(2025, 6, 30, 9, 0, 0))
                .open(10000L).high(11000L).low(9000L).close(10500L).volume(30000L)
                .build();
        given(stockDailyService.readByStockCode(stockCode))
                .willReturn(List.of(stockDaily));

        // when
        List<StockChartDto> result = stockChartQueryService
                .getStockHistoryByType(stockCode, StockPeriodType.DAILY);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).stockCode()).isEqualTo(stockCode);
        assertThat(result.get(0).close()).isEqualTo(10500L);
        verifyNoInteractions(stockMinuteCandleService);
    }
}
