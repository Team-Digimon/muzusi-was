package muzusi.infrastructure.redis.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeConstant {
    RESERVATION_PREFIX("trade:reservation")
    ;

    private final String value;
}
