package muzusi.application.stock.service;

import muzusi.application.market.service.MarketService;
import muzusi.application.stockchart.service.StockChartCreationProcessor;
import muzusi.application.stockcode.port.StockCodePort;
import muzusi.application.stockprice.port.FetchStockPricePort;
import muzusi.domain.stock.entity.StockDaily;
import muzusi.domain.stock.entity.StockMonthly;
import muzusi.domain.stock.entity.StockWeekly;
import muzusi.domain.stock.entity.StockYearly;
import muzusi.domain.stock.service.StockDailyService;
import muzusi.domain.stock.service.StockMonthlyService;
import muzusi.domain.stock.service.StockWeeklyService;
import muzusi.domain.stock.service.StockYearlyService;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockChartCreationProcessorTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Mock
    private StockDailyService stockDailyService;

    @Mock
    private StockWeeklyService stockWeeklyService;

    @Mock
    private StockMonthlyService stockMonthlyService;

    @Mock
    private StockYearlyService stockYearlyService;

    @Mock
    private StockCodePort stockCodePort;

    @Mock
    private FetchStockPricePort fetchStockPricePort;

    @Mock
    private MarketService marketService;

    @InjectMocks
    private StockChartCreationProcessor stockChartCreationProcessor;

    private void mockPriceMap() {
        given(stockCodePort.getAllStockCodes()).willReturn(List.of("005930", "000660"));
        given(fetchStockPricePort.getStockPrice(List.of("005930", "000660")))
                .willReturn(Map.of("005930", 70000L, "000660", 150000L));
    }

    private void runWithFixedToday(LocalDate fixedToday, Runnable action) {
        try (MockedStatic<LocalDate> mockedLocalDate
                     = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDate.when(() -> LocalDate.now(KST)).thenReturn(fixedToday);
            action.run();
        }
    }

    @Test
    @DisplayName("이미 이번 주/월/년 차트가 존재하면 - 일봉만 생성된다")
    void createOnlyDailyChartWhenPeriodChartsAlreadyExist() {
        // given: 2025-07-02(수), 이번 주/월/년 차트는 이미 앞선 거래일에 생성된 상태
        LocalDate today = LocalDate.of(2025, 7, 2);
        LocalDateTime date = today.atTime(9, 0);
        given(marketService.isMarketOpen()).willReturn(true);
        given(stockDailyService.existsByDate(date)).willReturn(false);
        given(stockWeeklyService.existsByDateBetween(any(), any())).willReturn(true);
        given(stockMonthlyService.existsByDateBetween(any(), any())).willReturn(true);
        given(stockYearlyService.existsByDateBetween(any(), any())).willReturn(true);
        mockPriceMap();

        // when
        runWithFixedToday(today, stockChartCreationProcessor::createStockChart);

        // then
        verify(stockDailyService).saveAll(argThat((List<StockDaily> list) ->
                list.size() == 2 && list.stream().allMatch(d -> d.getDate().equals(date))));
        verify(stockWeeklyService, never()).saveAll(any());
        verify(stockMonthlyService, never()).saveAll(any());
        verify(stockYearlyService, never()).saveAll(any());
    }

    @Test
    @DisplayName("휴장일 - 아무 차트도 생성하지 않고 즉시 종료한다")
    void skipEverythingWhenMarketClosed() {
        // given
        given(marketService.isMarketOpen()).willReturn(false);

        // when
        stockChartCreationProcessor.createStockChart();

        // then
        verify(stockCodePort, never()).getAllStockCodes();
        verify(stockDailyService, never()).saveAll(any());
        verify(stockWeeklyService, never()).saveAll(any());
        verify(stockMonthlyService, never()).saveAll(any());
        verify(stockYearlyService, never()).saveAll(any());
    }

    @Test
    @DisplayName("월요일이면서 이번 주 주봉이 아직 없으면 - 일봉과 주봉이 생성된다")
    void createDailyAndWeeklyChartWhenNoWeeklyChartYet() {
        // given: 2025-07-07(월), 이번 달/해 차트는 이미 생성된 상태
        LocalDate today = LocalDate.of(2025, 7, 7);
        LocalDateTime date = today.atTime(9, 0);
        given(marketService.isMarketOpen()).willReturn(true);
        given(stockDailyService.existsByDate(date)).willReturn(false);
        given(stockWeeklyService.existsByDateBetween(any(), any())).willReturn(false);
        given(stockMonthlyService.existsByDateBetween(any(), any())).willReturn(true);
        given(stockYearlyService.existsByDateBetween(any(), any())).willReturn(true);
        mockPriceMap();

        // when
        runWithFixedToday(today, stockChartCreationProcessor::createStockChart);

        // then
        verify(stockDailyService).saveAll(any());
        verify(stockWeeklyService).saveAll(argThat((List<StockWeekly> list) ->
                list.size() == 2 && list.stream().allMatch(w -> w.getDate().equals(date))));
        verify(stockMonthlyService, never()).saveAll(any());
        verify(stockYearlyService, never()).saveAll(any());
    }

    @Test
    @DisplayName("월요일이 휴장일이라 화요일에 실행돼도 - 이번 주 주봉이 아직 없으면 화요일에 생성된다")
    void createWeeklyChartOnTuesdayWhenMondayWasHoliday() {
        // given: 2025-07-08(화), 전날(월요일)이 휴장일이라 이번 주 주봉이 아직 없는 상태
        LocalDate today = LocalDate.of(2025, 7, 8);
        LocalDateTime date = today.atTime(9, 0);
        given(marketService.isMarketOpen()).willReturn(true);
        given(stockDailyService.existsByDate(date)).willReturn(false);
        given(stockWeeklyService.existsByDateBetween(any(), any())).willReturn(false);
        given(stockMonthlyService.existsByDateBetween(any(), any())).willReturn(true);
        given(stockYearlyService.existsByDateBetween(any(), any())).willReturn(true);
        mockPriceMap();

        // when
        runWithFixedToday(today, stockChartCreationProcessor::createStockChart);

        // then: 더 이상 DayOfWeek.MONDAY 여부가 아니라, 이번 주 주봉 존재 여부로 판단하므로
        // 화요일이어도 주봉이 생성되어야 한다.
        verify(stockWeeklyService).saveAll(argThat((List<StockWeekly> list) ->
                list.size() == 2 && list.stream().allMatch(w -> w.getDate().equals(date))));
    }

    @Test
    @DisplayName("이번 달 월봉이 아직 없으면 - 일봉과 월봉이 생성된다")
    void createDailyAndMonthlyChartWhenNoMonthlyChartYet() {
        // given: 2025-07-01(화), 이번 주 주봉/이번 해 연봉은 이미 생성된 상태
        LocalDate today = LocalDate.of(2025, 7, 1);
        LocalDateTime date = today.atTime(9, 0);
        given(marketService.isMarketOpen()).willReturn(true);
        given(stockDailyService.existsByDate(date)).willReturn(false);
        given(stockWeeklyService.existsByDateBetween(any(), any())).willReturn(true);
        given(stockMonthlyService.existsByDateBetween(any(), any())).willReturn(false);
        given(stockYearlyService.existsByDateBetween(any(), any())).willReturn(true);
        mockPriceMap();

        // when
        runWithFixedToday(today, stockChartCreationProcessor::createStockChart);

        // then
        verify(stockDailyService).saveAll(any());
        verify(stockWeeklyService, never()).saveAll(any());
        verify(stockMonthlyService).saveAll(argThat((List<StockMonthly> list) ->
                list.size() == 2 && list.stream().allMatch(m -> m.getDate().equals(date))));
        verify(stockYearlyService, never()).saveAll(any());
    }

    @Test
    @DisplayName("이번 해 연봉이 아직 없으면 - 일봉, 월봉, 연봉이 모두 생성된다")
    void createDailyMonthlyYearlyChartWhenNoYearlyChartYet() {
        // given: 2025-01-01(수), 이번 주/달/해 차트가 모두 아직 없는 상태
        LocalDate today = LocalDate.of(2025, 1, 1);
        LocalDateTime date = today.atTime(9, 0);
        given(marketService.isMarketOpen()).willReturn(true);
        given(stockDailyService.existsByDate(date)).willReturn(false);
        given(stockWeeklyService.existsByDateBetween(any(), any())).willReturn(true);
        given(stockMonthlyService.existsByDateBetween(any(), any())).willReturn(false);
        given(stockYearlyService.existsByDateBetween(any(), any())).willReturn(false);
        mockPriceMap();

        // when
        runWithFixedToday(today, stockChartCreationProcessor::createStockChart);

        // then
        verify(stockDailyService).saveAll(any());
        verify(stockWeeklyService, never()).saveAll(any());
        verify(stockMonthlyService).saveAll(any());
        verify(stockYearlyService).saveAll(argThat((List<StockYearly> list) ->
                list.size() == 2 && list.stream().allMatch(y -> y.getDate().equals(date))));
    }

    @Test
    @DisplayName("이미 당일 일봉 데이터가 존재하면 전체를 스킵한다")
    void skipEverythingWhenTodayChartAlreadyExists() {
        // given
        LocalDate today = LocalDate.of(2025, 7, 2);
        LocalDateTime date = today.atTime(9, 0);
        given(marketService.isMarketOpen()).willReturn(true);
        given(stockDailyService.existsByDate(date)).willReturn(true);

        // when
        runWithFixedToday(today, stockChartCreationProcessor::createStockChart);

        // then
        verify(stockCodePort, never()).getAllStockCodes();
        verify(stockDailyService, never()).saveAll(any());
        verify(stockWeeklyService, never()).saveAll(any());
        verify(stockMonthlyService, never()).saveAll(any());
        verify(stockYearlyService, never()).saveAll(any());
    }
}
