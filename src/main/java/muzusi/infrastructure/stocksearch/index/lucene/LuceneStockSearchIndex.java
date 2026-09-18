package muzusi.infrastructure.stocksearch.index.lucene;

import muzusi.domain.stock.entity.Stock;
import muzusi.infrastructure.stocksearch.util.HangulChosungExtractor;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class LuceneStockSearchIndex {
    private IndexSearcher indexSearcher;
    
    /**
     * 인덱스를 초기화하는 메서드
     *
     * <p> 인자로 주식 기본 정보 목록을 받아 인덱스를 생성(색인)한다.
     *
     * @param stocks        주식 기본 정보 목록
     * @throws IOException  인덱스 생성 과정 중 에러가 발생한 경우
     */
    public void build(List<Stock> stocks) throws IOException {
        Directory directory = new ByteBuffersDirectory();
        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(standardAnalyzer);

        try (IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig)) {
            for (Stock stock : stocks) {
                indexWriter.addDocument(parseStockToDocument(stock));
            }
            indexWriter.commit();
        }

        this.indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    }
    
    /**
     * 주식 기본 정보를 인덱스에 저장 가능한 Document 객체로 변환하는 메서드
     *
     * @param stock 주식 기본 정보
     * @return      인덱스에 저장될 Document
     */
    private Document parseStockToDocument(Stock stock) {
        Document document = new Document();
        document.add(new StringField(StockSearchIndexField.FIELD_STOCK_CODE, stock.getStockCode(), Field.Store.YES));
        document.add(new StringField(StockSearchIndexField.FIELD_STOCK_NAME_LOWER, stock.getStockName().trim().toLowerCase(), Field.Store.YES));
        document.add(new StringField(StockSearchIndexField.FIELD_STOCK_NAME_CHOSUNG, HangulChosungExtractor.extract(stock.getStockName().trim()), Field.Store.NO));
        return document;
    }
    
    private static class StockSearchIndexField {
        private static final String FIELD_STOCK_CODE = "stockCode";
        private static final String FIELD_STOCK_NAME_LOWER = "stockNameLower";
        private static final String FIELD_STOCK_NAME_CHOSUNG = "stockNameChosung";
    }
}
