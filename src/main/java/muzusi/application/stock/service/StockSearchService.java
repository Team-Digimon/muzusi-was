package muzusi.application.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.entity.StockItem;
import muzusi.infrastructure.repository.StockInMemoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockSearchService {
    private final StockInMemoryRepository stockInMemoryRepository;

    /**
     * 검색어 자동완성을 위한 주식 검색 메서드
     * 정렬 기준:
     * 1. 해당 종목이 검색된 횟수가 많은 순 (내림차순)
     * 2. 검색 횟수가 동일한 경우, 종목명을 기준으로 오름차순 정렬
     *
     * @param keyword : 사용자 입력한 값
     * @return : 자동완성 리스트
     */
    public List<StockItem> searchStocks(String keyword) {
        List<StockItem> stockItems = stockInMemoryRepository.findByKeyword(keyword);

        stockItems.sort((s1, s2) -> {
            int count1 = stockInMemoryRepository.findSearchCountByStockName(s1.getStockName());
            int count2 = stockInMemoryRepository.findSearchCountByStockName(s2.getStockName());
            if (count1 == count2)
                return s1.getStockName().compareTo(s2.getStockName());
            return count2 - count1;
        });

        return stockItems;
    }
}
