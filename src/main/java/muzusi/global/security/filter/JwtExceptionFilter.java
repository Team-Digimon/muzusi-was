package muzusi.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.global.response.error.ErrorResponse;
import muzusi.global.response.error.type.BaseErrorType;
import muzusi.global.response.error.type.CommonErrorType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            handleExceptionToken(response, CommonErrorType.INVALID_ACCESS_TOKEN);
        }
    }

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
