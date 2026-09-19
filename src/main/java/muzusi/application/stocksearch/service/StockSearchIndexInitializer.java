package muzusi.application.stocksearch.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import muzusi.application.stocksearch.port.StockSearchPort;
import muzusi.domain.stock.service.StockService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockSearchIndexInitializer {
    private final StockService stockService;
    private final StockSearchPort stockSearchPort;
    
    /**
     * 주식 검색 색인 초기화 메서드
     */
    @PostConstruct
    public void init() {
        stockSearchPort.init(stockService.readAll());
    }
}
