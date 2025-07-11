package muzusi.presentation.stock.controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.service.StockChartQueryService;
import muzusi.domain.stock.type.StockPeriodType;
import muzusi.global.response.success.SuccessResponse;
import muzusi.presentation.stock.api.StockHistoryApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockHistoryController implements StockHistoryApi {
    private final StockChartQueryService stockChartQueryService;

    @Override
    @GetMapping("/{stockCode}")
    public ResponseEntity<?> getStockHistory(@PathVariable String stockCode,
                                             @RequestParam StockPeriodType period) {
        return ResponseEntity.ok(
                SuccessResponse.from(stockChartQueryService.getStockHistoryByType(stockCode, period))
        );
    }
}
