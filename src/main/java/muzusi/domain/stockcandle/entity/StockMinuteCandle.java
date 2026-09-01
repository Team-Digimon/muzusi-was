package muzusi.domain.stockcandle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "stock_minute_candle")
public class StockMinuteCandle implements Persistable<StockMinuteCandleId> {
    @EmbeddedId
    private StockMinuteCandleId id;

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
    
    @Transient
    private boolean isNew = true;

    @Builder
    public StockMinuteCandle(String stockCode, LocalDateTime dateTime,
                             Long open, Long high, Long low, Long close, Long volume) {
        this.id = StockMinuteCandleId.builder()
                .stockCode(stockCode)
                .dateTime(dateTime)
                .build();
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public String getStockCode() {
        return id.getStockCode();
    }

    public LocalDateTime getDateTime() {
        return id.getDateTime();
    }
    
    @Override
    public StockMinuteCandleId getId() {
        return id;
    }
    
    @Override
    public boolean isNew() {
        return isNew;
    }
    
    @PrePersist
    @PostLoad
    public void markNotNew() {
        this.isNew = false;
    }
}
