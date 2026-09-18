package muzusi.infrastructure.stocksearch.index.lucene;

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
