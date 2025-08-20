package muzusi.application.kis.service;

import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.stock.dto.StockChartInfoDto;
import muzusi.domain.stock.entity.StockMinutes;
import muzusi.domain.stock.service.StockMinutesService;
import muzusi.domain.stock.service.StockPriceService;
import muzusi.global.exception.KisApiException;
import muzusi.infrastructure.data.StockCodeProvider;
import muzusi.infrastructure.kis.auth.KisAuthService;
import muzusi.infrastructure.kis.stock.KisStockClient;
import muzusi.infrastructure.kis.util.KisErrorParser;
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
public class KisStockChartUpdater {
    private final StockCodeProvider stockCodeProvider;
    private final KisStockClient kisStockClient;
    private final StockMinutesService stockMinutesService;
    private final StockPriceService stockPriceService;
    private final KisErrorParser kisErrorParser;
    private final KisAuthService kisAuthService;
    
    private final RateLimiter rateLimiter = RateLimiter.create(15);
    private static final int BATCH_SIZE = 500;

    /**
     * 한국투자증권 주식 분봉데이터 호출 및 저장 메서드.
     * - 주식 분봉 데이터 저장(캐싱)
     * - 주식 분봉 데이터를 주식 현재가 정보로 파싱해 저장(캐싱)
     *
     * - REST API 호출 유량 제한으로 인하여 쓰로틀링 허용량 설정 - 15
     * - 호출 유량 초과 에러 발생 시, 재시도 로직 수행
     *
     * @throws KisApiException 한국투자증권 주식 분봉 데이터 API 호출 시, 호출 유량 초과가 아닌 다른 에러가 발생한 경우
     */
    public void saveStockMinutesChartAndInquirePrice() throws InterruptedException {
        int count = 0;
        Map<String, StockChartInfoDto> stockChartInfoMap = new HashMap<>(BATCH_SIZE);
        LocalDateTime now = LocalDateTime.now();
        String accessToken = kisAuthService.getAccessToken();

        for (String code : stockCodeProvider.getAllStockCodes()) {
            try {
                rateLimiter.acquire();
                StockChartInfoDto stockChartInfo = kisStockClient.getStockMinutesChartInfo(code, now, accessToken);
                stockChartInfoMap.put(code, stockChartInfo);
    
                if (++count >= BATCH_SIZE) {
                    stockMinutesService.saveAllInCache(stockChartInfoMap.values());
                    stockPriceService.saveAllInCache(convertToStockPriceMap(stockChartInfoMap));
                    stockChartInfoMap.clear();
                    count = 0;
                }
            } catch (Exception e) {
                if (kisErrorParser.isApiRequestExceeded(e.getMessage())) {
                    retrySaveStockMinutesChartAndInquirePrice(stockChartInfoMap, code, now, accessToken);
                } else {
                    throw new KisApiException(e);
                }
            }
        }

        if (!stockChartInfoMap.isEmpty()) {
            stockMinutesService.saveAllInCache(stockChartInfoMap.values());
            stockPriceService.saveAllInCache(convertToStockPriceMap(stockChartInfoMap));
        }
    }
    
    /**
     * 한국투자증권 주식 분봉 데이터 호출 시 API 호출 유량 초과로 인한 실패 시 재시도를 수행하는 메서드
     *
     * - 안정성 보장을 위한 1초 쓰레드 정지
     * - API 호출 유량 초과된 주식 종목 코드를 바탕으로 재시도 수행 및 주식 분봉 차트 Map에 저장
     *
     * @param stockChartInfoMap         주식 분봉 차트 Map
     * @param code                      API 호출 유량 초과가 발생한 주식 종목 코드
     * @param now                       주식 분봉 데이터 호출 시각
     */
    private void retrySaveStockMinutesChartAndInquirePrice(
            Map<String, StockChartInfoDto> stockChartInfoMap,
            String code,
            LocalDateTime now,
            String accessToken
    ) throws InterruptedException {
        Thread.sleep(1000);
        StockChartInfoDto stockChartInfo = kisStockClient.getStockMinutesChartInfo(code, now, accessToken);
        stockChartInfoMap.put(code, stockChartInfo);
    }
    
    /**
     * 주식 분봉 차트 Map 데이터를 주식 현재가 Map 데이터로 변환하는 메서드
     *
     * @param stockChartInfoMap 주식 종목 코드를 Key, 주식 분봉 차트 정보를 Value로 가지는 Map
     * @return                  주식 종목 코드를 Key, 주식 현재가 정보를 Value로 가지는 Map
     */
    private Map<String, Object> convertToStockPriceMap(Map<String, StockChartInfoDto> stockChartInfoMap) {
        return stockChartInfoMap.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().toStockPrice()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 주식 분봉데이터 이관 메서드.
     *
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