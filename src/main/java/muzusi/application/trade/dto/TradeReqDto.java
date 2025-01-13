package muzusi.application.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import muzusi.domain.trade.type.TradeType;

public record TradeReqDto(
        @NotNull(message = "주식 가격은 필수 입력입니다.")
        Long stockPrice,
        @NotNull(message = "주식 개수는 필수 입력입니다.")
        Integer stockCount,
        @NotBlank(message = "주식 코드는 필수 정보입니다.")
        String stockCode,
        @NotNull(message = "매수/매도는 필수 선택입니다.")
        TradeType tradeType
) {
}
