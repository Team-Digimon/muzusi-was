package muzusi.application.stocksearch.policy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum StockSearchMatchType {
    EXACT(100),
    PREFIX(50),
    DEFAULT(30),
    ;

    private final int score;

    /**
     * 모든 매치 타입의 점수 중 인접한 두 점수 간 최소 차이를 반환하는 메서드
     *
     * <p> 주식 검색 결과의 매치 점수와 검색 빈도를 통한 최종 점수 계산 시 검색 빈도의 상한선에 사용한다.
     *
     * @return 인접한 점수 간 최소 차이
     */
    public static int minScoreGap() {
        int[] sortedScores = Arrays.stream(values())
                .mapToInt(StockSearchMatchType::getScore)
                .sorted()
                .toArray();

        int minGap = Integer.MAX_VALUE;
        for (int i = 1; i < sortedScores.length; i++) {
            minGap = Math.min(minGap, sortedScores[i] - sortedScores[i - 1]);
        }
        return minGap;
    }
}
