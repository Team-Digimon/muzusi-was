package muzusi.infrastructure.stocksearch.index.lucene;

import muzusi.application.stocksearch.dto.StockSearchCandidate;
import muzusi.domain.stock.entity.Stock;
import muzusi.infrastructure.stocksearch.exception.StockSearchIndexException;
import muzusi.infrastructure.stocksearch.util.HangulChosungExtractor;
import muzusi.infrastructure.stocksearch.util.StockSearchNameMatchScorer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
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
        document.add(new StringField(StockSearchIndexField.FIELD_STOCK_NAME, stock.getStockName(), Field.Store.YES));
        document.add(new StringField(StockSearchIndexField.FIELD_STOCK_NAME_LOWER, stock.getStockName().trim().toLowerCase(), Field.Store.YES));
        document.add(new StringField(StockSearchIndexField.FIELD_STOCK_NAME_CHOSUNG, HangulChosungExtractor.extract(stock.getStockName().trim()), Field.Store.NO));
        return document;
    }
    
    /**
     * 주식 인덱스에서 키워드를 통해 검색하는 메서드
     *
     * <p> 최대 {@code limit} 만큼의 검색한다.
     * <p> 검색 키워드({@code keyword})와 검색 결과의 유사도 점수를
     *     {@link StockSearchNameMatchScorer#matchScore(String, String)}를 호출하여 점수를 계산한다.
     *
     * @param keyword   검색어 키워드
     * @param limit     검색 결과 한도
     * @return          검색 결과 목록
     */
    public List<StockSearchCandidate> search(String keyword, int limit) {
        String keywordTrimmed = keyword.trim();
        String keywordLower = keywordTrimmed.toLowerCase();
        Query query = getQuery(keywordTrimmed, keywordLower);

        try {
            TopDocs topDocs = indexSearcher.search(query, limit);
            StoredFields storedFields = indexSearcher.storedFields();
            List<StockSearchCandidate> responses = new ArrayList<>();

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document document = storedFields.document(scoreDoc.doc);
                String stockCode = document.get(StockSearchIndexField.FIELD_STOCK_CODE);
                String stockName = document.get(StockSearchIndexField.FIELD_STOCK_NAME);
                double matchScore = calculateMatchScore(stockName, keywordTrimmed, keywordLower);
                
                responses.add(new StockSearchCandidate(stockCode, stockName, matchScore));
            }

            return responses;
        } catch (IOException e) {
            throw new StockSearchIndexException("주식 검색에 실패하였습니다. (keyword: %s)".formatted(keyword), e);
        }
    }

    /**
     * 종목명 원문/초성 매치 점수 중 더 높은 점수를 계산하는 메서드
     *
     * @param stockName         종목명 원문
     * @param keywordTrimmed    공백 제거한 검색어 키워드
     * @param keywordLower      소문자로 변환한 검색어 키워드
     * @return                  원문/초성 매치 점수 중 더 높은 점수
     */
    private double calculateMatchScore(String stockName, String keywordTrimmed, String keywordLower) {
        double nameMatchScore = StockSearchNameMatchScorer.matchScore(stockName.trim().toLowerCase(), keywordLower);
        double chosungMatchScore = StockSearchNameMatchScorer.matchScore(HangulChosungExtractor.extract(stockName.trim()), keywordTrimmed);
        return Math.max(nameMatchScore, chosungMatchScore);
    }
    
    /**
     * 주식 인덱스 검색을 위한 쿼리 생성 메서드
     *
     * <p> 검색 대상 조건은 다음과 같다.</p>
     *
     * <ol>
     *     <li>주식명 필드({@link StockSearchIndexField#FIELD_STOCK_NAME_LOWER})와 접두어가 일치하는 경우</li>
     *     <li>주식명 초성 필드({@link StockSearchIndexField#FIELD_STOCK_NAME_CHOSUNG})과 접두어가 일치하는 경우</li>
     * </ol>
     *
     * @param keywordTrimmed    검색어 키워드
     * @param keywordLower      소문자로 변환한 검색어 키워드
     * @return                  검색 쿼리
     */
    private Query getQuery(String keywordTrimmed, String keywordLower) {
        return new BooleanQuery.Builder()
                .add(new PrefixQuery(new Term(StockSearchIndexField.FIELD_STOCK_NAME_LOWER, keywordLower)), BooleanClause.Occur.SHOULD)
                .add(new PrefixQuery(new Term(StockSearchIndexField.FIELD_STOCK_NAME_CHOSUNG, keywordTrimmed)), BooleanClause.Occur.SHOULD)
                .build();
    }
    
    private static class StockSearchIndexField {
        private static final String FIELD_STOCK_CODE = "stockCode";
        private static final String FIELD_STOCK_NAME = "stockName";
        private static final String FIELD_STOCK_NAME_LOWER = "stockNameLower";
        private static final String FIELD_STOCK_NAME_CHOSUNG = "stockNameChosung";
    }
}
