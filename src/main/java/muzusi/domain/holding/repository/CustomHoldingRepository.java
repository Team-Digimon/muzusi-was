package muzusi.domain.holding.repository;

import muzusi.domain.holding.entity.Holding;

import java.util.List;
import java.util.Optional;

public interface CustomHoldingRepository {
    Optional<Holding> findLatestAccountHolding(Long userId, String stockCode);
    boolean existsByLatestAccountHolding(Long userId, String stockCode);
    void deleteByLatestAccountHolding(Long userId, String stockCode);
    List<Holding> findLatestAccountAllHolding(Long userId);
}
