package muzusi.presentation.auth.Controller;

import lombok.RequiredArgsConstructor;
import muzusi.application.auth.dto.OAuthCodeDto;
import muzusi.application.auth.service.AuthService;
import muzusi.domain.user.type.OAuthPlatform;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-in/{platform}")
    public ResponseEntity<?> signIn(@RequestBody OAuthCodeDto oAuthCodeDto,
                                    @PathVariable OAuthPlatform platform) {
        authService.signIn(platform, oAuthCodeDto.code());

        return ResponseEntity.ok("로그인 성공");
    }
}
