package muzusi.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        String accessToken = resolveAccessToken(authToken);

        Authentication auth = getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    /**
     * 토큰 파싱 및 유효성 검사 메서드
     *
     * @throws : 토큰이 만료되었다면 예외 발생
     */
    private String resolveAccessToken(String authToken) {
        String accessToken = JwtUtil.resolveToken(authToken);

        if (jwtProvider.isExpired(accessToken)) {
            throw new ExpiredJwtException(null, null, CommonErrorType.ACCESS_TOKEN_EXPIRED.getMessage());
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
}