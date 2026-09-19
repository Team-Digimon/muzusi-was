package muzusi.application.stocksearch.util;

public class StockSearchRankingScoreCalculator {
    private static final int PIVOT = 100;
    private static final int WEIGHT = 20;

    /**
     * 매치 점수와 검색 빈도를 종합하여 최종 랭킹 점수를 계산하는 메서드
     *
     * <p> 검색 빈도({@code searchCount})가 그대로 반영되면 값이 커질수록 매치 점수를 압도해버리므로,
     *     검색 빈도가 최종 점수에 영향을 미치는 범위는 매치 점수의 최소 차이인 {@value WEIGHT}를 넘지못하도록
     *     포화 함수를 적용하여 계산한다.
     *
     * <p> 검색 빈도에 대한 포화 함수 결과값에 가중치 {@value WEIGHT}를 곱하여 검색 빈도가 최종 점수에 반영되는
     *     점수 범위를 [0, {@value WEIGHT})로 제한한다.
     *
     * @param matchedScore  종목명 매치 점수
     * @param searchCount   해당 종목의 누적 검색 횟수
     * @return              매치 점수에 검색 빈도 가중치를 더한 최종 랭킹 점수
     */
    public static double calculateFinalScore(double matchedScore, int searchCount) {
        return matchedScore + WEIGHT * saturation(searchCount);
    }
    
    /**
     * 검색 빈도에 대한 포화(Saturation) 함수 결과값을 반환하는 메서드
     *
     * @param searchCount   검색 빈도
     * @return              포화 함수 결과값
     */
    private static double saturation(int searchCount) {
        return (double) searchCount / ((double) searchCount + PIVOT);
    }
}
