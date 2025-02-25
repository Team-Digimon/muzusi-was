package muzusi.infrastructure.kis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.StockRankDto;
import muzusi.global.exception.KisApiException;
import muzusi.infrastructure.kis.constant.KisUrlConstant;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KisRankingClient {
    private final KisProperties kisProperties;
    private final ObjectMapper objectMapper;
    private final KisRequestFactory kisRequestFactory;
    private final static String VOLUME_RANK_TR_ID = "FHPST01710000";
    private final static String FLUCTUATION_RANK_TR_ID = "FHPST01700000";

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
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestInfo,
                    String.class
            );

            JsonNode rootNode = objectMapper.readTree(response.getBody());

            Map<String, String> body = Map.of(
                    "name", "hts_kor_isnm",
                    "code", "mksc_shrn_iscd",
                    "rank", "data_rank",
                    "price", "stck_prpr",
                    "prdyVrss", "prdy_vrss",
                    "prdyCtrt", "prdy_ctrt",
                    "avgrVol", "avrg_vol"
            );

            return getRankStocks(rootNode.get("output"), body);
        } catch (Exception e) {
            throw new KisApiException(e);
        }
    }

    public List<StockRankDto> getRisingFluctuationRank() {
        return getFluctuationRank("0");
    }

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
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestInfo,
                    String.class
            );

            JsonNode rootNode = objectMapper.readTree(response.getBody());

             Map<String, String> body = Map.of(
                     "name", "hts_kor_isnm",
                     "code", "stck_shrn_iscd",
                     "rank", "data_rank",
                     "price", "stck_prpr",
                     "prdyVrss", "prdy_vrss",
                     "prdyCtrt", "prdy_ctrt",
                     "avgrVol", "acml_vol"
             );

            return getRankStocks(rootNode.get("output"), body);
        } catch (Exception e) {
            throw new KisApiException(e);
        }
    }

    private List<StockRankDto> getRankStocks(JsonNode node, Map<String, String> body) {
        List<StockRankDto> stockRankDtos = new ArrayList<>();

        for (JsonNode n : node) {
            StockRankDto stockRankDto = StockRankDto.builder()
                    .name(n.get(body.get("name")).asText())
                    .code(n.get(body.get("code")).asText())
                    .rank(n.get(body.get("rank")).asInt())
                    .price(n.get(body.get("price")).asLong())
                    .prdyVrss(n.get(body.get("prdyVrss")).asLong())
                    .prdyCtrt(n.get(body.get("prdyCtrt")).asDouble())
                    .avrgVol(n.get(body.get("avgrVol")).asLong())
                    .build();

            stockRankDtos.add(stockRankDto);
        }
        return stockRankDtos;
    }
}