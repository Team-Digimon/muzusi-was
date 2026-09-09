package muzusi.infrastructure.oauth;

import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.oauth.dto.AccessTokenResponse;
import muzusi.infrastructure.oauth.dto.UserInfoResponse;
import muzusi.infrastructure.oauth.exception.OAuthApiException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class OAuthClient {
    /**
     * OAuth 리소스 서버(플랫폼)에서 access token 발급 API 요청 메서드
     *
     * @param tokenUrl      access token 발급 URL
     * @param params        access token 발급 API 요청 파라미터
     * @param response      응답 역직렬화 타입
     * @return              access token
     */
    public String getAccessToken(String tokenUrl, MultiValueMap<String, String> params, Class<? extends AccessTokenResponse> response) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    response
            ).getBody().accessToken();
        } catch (Exception e) {
            throw new OAuthApiException("OAuth Access Token 발급 중 에러가 발생하였습니다.", e);
        }
    }
    
    /**
     * OAuth 리소스 서버(플랫폼)에서 사용자 정보 조회 API 요청 메서드
     *
     * <p> 사용자 정보 조회 후 해당 플랫폼에서 고유 식별자({@code id}) 값을 반환한다.
     *
     * @param userInfoUrl       사용자 정보 조회 URL
     * @param accessToken       access token
     * @param responseType      응답 역직렬화 타입
     * @return                  해당 플랫폼에서의 사용자 고유 식별자
     */
    public String getUserIdentifier(String userInfoUrl, String accessToken, Class<? extends UserInfoResponse> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    request,
                    responseType
            ).getBody().id();
        } catch (Exception e) {
            throw new OAuthApiException("OAuth 사용자 정보 조회 중 에러가 발생하였습니다.", e);
        }
    }
}