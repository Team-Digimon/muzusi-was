package muzusi.application.account.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.account.dto.AccountDetailsDto;
import muzusi.application.account.dto.AccountInfoDto;
import muzusi.application.account.dto.AccountProfitInfoDto;
import muzusi.application.account.dto.AccountSummaryDto;
import muzusi.application.holding.service.UserHoldingService;
import muzusi.domain.account.entity.Account;
import muzusi.domain.account.exception.AccountErrorType;
import muzusi.domain.account.service.AccountProfitService;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.user.entity.User;
import muzusi.domain.user.exception.UserErrorType;
import muzusi.domain.user.service.UserService;
import muzusi.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final UserService userService;
    private final AccountService accountService;
    private final AccountProfitService accountProfitService;
    private final AccountManagementService accountManagementService;
    private final UserHoldingService userHoldingService;

    /**
     * 새로운 계좌를 등록하기 위한 메서드.
     * 계좌 등록은 오전 9시 이전에만 가능하며, 하루에 한 개의 계좌만 등록할 수 있습니다.
     *
     * @param userId : 사용자의 pk값
     */
    @Transactional
    public void connectNewAccount(Long userId) {
        if (LocalDateTime.now().toLocalTime().isAfter(LocalTime.of(9, 0))) {
            throw new CustomException(AccountErrorType.ACCOUNT_CREATION_TIME_LIMIT);
        }

        LocalDateTime latestCreatedAt = accountService.readCreatedAt(userId);

        if (latestCreatedAt != null && latestCreatedAt.toLocalDate().equals(LocalDate.now())) {
            throw new CustomException(AccountErrorType.ACCOUNT_CREATION_LIMIT);
        }

        User foundUser = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        Account preAccount = accountService.readByUserId(userId)
                .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND));

        calculateAndUpdateTotalEvaluatedAmount(preAccount);
        foundUser.incrementAttemptCount();
        accountManagementService.createAndLinkAccount(foundUser);
    }

    /**
     * 이전 계좌 보유 주식 수익률 업데이트
     *
     * @param account : 사용자의 이전 계좌
     */
    private void calculateAndUpdateTotalEvaluatedAmount(Account account) {
        Long totalEvaluatedAmount = userHoldingService.calculateTotalRateOfReturn(account.getId()).totalEvaluatedAmount();
        account.updateTotalEvaluatedAmount(totalEvaluatedAmount);
    }

    /**
     * 사용자의 계좌들을 불러오는 메서드
     *
     * @param userId : 사용자의 pk값
     * @return : 사용자의 계좌 정보 list
     */
    @Transactional(readOnly = true)
    public List<AccountInfoDto> getAllAccounts(Long userId) {
        return accountService.readAllByUserId(userId)
                .stream().map(AccountInfoDto::fromEntity)
                .toList();
    }

    /**
     * 사용자의 현재 계좌 (최신계좌) 불러오는 메서드
     *
     * @param userId : 사용자의 pk값
     * @return : 사용자의 현재 계좌 정보
     */
    @Transactional(readOnly = true)
    public AccountDetailsDto getAccount(Long userId) {
        Account account = accountService.readByUserId(userId)
                .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND));

        List<AccountProfitInfoDto> accountProfits =
                accountProfitService.readByAccountId(account.getId())
                        .stream()
                        .map(AccountProfitInfoDto::fromEntity)
                        .toList();

        AccountSummaryDto summaryDto = userHoldingService.calculateTotalRateOfReturn(account.getId());

        return AccountDetailsDto.from(account, summaryDto, accountProfits);
    }
}
