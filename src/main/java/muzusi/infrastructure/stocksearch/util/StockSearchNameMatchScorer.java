package muzusi.infrastructure.stocksearch.util;

public class StockSearchNameMatchScorer {
    private static final int ALL_MATCH_SCORE = 100;
    private static final int START_MATCH_SCORE = 50;
    private static final int DEFAULT_MATCH_SCORE = 30;
    
    /**
     * 검색 결과 주식 이름({@code stockName})과 검색 키워드({@code keyword}) 간 유사도 점수를 반환하는 메서드
     *
     * <ul>
     *     <li>완전히 동일한 경우, {@value ALL_MATCH_SCORE}를 반환</li>
     *     <li>키워드가 결과의 접두어인 경우, {@value START_MATCH_SCORE}를 반환</li>
     *     <li>그 외의 경우, {@value DEFAULT_MATCH_SCORE}를 반환</li>
     * </ul>
     *
     * @param stockName 검색 결과 주식 이름
     * @param keyword   검색 키워드
     * @return          유사도 점수
     */
    public static double matchScore(String stockName, String keyword) {
        if (stockName.equals(keyword)) {
            return ALL_MATCH_SCORE;
        }
        
        if (stockName.startsWith(keyword)) {
            return START_MATCH_SCORE;
        }
        
        return DEFAULT_MATCH_SCORE;
    }
}
