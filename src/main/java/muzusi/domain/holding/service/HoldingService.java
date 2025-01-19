package muzusi.domain.holding.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.repository.HoldingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HoldingService {
    private final HoldingRepository holdingRepository;

    public void save(Holding holding) {
        holdingRepository.save(holding);
    }

    public Optional<Holding> readByUserIdAndStockCode(Long userId, String stockCode) {
        return holdingRepository.findByUser_IdAndStockCode(userId, stockCode);
    }

    public boolean existsByStockCode(String stockCode) {
        return holdingRepository.existsByStockCode(stockCode);
    }

    public void deleteByStockCode(String stockCode) {
        holdingRepository.deleteByStockCode(stockCode);
    }
}
