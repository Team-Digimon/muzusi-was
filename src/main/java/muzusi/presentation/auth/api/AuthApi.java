package muzusi.presentation.auth.api;

import api.link.checker.annotation.ApiGroup;
import api.link.checker.annotation.TrackApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import muzusi.application.auth.dto.OAuthCodeDto;
import muzusi.application.auth.dto.SignUpDto;
import muzusi.domain.user.type.OAuthPlatform;
import muzusi.global.security.auth.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@ApiGroup(value = "[인증 API]")
@Tag(name = "[인증 API]", description = "인증 관련 API")
public interface AuthApi {

    @TrackApi(description = "회원가입")
    @Operation(summary = "회원가입", description = "최초 로그인 시 회원 정보를 추가하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": {
                                                "accessToken": "<access token>"
                                            }
                                        }
                                    """)
                    })),
            @ApiResponse(responseCode = "422", description = "유효성 검사 실패",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "nickname": "2~8자의 한글, 영문, 숫자(공백, 특수문자 제외)"
                                        }
                                    """)
                    })),
    })
    ResponseEntity<?> signUp(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @Valid @RequestBody SignUpDto signUpDto);

    @TrackApi(description = "소셜 로그인")
    @Operation(summary = "소셜 로그인", description = "서비스를 이용하기 위해 로그인하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "FirstLogin", value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": {
                                                "accessToken": "<access token>",
                                                "isRegistered": false
                                            }
                                        }
                                    """),
                            @ExampleObject(name = "ReturningLogin", value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": {
                                                "accessToken": "<access token>",
                                                "isRegistered": true
                                            }
                                        }
                                    """)
                    }))
    })
    ResponseEntity<?> signIn(@RequestBody OAuthCodeDto oAuthCodeDto,
                             @PathVariable OAuthPlatform platform);

    @TrackApi(description = "토큰 재발급")
    @Operation(summary = "accessToken 재발급", description = "서버 인증을 위한 accessToken 재발급을 위한 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다.",
                                            "data": {
                                                "accessToken": "<access token>"
                                            }
                                        }
                                    """)
                    })),
            @ApiResponse(responseCode = "404", description = "RefreshToken 존재 x ",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": "0008",
                                            "message": "Refresh Token이 존재하지 않습니다."
                                        }
                                    """)
                    }))
    })
    ResponseEntity<?> reIssueToken(@CookieValue(name = "refreshToken", required = false) String refreshToken);

    @TrackApi(description = "로그아웃")
    @Operation(summary = "로그아웃", description = "사용자가 로그아웃하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = """
                                        {
                                            "code": 200,
                                            "message": "요청이 성공하였습니다."
                                        }
                                    """)
                    }))
    })
    ResponseEntity<?> signOut(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                              HttpServletResponse response);
}
