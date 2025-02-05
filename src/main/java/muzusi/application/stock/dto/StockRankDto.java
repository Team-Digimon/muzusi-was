package muzusi.application.stock.dto;

import lombok.Builder;

@Builder
public record StockRankDto(
        /* 종목 이름 */
        String name,
        /* 종목 코드 */
        String code,
        /* 랭킹 */
        int rank,
        /* 현재가 */
        Long price,
        /* 등락 금액 */
        Long prdyVrss,
        /* 등락률 */
        Double prdyCtrt,
        /* 거래량 */
        Long avrgVol
) {
}