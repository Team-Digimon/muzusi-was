package muzusi.domain.stock.repository;

public interface StockItemCustomRepository {
    void incrementSearchCount(String stockCode);
}
