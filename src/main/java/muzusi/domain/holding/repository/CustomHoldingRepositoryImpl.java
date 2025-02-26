package muzusi.domain.holding.repository;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import muzusi.domain.account.entity.QAccount;
import muzusi.domain.holding.entity.Holding;
import muzusi.domain.holding.entity.QHolding;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomHoldingRepositoryImpl implements CustomHoldingRepository {
    private final JPAQueryFactory jpaQueryFactory;

    private final QHolding holding = QHolding.holding;
    private final QAccount account = QAccount.account;

    @Override
    public Optional<Holding> findLatestAccountHolding(Long userId, String stockCode) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(holding)
                        .innerJoin(account).on(holding.account.id.eq(account.id))
                        .where(
                                holding.account.id.eq(
                                                JPAExpressions
                                                        .select(account.id.max())
                                                        .from(account)
                                                        .where(account.user.id.eq(userId))
                                        )
                                        .and(holding.stockCode.eq(stockCode))
                        )
                        .where(holding.stockCode.eq(stockCode))
                        .fetchOne()
        );
    }

    public boolean existsByLatestAccountHolding(Long userId, String stockCode) {
        return jpaQueryFactory
                .selectOne()
                .from(holding)
                .innerJoin(account).on(holding.account.id.eq(account.id))
                .where(
                        holding.account.id.eq(
                                        JPAExpressions
                                                .select(account.id.max())
                                                .from(account)
                                                .where(account.user.id.eq(userId))
                                )
                                .and(holding.stockCode.eq(stockCode))
                )
                .fetchFirst() != null;
    }

    @Override
    public void deleteByLatestAccountHolding(Long userId, String stockCode) {
        jpaQueryFactory
                .delete(holding)
                .where(
                        holding.account.id.in(
                                JPAExpressions
                                        .select(account.id.max())
                                        .from(account)
                                        .where(account.user.id.eq(userId))
                        ),
                        holding.stockCode.eq(stockCode)
                )
                .execute();
    }

    @Override
    public List<Holding> findLatestAccountAllHolding(Long userId) {
        return jpaQueryFactory
                .selectFrom(holding)
                .innerJoin(account).on(holding.account.id.eq(account.id))
                .where(holding.account.id.eq(
                        JPAExpressions
                                .select(account.id.max())
                                .from(account)
                                .where(account.user.id.eq(userId))
                ))
                .fetch();
    }
}
