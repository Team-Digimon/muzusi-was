package muzusi.domain.account.repository;

import muzusi.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long>, CustomAccountRepository {
}
