package muzusi.domain.stock.repository;

public interface CustomStockItemRepository {
    void incrementSearchCount(String stockCode);
}
