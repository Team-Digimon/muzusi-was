package muzusi.infrastructure.stockquote.websocket.kis;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.trade.dto.TradeNotificationDto;
import muzusi.application.websocket.service.TradeNotificationPublisher;
import muzusi.domain.trade.type.TradeType;
import muzusi.infrastructure.kis.websocket.dto.KisWebSocketResponseDto;
import muzusi.infrastructure.kis.websocket.handler.KisWebSocketHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisStockQuoteWebSocketHandler implements KisWebSocketHandler {
    private final TradeNotificationPublisher tradeNotificationPublisher;

    private static final String TR_ID = "H0STCNT0";
    private static final int FIELD_COUNT = 46;
    
    /**
     * 해당 핸들러가 처리하는 웹소켓 항목의 TR_ID를 반환하는 메서드
     *
     * @return  실시간 체결가 조회 TR_ID({@value #TR_ID})
     */
    @Override
    public String getTrId() {
        return TR_ID;
    }

    /**
     * 한국투자증권 실시간 체결가 응답을 파싱해 알림을 발행하는 메서드
     *
     * <p> 응답 데이터 파싱 또는 변환 중 예외가 발생하면 알림 발행 없이 로그만 남기고 종료한다.
     * <p> 체결 구분({@link Response#parseTradeType()})을 판별할 수 없는 데이터는 알림 대상에서 제외한다.
     *
     * @param response  한국투자증권 웹소켓 실시간 응답 DTO
     */
    @Override
    public void handle(KisWebSocketResponseDto response) {
        int count = response.dataCount();
        String data = response.data();
        List<TradeNotificationDto> tradeNotifications = new ArrayList<>();
        
        try {
            List<Response> responses = parseResponse(count, data);
            tradeNotifications = responses.stream()
                .map(res -> TradeNotificationDto.builder()
                        .stockCode(res.MKSC_SHRN_ISCD())
                        .time(convertTime(res.STCK_CNTG_HOUR()))
                        .price(res.STCK_PRPR())
                        .contingentVolume(res.CNTG_VOL())
                        .accumulatedVolume(res.ACML_VOL())
                        .tradeType(res.parseTradeType())
                        .changeRate(res.PRDY_CTRT())
                        .build())
                .filter(tradeNotification -> tradeNotification.tradeType() != null)
                .toList();
        } catch (Exception e) {
            log.error("[Error] Failed to parse KIS stock quote response - data: {}, message: {}", response.data(), e.getMessage());
            return;
        }

        tradeNotificationPublisher.publishTradeNotification(tradeNotifications);
    }

    /**
     * HHmmss 형식의 시간 문자열을 HH:mm:ss 형식으로 변환하는 메서드
     *
     * @param time  HHmmss 형식의 시간 문자열
     * @return      HH:mm:ss 형식으로 변환된 시간 문자열
     */
    private String convertTime(String time) {
        return time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);
    }

    /**
     * 실시간 체결가 응답 데이터({@code data})를 {@link Response} 목록으로 파싱하는 메서드
     *
     * <p> 데이터는 {@code ^} 로 구분된 {@value #FIELD_COUNT}개 필드가 {@code count}건 만큼 반복되는 형식이다.
     *
     * @param count 응답에 포함된 데이터 건수
     * @param data  {@code ^} 로 구분된 실시간 체결가 데이터
     * @return      파싱된 {@link Response} 목록
     */
    private List<Response> parseResponse(int count, String data) {
        String[] items = data.split("\\^", -1);
        List<Response> responses = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int offset = i * FIELD_COUNT;

            responses.add(Response.builder()
                    .MKSC_SHRN_ISCD(items[offset])
                    .STCK_CNTG_HOUR(items[offset + 1])
                    .STCK_PRPR(parseLong(items[offset + 2]))
                    .PRDY_VRSS_SIGN(items[offset + 3])
                    .PRDY_VRSS(parseInteger(items[offset + 4]))
                    .PRDY_CTRT(parseDouble(items[offset + 5]))
                    .WGHN_AVRG_STCK_PRC(parseDouble(items[offset + 6]))
                    .STCK_OPRC(parseLong(items[offset + 7]))
                    .STCK_HGPR(parseLong(items[offset + 8]))
                    .STCK_LWPR(parseLong(items[offset + 9]))
                    .ASKP1(parseLong(items[offset + 10]))
                    .BIDP1(parseLong(items[offset + 11]))
                    .CNTG_VOL(parseLong(items[offset + 12]))
                    .ACML_VOL(parseLong(items[offset + 13]))
                    .ACML_TR_PBMN(parseLong(items[offset + 14]))
                    .SELN_CNTG_CSNU(parseInteger(items[offset + 15]))
                    .SHNU_CNTG_CSNU(parseInteger(items[offset + 16]))
                    .NTBY_CNTG_CSNU(parseInteger(items[offset + 17]))
                    .CTTR(parseDouble(items[offset + 18]))
                    .SELN_CNTG_SMTN(parseInteger(items[offset + 19]))
                    .SHNU_CNTG_SMTN(parseInteger(items[offset + 20]))
                    .CCLD_DVSN(items[offset + 21])
                    .SHNU_RATE(parseDouble(items[offset + 22]))
                    .PRDY_VOL_VRSS_ACML_VOL_RATE(parseDouble(items[offset + 23]))
                    .OPRC_HOUR(items[offset + 24])
                    .OPRC_VRSS_PRPR_SIGN(items[offset + 25])
                    .OPRC_VRSS_PRPR(parseInteger(items[offset + 26]))
                    .HGPR_HOUR(items[offset + 27])
                    .HGPR_VRSS_PRPR_SIGN(items[offset + 28])
                    .HGPR_VRSS_PRPR(parseInteger(items[offset + 29]))
                    .LWPR_HOUR(items[offset + 30])
                    .LWPR_VRSS_PRPR_SIGN(items[offset + 31])
                    .LWPR_VRSS_PRPR(parseInteger(items[offset + 32]))
                    .BSOP_DATE(items[offset + 33])
                    .NEW_MKOP_CLS_CODE(items[offset + 34])
                    .TRHT_YN(items[offset + 35])
                    .ASKP_RSQN1(parseLong(items[offset + 36]))
                    .BIDP_RSQN1(parseLong(items[offset + 37]))
                    .TOTAL_ASKP_RSQN(parseLong(items[offset + 38]))
                    .TOTAL_BIDP_RSQN(parseLong(items[offset + 39]))
                    .VOL_TNRT(parseDouble(items[offset + 40]))
                    .PRDY_SMNS_HOUR_ACML_VOL(parseLong(items[offset + 41]))
                    .PRDY_SMNS_HOUR_ACML_VOL_RATE(parseDouble(items[offset + 42]))
                    .HOUR_CLS_CODE(items[offset + 43])
                    .MRKT_TRTM_CLS_CODE(items[offset + 44])
                    .VI_STND_PRC(parseLong(items[offset + 45]))
                    .build());
        }

        return responses;
    }

    /**
     * 문자열을 {@link Long}으로 변환하는 메서드
     *
     * @param value 변환할 문자열
     * @return      변환된 {@link Long} 값, {@code value}가 {@code null}이거나 공백이면 {@code null}
     */
    private Long parseLong(String value) {
        return (value == null || value.isBlank()) ? null : Long.valueOf(value);
    }

    /**
     * 문자열을 {@link Integer}로 변환하는 메서드
     *
     * @param value 변환할 문자열
     * @return      변환된 {@link Integer} 값, {@code value}가 {@code null}이거나 공백이면 {@code null}
     */
    private Integer parseInteger(String value) {
        return (value == null || value.isBlank()) ? null : Integer.valueOf(value);
    }

    /**
     * 문자열을 {@link Double}로 변환하는 메서드
     *
     * @param value 변환할 문자열
     * @return      변환된 {@link Double} 값, {@code value}가 {@code null}이거나 공백이면 {@code null}
     */
    private Double parseDouble(String value) {
        return (value == null || value.isBlank()) ? null : Double.valueOf(value);
    }
    
    /**
     * 한국투자증권 실시간 체결가 웹소켓 응답 필드
     *
     * @param MKSC_SHRN_ISCD                    유가증권 단축 종목코드
     * @param STCK_CNTG_HOUR                    주식 체결 시간
     * @param STCK_PRPR                         주식 현재가
     * @param PRDY_VRSS_SIGN                    전일 대비 부호 (1: 상한, 2: 상승, 3: 보합, 4: 하한, 5: 하락)
     * @param PRDY_VRSS                         전일 대비
     * @param PRDY_CTRT                         전일 대비율
     * @param WGHN_AVRG_STCK_PRC                가중평균 주식 가격
     * @param STCK_OPRC                         주식 시가
     * @param STCK_HGPR                         주식 최고가
     * @param STCK_LWPR                         주식 최저가
     * @param ASKP1                             매도호가1
     * @param BIDP1                             매수호가1
     * @param CNTG_VOL                          체결 거래량
     * @param ACML_VOL                          누적 거래량
     * @param ACML_TR_PBMN                      누적 거래대금
     * @param SELN_CNTG_CSNU                    매도 체결 건수
     * @param SHNU_CNTG_CSNU                    매수 체결 건수
     * @param NTBY_CNTG_CSNU                    순매수 체결 건수
     * @param CTTR                              체결 강도
     * @param SELN_CNTG_SMTN                    총 매도 수량
     * @param SHNU_CNTG_SMTN                    총 매수 수량
     * @param CCLD_DVSN                         체결 구분 (1: 매수(+), 3: 장전, 5: 매도(-))
     * @param SHNU_RATE                         매수 비율
     * @param PRDY_VOL_VRSS_ACML_VOL_RATE       전일 거래량 대비 누적 거래량 비율
     * @param OPRC_HOUR                         시가 시간
     * @param OPRC_VRSS_PRPR_SIGN               시가대비 구분 (1: 상한, 2: 상승, 3: 보합, 4: 하한, 5: 하락)
     * @param OPRC_VRSS_PRPR                    시가 대비
     * @param HGPR_HOUR                         최고가 시간
     * @param HGPR_VRSS_PRPR_SIGN               고가 대비 구분 (1: 상한, 2: 상승, 3: 보합, 4: 하한, 5: 하락)
     * @param HGPR_VRSS_PRPR                    고가 대비
     * @param LWPR_HOUR                         최저가 시간
     * @param LWPR_VRSS_PRPR_SIGN               저가 대비 구분 (1: 상한, 2: 상승, 3: 보합, 4: 하한, 5: 하락)
     * @param LWPR_VRSS_PRPR                    저가 대비
     * @param BSOP_DATE                         영업 일자
     * @param NEW_MKOP_CLS_CODE                 신 장 운영 구분 코드
     *                                          <p> - 첫번째 비트 (1: 장 개시 전, 2: 장 중, 3: 장 종료 후, 4: 시간외 단일가, 7: 일반 Buy-in, 8: 당일 Buy-in)
     *                                          <p> - 두번째 비트 (0: 보통, 1: 종가, 2: 대량, 3: 바스켓: 7: 정리매매: 8: Buy-in)
     * @param TRHT_YN                           거래 정지 여부 (Y: 정지, N: 정상거래)
     * @param ASKP_RSQN1                        매도호가 잔량1
     * @param BIDP_RSQN1                        매수호가 잔량1
     * @param TOTAL_ASKP_RSQN                   총 매도호가 잔량
     * @param TOTAL_BIDP_RSQN                   총 매수호가 잔량
     * @param VOL_TNRT                          거래량 회전율
     * @param PRDY_SMNS_HOUR_ACML_VOL           전일 동시간 누적 거래량
     * @param PRDY_SMNS_HOUR_ACML_VOL_RATE      전일 동시간 누적 거래량 비율
     * @param HOUR_CLS_CODE                     시간 구분 코드 (0: 장 중, A: 장후 예상: B: 장전 예상, C: 9시 이후의 예상가/VI 발동, D: 시간외 단일가 예상)
     * @param MRKT_TRTM_CLS_CODE                임의 종료 구분 코드
     * @param VI_STND_PRC                       정적VI발동기준가
     */
    @Builder
    private record Response(
            String MKSC_SHRN_ISCD,
            String STCK_CNTG_HOUR,
            Long STCK_PRPR,
            String PRDY_VRSS_SIGN,
            Integer PRDY_VRSS,
            Double PRDY_CTRT,
            Double WGHN_AVRG_STCK_PRC,
            Long STCK_OPRC,
            Long STCK_HGPR,
            Long STCK_LWPR,
            Long ASKP1,
            Long BIDP1,
            Long CNTG_VOL,
            Long ACML_VOL,
            Long ACML_TR_PBMN,
            Integer SELN_CNTG_CSNU,
            Integer SHNU_CNTG_CSNU,
            Integer NTBY_CNTG_CSNU,
            Double CTTR,
            Integer SELN_CNTG_SMTN,
            Integer SHNU_CNTG_SMTN,
            String CCLD_DVSN,
            Double SHNU_RATE,
            Double PRDY_VOL_VRSS_ACML_VOL_RATE,
            String OPRC_HOUR,
            String OPRC_VRSS_PRPR_SIGN,
            Integer OPRC_VRSS_PRPR,
            String HGPR_HOUR,
            String HGPR_VRSS_PRPR_SIGN,
            Integer HGPR_VRSS_PRPR,
            String LWPR_HOUR,
            String LWPR_VRSS_PRPR_SIGN,
            Integer LWPR_VRSS_PRPR,
            String BSOP_DATE,
            String NEW_MKOP_CLS_CODE,
            String TRHT_YN,
            Long ASKP_RSQN1,
            Long BIDP_RSQN1,
            Long TOTAL_ASKP_RSQN,
            Long TOTAL_BIDP_RSQN,
            Double VOL_TNRT,
            Long PRDY_SMNS_HOUR_ACML_VOL,
            Double PRDY_SMNS_HOUR_ACML_VOL_RATE,
            String HOUR_CLS_CODE,
            String MRKT_TRTM_CLS_CODE,
            Long VI_STND_PRC
    ) {
        
        /**
         * 체결 구분({@link #CCLD_DVSN}) 값을 {@link TradeType}으로 변환하는 메서드
         *
         * <p> {@code CCLD_DVSN}이 매수/매도(1, 5)가 아닌 값(예: 장전 3)인 경우 판별할 수 없으므로 {@code null}을 반환한다.
         *
         * @return  변환된 {@link TradeType}, 매수/매도로 판별할 수 없으면 {@code null}
         */
        public TradeType parseTradeType() {
            if (CCLD_DVSN.equals("1")) return TradeType.BUY;
            if (CCLD_DVSN.equals("5")) return TradeType.SELL;
            return null;
        }
    }
}
