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
        return holdingRepository.findLatestAccountHolding(userId, stockCode);
    }

    public boolean existsByUserIdAndStockCode(Long userId, String stockCode) {
        return holdingRepository.existsByLatestAccountHolding(userId, stockCode) == 1;
    }

    public void deleteByUserIdAndStockCode(Long userId, String stockCode) {
        holdingRepository.deleteByLatestAccountHolding(userId, stockCode);
    }
}
