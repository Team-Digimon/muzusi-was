package muzusi.domain.stockcandle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockMinuteCandleId implements Serializable {
    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Builder
    public StockMinuteCandleId(String stockCode, LocalDateTime dateTime) {
        this.stockCode = stockCode;
        this.dateTime = dateTime;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StockMinuteCandleId that)) return false;
        return Objects.equals(stockCode, that.stockCode) && Objects.equals(dateTime, that.dateTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(stockCode, dateTime);
    }
}
