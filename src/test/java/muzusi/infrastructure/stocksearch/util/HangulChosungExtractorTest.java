package muzusi.infrastructure.stocksearch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HangulChosungExtractorTest {

    @Test
    @DisplayName("한글 문자열에서 초성만 추출한다")
    void extractChosungFromHangul() {
        // given
        String text = "삼성전자";

        // when
        String result = HangulChosungExtractor.extract(text);

        // then
        assertThat(result).isEqualTo("ㅅㅅㅈㅈ");
    }

    @Test
    @DisplayName("한글과 영어가 섞인 경우, 영어는 그대로 두고 한글만 초성으로 추출한다")
    void extractChosungFromHangulMixedWithEnglish() {
        // given
        String text = "LG전자";

        // when
        String result = HangulChosungExtractor.extract(text);

        // then
        assertThat(result).isEqualTo("LGㅈㅈ");
    }

    @Test
    @DisplayName("한글과 숫자가 섞인 경우, 숫자는 그대로 두고 한글만 초성으로 추출한다")
    void extractChosungFromHangulMixedWithDigit() {
        // given
        String text = "3M코리아";

        // when
        String result = HangulChosungExtractor.extract(text);

        // then
        assertThat(result).isEqualTo("3Mㅋㄹㅇ");
    }

    @Test
    @DisplayName("한글, 영어, 숫자가 모두 섞인 경우, 영어/숫자는 그대로 두고 한글만 초성으로 추출한다")
    void extractChosungFromHangulMixedWithEnglishAndDigit() {
        // given
        String text = "SK2가스";

        // when
        String result = HangulChosungExtractor.extract(text);

        // then
        assertThat(result).isEqualTo("SK2ㄱㅅ");
    }
}
