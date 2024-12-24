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
import muzusi.application.auth.dto.OAuthCodeDto;
import muzusi.domain.user.type.OAuthPlatform;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@ApiGroup(value = "[인증 API]")
@Tag(name = "[인증 API]", description = "인증 관련 API")
public interface AuthApi {

    @TrackApi(description = "소셜 로그인")
    @Operation(summary = "소셜 로그인", description = "서비스를 이용하기 위해 로그인하는 API입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "FirstLogin", value = """
                                        {
                                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0NiIsbGUiOiJPV05FUiIsImNhdGVnb3J5IjoizIiwidXNlcklkIjo2LCJpYXQiOjE3MjI2Njc1MzYsImV4cCI6MTcyMjY2OTMzNn0.9eY_1aSfKLfDhKN5X4f85N2hv_I65QOPFtq_2YXEhoA",
                                            "isRegistered": false
                                        }
                                    """),
                            @ExampleObject(name = "ReturningLogin", value = """
                                        {
                                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0NiIsInJvbGUiOiJPV05FUihdGVnb3J5IjoiYWNjZXNlcklkIjo2LCJpYXQiOjE3MjI2Njc1MzYsImV4cCI6MTcyMjY2OTMzNn0.9eY_1aSfKLfDhKN5X4f85N2hv_I65QOPFtq_2YXEhoA",
                                            "isRegistered": true
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
                                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0NiIsbGUiOiJPV05FUiIsImNhdGVnb3J5IjoizIiwidXNlcklkIjo2LCJpYXQiOjE3MjI2Njc1MzYsImV4cCI6MTcyMjY2OTMzNn0.9eY_1aSfKLfDhKN5X4f85N2hv_I65QOPFtq_2YXEhoA"
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
                                            "message": "요청이 성공하였습니다."
                                        }
                                    """)
                    }))
    })
    ResponseEntity<?> signOut(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                              HttpServletResponse response);
}
