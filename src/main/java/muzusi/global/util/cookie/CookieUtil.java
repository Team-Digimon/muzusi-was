package muzusi.global.util.cookie;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    /**
     * 쿠키를 생성하기 위한 메서드
     *
     * @param key : 생성할 쿠키 이름
     * @param value : 생성할 쿠키 값
     * @param maxAge : 쿠키 만료 시
     * @return : 생성된 쿠키
     */
    public static ResponseCookie createCookie(String key, String value, long maxAge) {
        return ResponseCookie.from(key, value)
                .path("/")
                .httpOnly(true)
                .maxAge(maxAge)
                .secure(true)
                .sameSite("None")
                .build();
    }

    /**
     * 쿠키를 삭제하기 위한 메서드
     *
     * @param cookieName : 삭제할 쿠키 이름
     * @param response : 삭제할 쿠키의 Http 서블릿
     */
    public static void deleteCookie(String cookieName, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .secure(true)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}