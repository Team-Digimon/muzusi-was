package muzusi.infrastructure.stockranking.client.kis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import muzusi.application.stockranking.dto.StockRankDto;
import muzusi.infrastructure.kis.KisRequestFactory;
import muzusi.infrastructure.kis.aop.KisRateLimit;
import muzusi.infrastructure.kis.constant.KisUrlConstant;
import muzusi.infrastructure.kis.dto.KisResponse;
import muzusi.infrastructure.kis.exception.KisApiException;
import muzusi.infrastructure.kis.util.KisErrorParser;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KisStockRankingClient {
    private final KisProperties kisProperties;
    private final KisRequestFactory kisRequestFactory;
    private final static String VOLUME_RANK_TR_ID = "FHPST01710000";
    private final static String FLUCTUATION_RANK_TR_ID = "FHPST01700000";
    
    @KisRateLimit
    public List<StockRankDto> getVolumeRank() {
        HttpHeaders headers = kisRequestFactory.getHttpHeader(VOLUME_RANK_TR_ID);
        
        String uri = UriComponentsBuilder.fromUriString(kisProperties.getUrl(KisUrlConstant.VOLUME_RANK))
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_COND_SCR_DIV_CODE", "20171")
                .queryParam("FID_INPUT_ISCD", "0000")
                .queryParam("FID_DIV_CLS_CODE", "0")
                .queryParam("FID_BLNG_CLS_CODE", "0")
                .queryParam("FID_TRGT_CLS_CODE", "")
                .queryParam("FID_TRGT_EXLS_CLS_CODE", "")
                .queryParam("FID_INPUT_PRICE_1", "")
                .queryParam("FID_INPUT_PRICE_2", "")
                .queryParam("FID_VOL_CNT", "")
                .queryParam("FID_INPUT_DATE_1", "")
                .build()
                .toUriString();
        
        HttpEntity<String> requestInfo = new HttpEntity<>(headers);
        
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            KisVolumeRankResponse response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestInfo,
                    KisVolumeRankResponse.class
            ).getBody();
            
            KisErrorParser.validate(response);
            
            return response.output().stream()
                    .map(item -> StockRankDto.builder()
                            .name(item.name())
                            .code(item.code())
                            .rank(item.rank())
                            .price(item.price())
                            .prdyVrss(item.changeFromPreviousDay())
                            .prdyCtrt(item.changeRateFromPreviousDay())
                            .avrgVol(item.averageTradingVolume())
                            .build()
                    )
                    .toList();
        } catch (Exception e) {
            throw new KisApiException("한국투자증권 거래량 순위 API 호출 중 에러가 발생하였습니다.", e);
        }
    }
    
    @KisRateLimit
    public List<StockRankDto> getRisingFluctuationRank() {
        return getFluctuationRank("0");
    }
    
    @KisRateLimit
    public List<StockRankDto> getFallingFluctuationRank() {
        return getFluctuationRank("1");
    }
    
    private List<StockRankDto> getFluctuationRank(String fluctuation) {
        HttpHeaders headers = kisRequestFactory.getHttpHeader(FLUCTUATION_RANK_TR_ID);
        
        String uri = UriComponentsBuilder.fromUriString(kisProperties.getUrl(KisUrlConstant.FLUCTUATION_RANK))
                .queryParam("fid_rsfl_rate2", "")
                .queryParam("fid_cond_mrkt_div_code", "J")
                .queryParam("fid_cond_scr_div_code", "20170")
                .queryParam("fid_input_iscd", "0000")
                .queryParam("fid_rank_sort_cls_code", fluctuation)
                .queryParam("fid_input_cnt_1", "0")
                .queryParam("fid_prc_cls_code", "1")
                .queryParam("fid_input_price_1", "")
                .queryParam("fid_input_price_2", "")
                .queryParam("fid_vol_cnt", "")
                .queryParam("fid_trgt_cls_code", "0")
                .queryParam("fid_trgt_exls_cls_code", "0")
                .queryParam("fid_div_cls_code", "0")
                .queryParam("fid_rsfl_rate1", "")
                .build()
                .toUriString();
        
        HttpEntity<String> requestInfo = new HttpEntity<>(headers);
        
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            KisFluctuationRankResponse response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestInfo,
                    KisFluctuationRankResponse.class
            ).getBody();
            
            KisErrorParser.validate(response);
            
            return response.output().stream()
                    .map(item -> StockRankDto.builder()
                            .name(item.name())
                            .code(item.code())
                            .rank(item.rank())
                            .price(item.price())
                            .prdyVrss(item.changeFromPreviousDay())
                            .prdyCtrt(item.changeRateFromPreviousDay())
                            .avrgVol(item.accumulatedTradingVolume())
                            .build()
                    )
                    .toList();
        } catch (Exception e) {
            String type = fluctuation.equals("0") ? "급상승" : "급하락";
            throw new KisApiException("한국투자증권 %s 순위 API 호출 중 에러가 발생하였습니다.".formatted(type), e);
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KisVolumeRankResponse(
            @JsonProperty(value = "rt_cd") String rtCd,
            @JsonProperty(value = "msg_cd") String msgCd,
            @JsonProperty(value = "msg1") String msg1,
            @JsonProperty(value = "output") List<Output> output
    
    ) implements KisResponse {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Output(
                @JsonProperty(value = "hts_kor_isnm") String name,
                @JsonProperty(value = "mksc_shrn_iscd") String code,
                @JsonProperty(value = "data_rank") int rank,
                @JsonProperty(value = "stck_prpr") long price,
                @JsonProperty(value = "prdy_vrss") long changeFromPreviousDay,
                @JsonProperty(value = "prdy_ctrt") double changeRateFromPreviousDay,
                @JsonProperty(value = "avgr_vol") long averageTradingVolume,
                @JsonProperty(value = "acml_vol") long accumulatedTradingVolume
        ) {
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KisFluctuationRankResponse(
            @JsonProperty(value = "rt_cd") String rtCd,
            @JsonProperty(value = "msg_cd") String msgCd,
            @JsonProperty(value = "msg1") String msg1,
            @JsonProperty(value = "output") List<Output> output
    ) implements KisResponse {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Output(
                @JsonProperty(value = "hts_kor_isnm") String name,
                @JsonProperty(value = "stck_shrn_iscd") String code,
                @JsonProperty(value = "data_rank") int rank,
                @JsonProperty(value = "stck_prpr") long price,
                @JsonProperty(value = "prdy_vrss") long changeFromPreviousDay,
                @JsonProperty(value = "prdy_ctrt") double changeRateFromPreviousDay,
                @JsonProperty(value = "acml_vol") long accumulatedTradingVolume
        ) {
        }
    }
}