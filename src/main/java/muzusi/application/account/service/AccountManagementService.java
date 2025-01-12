package muzusi.application.account.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.user.entity.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountManagementService {
    private final AccountService accountService;

    private static final Long DEFAULT_BALANCE = 10_000_000L;

    /**
     * 새로운 계좌 연결하는 메서드.
     *
     * @param user : 사용자 정보
     */
    public void createAndLinkAccount(User user) {
        Account account = Account.builder()
                .user(user)
                .balance(DEFAULT_BALANCE)
                .build();

        accountService.save(account);
    }
}
