package muzusi.application.account.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.account.service.AccountProfitUpdateExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountScheduler {
    private final AccountProfitUpdateExecutor accountProfitUpdateExecutor;

    @Scheduled(cron = "0 0 0 * * ?")
    public void runAccountProfitUpdateJob() {
        accountProfitUpdateExecutor.updateAccountProfitRate();
    }
}
