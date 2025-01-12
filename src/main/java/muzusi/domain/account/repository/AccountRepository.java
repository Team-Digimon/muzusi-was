package muzusi.domain.account.repository;

import muzusi.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser_Id(Long userId);

    @Query(value = "SELECT * FROM account a WHERE a.user_id = :userId ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<Account> findLatestAccount(@Param("userId") Long userId);
}
