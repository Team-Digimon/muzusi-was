package muzusi.domain.stock.repository;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stock.entity.StockItem;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@RequiredArgsConstructor
public class CustomStockItemRepositoryImpl implements CustomStockItemRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public void incrementSearchCount(String stockCode) {
        Query query = new Query(Criteria.where("stockCode").is(stockCode));
        Update update = new Update().inc("searchCount", 1);
        mongoTemplate.updateFirst(query, update, StockItem.class);
    }
}
