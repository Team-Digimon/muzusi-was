package muzusi.presentation.trade.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import muzusi.application.trade.dto.TradeReqDto;
import muzusi.application.trade.service.StockTradeService;
import muzusi.global.response.success.SuccessResponse;
import muzusi.global.security.auth.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class TradeController {
    private final StockTradeService stockTradeService;

    @PostMapping
    public ResponseEntity<?> tradeStock(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @Valid @RequestBody TradeReqDto tradeReqDto) {
        stockTradeService.tradeStock(userDetails.getUserId(), tradeReqDto);

        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
