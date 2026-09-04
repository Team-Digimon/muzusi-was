package muzusi.presentation.stockchart.controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.stockchart.service.StockChartQueryService;
import muzusi.domain.stock.type.StockPeriodType;
import muzusi.global.response.success.SuccessResponse;
import muzusi.presentation.stockchart.api.StockChartApi;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockChartController implements StockChartApi {
    private final StockChartQueryService stockChartQueryService;

    @Override
    @GetMapping("/{stockCode}")
    public ResponseEntity<?> getStockHistory(
            @PathVariable String stockCode,
            @RequestParam StockPeriodType period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(SuccessResponse.from(stockChartQueryService.getStockChartByType(stockCode, from, to, period)));
    }
}
