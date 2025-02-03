package muzusi.domain.trade.repository;

import muzusi.domain.trade.entity.TradeReservation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TradeReservationRepository extends MongoRepository<TradeReservation, String> {
    List<TradeReservation> findByStockCode(String stockCode);
}
