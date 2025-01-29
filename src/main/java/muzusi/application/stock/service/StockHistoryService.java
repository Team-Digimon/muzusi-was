package muzusi.application.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.StockChartInfoDto;
import muzusi.domain.stock.service.StockDailyService;
import muzusi.domain.stock.service.StockMonthlyService;
import muzusi.domain.stock.service.StockWeeklyService;
import muzusi.domain.stock.service.StockYearlyService;
import muzusi.domain.stock.type.StockPeriodType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockHistoryService {
    private final StockDailyService stockDailyService;
    private final StockWeeklyService stockWeeklyService;
    private final StockMonthlyService stockMonthlyService;
    private final StockYearlyService stockYearlyService;

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
}
