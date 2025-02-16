package muzusi.presentation.holding.controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.holding.service.UserHoldingService;
import muzusi.global.response.success.SuccessResponse;
import muzusi.global.security.auth.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class HoldingController {
    private final UserHoldingService userHoldingService;

    @GetMapping("/holdings")
    public ResponseEntity<?> getHoldings(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                SuccessResponse.from(userHoldingService.getUserHoldings(userDetails.getUserId()))
        );
    }
}
