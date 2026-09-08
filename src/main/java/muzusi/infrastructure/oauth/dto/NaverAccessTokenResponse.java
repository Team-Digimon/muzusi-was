package muzusi.infrastructure.oauth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverAccessTokenResponse(
        @JsonProperty(value = "access_token") String accessToken
) implements AccessTokenResponse { }
