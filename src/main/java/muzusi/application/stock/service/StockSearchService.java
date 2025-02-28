package muzusi.application.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.StockItemDto;
import muzusi.domain.stock.service.StockItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockSearchService {
    private final StockItemService stockItemService;

    /**
     * 검색어 자동완성을 위한 주식 검색 메서드
     *
     * @param keyword : 사용자 입력한 값
     * @return : 자동완성 리스트
     */
    public List<StockItemDto> searchStocks(String keyword) {
        return stockItemService.readByKeyword(keyword)
                .stream().map(StockItemDto::fromEntity)
                .toList();
    }

    /**
     * 종목 검색 횟수를 증가시키는 메서드
     *
     * @param stockCode : 종목 코드
     */
    public void increaseStockSearchCount(String stockCode) {
        stockItemService.updateSearchCount(stockCode);
    }
}
