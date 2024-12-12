package muzusi.global.util.jwt;

public class JwtUtil {

    /**
     * Token TYPE("Bearer ") 파싱 메서드
     * @param token : 토큰
     * @return : TYPE 파싱 토큰
     */
    public static String resolveToken(String token) {
        return token.substring(AuthConstants.TOKEN_TYPE.getValue().length()).trim();
    }
}
