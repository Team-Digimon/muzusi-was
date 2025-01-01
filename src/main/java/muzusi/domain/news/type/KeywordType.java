package muzusi.domain.news.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KeywordType {
    KOSPI("코스피"), KOSDAQ("코스닥");

    private final String name;

    public static boolean containsKeyword(String keyword) {
        for (KeywordType type : KeywordType.values()) {
            if (type.name.equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }
}
