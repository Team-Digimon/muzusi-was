package muzusi.application.stock.dto;

import lombok.Builder;

@Builder
public record RankStockDto(
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
        Long avrgVol,
        /* 누적 거래 대금 */
        Long acmlTrPbmn
) {
    public static RankStockDto of(FluctuationRankStockDto fluctuationRankStockDto, String tradingValue) {
        return RankStockDto.builder()
                .id(fluctuationRankStockDto.id())
                .name(fluctuationRankStockDto.name())
                .code(fluctuationRankStockDto.code())
                .rank(fluctuationRankStockDto.rank())
                .price(fluctuationRankStockDto.price())
                .prdyVrss(fluctuationRankStockDto.prdyVrss())
                .prdyCtrt(fluctuationRankStockDto.prdyCtrt())
                .avrgVol(fluctuationRankStockDto.avrgVol())
                .acmlTrPbmn(Long.valueOf(tradingValue))
                .build();
    }
}