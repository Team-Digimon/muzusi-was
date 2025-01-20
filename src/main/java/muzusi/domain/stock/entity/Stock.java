package muzusi.domain.stock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muzusi.domain.stock.type.MarketType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "stock")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_type", nullable = false)
    private MarketType marketType;

    @Column(name = "industry", nullable = false)
    private String industry;

    @Builder
    public Stock(String stockName, String stockCode, MarketType marketType, String industry) {
        this.stockName = stockName;
        this.stockCode = stockCode;
        this.marketType = marketType;
        this.industry = industry;
    }
}
