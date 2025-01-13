package muzusi.infrastructure.kis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum KisUrlConstant {
    ACCESS_TOKEN_ISSUE("/oauth2/tokenP"),
    WEBSOCKET_KEY_ISSUE("/oauth2/Approval"),
    VOLUME_RANK("/uapi/domestic-stock/v1/quotations/volume-rank"),
    FLUCTUATION("/uapi/domestic-stock/v1/ranking/fluctuation"),
    INQUIRE_PRICE("/uapi/domestic-stock/v1/quotations/inquire-price"),
    ;

    private final String url;
}
