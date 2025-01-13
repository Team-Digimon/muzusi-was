package muzusi.application.stock.dto;

import lombok.Builder;

@Builder
public record FluctuationRankStockDto(
        /* 종목 이름 */
        String name,
        /* 고유 식별 번호*/
        Long id,
        /* 종목 코드 */
        int code,
        /* 랭킹 */
        int rank,
        /* 현재가 */
        Long price,
        /* 등락 금액 */
        Long prdyVrss,
        /* 등락률 */
        int prdyCtrt,
        /* 거래량 */
        Long avrgVol
) {
}
