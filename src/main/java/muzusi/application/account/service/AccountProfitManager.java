package muzusi.application.account.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.holding.service.UserHoldingService;
import muzusi.domain.account.entity.AccountProfit;
import muzusi.domain.account.service.AccountProfitService;
import muzusi.domain.account.service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountProfitManager {
    private final AccountProfitService accountProfitService;
    private final UserHoldingService userHoldingService;
    private final AccountService accountService;

    /**
     * 사용자 계좌의 일일 수익률을 저장하는 메서드
     * 1. 사용자들의 최신 계좌 불러오기
     * 2. 사용자 별로 계좌의 총 자산 계산
     */
    @Transactional
    public void updateAccountProfitRate() {
        List<AccountProfit> accountProfits = new ArrayList<>();

        accountService.readAll()
                .forEach(account -> {
                    long evaluatedAmount = userHoldingService
                            .calculateTotalRateOfReturn(account.getId())
                            .totalEvaluatedAmount();

                    accountProfits.add(AccountProfit.builder()
                            .totalBalance(account.getBalance() + evaluatedAmount)
                            .account(account)
                            .build()
                    );
                });

        accountProfitService.saveAll(accountProfits);
    }
}
