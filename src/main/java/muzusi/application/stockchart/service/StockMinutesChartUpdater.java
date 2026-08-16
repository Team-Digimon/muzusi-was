package muzusi.application.stockchart.service;

import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.stockchart.dto.StockChartDto;
import muzusi.application.stockchart.port.FetchStockChartPort;
import muzusi.application.stockcode.port.StockCodePort;
import muzusi.domain.stock.entity.StockMinutes;
import muzusi.domain.stock.service.StockMinutesService;
import muzusi.domain.stock.service.StockPriceService;
import muzusi.global.exception.ExternalApiRateLimitExceededException;
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
public class StockMinutesChartUpdater {
    private final StockCodePort stockCodePort;
    private final FetchStockChartPort fetchStockChartPort;
    private final StockMinutesService stockMinutesService;
    private final StockPriceService stockPriceService;
    private final RateLimiter kisRateLimiter; // TODO: 향후 infrastructure 레이어로 분리
    
    private static final int CHART_MINUTES_GAP = 10;
    private static final int BATCH_SIZE = 500;
    private static final int MINUTES_CHART_RETENTION_DAYS = 6;
    
    /**
     * 주식 차트 조회 포트를 통한 주식 분봉 데이터 수집 후, 주식 분봉 차트 및 현재가를 갱신하는 메서드
     *
     * <p> 전체 종목 코드를 순회하며 분봉 데이터를 {@link #CHART_MINUTES_GAP}단위로 조회한 뒤, {@link #BATCH_SIZE}마다 데이터를 모아서 일괄 캐싱한다.
     * <p> 유량 초과({@link ExternalApiRateLimitExceededException}) 발생 시 1초 대기 후 1회 재시도하며, 재시도까지 실패한 종목은 로그만 남기고 다음 종목으로 넘어간다.
     *
     * @throws InterruptedException 쓰레드 대기 중 인터럽트가 발생한 경우
     */
    public void updateStockMinutesChart() throws InterruptedException {
        Map<String, StockChartDto> stockChartMap = new HashMap<>(BATCH_SIZE);
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        
        for (String stockCode: stockCodePort.getAllStockCodes()) {
            try {
                kisRateLimiter.acquire();
                
                StockChartDto stockChart = fetchStockChartPort.getStockMinutesChart(stockCode, now, CHART_MINUTES_GAP);
                stockChartMap.put(stockCode, stockChart);
            } catch (Exception exception) {
                if (exception instanceof ExternalApiRateLimitExceededException e) {
                    Thread.sleep(1000);
                    StockChartDto stockChart = fetchStockChartPort.getStockMinutesChart(stockCode, now, CHART_MINUTES_GAP);
                    stockChartMap.put(stockCode, stockChart);
                } else {
                    log.error("[Error] Failed to fetch '{}' StockChart - {}", stockCode, exception.getMessage());
                }
            }
            
            if (++count >= BATCH_SIZE) {
                stockMinutesService.saveAllInCache(stockChartMap.values());
                stockPriceService.saveAllInCache(convertToStockPriceMap(stockChartMap));
                stockChartMap.clear();
                count = 0;
            }
        }
        
        if (!stockChartMap.isEmpty()) {
            stockMinutesService.saveAllInCache(stockChartMap.values());
            stockPriceService.saveAllInCache(convertToStockPriceMap(stockChartMap));
        }
    }
    
    /**
     * 주식 분봉 차트 Map 데이터를 주식 현재가 Map 데이터로 변환하는 메서드
     *
     * @param stockCharts 주식 종목 별 분봉 차트 정보 Map
     * @return            주식 종목 별 현재가 정보 Map
     */
    private Map<String, Object> convertToStockPriceMap(Map<String, StockChartDto> stockCharts) {
        return stockCharts.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toStockPriceDto()));
    }
    
    /**
     * 당일 분봉 데이터를 캐시에서 영구 저장소로 이관하는 메서드
     *
     * <p> 캐시 저장소에서 당일 분봉 데이터를 {@link #BATCH_SIZE}만큼 조회하여 영구 저장소로 이관한다.
     * <p> 이관 배치 작업이 완료되고나면 {@link #deleteOldMinutesChart()}를 호출하여 영구 저장소 내 오래된 분봉 데이터를 삭제한다.
     */
    public void flushStockMinutesChart() {
        List<StockMinutes> stockMinutes = new ArrayList<>(BATCH_SIZE);
        int count = 0;
        
        for (String stockCode : stockCodePort.getAllStockCodes()) {
            List<StockChartDto> stockMinuteCharts = stockMinutesService.readAllInCache(stockCode);
            
            stockMinutes.add(StockMinutes.builder()
                    .stockCode(stockCode)
                    .date(LocalDate.now(ZoneId.of("Asia/Seoul")))
                    .minutesChart(stockMinuteCharts)
                    .build());
            
            stockMinutesService.deleteInCache(stockCode);
            
            if (++count >= BATCH_SIZE) {
                stockMinutesService.saveAll(stockMinutes);
                stockMinutes.clear();
                count = 0;
            }
        }
        
        if (!stockMinutes.isEmpty()) {
            stockMinutesService.saveAll(stockMinutes);
        }
        
        deleteOldMinutesChart();
    }
    
    /**
     * 보관 기간({@link #MINUTES_CHART_RETENTION_DAYS}일)이 지난 분봉 데이터를 삭제하는 메서드
     */
    private void deleteOldMinutesChart() {
        stockMinutesService.deleteByDateBefore(LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(MINUTES_CHART_RETENTION_DAYS));
    }
}
