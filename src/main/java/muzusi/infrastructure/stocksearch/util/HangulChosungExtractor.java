package muzusi.infrastructure.stocksearch.util;

public class HangulChosungExtractor {
    private static final char[] CHOSUNG = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    private static final int HANGUL_BASE = 0xAC00;
    private static final int HANGUL_LAST = 0xD7A3;
    private static final int CHOSUNG_UNIT = 588;

    private HangulChosungExtractor() {
    }
    
    /**
     * 주어진 문자열에서 한글 초성을 추출하는 메서드
     *
     * @param text  문자열
     * @return      한글은 초성만 추출하여 변환한 문자열
     */
    public static String extract(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= HANGUL_BASE && c <= HANGUL_LAST) {
                int chosungIndex = (c - HANGUL_BASE) / CHOSUNG_UNIT;
                result.append(CHOSUNG[chosungIndex]);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
