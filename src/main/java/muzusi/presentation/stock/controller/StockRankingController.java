package muzusi.presentation.stock.controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.stock.service.StockRankingService;
import muzusi.domain.stock.type.StockRankingType;
import muzusi.global.response.success.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks/rank")
@RequiredArgsConstructor
public class StockRankingController {
    private final StockRankingService stockRankingService;

    @GetMapping
    public ResponseEntity<?> getStockRanking(@RequestParam StockRankingType type) {
        return ResponseEntity.ok()
                .body(SuccessResponse.from(stockRankingService.getStockRanking(type)));
    }
}