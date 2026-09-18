package muzusi.application.stocksearch.port;

import muzusi.domain.stock.entity.Stock;

import java.util.List;

public interface StockSearchPort {
    void init(List<Stock> stocks);
}