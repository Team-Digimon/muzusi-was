package muzusi.application.account.scheduler;

import lombok.RequiredArgsConstructor;
import muzusi.application.account.service.AccountProfitManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountScheduler {
    private final AccountProfitManager accountProfitManager;

    @Scheduled(cron = "0 0 0 * * ?")
    public void runAccountProfitUpdateJob() {
        accountProfitManager.updateAccountProfitRate();
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void runAccountProfitDeleteJob() {
        accountProfitManager.deleteOldAccountProfits();
    }
}
