package muzusi.domain.stockcandle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "stock_daily_candle")
public class StockDailyCandle {
    @EmbeddedId
    private StockDailyCandleId id;
    
    @Column(nullable = false)
    private Long open;
    
    @Column(nullable = false)
    private Long high;
    
    @Column(nullable = false)
    private Long low;
    
    @Column(nullable = false)
    private Long close;
    
    @Column(nullable = false)
    private Long volume;
    
    @Builder
    public StockDailyCandle(String stockCode, LocalDate date, Long open, Long high, Long low, Long close, Long volume) {
        this.id = StockDailyCandleId.builder()
                .stockCode(stockCode)
                .date(date)
                .build();
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
}
