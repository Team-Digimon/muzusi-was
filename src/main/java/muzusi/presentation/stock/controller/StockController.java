package muzusi.presentation.stock.controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.service.StockPriceService;
import muzusi.application.stock.service.StockSearchService;
import muzusi.global.response.success.SuccessResponse;
import muzusi.presentation.stock.api.StockApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController implements StockApi {
    private final StockSearchService stockSearchService;
    private final StockPriceService stockPriceService;

    @Override
    @GetMapping
    public ResponseEntity<?> searchStock(@RequestParam String keyword) {
        return ResponseEntity.ok(
                SuccessResponse.from(stockSearchService.searchStocks(keyword))
        );
    }

    @GetMapping("/{code}/price")
    public ResponseEntity<?> getStockPrice(@PathVariable String code) {
        return ResponseEntity.ok(
                SuccessResponse.from(stockPriceService.getStockPrice(code))
        );
    }
}