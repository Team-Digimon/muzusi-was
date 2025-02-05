package muzusi.presentation.trade.controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.trade.service.UserTradeReservationService;
import muzusi.global.response.success.SuccessResponse;
import muzusi.global.security.auth.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trades")
@RequiredArgsConstructor
public class TradeReservationController {
    private final UserTradeReservationService userTradeReservationService;

    @GetMapping("/reservations")
    public ResponseEntity<?> getAllReservations(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                SuccessResponse.from(userTradeReservationService.getReservationsByUserId(userDetails.getUserId()))
        );
    }
}
