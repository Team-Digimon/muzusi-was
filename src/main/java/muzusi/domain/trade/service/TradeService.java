package muzusi.domain.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.trade.entity.Trade;
import muzusi.domain.trade.repository.TradeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeService {
    private final TradeRepository tradeRepository;

    public void save(Trade trade) {
        tradeRepository.save(trade);
    }
}
