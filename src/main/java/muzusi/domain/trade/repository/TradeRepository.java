package muzusi.domain.trade.repository;

import muzusi.domain.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long>, CustomTradeRepository {
    List<Trade> findByAccount_Id(Long accountId);
}
