package muzusi.domain.account.entity;

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
import muzusi.domain.trade.type.TradeType;
import muzusi.domain.user.entity.User;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "account")
@DynamicUpdate
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public static final Long INITIAL_BALANCE = 10_000_000L;

    private Long balance;

    @Column(name = "reserved_price")
    private Long reservedPrice = 0L;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Account(Long balance, User user) {
        this.balance = balance;
        this.user = user;
    }

    /**
     * 주식 거래로 인한 계좌 업데이트
     *
     * @param tradeType : 거래 타입
     * @param price : 거래 금액
     */
    public void updateAccount(TradeType tradeType, Long price) {
        switch (tradeType) {
            case BUY -> balance -= price;
            case SELL -> balance += price;
        }
    }

    /**
     * 예약 매수 금액 추가
     *
     * @param price : 예약 매수 금액
     */
    public void addReservedBuy(Long price) {
        this.balance -= price;
        this.reservedPrice += price;
    }

    /**
     * 예약 매수 금액 취소
     *
     * @param price : 취소할 예약 매수 금액
     */
    public void cancelReservedBuy(Long price) {
        this.balance += price;
        this.reservedPrice -= price;
    }

    /**
     * 예약 매수 성공으로 인한 예약 가격 삭제
     *
     * @param price : 금액
     */
    public void confirmReservedBuy(Long price) {
        this.reservedPrice -= price;
    }
}