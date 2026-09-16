package muzusi.infrastructure.kis.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import muzusi.infrastructure.kis.dto.KisResponse;
import muzusi.infrastructure.kis.exception.KisApiException;
import muzusi.infrastructure.kis.exception.KisApiRateLimitExceedException;

public final class KisErrorParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String ERROR_RT_CD = "1";
    private static final String API_RATE_LIMIT_EXCEED_ERROR_MSG_CD = "EGW00201";

    private KisErrorParser() { }

    /**
     * 한국투자증권 REST API 응답의 응답 코드({@code rt_cd})와 메시지 코드({@code msg_cd})를 확인해 예외를 발생시키는 메서드
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
    
    /**
     * 한국투자증권 REST API RAW 응답 본문을 받아 커스텀 예외를 발생시키는 메서드
     *
     * <p> RAW 응답 본문을 {@link KisErrorResponse}로 파싱하여 {@link #validate(KisResponse)}를 호출한다.
     *
     * @param rawResponse 한국투자증권 REST API 응답 RAW 본문
     */
    public static void validate(String rawResponse) {
        if (rawResponse == null || rawResponse.isEmpty()) {
            throw new KisApiException("한국투자증권 REST API 응답 본문이 비어있습니다.");
        }
        
        try {
            validate(objectMapper.readValue(rawResponse, KisErrorResponse.class));
        } catch (JsonProcessingException e) {
            throw new KisApiException("한국투자증권 REST API 에러 응답 파싱에 실패하였습니다. (raw: %s)".formatted(rawResponse), e);
        }
    }
    
    private static boolean isError(String rtCd) {
        return ERROR_RT_CD.equals(rtCd);
    }
    
    private static boolean isRateLimitExceed(String msgCd) {
        return API_RATE_LIMIT_EXCEED_ERROR_MSG_CD.equals(msgCd);
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KisErrorResponse(
            @JsonProperty(value = "rt_cd") String rtCd,
            @JsonProperty(value = "msg_cd") String msgCd,
            @JsonProperty(value = "msg1") String msg1,
            @JsonProperty(value = "message") String message
    ) implements KisResponse { }
}
