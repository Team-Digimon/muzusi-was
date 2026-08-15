package muzusi.application.stockchart.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.market.service.MarketService;
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
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StockChartCreationProcessor {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final StockDailyService stockDailyService;
    private final StockWeeklyService stockWeeklyService;
    private final StockMonthlyService stockMonthlyService;
    private final StockYearlyService stockYearlyService;
    private final StockCodePort stockCodePort;
    private final FetchStockPricePort fetchStockPricePort;
    private final MarketService marketService;

    /**
     * 당일 시가를 기준으로 일봉/주봉/월봉/연봉 차트를 생성하는 메서드
     *
     * <p> 국내 주식 시장 개장일이 아니면, 아무 작업을 진행하지 않고 종료한다.
     * <p> 당일 일봉이 이미 생성되어있는 경우, 중복 생성을 막기 위해 종료한다.
     *
     * <p> 모든 주식의 현재가를 조회하여 당일 시가로 사용한다.
     * <p> 조회한 주식의 현재가를 기준으로 일봉을 생성하여 저장한다.
     *
     * <p> 이번 주(이번 주 월요일부터 오늘까지) 구간에 주봉이 아직 없으면 주봉을 생성하여 저장한다.
     * <p> 이번 달(이번 달 1일부터 오늘까지) 구간에 월봉이 아직 없으면 월봉을 생성하여 저장한다.
     * <p> 이번 해(이번 해 1월 1일부터 오늘까지) 구간에 연봉이 아직 없으면 연봉을 생성하여 저장한다.
     */
    public void createStockChart() {
        if (!marketService.isMarketOpen()) {
            return;
        }

        LocalDate today = LocalDate.now(KST);
        LocalDateTime date = today.atTime(9, 0);

        if (stockDailyService.existsByDate(date)) {
            return;
        }

        List<String> stockCodeList = stockCodePort.getAllStockCodes();
        Map<String, Long> stockPriceMap = fetchStockPricePort.getStockPrice(stockCodeList);

        List<StockDaily> stockDailies = stockPriceMap.entrySet().stream()
                .map(stockPriceInfo -> StockDaily.builder()
                        .stockCode(stockPriceInfo.getKey())
                        .date(date)
                        .open(stockPriceInfo.getValue())
                        .build()).toList();

        stockDailyService.saveAll(stockDailies);

        LocalDateTime weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        if (!stockWeeklyService.existsByDateBetween(weekStart, date)) {
            List<StockWeekly> stockWeeklies = stockPriceMap.entrySet().stream()
                    .map(stockPriceInfo -> StockWeekly.builder()
                            .stockCode(stockPriceInfo.getKey())
                            .date(date)
                            .open(stockPriceInfo.getValue())
                            .build()).toList();

            stockWeeklyService.saveAll(stockWeeklies);
        }

        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        if (!stockMonthlyService.existsByDateBetween(monthStart, date)) {
            List<StockMonthly> stockMonthlies = stockPriceMap.entrySet().stream()
                    .map(stockPriceInfo -> StockMonthly.builder()
                            .stockCode(stockPriceInfo.getKey())
                            .date(date)
                            .open(stockPriceInfo.getValue())
                            .build()).toList();

            stockMonthlyService.saveAll(stockMonthlies);
        }

        LocalDateTime yearStart = today.withDayOfYear(1).atStartOfDay();
        if (!stockYearlyService.existsByDateBetween(yearStart, date)) {
            List<StockYearly> stockYearlies = stockPriceMap.entrySet().stream()
                    .map(stockPriceInfo -> StockYearly.builder()
                            .stockCode(stockPriceInfo.getKey())
                            .date(date)
                            .open(stockPriceInfo.getValue())
                            .build()).toList();

            stockYearlyService.saveAll(stockYearlies);
        }
    }
}
