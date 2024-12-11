package muzusi.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.global.response.error.ErrorResponse;
import muzusi.global.response.error.type.BaseErrorType;
import muzusi.global.response.error.type.CommonErrorType;
import muzusi.global.security.jwt.JwtProvider;
import muzusi.global.util.jwt.AuthConstants;
import muzusi.global.util.jwt.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authToken == null || !authToken.startsWith(AuthConstants.TOKEN_TYPE.getValue())) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveAccessToken(response, authToken);

        if (accessToken.isEmpty())
            return;

        Authentication auth = getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    /**
     * 토큰 파싱 및 유효성 검사 메서드
     *
     * @throws : 토큰이 만료되었다면 예외 발생
     */
    private String resolveAccessToken(HttpServletResponse response, String authToken) throws IOException {
        String accessToken = JwtUtil.resolveToken(authToken);

        if (jwtProvider.isExpired(accessToken)) {
            handleExceptionToken(response, CommonErrorType.ACCESS_TOKEN_EXPIRED);
            return "";
        }

        return accessToken;
    }

    /**
     * SecurityContextHolder에 UserDetails를 등록하는 메서드
     */
    private Authentication getAuthentication(String token) {
        String userId = String.valueOf(jwtProvider.getUserId(token));

        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    /**
     * Jwt 인증 과정 중, 예외가 발생했을 때 예외를 처리하는 메서드
     */
    private void handleExceptionToken(HttpServletResponse response, BaseErrorType errorType) throws IOException {
        ErrorResponse error = ErrorResponse.from(errorType);
        String messageBody = objectMapper.writeValueAsString(error);

        log.error("[Error occurred] {}", error.message());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(messageBody);
    }
}