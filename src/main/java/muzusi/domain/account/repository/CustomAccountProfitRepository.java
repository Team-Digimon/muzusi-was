package muzusi.domain.account.repository;

import muzusi.domain.account.entity.AccountProfit;

import java.util.List;

public interface CustomAccountProfitRepository {
    void saveAllInBatch(List<AccountProfit> accountProfits);
}
