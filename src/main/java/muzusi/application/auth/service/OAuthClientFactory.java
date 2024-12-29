package muzusi.application.auth.service;

import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.oauth.KakaoClient;
import muzusi.infrastructure.oauth.NaverClient;
import muzusi.infrastructure.oauth.OAuthClient;
import muzusi.domain.user.exception.UserErrorType;
import muzusi.domain.user.type.OAuthPlatform;
import muzusi.global.exception.CustomException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthClientFactory {
    private final NaverClient naverClient;
    private final KakaoClient kakaoClient;

    public OAuthClient getPlatformService(OAuthPlatform platform) {
        return switch (platform) {
            case NAVER -> naverClient;
            case KAKAO -> kakaoClient;
            default -> throw new CustomException(UserErrorType.UNSUPPORTED_SOCIAL_LOGIN);
        };
    }
}
