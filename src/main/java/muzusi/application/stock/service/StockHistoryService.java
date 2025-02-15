package muzusi.application.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.StockChartInfoDto;
import muzusi.domain.stock.service.StockDailyService;
import muzusi.domain.stock.service.StockMinutesService;
import muzusi.domain.stock.service.StockMonthlyService;
import muzusi.domain.stock.service.StockWeeklyService;
import muzusi.domain.stock.service.StockYearlyService;
import muzusi.domain.stock.type.StockPeriodType;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockHistoryService {
    private final StockMinutesService stockMinutesService;
    private final StockDailyService stockDailyService;
    private final StockWeeklyService stockWeeklyService;
    private final StockMonthlyService stockMonthlyService;
    private final StockYearlyService stockYearlyService;
    private final RedisService redisService;

    /**
     * 과거 주식 차트 불러오는 메서드
     * StockPeriodType
     * - MINUTES_TODAY: 당일 주식 분봉 차트 데이터를 조회
     * - MINUTES_WEEK: 지난 1주일 간 주식 분봉 차트 데이터를 조회
     * - DAILY: 일 단위 주식 차트 데이터를 조회
     * - WEEKLY: 주 단위 주식 차트 데이터를 조회
     * - MONTHLY: 월 단위 주식 차트 데이터를 조회
     * - YEARLY: 연 단위 주식 차트 데이터를 조회
     *
     * @param stockCode : 주식 코드
     * @param stockPeriodType : 주식 차트 기간 유형 (MINUTES_TODAY, MINUTES_WEEK, DAILY, WEEKLY, MONTHLY, YEARLY)
     * @return 과거 주식 차트
     */
    public List<StockChartInfoDto> getStockHistoryByType(String stockCode, StockPeriodType stockPeriodType) {
        return switch (stockPeriodType) {
            case MINUTES_TODAY -> getMinutesTodayChartByStockCode(stockCode);
            case MINUTES_WEEK -> getMinutesWeekChartByStockCode(stockCode);
            case DAILY -> stockDailyService.readByStockCode(stockCode)
                    .stream().map(StockChartInfoDto::from).toList();
            case WEEKLY -> stockWeeklyService.readByStockCode(stockCode)
                    .stream().map(StockChartInfoDto::from).toList();
            case MONTHLY -> stockMonthlyService.readByStockCode(stockCode)
                    .stream().map(StockChartInfoDto::from).toList();
            case YEARLY -> stockYearlyService.readByStockCode(stockCode)
                    .stream().map(StockChartInfoDto::from).toList();
        };
    }

    /**
     * 당일 주식 분봉 차트를 반환하는 메서드
     *
     * @param stockCode : 주식 코드
     * @return 당일 주식 분봉 차트
     */
    private List<StockChartInfoDto> getMinutesTodayChartByStockCode(String stockCode) {
        return redisService.getList(KisConstant.MINUTES_CHART_PREFIX.getValue() + ":" + stockCode)
                .stream().map(stockChartInfo -> (StockChartInfoDto) stockChartInfo)
                .toList();
    }

    /**
     * 지난 1주일 간 주식 분봉 차트를 반환하는 메서드
     *
     * @param stockCode : 주식 코드
     * @return 지난 1주일 간 주식 분봉 차트
     */
    private List<StockChartInfoDto> getMinutesWeekChartByStockCode(String stockCode) {
        return stockMinutesService.readByStockCode(stockCode).stream()
                .flatMap(stockMinutes -> stockMinutes.getMinutesChart().stream())
                .collect(Collectors.toList());
    }
}
