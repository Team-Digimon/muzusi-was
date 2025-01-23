package muzusi.presentation.trade.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import muzusi.application.trade.dto.TradeReqDto;
import muzusi.application.trade.service.StockTradeService;
import muzusi.global.response.success.SuccessResponse;
import muzusi.global.security.auth.CustomUserDetails;
import muzusi.presentation.trade.api.TradeApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class TradeController implements TradeApi {
    private final StockTradeService stockTradeService;

    @Override
    @PostMapping
    public ResponseEntity<?> tradeStock(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @Valid @RequestBody TradeReqDto tradeReqDto) {
        stockTradeService.tradeStock(userDetails.getUserId(), tradeReqDto);

        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @Override
    @DeleteMapping("/{tradeReservationId}")
    public ResponseEntity<?> cancelTradeReservation(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @PathVariable String tradeReservationId) {
        stockTradeService.cancelTradeReservation(userDetails.getUserId(), tradeReservationId);

        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
