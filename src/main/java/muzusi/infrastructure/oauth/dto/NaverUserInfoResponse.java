package muzusi.infrastructure.oauth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverUserInfoResponse(
        @JsonProperty(value = "response") Response response
) implements UserInfoResponse {
    @Override
    public String id() {
        return response.id();
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Response(
            @JsonProperty(value = "id") String id
    ) { }
}
