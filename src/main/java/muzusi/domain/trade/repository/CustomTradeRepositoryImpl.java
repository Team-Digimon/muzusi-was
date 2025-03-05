package muzusi.domain.trade.repository;

import lombok.RequiredArgsConstructor;
import muzusi.domain.trade.entity.Trade;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CustomTradeRepositoryImpl implements CustomTradeRepository {
    private final JdbcTemplate jdbcTemplate;

    private final static int BATCH_SIZE = 100;

    @Override
    public void saveAllInBatch(List<Trade> trades) {
        String sql = "INSERT INTO trade (stock_price, stock_count, stock_name, stock_code, trade_type, account_id, trade_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                trades,
                BATCH_SIZE,
                (PreparedStatement ps, Trade trade) -> {
                    ps.setLong(1, trade.getStockPrice());
                    ps.setInt(2, trade.getStockCount());
                    ps.setString(3, trade.getStockName());
                    ps.setString(4, trade.getStockCode());
                    ps.setString(5, String.valueOf(trade.getTradeType()));
                    ps.setLong(6, trade.getAccount().getId());
                    ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                });
    }
}
