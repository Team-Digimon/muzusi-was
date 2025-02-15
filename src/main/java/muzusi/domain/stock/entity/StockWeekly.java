package muzusi.domain.stock.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "stock_weekly")
public class StockWeekly {
    @Id
    private String id;

    private String stockCode;

    private LocalDateTime date;

    private Long open;

    private Long high;

    private Long low;

    private Long close;

    private Long volume;

    @Builder
    public StockWeekly(String stockCode, LocalDateTime date, Long open, Long high, Long low, Long close, Long volume) {
        this.stockCode = stockCode;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
}
