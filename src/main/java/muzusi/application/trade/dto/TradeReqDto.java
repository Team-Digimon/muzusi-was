package muzusi.application.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import muzusi.domain.trade.type.TradeType;

@Schema(name = "TradeReqDto", description = "주식 매수/매도를 위한 DTO")
public record TradeReqDto(
        @Schema(description = "주식 현재 가격", example = "3000")
        @NotNull
        Long stockPrice,

        @Schema(description = "사용자가 입력한 가격", example = "3000")
        @NotNull(message = "주식 가격은 필수 입력입니다.")
        Long inputPrice,

        @Schema(description = "사용자가 입력한 매수/매도 개수", example = "3")
        @NotNull(message = "주식 개수는 필수 입력입니다.")
        Integer stockCount,

        @Schema(description = "주식 코드", example = "000610")
        @NotBlank(message = "주식 코드는 필수 정보입니다.")
        String stockCode,

        @Schema(description = "매수/매도 타입", example = "BUY or SELL")
        @NotNull(message = "매수/매도는 필수 선택입니다.")
        TradeType tradeType
) {
}
