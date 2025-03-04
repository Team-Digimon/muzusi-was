package muzusi.domain.account.repository;

import muzusi.domain.account.entity.AccountProfit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AccountProfitRepository extends JpaRepository<AccountProfit, Long>, CustomAccountProfitRepository {
    List<AccountProfit> findByAccount_idOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM account_profit a WHERE a.createdAt < :dateTime")
    void deleteByDateTimeBefore(@Param("dateTime") LocalDate dateTime);
}
