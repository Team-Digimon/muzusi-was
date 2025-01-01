package muzusi.domain.news.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KeywordType {
    KOSPI("코스피"), KOSDAQ("코스닥");

    private final String name;

    /**
     * 키워드 유효성 검사 메서드
     *
     * @param keyword : 클라이언트에서 요청한 키워드
     * @return : 키워드 일치 여부
     */
    public static boolean containsKeyword(String keyword) {
        for (KeywordType type : KeywordType.values()) {
            if (type.name.equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }
}
