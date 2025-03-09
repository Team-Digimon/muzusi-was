package muzusi.domain.account.repository;

import muzusi.domain.account.entity.Account;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomAccountRepository {
    Optional<Account> findLatestAccount(Long userId);
    LocalDateTime findLatestCreatedAt(Long userId);
    List<Account> findLatestAccountsForAllUsers();
    List<Account> findAllExceptLatestByUserId(Long userId);
}
