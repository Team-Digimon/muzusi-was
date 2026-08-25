package muzusi.infrastructure.stockprice.adapter.kis;

import lombok.RequiredArgsConstructor;
import muzusi.application.stockprice.port.FetchStockPricePort;
import muzusi.infrastructure.stockprice.client.kis.KisMultiStockPriceClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KisFetchStockPriceAdapter implements FetchStockPricePort {
    private final KisMultiStockPriceClient kisMultiStockPriceClient;
    
    /**
     * 주어진 주식 종목 목록에 대한 현재가를 조회하여 반환하는 메서드
     *
     * @param stockCodes    주식 종목 코드 목록
     * @return              주식 종목 별 현재가 Map
     */
    @Override
    public Map<String, Long> getStockPrice(List<String> stockCodes) {
        return kisMultiStockPriceClient.getMultiStockPrice(stockCodes);
    }
}
