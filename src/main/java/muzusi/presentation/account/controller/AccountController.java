package muzusi.presentation.account.controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.account.service.UserAccountService;
import muzusi.global.response.success.SuccessResponse;
import muzusi.global.security.auth.CustomUserDetails;
import muzusi.presentation.account.api.AccountApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController implements AccountApi {
    private final UserAccountService userAccountService;

    @Override
    @PostMapping
    public ResponseEntity<?> createNewAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userAccountService.connectNewAccount(userDetails.getUserId());

        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @GetMapping
    public ResponseEntity<?> getAllAccounts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                SuccessResponse.from(userAccountService.getAllAccounts(userDetails.getUserId()))
        );
    }

    @GetMapping("/current")
    public ResponseEntity<?> getAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                SuccessResponse.from(userAccountService.getAccount(userDetails.getUserId()))
        );
    }
}
