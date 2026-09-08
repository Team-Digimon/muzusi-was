package muzusi.infrastructure.oauth;

import muzusi.application.auth.dto.UserInfoDto;
import muzusi.infrastructure.oauth.dto.AccessTokenResponse;
import muzusi.infrastructure.oauth.dto.UserInfoResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public abstract class OAuthProvider {
    private final OAuthClient oAuthClient;
    private final OAuthProperties oAuthProperties;
    
    public OAuthProvider(OAuthClient oAuthClient, OAuthProperties oAuthProperties) {
        this.oAuthClient = oAuthClient;
        this.oAuthProperties = oAuthProperties;
    }
    
    /**
     * 인가 코드로 리소스 서버에 접근하여 사용자 식별자를 조회하는 메서드
     *
     * <p>
     * 인가 코드로 access token을 발급받은 뒤, 해당 token으로 사용자 정보를 조회하여
     * 플랫폼이 부여한 사용자 식별자를 {@link UserInfoDto}로 반환한다.
     *
     * @param code  플랫폼 로그인 후 리다이렉트로 전달받은 인가 코드
     * @return      플랫폼 사용자 식별자를 담은 DTO
     */
    public UserInfoDto fetchUserInfoFromPlatform(String code) {
        String accessToken = oAuthClient.getAccessToken(oAuthProperties.tokenUrl(), getAccessTokenParams(code), getAccessTokenResponseType());
        String id = oAuthClient.getUserIdentifier(oAuthProperties.userInfoUrl(), accessToken, getUserInfoResponseType());
        return UserInfoDto.of(id);
    }
    
    /**
     * access token 발급 요청에 사용할 파라미터를 생성하는 메서드
     *
     * @param code  플랫폼으로부터 전달받은 인가 코드
     * @return      token 요청 파라미터
     */
    public MultiValueMap<String, String> getAccessTokenParams(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", oAuthProperties.clientId());
        params.add("client_secret", oAuthProperties.clientSecret());
        params.add("redirect_uri", oAuthProperties.redirectUri());
        params.add("code", code);
        
        return params;
    }
    
    /**
     * access token 응답 본문을 역직렬화할 타입을 반환하는 메서드
     *
     * <p>
     * 플랫폼마다 응답 스키마가 다르므로 하위 클래스에서 해당 플랫폼 전용
     * {@link AccessTokenResponse} 구현 타입을 지정한다.
     *
     * @return  access token 응답 매핑 타입
     */
    protected abstract Class<? extends AccessTokenResponse> getAccessTokenResponseType();

    /**
     * 사용자 정보 응답 본문을 역직렬화할 타입을 반환하는 메서드
     *
     * <p>
     * 플랫폼마다 응답 스키마가 다르므로 하위 클래스에서 해당 플랫폼 전용
     * {@link UserInfoResponse} 구현 타입을 지정한다.
     *
     * @return  사용자 정보 응답 매핑 타입
     */
    protected abstract Class<? extends UserInfoResponse> getUserInfoResponseType();
}