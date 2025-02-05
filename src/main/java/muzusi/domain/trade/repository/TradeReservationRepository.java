package muzusi.domain.trade.repository;

import muzusi.domain.trade.entity.TradeReservation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TradeReservationRepository extends MongoRepository<TradeReservation, String> {
    List<TradeReservation> findByStockCode(String stockCode);

    @Query(value = "{ 'userId': ?0 }", sort = "{ 'createdAt': -1 }")
    List<TradeReservation> findByUserIdOrderByCreatedAtDesc(Long userId);
}
