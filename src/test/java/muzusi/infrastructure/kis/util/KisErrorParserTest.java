package muzusi.infrastructure.kis.util;

import muzusi.infrastructure.kis.dto.KisResponse;
import muzusi.infrastructure.kis.exception.KisApiException;
import muzusi.infrastructure.kis.exception.KisApiRateLimitExceedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KisErrorParserTest {
    private record TestKisResponse(String rtCd, String msgCd, String msg1) implements KisResponse {
    }

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
        assertThatThrownBy(() -> KisErrorParser.validate(null))
                .isInstanceOf(KisApiException.class);
    }
}
