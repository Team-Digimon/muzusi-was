package muzusi.domain.stock.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RankStock {
    /* 종목 이름 */
    private String name;
    /* 고유 식별 번호*/
    private Long id;
    /* 종목 코드 */
    private int code;
    /* 랭킹 */
    private int rank;
    /* 현재가 */
    private Long price;
    /* 등락 금액 */
    private Long prdy_vrss;
    /* 등락률 */
    private int prdy_ctrt;
    /* 거래량 */
    private Long avrg_vol;
    /* 누적 거래 대금 */
    private Long acml_tr_pbmn;

    @Builder
    public RankStock(String name, Long id, int code, int rank, Long price, Long prdy_vrss, int prdy_ctrt, Long avrg_vol, Long acml_tr_pbmn) {
        this.name = name;
        this.id = id;
        this.code = code;
        this.rank = rank;
        this.price = price;
        this.prdy_vrss = prdy_vrss;
        this.prdy_ctrt = prdy_ctrt;
        this.avrg_vol = avrg_vol;
        this.acml_tr_pbmn = acml_tr_pbmn;
    }
}
