package muzusi.domain.account.repository;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.entity.QAccount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomAccountRepositoryImpl implements CustomAccountRepository {
    private final JPAQueryFactory jpaQueryFactory;

    private final QAccount account = QAccount.account;

    @Override
    public Optional<Account> findLatestAccount(Long userId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(account)
                        .where(account.user.id.eq(userId))
                        .orderBy(account.createdAt.desc())
                        .limit(1)
                        .fetchOne()
        );
    }

    @Override
    public LocalDateTime findLatestCreatedAt(Long userId) {
        return jpaQueryFactory
                .select(account.createdAt)
                .from(account)
                .where(account.user.id.eq(userId))
                .orderBy(account.createdAt.desc())
                .limit(1)
                .fetchOne();
    }

    @Override
    public List<Account> findLatestAccountsForAllUsers() {
        QAccount subAccount = new QAccount("subAccount");

        return jpaQueryFactory
                .selectFrom(account)
                .where(account.createdAt.eq(
                        JPAExpressions
                                .select(subAccount.createdAt.max())
                                .from(subAccount)
                                .where(subAccount.user.id.eq(account.user.id))
                ))
                .fetch();
    }

    @Override
    public List<Account> findAllExceptLatestByUserId(Long userId) {
        return jpaQueryFactory
                .selectFrom(account)
                .where(account.user.id.eq(userId)
                        .and(account.createdAt.lt(
                                jpaQueryFactory
                                        .select(account.createdAt.max())
                                        .from(account)
                                        .where(account.user.id.eq(userId))
                        )))
                .orderBy(account.createdAt.asc())
                .fetch();
    }
}
