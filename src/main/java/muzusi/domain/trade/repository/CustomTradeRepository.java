package muzusi.domain.trade.repository;

import muzusi.domain.trade.entity.Trade;

import java.util.List;

public interface CustomTradeRepository {
    void saveAllInBatch(List<Trade> trades);
}
