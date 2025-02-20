package muzusi.domain.account.repository;

import muzusi.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser_Id(Long userId);

    @Query(value = "SELECT * FROM account a " +
            "WHERE a.user_id = :userId " +
            "ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<Account> findLatestAccount(@Param("userId") Long userId);

    @Query(value = "SELECT a.created_at FROM account a " +
            "WHERE a.user_id = :userId " +
            "ORDER BY a.created_at DESC LIMIT 1", nativeQuery = true)
    LocalDateTime findLatestCreatedAt(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM account a " +
            "WHERE a.id = ( " +
            "    SELECT a2.id FROM account a2 " +
            "    WHERE a2.user_id = a.user_id " +
            "    ORDER BY a2.created_at DESC " +
            "    LIMIT 1 " +
            ")", nativeQuery = true)
    List<Account> findLatestAccountsForAllUsers();
}
