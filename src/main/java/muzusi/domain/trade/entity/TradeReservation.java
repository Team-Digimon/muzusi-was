package muzusi.domain.trade.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muzusi.domain.trade.type.TradeType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "trade_reservation")
public class TradeReservation {
    @Id
    private String id;

    private Long userId;

    private Long inputPrice;

    private Integer stockCount;

    private String stockName;

    private String stockCode;

    private TradeType tradeType;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public TradeReservation(Long userId, Long inputPrice, Integer stockCount, String stockName, String stockCode, TradeType tradeType) {
        this.userId = userId;
        this.inputPrice = inputPrice;
        this.stockCount = stockCount;
        this.stockName = stockName;
        this.stockCode = stockCode;
        this.tradeType = tradeType;
    }
}
