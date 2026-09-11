package muzusi.application.stockcandle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.stockcandle.dto.StockMinuteCandleDto;
import muzusi.application.stockchart.port.FetchStockChartPort;
import muzusi.application.stockcode.port.StockCodePort;
import muzusi.domain.stockcandle.entity.StockMinuteCandle;
import muzusi.domain.stockcandle.service.StockMinuteCandleService;
import muzusi.global.exception.ExternalApiRateLimitExceededException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockMinuteCandleCollector {
    private final StockCodePort stockCodePort;
    private final FetchStockChartPort fetchStockChartPort;
    private final StockMinuteCandleService stockMinuteCandleService;
    
    private static final int CHART_MINUTE_GAP = 10;
    private static final int BATCH_SIZE = 500;
    
    /**
     * 모든 주식 종목에 대하여 분봉 데이터를 수집하는 메서드
     *
     * <p> 분봉 데이터는 {@value CHART_MINUTE_GAP}분 단위로 수집한다.
     * <p> 외부 주식 분봉 데이터 수집 포트를 통해 데이터를 수집하고 이를 {@value BATCH_SIZE} 단위로 저장한다.
     *
     * @throws InterruptedException 유량 초과 재시도 대기({@code Thread.sleep}) 중 인터럽트된 경우
     */
    public void collectAllStockMinuteCandle() throws InterruptedException {
        Map<String, StockMinuteCandleDto> stockMinuteCandleDtoMap = new HashMap<>();
        List<String> failedStockCodes = new ArrayList<>();
        Map<String, Integer> failureReason = new HashMap<>();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<String> stockCodes = stockCodePort.getAllStockCodes();
        int count = 0;

        for (String stockCode : stockCodes) {
            try {
                fetchStockMinuteCandle(stockCode, now)
                        .ifPresent(dto -> stockMinuteCandleDtoMap.put(stockCode, dto));
            } catch (ExternalApiRateLimitExceededException e) {
                throw e;
            }
            catch (RuntimeException e) {
                String reason = e.getClass().getSimpleName();
                failedStockCodes.add(stockCode);
                failureReason.put(reason, failureReason.getOrDefault(reason, 0) + 1);
            }

            if (++count >= BATCH_SIZE) {
                flush(stockMinuteCandleDtoMap);
                count = 0;
            }
        }

        if (!stockMinuteCandleDtoMap.isEmpty()) {
            flush(stockMinuteCandleDtoMap);
        }

        if (!failedStockCodes.isEmpty()) {
            log.error("[Error/StockMinuteCandle] 분봉 수집 실패 {}/{}건 - 원인: {} / 종목: {}",
                    failedStockCodes.size(), stockCodes.size(), failureReason, failedStockCodes);
        }
    }

    /**
     * 단일 종목의 분봉을 조회한다. 유량 초과 시 1초 대기 후 1회 재시도한다.
     *
     * @param stockCode 조회할 주식 종목 코드
     * @param now       조회 기준 시각
     * @return 분봉 DTO ({@code Optional}), 조회 결과가 없으면 비어 있음
     * @throws InterruptedException             유량 초과 재시도 대기 중 인터럽트된 경우
     * @throws ExternalApiRateLimitExceededException 재시도 후에도 유량 초과인 경우
     */
    private Optional<StockMinuteCandleDto> fetchStockMinuteCandle(String stockCode, LocalDateTime now) throws InterruptedException {
        try {
            return fetchStockChartPort.getStockMinuteCandle(stockCode, now, CHART_MINUTE_GAP);
        } catch (ExternalApiRateLimitExceededException e) {
            Thread.sleep(1000L);
            return fetchStockChartPort.getStockMinuteCandle(stockCode, now, CHART_MINUTE_GAP);
        }
    }
    
    /**
     * 분봉 DTO를 엔티티로 변환 후 일괄 저장하는 메서드
     *
     * <p> 일괄 저장 후, 분봉 DTO Map은 초기화한다.
     *
     * @param stockMinuteCandleDtoMap 종목 코드별 분봉 DTO 맵
     */
    private void flush(Map<String, StockMinuteCandleDto> stockMinuteCandleDtoMap) {
        List<StockMinuteCandle> stockMinuteCandles = stockMinuteCandleDtoMap.values().stream().map(StockMinuteCandleDto::toEntity).toList();
        stockMinuteCandleService.saveAll(stockMinuteCandles);
        stockMinuteCandleDtoMap.clear();
    }
}
