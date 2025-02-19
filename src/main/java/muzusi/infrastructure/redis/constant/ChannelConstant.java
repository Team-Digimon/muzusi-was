package muzusi.infrastructure.redis.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChannelConstant {
    TRADE("trade"),
    ;

    private final String value;
}