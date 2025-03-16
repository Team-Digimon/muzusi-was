package muzusi.application.kis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.stock.dto.StockChartInfoDto;
import muzusi.application.stock.dto.StockPriceDto;
import muzusi.domain.stock.entity.StockMinutes;
import muzusi.domain.stock.service.StockMinutesService;
import muzusi.domain.stock.service.StockPriceService;
import muzusi.infrastructure.data.StockCodeProvider;
import muzusi.infrastructure.kis.KisStockClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisStockMinutesService {
    private final StockCodeProvider stockCodeProvider;
    private final KisStockClient kisStockClient;
    private final StockMinutesService stockMinutesService;
    private final StockPriceService stockPriceService;
    private static final int BATCH_SIZE = 500;

    /**
     * 한국투자증권 주식 분봉데이터 호출 및 저장 메서드.
     *
     * REST API 호출 유량 제한으로 인하여 초당 15개 단위 주식 데이터 호출 제한
     */
    public void saveStockMinutesChartAndInquirePrice() throws InterruptedException {
        int count = 0;
        Map<String, StockChartInfoDto> stockChartInfoMap = new HashMap<>(BATCH_SIZE);
        LocalDateTime now = LocalDateTime.now();

        for (String code : stockCodeProvider.getAllStockCodes()) {
            if (++count % 15 == 0) {
                Thread.sleep(1000L);
            }
            StockChartInfoDto stockChartInfo = kisStockClient.getStockMinutesChartInfo(code, now);
            stockChartInfoMap.put(code, stockChartInfo);

            if (count == BATCH_SIZE) {
                stockMinutesService.saveAllInCache(stockChartInfoMap.values());
                stockPriceService.saveAll(convertToStockPriceMap(stockChartInfoMap));
                stockChartInfoMap.clear();
                count = 0;
            }
        }

        if (!stockChartInfoMap.isEmpty()) {
            stockMinutesService.saveAllInCache(stockChartInfoMap.values());
            stockPriceService.saveAll(convertToStockPriceMap(stockChartInfoMap));
        }
    }

    private Map<String, Object> convertToStockPriceMap(Map<String, StockChartInfoDto> stockChartInfoMap) {
        return stockChartInfoMap.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), extractStockPrice(entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private StockPriceDto extractStockPrice(StockChartInfoDto stockMinutesChartInfo) {
        return StockPriceDto.builder()
                .stockCode(stockMinutesChartInfo.stockCode())
                .low(stockMinutesChartInfo.low())
                .high(stockMinutesChartInfo.high())
                .close(stockMinutesChartInfo.close())
                .build();
    }

    /**
     * 주식 분봉데이터 이관 메서드.
     * Redis -> MongoDB
     */
    public void saveDailyStockMinutesChart() {
        List<StockMinutes> stockMinutesList = new ArrayList<>(BATCH_SIZE);
        int count = 0;

        for (String stockCode : stockCodeProvider.getAllStockCodes()) {
            List<StockChartInfoDto> minutesCharts = stockMinutesService.readAllInCache(stockCode);

            stockMinutesList.add(StockMinutes.builder()
                    .stockCode(stockCode)
                    .date(LocalDate.now(ZoneId.of("Asia/Seoul")))
                    .minutesChart(minutesCharts)
                    .build());

            stockMinutesService.deleteInCache(stockCode);

            if (++count == BATCH_SIZE) {
               stockMinutesService.saveAll(stockMinutesList);
               stockMinutesList.clear();
               count = 0;
            }
        }

        if (!stockMinutesList.isEmpty()) {
           stockMinutesService.saveAll(stockMinutesList);
        }
    }
}