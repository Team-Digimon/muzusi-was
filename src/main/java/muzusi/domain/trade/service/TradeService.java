package muzusi.domain.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.trade.entity.Trade;
import muzusi.domain.trade.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeService {
    private final TradeRepository tradeRepository;

    public void save(Trade trade) {
        tradeRepository.save(trade);
    }

    public List<Trade> readByAccountId(Long accountId) {
        return tradeRepository.findByAccount_Id(accountId);
    }
}
