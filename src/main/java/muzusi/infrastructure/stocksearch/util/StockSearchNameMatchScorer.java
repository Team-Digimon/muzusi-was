package muzusi.infrastructure.stocksearch.util;

import muzusi.application.stocksearch.policy.StockSearchMatchType;

public class StockSearchNameMatchScorer {
    /**
     * 검색 결과 주식 이름({@code stockName})과 검색 키워드({@code keyword}) 간 유사도 점수를 반환하는 메서드
     *
     * <ul>
     *     <li>완전히 동일한 경우, {@link StockSearchMatchType#EXACT}를 반환</li>
     *     <li>키워드가 결과의 접두어인 경우, {@link StockSearchMatchType#PREFIX}를 반환</li>
     *     <li>그 외의 경우, {@link StockSearchMatchType#DEFAULT}를 반환</li>
     * </ul>
     *
     * @param stockName 검색 결과 주식 이름
     * @param keyword   검색 키워드
     * @return          유사도 점수
     */
    public static double matchScore(String stockName, String keyword) {
        if (stockName.equals(keyword)) {
            return StockSearchMatchType.EXACT.getScore();
        }
        
        if (stockName.startsWith(keyword)) {
            return StockSearchMatchType.PREFIX.getScore();
        }
        
        return StockSearchMatchType.DEFAULT.getScore();
    }
}
