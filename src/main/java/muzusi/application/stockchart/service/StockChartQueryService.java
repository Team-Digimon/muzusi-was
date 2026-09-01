package muzusi.application.stockchart.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stockchart.dto.StockChartDto;
import muzusi.domain.stock.service.StockDailyService;
import muzusi.domain.stock.service.StockMonthlyService;
import muzusi.domain.stock.service.StockWeeklyService;
import muzusi.domain.stock.service.StockYearlyService;
import muzusi.domain.stock.type.StockPeriodType;
import muzusi.domain.stockcandle.service.StockMinuteCandleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockChartQueryService {
    private final StockMinuteCandleService stockMinuteCandleService;
    private final StockDailyService stockDailyService;
    private final StockWeeklyService stockWeeklyService;
    private final StockMonthlyService stockMonthlyService;
    private final StockYearlyService stockYearlyService;

    /**
     * 주식 차트 정보를 조회하는 메서드
     *
     * <p> {@link StockPeriodType}에 따라 조회 대상 차트를 다르게 설정해 반환
     *
     * <ul><li> MINUTES: 주식 분봉 차트 데이터를 조회</li></ul>
     * <ul><li> DAILY: 일 단위 주식 차트 데이터를 조회</li></ul>
     * <ul><li> WEEKLY: 주 단위 주식 차트 데이터를 조회</li></ul>
     * <ul><li> MONTHLY: 월 단위 주식 차트 데이터를 조회</li></ul>
     * <ul><li> YEARLY: 연 단위 주식 차트 데이터를 조회</li></ul>
     *
     * @param stockCode : 주식 코드
     * @param stockPeriodType : 주식 차트 기간 유형 {@link StockPeriodType}
     * @return 주식 차트 목록
     */
    public List<StockChartDto> getStockHistoryByType(String stockCode, StockPeriodType stockPeriodType) {
        return switch (stockPeriodType) {
            case MINUTES -> stockMinuteCandleService.readByStockCode(stockCode)
                    .stream().map(StockChartDto::from).toList();
            case DAILY -> stockDailyService.readByStockCode(stockCode)
                    .stream().map(StockChartDto::from).toList();
            case WEEKLY -> stockWeeklyService.readByStockCode(stockCode)
                    .stream().map(StockChartDto::from).toList();
            case MONTHLY -> stockMonthlyService.readByStockCode(stockCode)
                    .stream().map(StockChartDto::from).toList();
            case YEARLY -> stockYearlyService.readByStockCode(stockCode)
                    .stream().map(StockChartDto::from).toList();
        };
    }
}
