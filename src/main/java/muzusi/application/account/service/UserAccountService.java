package muzusi.application.account.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.account.dto.AccountInfoDto;
import muzusi.domain.account.exception.AccountErrorType;
import muzusi.domain.account.service.AccountService;
import muzusi.domain.user.entity.User;
import muzusi.domain.user.exception.UserErrorType;
import muzusi.domain.user.service.UserService;
import muzusi.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final UserService userService;
    private final AccountService accountService;
    private final AccountManagementService accountManagementService;

    /**
     * 새로운 계좌를 등록하기 위한 메서드.
     *
     * @param userId : 사용자의 pk값
     */
    @Transactional
    public void connectNewAccount(Long userId) {
        LocalDateTime latestCreatedAt = accountService.readCreatedAt(userId);

        if (latestCreatedAt != null && latestCreatedAt.toLocalDate().equals(LocalDate.now())) {
            throw new CustomException(AccountErrorType.ACCOUNT_CREATION_LIMIT);
        }
        User foundUser = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        foundUser.incrementAttemptCount();
        accountManagementService.createAndLinkAccount(foundUser);
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
    public AccountInfoDto getAccount(Long userId) {
        return AccountInfoDto.fromEntity(
                accountService.readByUserId(userId)
                        .orElseThrow(() -> new CustomException(AccountErrorType.NOT_FOUND))
        );
    }
}
