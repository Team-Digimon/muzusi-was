package muzusi.application.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.StockResponse;
import muzusi.domain.stock.entity.Stock;
import muzusi.domain.stock.exception.StockErrorType;
import muzusi.domain.stock.service.StockService;
import muzusi.global.exception.CustomException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockQueryService {
    private final StockService stockService;
    
    /**
     * 주식 기본 정보를 조회하는 메서드
     *
     * @param stockCode 주식 종목 코드
     * @return          주식 기본 정보 DTO
     */
    public StockResponse getStock(String stockCode) {
        Stock stock = stockService.readByStockCode(stockCode)
                .orElseThrow(() -> new CustomException(StockErrorType.NOT_FOUND));
        
        return StockResponse.from(stock);
    }
}
