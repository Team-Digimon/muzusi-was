package muzusi.domain.stock.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MarketType {
    KOSPI("코스피"),
    KOSDAQ("코스닥"),
    ;
    
    private final String value;
}
