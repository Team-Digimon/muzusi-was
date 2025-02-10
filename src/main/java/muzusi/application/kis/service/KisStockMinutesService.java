package muzusi.application.kis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.stock.dto.StockMinutesChartInfoDto;
import muzusi.application.stock.dto.StockPriceDto;
import muzusi.domain.stock.entity.StockMinutes;
import muzusi.domain.stock.service.StockMinutesService;
import muzusi.infrastructure.data.StockCodeProvider;
import muzusi.infrastructure.kis.KisStockClient;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
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
    private final RedisService redisService;
    private final KisStockClient kisStockClient;
    private final StockMinutesService stockMinutesService;

    private static final int BATCH_SIZE = 500;

    /**
     * 한국투자증권 주식 분봉데이터 호출 및 저장 메서드.
     *
     * REST API 호출 유량 제한으로 인하여 초당 20개 단위 주식 데이터 호출 제한
     */
    public void saveStockMinutesChartAndInquirePrice() throws InterruptedException {
        int count = 0;
        Map<String, Object> inquirePriceMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        for (String code : stockCodeProvider.getAllStockCodes()) {
            if (++count % 15 == 0) {
                Thread.sleep(1000L);
            }
            StockMinutesChartInfoDto stockMinutesChartInfo = kisStockClient.getStockMinutesChartInfo(code, now);
            inquirePriceMap.put(code, stockMinutesChartInfo);

            if (count == BATCH_SIZE) {
                for (String key : inquirePriceMap.keySet()) {
                    redisService.setList(KisConstant.MINUTES_CHART_PREFIX.getValue() + ":" + key, inquirePriceMap.get(key));
                }
                redisService.addToHash(KisConstant.INQUIRE_PRICE_PREFIX.getValue(), convertStockPriceMap(inquirePriceMap));
                inquirePriceMap.clear();
                count = 0;
            }
        }

        if (!inquirePriceMap.isEmpty()) {
            for (String key : inquirePriceMap.keySet()) {
                redisService.setList(KisConstant.MINUTES_CHART_PREFIX.getValue() + ":" + key, inquirePriceMap.get(key));
            }
            redisService.addToHash(KisConstant.INQUIRE_PRICE_PREFIX.getValue(), convertStockPriceMap(inquirePriceMap));
        }
    }

    private Map<String, Object> convertStockPriceMap(Map<String, Object> stockMinutesChartMap) {
        return stockMinutesChartMap.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), extractStockPrice((StockMinutesChartInfoDto) entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    private StockPriceDto extractStockPrice(StockMinutesChartInfoDto stockMinutesChartInfo) {
        return StockPriceDto.builder()
                .stockCode(stockMinutesChartInfo.stockCode())
                .price(stockMinutesChartInfo.close())
                .build();
    }

    /**
     * 주식 분봉데이터 이관 메서드.
     * Redis -> MongoDB
     */
    public void saveDailyStockMinutesChart() {
        List<StockMinutes> stockMinutesList = new ArrayList<>(BATCH_SIZE);
        int count = 0;

        for (String code : stockCodeProvider.getAllStockCodes()) {
            String key = KisConstant.MINUTES_CHART_PREFIX.getValue() + ":" + code;

            List<StockMinutesChartInfoDto> minutesCharts = redisService.getList(key).stream()
                            .map(stockMinutesChart -> (StockMinutesChartInfoDto) stockMinutesChart)
                            .toList();

            stockMinutesList.add(StockMinutes.builder()
                    .stockCode(code)
                    .date(LocalDate.now(ZoneId.of("Asia/Seoul")))
                    .minutesChart(minutesCharts)
                    .build());

            redisService.del(key);

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