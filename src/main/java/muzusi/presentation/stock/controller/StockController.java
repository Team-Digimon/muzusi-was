package muzusi.presentation.stock.controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.service.StockQueryService;
import muzusi.global.response.success.SuccessResponse;
import muzusi.presentation.stock.api.StockApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController implements StockApi {
    private final StockQueryService stockQueryService;

    @Override
    @GetMapping("/{stockCode}")
    public ResponseEntity<?> getStock(@PathVariable(name = "stockCode") String stockCode) {
        return ResponseEntity.ok(SuccessResponse.from(stockQueryService.getStock(stockCode)));
    }
}
