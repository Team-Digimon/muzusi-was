package muzusi.domain.account.repository;

import lombok.RequiredArgsConstructor;
import muzusi.domain.account.entity.AccountProfit;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CustomAccountProfitRepositoryImpl implements CustomAccountProfitRepository {
    private final JdbcTemplate jdbcTemplate;

    private final static int BATCH_SIZE = 100;

    @Override
    public void saveAllInBatch(List<AccountProfit> accountProfits) {
        String sql = "INSERT INTO account_profit (total_balance, account_id, created_at) " +
                "VALUES (?, ?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                accountProfits,
                BATCH_SIZE,
                (PreparedStatement ps, AccountProfit accountProfit) -> {
                    ps.setLong(1, accountProfit.getTotalBalance());
                    ps.setLong(2, accountProfit.getAccount().getId());
                    ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                });
    }
}
