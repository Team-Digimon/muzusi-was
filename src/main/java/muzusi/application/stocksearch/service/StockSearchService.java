package muzusi.application.stocksearch.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.stocksearch.dto.StockSearchCandidate;
import muzusi.application.stocksearch.dto.StockSearchResponse;
import muzusi.application.stocksearch.port.StockSearchPort;
import muzusi.application.stocksearch.util.StockSearchRankingScoreCalculator;
import muzusi.domain.stocksearch.entity.StockSearchStat;
import muzusi.domain.stocksearch.service.StockSearchStatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockSearchService {
    private final StockSearchPort stockSearchPort;
    private final StockSearchStatService stockSearchStatService;
    
    private static final int SEARCH_RESULT_LIMIT = 10;
    
    /**
     * 검색어 자동완성을 위한 주식 검색 메서드
     *
     * @param keyword   사용자 입력한 값
     * @return          자동완성 리스트
     */
    public List<StockSearchResponse> searchStocks(String keyword) {
        List<StockSearchCandidate> candidates = stockSearchPort.search(keyword, SEARCH_RESULT_LIMIT);
        
        Map<String, Integer> stockSearchStatMap = getStockSearchCountMap(candidates);
        
        return candidates.stream()
                .map(candidate -> {
                    int searchCount = stockSearchStatMap.getOrDefault(candidate.stockCode(), 0);
                    
                    double finalScore = StockSearchRankingScoreCalculator.calculateFinalScore(candidate.score(), searchCount);
                    return candidate.withScore(finalScore);
                })
                .sorted(Comparator.comparingDouble(StockSearchCandidate::score).reversed())
                .map(StockSearchResponse::from)
                .toList();
    }
    
    /**
     * 주어진 검색 결과 후보에 해당하는 검색 통계 정보를 반환하는 메서드
     *
     * @param candidates    검색 결과 후보
     * @return              각 종목 별 검색 통계 Map
     */
    private Map<String, Integer> getStockSearchCountMap(List<StockSearchCandidate> candidates) {
        List<String> stockCodes = candidates.stream().map(StockSearchCandidate::stockCode).toList();
        List<StockSearchStat> stockSearchStats = stockSearchStatService.readByStockCodeIn(stockCodes);
        
        return stockSearchStats.stream().collect(
                Collectors.toMap(StockSearchStat::getStockCode, StockSearchStat::getSearchCount)
        );
    }
    
    /**
     * 주식 통계에서 검색 빈도({@code searchCount})를 증가시키는 메서드
     *
     * @param stockCode 주식 종목 코드
     */
    @Transactional
    public void increaseSearchCount(String stockCode) {
        stockSearchStatService.increaseSearchCount(stockCode);
    }
}
