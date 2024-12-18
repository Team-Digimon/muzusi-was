package muzusi.application.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.application.auth.dto.UserInfoDto;
import muzusi.global.exception.CustomException;
import muzusi.global.response.error.type.CommonErrorType;
import muzusi.infrastructure.properties.NaverProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class NaverClient extends OAuthClient{
    private final NaverProperties naverProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String getAccessToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("grant_type", "authorization_code");
        param.add("client_id", naverProperties.getClientId());
        param.add("client_secret", naverProperties.getClientSecret());
        param.add("redirect_uri", naverProperties.getRedirectUri());
        param.add("code", code);

        HttpEntity<MultiValueMap<String, String>> requestInfo = new HttpEntity<>(param, headers);

        RestTemplate req = new RestTemplate();
        try {
            ResponseEntity<String> response = req.exchange(
                    naverProperties.getTokenUri(),
                    HttpMethod.POST,
                    requestInfo,
                    String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            return rootNode.path("access_token").asText();
        } catch (Exception e) {
            throw new CustomException(CommonErrorType.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public UserInfoDto getUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> requestInfo = new HttpEntity<>(headers);

        RestTemplate req = new RestTemplate();
        try {
            ResponseEntity<String> response = req.exchange(
                    naverProperties.getGetUserInfoUri(),
                    HttpMethod.GET,
                    requestInfo,
                    String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());

            String id = rootNode.path("response").get("id").asText();

            return UserInfoDto.of(id);
        } catch (Exception e) {
            throw new CustomException(CommonErrorType.INTERNAL_SERVER_ERROR);
        }
    }
}