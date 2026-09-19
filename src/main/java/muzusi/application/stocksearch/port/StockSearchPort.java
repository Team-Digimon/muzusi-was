package muzusi.application.stocksearch.port;

import muzusi.application.stocksearch.dto.StockSearchCandidate;
import muzusi.domain.stock.entity.Stock;

import java.util.List;

public interface StockSearchPort {
    void init(List<Stock> stocks);
    List<StockSearchCandidate> search(String keyword, int limit);
}