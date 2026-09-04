package muzusi.domain.stockcandle.repository;

import lombok.RequiredArgsConstructor;
import muzusi.domain.stockcandle.entity.StockDailyCandle;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;

@RequiredArgsConstructor
public class CustomStockDailyCandleRepositoryImpl implements CustomStockDailyCandleRepository {
    private final JdbcTemplate jdbcTemplate;
    
    private static final int BATCH_SIZE = 500;
    private static final String BATCH_INSERT_SQL = """
            INSERT INTO stock_daily_candle(stock_code, `date`, `open`, high, low, `close`, volume)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    @Override
    public void saveAllInBatch(List<StockDailyCandle> stockDailyCandles) {
        jdbcTemplate.batchUpdate(
                BATCH_INSERT_SQL,
                stockDailyCandles,
                BATCH_SIZE,
                (PreparedStatement ps, StockDailyCandle candle) -> {
                    ps.setString(1, candle.getId().getStockCode());
                    ps.setDate(2, Date.valueOf(candle.getId().getDate()));
                    ps.setLong(3, candle.getOpen());
                    ps.setLong(4, candle.getHigh());
                    ps.setLong(5, candle.getLow());
                    ps.setLong(6, candle.getClose());
                    ps.setLong(7, candle.getVolume());
                }
        );
    }
}
