package muzusi.infrastructure.stocksearch.adapter.lucene;

import lombok.RequiredArgsConstructor;
import muzusi.application.stocksearch.dto.StockSearchCandidate;
import muzusi.application.stocksearch.port.StockSearchPort;
import muzusi.domain.stock.entity.Stock;
import muzusi.infrastructure.stocksearch.exception.StockSearchIndexException;
import muzusi.infrastructure.stocksearch.index.lucene.LuceneStockSearchIndex;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LuceneStockSearchAdapter implements StockSearchPort {
    private final LuceneStockSearchIndex stockSearchIndex;
    
    /**
     * 주식 검색을 위한 색인을 생성하는 메서드
     *
     * @param stocks 주식 정보 목록
     */
    @Override
    public void init(List<Stock> stocks) {
        try {
            stockSearchIndex.build(stocks);
        } catch (Exception e) {
            throw new StockSearchIndexException("주식 검색 인덱스 생성에 실패하였습니다.", e);
        }
    }
    
    /**
     * 주식 검색 메서드
     *
     * @param keyword   검색어 키워드
     * @param limit     검색 결과 한도
     * @return          검색 결과 목록
     */
    @Override
    public List<StockSearchCandidate> search(String keyword, int limit) {
        return stockSearchIndex.search(keyword, limit);
    }
}
