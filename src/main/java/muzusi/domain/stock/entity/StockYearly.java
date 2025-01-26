package muzusi.domain.stock.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "stock_yearly")
public class StockYearly {
    @Id
    private String id;

    private String stockCode;

    private String date;

    private Double open;

    private Double high;

    private Double low;

    private Double close;

    private Long volume;

    @Builder
    public StockYearly(String stockCode, String date, Double open, Double high, Double low, Double close, Long volume) {
        this.stockCode = stockCode;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
}
