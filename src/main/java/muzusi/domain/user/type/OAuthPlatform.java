package muzusi.domain.user.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuthPlatform {
    KAKAO("KAKAO"),
    NAVER("NAVER");

    private final String name;
}

