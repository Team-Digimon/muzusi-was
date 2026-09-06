package muzusi.domain.stockcandle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockDailyCandleId implements Serializable {
    @Column(name = "stock_code", nullable = false)
    private String stockCode;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Builder
    public StockDailyCandleId(String stockCode, LocalDate date) {
        this.stockCode = stockCode;
        this.date = date;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StockDailyCandleId that)) return false;
        return Objects.equals(stockCode, that.stockCode) && Objects.equals(date, that.date);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(stockCode, date);
    }
}
