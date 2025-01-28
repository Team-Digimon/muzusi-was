package muzusi.presentation.stock.controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.service.StockSearchService;
import muzusi.global.response.success.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {
    private final StockSearchService stockSearchService;

    @GetMapping
    public ResponseEntity<?> searchStock(@RequestParam String keyword) {
        return ResponseEntity.ok(
                SuccessResponse.from(stockSearchService.searchStocks(keyword))
        );
    }
}
