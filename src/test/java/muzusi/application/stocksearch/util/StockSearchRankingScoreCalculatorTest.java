package muzusi.application.stocksearch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class StockSearchRankingScoreCalculatorTest {
    @Test
    @DisplayName("검색 빈도가 0이면 매치 점수를 그대로 반환한다")
    void calculateFinalScore_returnsMatchedScoreWhenSearchCountIsZero() {
        // given
        double matchedScore = 50;
        int searchCount = 0;

        // when
        double result = StockSearchRankingScoreCalculator.calculateFinalScore(matchedScore, searchCount);

        // then
        assertThat(result).isEqualTo(matchedScore);
    }

    @Test
    @DisplayName("검색 빈도가 PIVOT(100)과 같으면 가중치의 절반(10)만큼 가산된다")
    void calculateFinalScore_addsHalfOfWeightWhenSearchCountEqualsPivot() {
        // given
        double matchedScore = 50;
        int searchCount = 100;

        // when
        double result = StockSearchRankingScoreCalculator.calculateFinalScore(matchedScore, searchCount);

        // then
        assertThat(result).isCloseTo(matchedScore + 10, within(1e-9));
    }

    @Test
    @DisplayName("검색 빈도가 늘어날수록 최종 점수도 함께 증가한다")
    void calculateFinalScore_increasesAsSearchCountIncreases() {
        // given
        double matchedScore = 30;

        // when
        double lowCountScore = StockSearchRankingScoreCalculator.calculateFinalScore(matchedScore, 10);
        double highCountScore = StockSearchRankingScoreCalculator.calculateFinalScore(matchedScore, 10_000);

        // then
        assertThat(highCountScore).isGreaterThan(lowCountScore);
    }

    @Test
    @DisplayName("검색 빈도가 아무리 커도 가산 점수는 가중치(20)를 넘지 못한다")
    void calculateFinalScore_boostNeverReachesWeightCap() {
        // given
        double matchedScore = 30;

        // when
        double result = StockSearchRankingScoreCalculator.calculateFinalScore(matchedScore, Integer.MAX_VALUE);

        // then
        assertThat(result).isLessThan(matchedScore + 20);
        assertThat(result).isCloseTo(matchedScore + 20, within(1e-6));
    }

    @Test
    @DisplayName("검색 빈도가 음수여도 예외 없이 계산된다")
    void calculateFinalScore_handlesNegativeSearchCount() {
        // given
        double matchedScore = 30;
        int searchCount = -1;

        // when
        double result = StockSearchRankingScoreCalculator.calculateFinalScore(matchedScore, searchCount);

        // then
        assertThat(result).isLessThan(matchedScore);
    }
}
