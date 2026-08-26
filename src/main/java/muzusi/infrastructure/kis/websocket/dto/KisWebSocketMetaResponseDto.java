package muzusi.infrastructure.kis.websocket.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KisWebSocketMetaResponseDto(
        Header header,
        Body body
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(
            @JsonProperty("tr_id") String trId
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(
            @JsonProperty("rt_cd") String rtCd,
            @JsonProperty("msg_cd") String msgCd,
            String msg1,
            Output output
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Output(
            String iv,
            String key
    ) {
    }

    public boolean isSuccess() {
        return body != null && "0".equals(body.rtCd());
    }
}
