package muzusi.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoder;
import io.jsonwebtoken.io.Encoder;
import muzusi.global.security.jwt.base64.CustomBase64UrlDecoder;
import muzusi.global.security.jwt.base64.CustomBase64UrlEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {
    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;
    private final String issuer;

    private final Encoder<OutputStream, OutputStream> base64UrlEncoder = new CustomBase64UrlEncoder();
    private final Decoder<InputStream, InputStream> base64UrlDecoder = new CustomBase64UrlDecoder();

    public JwtProvider(@Value("${jwt.secret}") String SECRET_KEY,
                       @Value("${jwt.access-expiration}") long accessExpiration,
                       @Value("${jwt.refresh-expiration}") long refreshExpiration,
                       @Value("${jwt.issuer}") String issuer) {
        this.secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.issuer = issuer;
    }

    /**
     * accessToken 생성 메서드
     *
     * @param username : Token payload에 담을 정보
     * @param userId : Token payload에 담을 정보
     * @return : accessToken
     */
    public String generateAccessToken(String username, String nickname, Long userId) {

        return createJwt(createClaims(userId, nickname), username, accessExpiration);
    }

    /**
     * refreshToken 생성 메서드
     *
     * @param username : Token payload에 담을 정보
     * @param userId : Token payload에 담을 정보
     * @return : refreshToken
     */
    public String generateRefreshToken(String username, String nickname, Long userId) {

        return createJwt(createClaims(userId, nickname), username, refreshExpiration);
    }

    /**
     * 토큰 payload에 담을 claim 생성 메서드
     *
     * @param userId : claim에 담을 정보
     * @return : claims
     */
    private Map<String, Object> createClaims(Long userId, String nickname) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("nickname", nickname);
        return claims;
    }

    /**
     * 토큰 생성 메서드
     *
     * @param claims : claims에 담을 정보
     * @param subject : subject에 담을 정보
     * @param expirationTime : 만료 시간
     * @return : 토큰
     */
    private String createJwt(Map<String, Object> claims, String subject, Long expirationTime) {

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .b64Url(base64UrlEncoder)
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰으로 부터 payload를 추출하는 메서드
     *
     * @param token : 토큰
     * @return : 사용자 정보
     */
    private Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .b64Url(base64UrlDecoder)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰의 만료일을 추출하는 메서드
     *
     * @param token : 토큰
     * @return : 만료일
     */
    public LocalDateTime getExpiryDate(String token) {
        return parseClaims(token).getExpiration().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * 토큰의 Payload(Subject)를 추출하는 메서드
     *
     * @param token : 토큰
     * @return : Subject
     */
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰의 Payload(Subject)를 추출하는 메서드
     *
     * @param token : 토큰
     * @return : Subject
     */
    public String getNickname(String token) {
        return parseClaims(token).get("nickname", String.class);
    }

    /**
     * 토큰의 Pauload(userId)를 추출하는 메서드
     *
     * @param token : 토큰
     * @return : userId
     */
    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    /**
     * 토큰의 만료 여부를 검사하는 메서드
     *
     * @param token : 토큰
     */
    public boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

}