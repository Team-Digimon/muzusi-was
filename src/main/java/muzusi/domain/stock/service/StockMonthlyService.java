package muzusi.domain.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.entity.StockMonthly;
import muzusi.domain.stock.repository.StockMonthlyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMonthlyService {
    private final StockMonthlyRepository stockMonthlyRepository;

    public void save(StockMonthly stockMonthly) {
        stockMonthlyRepository.save(stockMonthly);
    }

    public List<StockMonthly> readByStockCode(String stockCode) {
        return stockMonthlyRepository.findByStockCodeOrderByDateAsc(stockCode);
    }
}
