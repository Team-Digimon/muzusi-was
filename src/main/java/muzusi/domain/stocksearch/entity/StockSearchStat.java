package muzusi.domain.stocksearch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "stock_search_stat")
public class StockSearchStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_code", unique = true, nullable = false)
    private String stockCode;

    @Column(name = "search_count", nullable = false)
    private int searchCount;

    @Builder
    public StockSearchStat(String stockCode) {
        this.stockCode = stockCode;
    }
}
