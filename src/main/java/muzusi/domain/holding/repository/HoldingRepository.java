package muzusi.domain.holding.repository;

import muzusi.domain.holding.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    @Query(value = "SELECT * FROM holding " +
            "WHERE account_id = (SELECT id FROM account WHERE user_id = :userId ORDER BY created_at DESC LIMIT 1) " +
            "AND stock_code = :stockCode", nativeQuery = true)
    Optional<Holding> findLatestAccountHolding(@Param("userId") Long userId, @Param("stockCode") String stockCode);

    @Query(value = "SELECT EXISTS ( " +
            "SELECT 1 FROM holding " +
            "WHERE account_id = (SELECT id FROM account WHERE user_id = :userId ORDER BY created_at DESC LIMIT 1) " +
            "AND stock_code = :stockCode )", nativeQuery = true)
    Integer existsByLatestAccountHolding(@Param("userId") Long userId, @Param("stockCode") String stockCode);

    @Modifying
    @Query(value = "DELETE FROM holding " +
            "WHERE account_id = (SELECT id FROM account WHERE user_id = :userId ORDER BY created_at DESC LIMIT 1 )" +
            "AND stock_code = :stockCode", nativeQuery = true)
    void deleteByLatestAccountHolding(@Param("userId") Long userId, @Param("stockCode") String stockCode);

    @Query(value = "SELECT * FROM holding " +
            "WHERE account_id = (SELECT id FROM account WHERE user_id = :userId ORDER BY created_at DESC LIMIT 1)",
            nativeQuery = true)
    List<Holding> findLatestAccountAllHolding(@Param("userId") Long userId);

    List<Holding> findByAccount_Id(Long accountId);
}
