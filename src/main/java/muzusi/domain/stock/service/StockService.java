package muzusi.domain.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.entity.Stock;
import muzusi.domain.stock.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;
    
    public Optional<Stock> readByStockCode(String stockCode) {
        return stockRepository.findByStockCode(stockCode);
    }
    
    public List<Stock> readAll() {
        return stockRepository.findAll();
    }
}
