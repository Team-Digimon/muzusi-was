package muzusi.domain.trade.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muzusi.domain.account.entity.Account;
import muzusi.domain.trade.type.TradeType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "trade")
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_price", nullable = false)
    private Long stockPrice;

    @Column(name = "stock_count")
    private Integer stockCount;

    @Column(name = "stock_name")
    private String stockName;

    @Column(name = "stock_code")
    private String stockCode;

    @Column(name = "trade_at")
    @CreatedDate
    private LocalDateTime tradeAt;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Builder
    public Trade(Long stockPrice, Integer stockCount, String stockName, String stockCode, TradeType tradeType, Account account) {
        this.stockPrice = stockPrice;
        this.stockCount = stockCount;
        this.stockName = stockName;
        this.stockCode = stockCode;
        this.tradeType = tradeType;
        this.account = account;
    }
}