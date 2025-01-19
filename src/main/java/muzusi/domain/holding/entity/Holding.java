package muzusi.domain.holding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import muzusi.domain.user.entity.User;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "holding")
@DynamicUpdate
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "stock_count", nullable = false)
    private Integer stockCount;

    @Column(name = "average_price", nullable = false)
    private Long averagePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Builder
    public Holding(String stockCode, Integer stockCount, Long averagePrice, User user, Account account) {
        this.stockCode = stockCode;
        this.stockCount = stockCount;
        this.averagePrice = averagePrice;
        this.user = user;
        this.account = account;
    }

    /**
     * 주식을 추가 매수할 때 평균 단가를 재계산하는 메서드
     */
    public void addStock(int count, long price) {
        long totalCost = (this.stockCount * this.averagePrice) + (count * price);
        this.stockCount += count;
        this.averagePrice = totalCost / this.stockCount;
    }

    /**
     * 주식을 매도할 때 수량을 차감하는 메서드
     */
    public void sellStock(int count) {
        this.stockCount -= count;
    }

    /**
     * 보유 수량이 0이면 삭제할 수 있도록 체크하는 메서드
     */
    public boolean isEmpty() {
        return this.stockCount == 0;
    }
}
