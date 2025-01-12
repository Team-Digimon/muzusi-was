package muzusi.application.account.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.user.entity.User;
import muzusi.domain.user.exception.UserErrorType;
import muzusi.domain.user.service.UserService;
import muzusi.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final UserService userService;
    private final AccountManagementService accountManagementService;

    /**
     * 새로운 계좌를 등록하기 위한 메서드.
     *
     * @param userId : 사용자의 pk값
     */
    @Transactional
    public void connectNewAccount(Long userId) {
        User foundUser = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        foundUser.incrementAttemptCount();
        accountManagementService.createAndLinkAccount(foundUser);
    }
}
