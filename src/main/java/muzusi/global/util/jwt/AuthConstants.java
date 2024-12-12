package muzusi.global.util.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthConstants {
    AUTHORIZATION("Authorization"), TOKEN_TYPE("Bearer ");

    private final String value;
}
