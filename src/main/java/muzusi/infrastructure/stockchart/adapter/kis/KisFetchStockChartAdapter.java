package muzusi.infrastructure.stockchart.adapter.kis;

import lombok.RequiredArgsConstructor;
import muzusi.application.stockchart.port.FetchStockChartPort;
import muzusi.infrastructure.stockchart.client.kis.KisStockChartClient;
import muzusi.application.stockchart.dto.StockChartDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class KisFetchStockChartAdapter implements FetchStockChartPort {
    private final KisStockChartClient stockChartClient;
    
    /**
     * 한국투자증권 당일분봉조회 API를 통해 {@code gap} 간격의 분봉 데이터를 조회해 차트 정보를 반환하는 메서드
     *
     * @param stockCode 주식 종목 코드 번호
     * @param time      조회 시간
     * @param gap       차트 간격
     * @return          차트 정보 DTO
     */
    @Override
    public StockChartDto getStockMinutesChart(String stockCode, LocalDateTime time, int gap) {
        return stockChartClient.getStockMinutesChart(stockCode, time, gap);
    }
}
