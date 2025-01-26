package muzusi.presentation.stock.controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.service.StockHistoryService;
import muzusi.domain.stock.type.StockPeriodType;
import muzusi.global.response.success.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockHistoryController {
    private final StockHistoryService stockHistoryService;

    @GetMapping("/{stockCode}")
    public ResponseEntity<?> read(@PathVariable String stockCode,
                                  @RequestParam StockPeriodType period) {
        return ResponseEntity.ok(
                SuccessResponse.from(stockHistoryService.getStockHistoryByType(stockCode, period))
        );
    }

}
