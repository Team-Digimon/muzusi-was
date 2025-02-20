package muzusi.domain.account.repository;

import muzusi.domain.account.entity.AccountProfit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountProfitRepository extends JpaRepository<AccountProfit, Long> {
    List<AccountProfit> findByAccount_idOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}
