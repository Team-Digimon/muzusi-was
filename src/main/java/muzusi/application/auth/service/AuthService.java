package muzusi.application.auth.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.auth.dto.LoginDto;
import muzusi.application.auth.service.client.OAuthClient;
import muzusi.application.auth.service.client.OAuthClientFactory;
import muzusi.domain.user.type.OAuthPlatform;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final OAuthClientFactory oAuthClientFactory;

    @Transactional
    public void signIn(OAuthPlatform platform, String code) {
        OAuthClient oAuthClient = oAuthClientFactory.getPlatformService(platform);

        LoginDto loginDto = oAuthClient.login(code);
    }
}
