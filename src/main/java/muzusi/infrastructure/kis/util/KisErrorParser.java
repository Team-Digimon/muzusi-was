package muzusi.infrastructure.kis.util;

import muzusi.infrastructure.kis.dto.KisResponse;
import muzusi.infrastructure.kis.exception.KisApiException;
import muzusi.infrastructure.kis.exception.KisApiRateLimitExceedException;

public final class KisErrorParser {
    private static final String ERROR_RT_CD = "1";
    private static final String API_RATE_LIMIT_EXCEED_ERROR_MSG_CD = "EGW00201";

    private KisErrorParser() { }

    /**
     * 응답의 rt_cd가 에러를 나타내면 msg_cd에 따라 적절한 예외를 던진다.
     *
     * @param response                          한국투자증권 REST API 응답
     * @throws KisApiRateLimitExceedException   유량 초과 에러인 경우 (msg_cd: {@value API_RATE_LIMIT_EXCEED_ERROR_MSG_CD})
     * @throws KisApiException                  response가 null이거나, 그 외 에러 응답인 경우
     */
    public static void validate(KisResponse response) {
        if (response == null) {
            throw new KisApiException("한국투자증권 REST API 응답 본문이 비어있습니다.");
        }
        
        if (isError(response.rtCd())) {
            if (isRateLimitExceed(response.msgCd())) {
                throw new KisApiRateLimitExceedException("한국투자증권 REST API 호출 결과 유량 초과 에러가 발생하였습니다.");
            }
            
            throw new KisApiException(
                    "한국투자증권 REST API 호출 결과 에러가 발생하였습니다. (msg_cd: %s, msg1: %s)".formatted(
                            response.msgCd(),
                            response.msg1()
                    )
            );
        }
    }
    
    private static boolean isError(String rtCd) {
        return ERROR_RT_CD.equals(rtCd);
    }
    
    private static boolean isRateLimitExceed(String msgCd) {
        return API_RATE_LIMIT_EXCEED_ERROR_MSG_CD.equals(msgCd);
    }
}
