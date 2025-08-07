package muzusi.application.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.entity.StockDaily;
import muzusi.domain.stock.entity.StockMonthly;
import muzusi.domain.stock.entity.StockWeekly;
import muzusi.domain.stock.entity.StockYearly;
import muzusi.domain.stock.service.StockDailyService;
import muzusi.domain.stock.service.StockMonthlyService;
import muzusi.domain.stock.service.StockWeeklyService;
import muzusi.domain.stock.service.StockYearlyService;
import muzusi.infrastructure.data.StockCodeProvider;
import muzusi.infrastructure.kis.stock.KisStockClient;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class StockChartCreationProcessor {
    private final StockDailyService stockDailyService;
    private final StockWeeklyService stockWeeklyService;
    private final StockMonthlyService stockMonthlyService;
    private final StockYearlyService stockYearlyService;
    private final StockCodeProvider stockCodeProvider;
    private final KisStockClient kisStockClient;

    public void createStockChart() {
        LocalDateTime now = LocalDateTime.now();
        List<String> stockCodeList = stockCodeProvider.getAllStockCodes();

        Map<String, Long> stockPriceMap = IntStream.range(0, stockCodeList.size())
                .mapToObj(idx -> {
                    if (idx % 15 == 0) {
                        try {
                            Thread.sleep(1500L);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    return Map.entry(stockCodeList.get(idx), kisStockClient.getStockInquirePrice(stockCodeList.get(idx)));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<StockDaily> stockDailies = stockPriceMap.entrySet().stream()
                .map(stockPriceInfo -> StockDaily.builder()
                        .stockCode(stockPriceInfo.getKey())
                        .date(now)
                        .open(stockPriceInfo.getValue())
                        .build()).toList();

        stockDailyService.saveAll(stockDailies);

        if (now.getDayOfWeek() == DayOfWeek.MONDAY) {
            List<StockWeekly> stockWeeklies = stockPriceMap .entrySet().stream()
                    .map(stockPriceInfo -> StockWeekly.builder()
                            .stockCode(stockPriceInfo.getKey())
                            .date(now)
                            .open(stockPriceInfo.getValue())
                            .build()).toList();

            stockWeeklyService.saveAll(stockWeeklies);
        }

        if (now.equals(getFirstDayOfMonth(now))) {
            List<StockMonthly> stockMonthlies = stockPriceMap.entrySet().stream()
                    .map(stockPriceInfo -> StockMonthly.builder()
                            .stockCode(stockPriceInfo.getKey())
                            .date(now)
                            .open(stockPriceInfo.getValue())
                            .build()).toList();

            stockMonthlyService.saveAll(stockMonthlies);
        }

        if (now.equals(getFirstDayOfYear(now))) {
            List<StockYearly> stockYearlies = stockPriceMap.entrySet().stream()
                    .map(stockPriceInfo -> StockYearly.builder()
                            .stockCode(stockPriceInfo.getKey())
                            .date(now)
                            .open(stockPriceInfo.getValue())
                            .build()).toList();

            stockYearlyService.saveAll(stockYearlies);
        }
    }

    private LocalDateTime getFirstDayOfMonth(LocalDateTime date) {
        LocalDateTime day = LocalDateTime.of(date.getYear(), date.getMonth(), 1, 9, 0);

        while (day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY) {
            day = day.plusDays(1);
        }

        return day;
    }

    private LocalDateTime getFirstDayOfYear(LocalDateTime date) {
        LocalDateTime day = LocalDateTime.of(date.getYear(), 1, 1, 9, 0);

        while (day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY) {
            day = day.plusDays(1);
        }

        return day;
    }
}