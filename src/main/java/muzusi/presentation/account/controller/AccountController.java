package muzusi.presentation.account.controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.account.service.UserAccountService;
import muzusi.global.response.success.SuccessResponse;
import muzusi.global.security.auth.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final UserAccountService userAccountService;

    @PostMapping
    public ResponseEntity<?> createNewAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userAccountService.connectNewAccount(userDetails.getUserId());

        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
