package muzusi.domain.trade.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muzusi.domain.trade.type.TradeType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "trade_reservation")
public class TradeReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "input_price")
    private Long inputPrice;

    @Column(name = "input_count")
    private Integer stockCount;

    @Column(name = "stock_name")
    private String stockName;

    @Column(name = "stock_code")
    private String stockCode;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType;

    @Column(name = "created_at")
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
