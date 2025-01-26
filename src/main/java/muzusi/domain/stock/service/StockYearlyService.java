package muzusi.domain.stock.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.entity.StockYearly;
import muzusi.domain.stock.repository.StockYearlyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockYearlyService {
    private final StockYearlyRepository stockYearlyRepository;

    public void save(StockYearly stockYearly) {
        stockYearlyRepository.save(stockYearly);
    }

    public List<StockYearly> readByStockCode(String stockCode) {
        return stockYearlyRepository.findByStockCode(stockCode);
    }
}
