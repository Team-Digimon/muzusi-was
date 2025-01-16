package muzusi.domain.trade.repository;

import muzusi.domain.trade.entity.TradeReservation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TradeReservationRepository extends MongoRepository<TradeReservation, String> {
}
