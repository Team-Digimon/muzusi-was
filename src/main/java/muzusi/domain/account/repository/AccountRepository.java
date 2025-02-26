package muzusi.domain.account.repository;

import muzusi.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long>, CustomAccountRepository {
    List<Account> findByUser_Id(Long userId);
}
