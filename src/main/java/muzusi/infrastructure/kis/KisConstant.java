package muzusi.infrastructure.kis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisConstant {
    ACCESS_TOKEN_PREFIX("kis:access-token"),
    WEBSOCKET_KEY_PREFIX("kis:websocket-key"),
    VOLUME_RANK_PREFIX("kis:volume-rank"),
    ;

    private final String value;
}
