package muzusi.domain.holding.repository;

import muzusi.domain.holding.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    Optional<Holding> findByUser_IdAndStockCode(Long userId, String stockCode);
    boolean existsByStockCode(String stockCode);
    void deleteByStockCode(String stockCode);
}
