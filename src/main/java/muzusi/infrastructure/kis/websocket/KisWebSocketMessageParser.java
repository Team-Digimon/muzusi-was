package muzusi.infrastructure.kis.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.kis.websocket.dto.KisWebSocketMetaResponseDto;
import muzusi.infrastructure.kis.websocket.dto.KisWebSocketResponseDto;
import org.springframework.stereotype.Component;

import java.util.StringTokenizer;

@Component
@RequiredArgsConstructor
public class KisWebSocketMessageParser {
    private final ObjectMapper objectMapper;

    private static final String PAYLOAD_DELIMITER = "|";
    private static final String IS_ENCODED_FLAG = "1";

    /**
     * 한국투자증권 웹소켓 실시간 응답 페이로드를 파싱하는 메서드
     *
     * <p> 실시간 응답은 {@link #PAYLOAD_DELIMITER} 로 구분된 "암호화 여부 | TR_ID | 데이터 건수 | 데이터" 형식으로 전달된다.
     *
     * @param payload   한국투자증권 웹소켓 실시간 응답 페이로드
     * @return          파싱된 실시간 응답 DTO
     */
    public KisWebSocketResponseDto parse(String payload) {
        StringTokenizer st = new StringTokenizer(payload, PAYLOAD_DELIMITER);

        return KisWebSocketResponseDto.builder()
                .isEncoded(IS_ENCODED_FLAG.equals(st.nextToken()))
                .trId(st.nextToken())
                .dataCount(Integer.parseInt(st.nextToken()))
                .data(st.nextToken())
                .build();
    }

    /**
     * 한국투자증권 웹소켓 메타 메시지(JSON) 페이로드를 파싱하는 메서드
     *
     * @param payload   한국투자증권 웹소켓 메타 메시지 페이로드
     * @return          파싱된 메타 메시지 응답 DTO
     * @throws JsonProcessingException JSON 역직렬화에 실패한 경우
     */
    public KisWebSocketMetaResponseDto parseMeta(String payload) throws JsonProcessingException {
        return objectMapper.readValue(payload, KisWebSocketMetaResponseDto.class);
    }
}
