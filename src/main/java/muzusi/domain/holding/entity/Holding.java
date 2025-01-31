package muzusi.domain.holding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "holding")
@DynamicUpdate
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "stock_count", nullable = false)
    private Integer stockCount;

    @Column(name = "reserved_stock_count")
    private Integer reservedStockCount = 0;

    @Column(name = "average_price", nullable = false)
    private Long averagePrice;

    @Column(name = "holding_at")
    @CreatedDate
    private LocalDateTime holdingAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Builder
    public Holding(String stockName, String stockCode, Integer stockCount, Long averagePrice, User user, Account account) {
        this.stockName = stockName;
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
     * 예약 매도를 추가하는 메서드
     */
    public boolean increaseReservedStock(int count) {
        if (count > this.stockCount - this.reservedStockCount) {
            return false;
        }
        this.reservedStockCount += count;
        return true;
    }

    /**
     * 예약 매도를 취소하는 메서드
     */
    public void decreaseReservedStock(int count) {
        this.reservedStockCount -= count;
    }

    /**
     * 예약 매도가 확정되었을 때 실제 보유 주식에서 차감
     */
    public void clearReservedStock(int count) {
        sellStock(count);
        this.reservedStockCount -= count;
    }

    /**
     * 보유 수량이 0이면 삭제할 수 있도록 체크하는 메서드
     */
    public boolean isEmpty() {
        return this.stockCount == 0;
    }

    /**
     * 매도 거래 가능한 주식 수 확인
     */
    public int getSellableStockCount() {
        return this.stockCount - this.reservedStockCount;
    }
}
