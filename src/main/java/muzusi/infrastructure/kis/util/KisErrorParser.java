package muzusi.infrastructure.kis.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KisErrorParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ERROR_MSG_KEY = "msg_cd";
    private static final String API_REQUEST_EXCEEDED_ERROR_CODE = "EGW00201";
    
    private KisErrorParser() { }
    
    /**
     * 한국투자증권 응답 에러 메시지에서 API 호출 유량 초과 인지를 확인하는 유틸 메서드
     *
     * @param errorMessage  한국투자증권 응답 에러 메시지
     * @return              API 호출 유량 초과에 따른 에러 발생 여부
     */
    public static boolean isApiRequestExceeded(String errorMessage) {
        try {
            int startIndex = errorMessage.indexOf('{');
            
            if (startIndex == -1) {
                return false;
            }
            
            String errorCode = parseErrorCode(errorMessage, startIndex);
            
            if (errorCode == null) {
                return false;
            }
            
            if (API_REQUEST_EXCEEDED_ERROR_CODE.equals(errorCode)) {
                return true;
            }
            
            return false;
        } catch (JsonProcessingException e) {
            log.error("[JSON PARSING ERROR] Failed to parse a KIS error message");
            return false;
        }
    }
    
    /**
     * 한국투자증권 응답 에러 메시지의 Json 파트 부분에서 에러 응답 코드를 파싱하는 메서드
     *
     * @param errorMessage  한국투자증권 응답 에러 메시지
     * @param startIndex    Json 파트 부분 시작 인덱스
     * @return              에러 응답 코드
     */
    private static String parseErrorCode(String errorMessage, int startIndex) throws JsonProcessingException {
        JsonNode errorNode = objectMapper.readTree(errorMessage.substring(startIndex));
        JsonNode errorCode = errorNode.get(ERROR_MSG_KEY);
        
        return errorCode == null ? null : errorCode.asText();
    }
}
