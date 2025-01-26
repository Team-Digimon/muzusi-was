package muzusi.domain.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.entity.StockWeekly;
import muzusi.domain.stock.repository.StockWeeklyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockWeeklyService {
    private final StockWeeklyRepository stockWeeklyRepository;

    public void save(StockWeekly stockWeekly) {
        stockWeeklyRepository.save(stockWeekly);
    }

    public List<StockWeekly> readByStockCode(String stockCode) {
        return stockWeeklyRepository.findByStockCodeOrderByDateAsc(stockCode);
    }
}
