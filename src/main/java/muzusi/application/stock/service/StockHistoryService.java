package muzusi.application.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.StockChartInfoDto;
import muzusi.application.stock.dto.StockMinutesChartInfoDto;
import muzusi.domain.stock.exception.StockErrorType;
import muzusi.domain.stock.service.StockDailyService;
import muzusi.domain.stock.service.StockMinutesService;
import muzusi.domain.stock.service.StockMonthlyService;
import muzusi.domain.stock.service.StockWeeklyService;
import muzusi.domain.stock.service.StockYearlyService;
import muzusi.application.stock.dto.StockMinutesPeriodDto;
import muzusi.domain.stock.type.StockPeriodType;
import muzusi.global.exception.CustomException;
import muzusi.infrastructure.redis.RedisService;
import muzusi.infrastructure.redis.constant.KisConstant;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
     * - DAILY: 일 단위 주식 차트 데이터를 조회
     * - WEEKLY: 주 단위 주식 차트 데이터를 조회
     * - MONTHLY: 월 단위 주식 차트 데이터를 조회
     * - YEARLY: 연 단위 주식 차트 데이터를 조회
     *
     * @param stockCode : 주식 코드
     * @param stockPeriodType : 주식 차트 기간 유형 (DAILY, WEEKLY, MONTHLY, YEARLY)
     * @return 과거 주식 차트
     */
    public List<StockChartInfoDto> getStockHistoryByType(String stockCode, StockPeriodType stockPeriodType) {
        return switch (stockPeriodType) {
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
     * 주식 종목 별 분봉 데이터를 조회하는 메서드
     *
     * @param stockCode : 주식 코드
     * @param period    : 분봉 데이터 조회 유형(당일 TODAY, 지난 일주일 WEEK)
     * @return 주식 분봉 데이터
     */
    public List<StockMinutesChartInfoDto> getStockMinutesHistory(String stockCode, StockMinutesPeriodDto period) {
        return switch (period) {
            case TODAY -> getTodayStockMinutes(stockCode);
            case WEEK -> getLastStockMinutes(stockCode);
            default -> throw new CustomException(StockErrorType.UNSUPPORTED_MINUTES_PERIOD);
        };
    }

    /**
     * 주식 종목 별 당일 분봉 데이터를 반환하는 메서드
     * 
     * @param stockCode : 주식 종목 코드
     * @return 당일 주식 분봉 데이터
     */
    private List<StockMinutesChartInfoDto> getTodayStockMinutes(String stockCode) {
        if (LocalDateTime.now().isBefore(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 10)))
            || LocalDateTime.now().isAfter(LocalDateTime.of(LocalDate.now(), LocalTime.of(16, 0)))) {
            throw new CustomException(StockErrorType.NOT_AVAILABLE_MINUTES_CHART);
        }

        return redisService.getList(KisConstant.MINUTES_CHART_PREFIX.getValue() + ":" + stockCode).stream()
                .map(stockMinutesChartInfoDto -> (StockMinutesChartInfoDto) stockMinutesChartInfoDto)
                .toList();
    }

    /**
     * 주식 종목 별 지난 일주일 분봉 데이터를 반환하는 메서드
     *
     * @param stockCode : 주식 종목 코드
     * @return 지난 일주일 주식 분봉 데이터
     */
    private List<StockMinutesChartInfoDto> getLastStockMinutes(String stockCode) {
        List<StockMinutesChartInfoDto> stockMinutesCharts = new ArrayList<>();

        stockMinutesService.readByStockCode(stockCode).forEach(
                stockMinutes -> stockMinutesCharts.addAll(stockMinutes.getMinutesChart())
        );

        return stockMinutesCharts;
    }
}
