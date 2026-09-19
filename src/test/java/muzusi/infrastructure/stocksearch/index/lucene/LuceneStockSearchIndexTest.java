package muzusi.infrastructure.stocksearch.index.lucene;

import muzusi.application.stocksearch.dto.StockSearchCandidate;
import muzusi.domain.stock.entity.Stock;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LuceneStockSearchIndexTest {
    private LuceneStockSearchIndex luceneStockSearchIndex;

    @BeforeEach
    void setUp() {
        luceneStockSearchIndex = new LuceneStockSearchIndex();
    }

    @Test
    @DisplayName("주식 목록을 받아 종목마다 하나씩 문서를 색인한다")
    void buildIndexesOneDocumentPerStock() throws IOException {
        // given
        List<Stock> stocks = List.of(
                Stock.builder().stockCode("005930").stockName("삼성전자").build(),
                Stock.builder().stockCode("000660").stockName("SK하이닉스").build()
        );

        // when
        luceneStockSearchIndex.build(stocks);

        // then
        IndexSearcher indexSearcher = getIndexSearcher();
        assertThat(indexSearcher.getIndexReader().numDocs()).isEqualTo(2);
    }

    @Test
    @DisplayName("종목코드와 소문자로 정규화된 종목명은 저장되고, 초성 필드는 저장되지 않는다")
    void storesStockCodeAndLowerNameButNotChosung() throws IOException {
        // given
        List<Stock> stocks = List.of(
                Stock.builder().stockCode("005930").stockName("삼성전자").build()
        );

        // when
        luceneStockSearchIndex.build(stocks);

        // then
        IndexSearcher indexSearcher = getIndexSearcher();
        Document document = indexSearcher.storedFields().document(0);

        assertThat(document.get("stockCode")).isEqualTo("005930");
        assertThat(document.get("stockNameLower")).isEqualTo("삼성전자");
        assertThat(document.get("stockNameChosung")).isNull();
    }

    @Test
    @DisplayName("build를 다시 호출하면 이전 색인은 사라지고 새 데이터로 완전히 교체된다")
    void rebuildingReplacesPreviousIndex() throws IOException {
        // given
        luceneStockSearchIndex.build(List.of(
                Stock.builder().stockCode("005930").stockName("삼성전자").build(),
                Stock.builder().stockCode("000660").stockName("SK하이닉스").build()
        ));

        // when
        luceneStockSearchIndex.build(List.of(
                Stock.builder().stockCode("035420").stockName("NAVER").build()
        ));

        // then
        IndexSearcher indexSearcher = getIndexSearcher();
        assertThat(indexSearcher.getIndexReader().numDocs()).isEqualTo(1);
        assertThat(indexSearcher.storedFields().document(0).get("stockCode")).isEqualTo("035420");
    }

    @Test
    @DisplayName("검색어와 종목명이 완전히 일치하면 완전일치 점수(100)를 받는다")
    void searchReturnsAllMatchScoreWhenKeywordEqualsStockName() throws IOException {
        // given
        luceneStockSearchIndex.build(List.of(
                Stock.builder().stockCode("005930").stockName("삼성전자").build()
        ));

        // when
        List<StockSearchCandidate> responses = luceneStockSearchIndex.search("삼성전자", 10);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).stockCode()).isEqualTo("005930");
        assertThat(responses.get(0).score()).isEqualTo(100);
    }

    @Test
    @DisplayName("검색어가 종목명의 접두어이면 접두어 일치 점수(50)를 받는다")
    void searchReturnsStartMatchScoreWhenKeywordIsPrefixOfStockName() throws IOException {
        // given
        luceneStockSearchIndex.build(List.of(
                Stock.builder().stockCode("005930").stockName("삼성전자").build(),
                Stock.builder().stockCode("000660").stockName("SK하이닉스").build()
        ));

        // when
        List<StockSearchCandidate> responses = luceneStockSearchIndex.search("삼성", 10);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).stockCode()).isEqualTo("005930");
        assertThat(responses.get(0).score()).isEqualTo(50);
    }

    @Test
    @DisplayName("영문 종목명은 대소문자 구분 없이 접두어로 검색된다")
    void searchIsCaseInsensitiveForEnglishStockName() throws IOException {
        // given
        luceneStockSearchIndex.build(List.of(
                Stock.builder().stockCode("051910").stockName("LG화학").build()
        ));

        // when
        List<StockSearchCandidate> responses = luceneStockSearchIndex.search("lg", 10);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).stockCode()).isEqualTo("051910");
    }

    @Test
    @DisplayName("검색어가 종목명 초성과 완전히 일치하면 완전일치 점수(100)를 받는다")
    void searchReturnsAllMatchScoreWhenKeywordEqualsStockNameChosung() throws IOException {
        // given
        luceneStockSearchIndex.build(List.of(
                Stock.builder().stockCode("005930").stockName("삼성전자").build(),
                Stock.builder().stockCode("000660").stockName("SK하이닉스").build()
        ));

        // when
        List<StockSearchCandidate> responses = luceneStockSearchIndex.search("ㅅㅅㅈㅈ", 10);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).stockCode()).isEqualTo("005930");
        assertThat(responses.get(0).score()).isEqualTo(100);
    }

    @Test
    @DisplayName("검색어가 종목명 초성의 접두어이면 접두어 일치 점수(50)를 받는다")
    void searchReturnsStartMatchScoreWhenKeywordIsPrefixOfStockNameChosung() throws IOException {
        // given
        luceneStockSearchIndex.build(List.of(
                Stock.builder().stockCode("005930").stockName("삼성전자").build(),
                Stock.builder().stockCode("000660").stockName("SK하이닉스").build()
        ));

        // when
        List<StockSearchCandidate> responses = luceneStockSearchIndex.search("ㅅㅅㅈ", 10);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).stockCode()).isEqualTo("005930");
        assertThat(responses.get(0).score()).isEqualTo(50);
    }

    @Test
    @DisplayName("영문과 한글이 섞인 종목명도 대소문자를 유지한 채로 초성 검색이 된다")
    void searchMatchesByChosungPreservingEnglishCase() throws IOException {
        // given
        luceneStockSearchIndex.build(List.of(
                Stock.builder().stockCode("051910").stockName("LG화학").build()
        ));

        // when
        List<StockSearchCandidate> responses = luceneStockSearchIndex.search("LGㅎㅎ", 10);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).stockCode()).isEqualTo("051910");
    }

    @Test
    @DisplayName("검색어 앞뒤 공백은 무시하고 검색된다")
    void searchTrimsKeyword() throws IOException {
        // given
        luceneStockSearchIndex.build(List.of(
                Stock.builder().stockCode("005930").stockName("삼성전자").build()
        ));

        // when
        List<StockSearchCandidate> responses = luceneStockSearchIndex.search("  삼성  ", 10);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).stockCode()).isEqualTo("005930");
    }

    @Test
    @DisplayName("limit 개수만큼만 결과를 반환한다")
    void searchRespectsLimit() throws IOException {
        // given
        luceneStockSearchIndex.build(List.of(
                Stock.builder().stockCode("005930").stockName("삼성전자").build(),
                Stock.builder().stockCode("006400").stockName("삼성SDI").build(),
                Stock.builder().stockCode("028260").stockName("삼성물산").build()
        ));

        // when
        List<StockSearchCandidate> responses = luceneStockSearchIndex.search("삼성", 2);

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("일치하는 종목이 없으면 빈 리스트를 반환한다")
    void searchReturnsEmptyListWhenNoMatch() throws IOException {
        // given
        luceneStockSearchIndex.build(List.of(
                Stock.builder().stockCode("005930").stockName("삼성전자").build()
        ));

        // when
        List<StockSearchCandidate> responses = luceneStockSearchIndex.search("카카오", 10);

        // then
        assertThat(responses).isEmpty();
    }

    private IndexSearcher getIndexSearcher() {
        try {
            Field field = LuceneStockSearchIndex.class.getDeclaredField("indexSearcher");
            field.setAccessible(true);
            return (IndexSearcher) field.get(luceneStockSearchIndex);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
