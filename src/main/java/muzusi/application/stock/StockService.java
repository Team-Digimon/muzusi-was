package muzusi.application.stock;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.entity.Stock;
import muzusi.domain.stock.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    public Optional<Stock> readByStockName(String stockName) {
        return stockRepository.findByStockName(stockName);
    }
}