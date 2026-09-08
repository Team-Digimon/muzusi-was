package muzusi.infrastructure.oauth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoAccessTokenResponse(
        @JsonProperty(value = "access_token") String accessToken,
        @JsonProperty(value = "token_type") String tokenType
) implements AccessTokenResponse { }
