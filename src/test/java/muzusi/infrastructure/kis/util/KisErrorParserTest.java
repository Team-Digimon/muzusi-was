package muzusi.infrastructure.kis.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import muzusi.infrastructure.kis.dto.KisResponse;
import muzusi.infrastructure.kis.exception.KisApiException;
import muzusi.infrastructure.kis.exception.KisApiRateLimitExceedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KisErrorParserTest {
    private record TestKisResponse(String rtCd, String msgCd, String msg1) implements KisResponse {
    }

    @Nested
    @DisplayName("validate(KisResponse)")
    class ValidateResponse {
        @Test
        @DisplayName("정상 응답(rt_cd: 0)이면 예외를 던지지 않는다")
        void validateWhenSuccess() {
            // given
            KisResponse response = new TestKisResponse("0", "MCA00000", "정상처리 되었습니다.");

            // when & then
            assertThatCode(() -> KisErrorParser.validate(response))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("유량 초과 응답(rt_cd: 1, msg_cd: EGW00201)이면 KisApiRateLimitExceedException을 던진다")
        void validateWhenRateLimitExceeded() {
            // given
            KisResponse response = new TestKisResponse("1", "EGW00201", "초당 거래건수를 초과하였습니다.");

            // when & then
            assertThatThrownBy(() -> KisErrorParser.validate(response))
                    .isInstanceOf(KisApiRateLimitExceedException.class);
        }

        @Test
        @DisplayName("유량 초과가 아닌 에러 응답(rt_cd: 1)이면 KisApiException을 던진다")
        void validateWhenOtherError() {
            // given
            KisResponse response = new TestKisResponse("1", "OTHER_ERROR", "유효하지 않은 요청입니다.");

            // when & then
            assertThatThrownBy(() -> KisErrorParser.validate(response))
                    .isInstanceOf(KisApiException.class)
                    .isNotInstanceOf(KisApiRateLimitExceedException.class);
        }

        @Test
        @DisplayName("응답이 null이면 KisApiException을 던진다")
        void validateWhenResponseIsNull() {
            // when & then
            assertThatThrownBy(() -> KisErrorParser.validate((KisResponse) null))
                    .isInstanceOf(KisApiException.class);
        }
    }

    @Nested
    @DisplayName("validate(String)")
    class ValidateRawResponse {
        @Test
        @DisplayName("원본 응답이 null이면 KisApiException을 던진다")
        void validateWhenRawResponseIsNull() {
            // when & then
            assertThatThrownBy(() -> KisErrorParser.validate((String) null))
                    .isInstanceOf(KisApiException.class);
        }

        @Test
        @DisplayName("원본 응답이 빈 문자열이면 KisApiException을 던진다")
        void validateWhenRawResponseIsEmpty() {
            // when & then
            assertThatThrownBy(() -> KisErrorParser.validate(""))
                    .isInstanceOf(KisApiException.class);
        }

        @Test
        @DisplayName("정상 응답(rt_cd: 0)이면 예외를 던지지 않는다")
        void validateWhenSuccess() {
            // given
            String rawResponse = """
                    {"rt_cd":"0","msg_cd":"MCA00000","msg1":"정상처리 되었습니다."}
                    """;

            // when & then
            assertThatCode(() -> KisErrorParser.validate(rawResponse))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("유량 초과 응답(rt_cd: 1, msg_cd: EGW00201)이면 KisApiRateLimitExceedException을 던진다")
        void validateWhenRateLimitExceeded() {
            // given
            String rawResponse = """
                    {"rt_cd":"1","msg1":"초당 거래건수를 초과하였습니다.","msg_cd":"EGW00201","message":"EGW00201"}
                    """;

            // when & then
            assertThatThrownBy(() -> KisErrorParser.validate(rawResponse))
                    .isInstanceOf(KisApiRateLimitExceedException.class);
        }

        @Test
        @DisplayName("유량 초과가 아닌 에러 응답(rt_cd: 1)이면 KisApiException을 던진다")
        void validateWhenOtherError() {
            // given
            String rawResponse = """
                    {"rt_cd":"1","msg_cd":"OTHER_ERROR","msg1":"유효하지 않은 요청입니다."}
                    """;

            // when & then
            assertThatThrownBy(() -> KisErrorParser.validate(rawResponse))
                    .isInstanceOf(KisApiException.class)
                    .isNotInstanceOf(KisApiRateLimitExceedException.class);
        }

        @Test
        @DisplayName("알 수 없는 필드가 포함되어도 파싱에 성공한다")
        void validateIgnoresUnknownFields() {
            // given
            String rawResponse = """
                    {"rt_cd":"0","msg_cd":"MCA00000","msg1":"정상처리 되었습니다.","unknown_field":"foo"}
                    """;

            // when & then
            assertThatCode(() -> KisErrorParser.validate(rawResponse))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("JSON 형식이 아니면 KisApiException을 던지고, 원인으로 파싱 예외를 보존한다")
        void validateWhenParsingFails() {
            // given
            String rawResponse = "not a json";

            // when & then
            assertThatThrownBy(() -> KisErrorParser.validate(rawResponse))
                    .isInstanceOf(KisApiException.class)
                    .hasCauseInstanceOf(JsonProcessingException.class);
        }
    }
}
