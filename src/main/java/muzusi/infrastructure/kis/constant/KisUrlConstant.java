package muzusi.infrastructure.kis.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum KisUrlConstant {
    ACCESS_TOKEN_ISSUE("/oauth2/tokenP"),
    WEBSOCKET_KEY_ISSUE("/oauth2/Approval"),
    VOLUME_RANK("/uapi/domestic-stock/v1/quotations/volume-rank"),
    FLUCTUATION_RANK("/uapi/domestic-stock/v1/ranking/fluctuation"),
    INQUIRE_PRICE("/uapi/domestic-stock/v1/quotations/inquire-price"),
    TIME_ITEM_CHART_PRICE("/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice"),
    ;

    private final String url;
}
