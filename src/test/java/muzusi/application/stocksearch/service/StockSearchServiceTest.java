package muzusi.application.stocksearch.service;

import muzusi.application.stocksearch.dto.StockSearchCandidate;
import muzusi.application.stocksearch.dto.StockSearchResponse;
import muzusi.application.stocksearch.port.StockSearchPort;
import muzusi.domain.stock.service.StockItemService;
import muzusi.domain.stocksearch.entity.StockSearchStat;
import muzusi.domain.stocksearch.service.StockSearchStatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockSearchServiceTest {
    @Mock
    private StockItemService stockItemService;

    @Mock
    private StockSearchPort stockSearchPort;

    @Mock
    private StockSearchStatService stockSearchStatService;

    @InjectMocks
    private StockSearchService stockSearchService;

    private static final String KEYWORD = "삼성";

    @Test
    @DisplayName("매치 점수가 같아도 검색 빈도가 높은 종목이 앞에 오도록 정렬한다")
    void searchStocks_ranksHigherSearchCountFirstWhenMatchScoreIsEqual() {
        // given
        StockSearchCandidate lowCountCandidate = new StockSearchCandidate("005930", "삼성전자", 30);
        StockSearchCandidate highCountCandidate = new StockSearchCandidate("006400", "삼성SDI", 30);
        given(stockSearchPort.search(KEYWORD, 10)).willReturn(List.of(lowCountCandidate, highCountCandidate));

        StockSearchStat lowStat = createStat("005930", 0);
        StockSearchStat highStat = createStat("006400", 1000);
        given(stockSearchStatService.readByStockCodeIn(List.of("005930", "006400")))
                .willReturn(List.of(lowStat, highStat));

        // when
        List<StockSearchResponse> results = stockSearchService.searchStocks(KEYWORD);

        // then
        assertThat(results).extracting(StockSearchResponse::stockCode)
                .containsExactly("006400", "005930");
    }

    @Test
    @DisplayName("검색 통계가 없는 종목은 매치 점수를 그대로 사용해 정렬한다")
    void searchStocks_usesRawMatchScoreWhenStatIsMissing() {
        // given
        StockSearchCandidate exactMatch = new StockSearchCandidate("005930", "삼성전자", 100);
        StockSearchCandidate prefixMatch = new StockSearchCandidate("006400", "삼성SDI", 50);
        given(stockSearchPort.search(KEYWORD, 10)).willReturn(List.of(prefixMatch, exactMatch));
        given(stockSearchStatService.readByStockCodeIn(List.of("006400", "005930")))
                .willReturn(List.of());

        // when
        List<StockSearchResponse> results = stockSearchService.searchStocks(KEYWORD);

        // then
        assertThat(results).extracting(StockSearchResponse::stockCode)
                .containsExactly("005930", "006400");
    }

    @Test
    @DisplayName("검색 빈도가 아무리 높아도 매치 점수 차이를 넘어서지 못해 원래 순서가 유지된다")
    void searchStocks_searchCountCannotOvertakeMatchScoreGap() {
        // given
        StockSearchCandidate exactMatch = new StockSearchCandidate("005930", "삼성전자", 100);
        StockSearchCandidate prefixMatch = new StockSearchCandidate("006400", "삼성SDI", 50);
        given(stockSearchPort.search(KEYWORD, 10)).willReturn(List.of(prefixMatch, exactMatch));

        StockSearchStat noBoost = createStat("005930", 0);
        StockSearchStat maxBoost = createStat("006400", Integer.MAX_VALUE);
        given(stockSearchStatService.readByStockCodeIn(List.of("006400", "005930")))
                .willReturn(List.of(maxBoost, noBoost));

        // when
        List<StockSearchResponse> results = stockSearchService.searchStocks(KEYWORD);

        // then
        assertThat(results).extracting(StockSearchResponse::stockCode)
                .containsExactly("005930", "006400");
    }

    @Test
    @DisplayName("검색 결과를 종목코드와 종목명만 담은 응답으로 변환한다")
    void searchStocks_mapsCandidatesToResponse() {
        // given
        StockSearchCandidate candidate = new StockSearchCandidate("005930", "삼성전자", 100);
        given(stockSearchPort.search(KEYWORD, 10)).willReturn(List.of(candidate));
        given(stockSearchStatService.readByStockCodeIn(List.of("005930"))).willReturn(List.of());

        // when
        List<StockSearchResponse> results = stockSearchService.searchStocks(KEYWORD);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).stockCode()).isEqualTo("005930");
        assertThat(results.get(0).stockName()).isEqualTo("삼성전자");
    }

    @Test
    @DisplayName("종목 검색 횟수 증가 요청을 위임한다")
    void increaseStockSearchCount_delegatesToStockItemService() {
        // given
        String stockCode = "005930";

        // when
        stockSearchService.increaseStockSearchCount(stockCode);

        // then
        verify(stockItemService).updateSearchCount(stockCode);
    }

    private StockSearchStat createStat(String stockCode, int searchCount) {
        StockSearchStat stat = StockSearchStat.builder().stockCode(stockCode).build();
        try {
            var field = StockSearchStat.class.getDeclaredField("searchCount");
            field.setAccessible(true);
            field.set(stat, searchCount);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return stat;
    }
}
