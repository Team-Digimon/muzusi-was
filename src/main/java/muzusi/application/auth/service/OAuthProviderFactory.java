package muzusi.application.auth.service;

import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.oauth.KakaoOAuthProvider;
import muzusi.infrastructure.oauth.NaverOAuthProvider;
import muzusi.infrastructure.oauth.OAuthProvider;
import muzusi.domain.user.exception.UserErrorType;
import muzusi.domain.user.type.OAuthPlatform;
import muzusi.global.exception.CustomException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthProviderFactory {
    private final NaverOAuthProvider naverOAuthProvider;
    private final KakaoOAuthProvider kakaoOAuthProvider;

    public OAuthProvider getPlatformService(OAuthPlatform platform) {
        return switch (platform) {
            case NAVER -> naverOAuthProvider;
            case KAKAO -> kakaoOAuthProvider;
            default -> throw new CustomException(UserErrorType.UNSUPPORTED_SOCIAL_LOGIN);
        };
    }
}
