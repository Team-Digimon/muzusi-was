package muzusi.domain.holding.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.repository.HoldingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HoldingService {
    private final HoldingRepository holdingRepository;

    public Holding save(Holding holding) {
        return holdingRepository.save(holding);
    }

    public Optional<Holding> readByUserIdAndStockCode(Long userId, String stockCode) {
        return holdingRepository.findLatestAccountHolding(userId, stockCode);
    }

    public List<Holding> readByUserId(Long userId) {
        return holdingRepository.findLatestAccountAllHolding(userId);
    }

    public List<Holding> readByAccountId(Long accountId) {
        return holdingRepository.findByAccount_Id(accountId);
    }

    public boolean existsByUserIdAndStockCode(Long userId, String stockCode) {
        return holdingRepository.existsByLatestAccountHolding(userId, stockCode);
    }

    public void deleteByUserIdAndStockCode(Long userId, String stockCode) {
        holdingRepository.deleteByLatestAccountHolding(userId, stockCode);
    }
}
