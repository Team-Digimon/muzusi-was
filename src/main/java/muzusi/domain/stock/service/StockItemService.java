package muzusi.domain.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.entity.StockItem;
import muzusi.domain.stock.repository.StockItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockItemService {
    private final StockItemRepository stockItemRepository;

    public void saveAll(List<StockItem> stockItems) {
        stockItemRepository.saveAll(stockItems);
    }

    public List<StockItem> readByKeyword(String keyword) {
        return stockItemRepository.findByKeyword(keyword, PageRequest.of(0, 20));
    }

    public void updateSearchCount(String stockCode) {
        stockItemRepository.incrementSearchCount(stockCode);
    }
}
