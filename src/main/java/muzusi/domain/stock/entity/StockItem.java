package muzusi.domain.stock.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "stock_item")
public class StockItem {
    @Id
    private String id;

    private String stockName;

    private String stockCode;

    private int searchCount;

    @Builder
    public StockItem(String stockName, String stockCode) {
        this.stockName = stockName;
        this.stockCode = stockCode;
    }
}