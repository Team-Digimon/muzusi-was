package muzusi.domain.stockcandle.repository;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stockcandle.entity.StockMinuteCandle;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

@RequiredArgsConstructor
public class CustomStockMinuteCandleRepositoryImpl implements CustomStockMinuteCandleRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 1000;

    @Override
    public void saveAllInBatch(List<StockMinuteCandle> stockMinuteCandles) {
        String sql = "INSERT INTO stock_minute_candle " +
                "(stock_code, date_time, `open`, high, low, `close`, volume) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                stockMinuteCandles,
                BATCH_SIZE,
                (PreparedStatement ps, StockMinuteCandle candle) -> {
                    ps.setString(1, candle.getStockCode());
                    ps.setTimestamp(2, Timestamp.valueOf(candle.getDateTime()));
                    ps.setLong(3, candle.getOpen());
                    ps.setLong(4, candle.getHigh());
                    ps.setLong(5, candle.getLow());
                    ps.setLong(6, candle.getClose());
                    ps.setLong(7, candle.getVolume());
                });
    }
}
