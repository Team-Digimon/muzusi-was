package muzusi.domain.stock.repository;

import muzusi.domain.stock.entity.StockItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface StockItemRepository extends MongoRepository<StockItem, String>, StockItemCustomRepository {

    @Query(value = "{'stockName': {$regex: ?0, $options: 'i'}}", sort = "{'searchCount': -1, 'stockName': 1}")
    List<StockItem> findByKeyword(String keyword, Pageable pageable);


}
