package muzusi.infrastructure.kis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.kis.dto.KisAuthDto;
import muzusi.application.stock.dto.RankStockDto;
import muzusi.application.stock.service.StockService;
import muzusi.global.redis.RedisService;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisRankingClient {
    private final KisProperties kisProperties;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;


    public List<RankStockDto> getVolumeRank() {
        HttpHeaders headers = getHttpHeaders("FHPST01710000");

        String uri = UriComponentsBuilder.fromUriString(kisProperties.getUrl(KisUrlConstant.VOLUME_RANK))
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_COND_SCR_DIV_CODE", "20171")
                .queryParam("FID_INPUT_ISCD", "0000")
                .queryParam("FID_DIV_CLS_CODE", "2")
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

        try{
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
                    "avrgVol", "avrg_vol"
            );

            return getRankStocks(rootNode.get("output"), body);
        } catch (Exception e) {
            log.error("[KIS ERROR] " + e.getMessage());
            return null;
        }
    }

    public List<RankStockDto> getRisingFluctuationRank() {
        return getFluctuationRank("0");
    }

    public List<RankStockDto> getFallingFluctuationRank() {
        return getFluctuationRank("1");
    }

    private List<RankStockDto> getFluctuationRank(String fluctuation) {
        HttpHeaders headers = getHttpHeaders("FHPST01700000");

        String uri = UriComponentsBuilder.fromUriString(kisProperties.getUrl(KisUrlConstant.FLUCTUATION_RANK))
                .queryParam("fid_rsfl_rate2","")
                .queryParam("fid_cond_mrkt_div_code","J")
                .queryParam("fid_cond_scr_div_code","20170")
                .queryParam("fid_input_iscd","0000")
                .queryParam("fid_rank_sort_cls_code",fluctuation)
                .queryParam("fid_input_cnt_1","1")
                .queryParam("fid_prc_cls_code","1")
                .queryParam("fid_input_price_1","")
                .queryParam("fid_input_price_2","")
                .queryParam("fid_vol_cnt","")
                .queryParam("fid_trgt_cls_code","0")
                .queryParam("fid_trgt_exls_cls_code","0")
                .queryParam("fid_div_cls_code","2")
                .queryParam("fid_rsfl_rate1","")
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
                     "avrgVol", "acml_vol"
             );

            return getRankStocks(rootNode.get("output"), body);
        } catch (Exception e) {
            log.error("[KIS ERROR] " + e.getMessage());
            return null;
        }
    }

    private HttpHeaders getHttpHeaders(String trId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        KisAuthDto.AccessToken accessToken = (KisAuthDto.AccessToken) redisService.get(KisConstant.ACCESS_TOKEN_PREFIX.getValue());
        headers.add("authorization", accessToken.getValue());
        headers.add("appkey", kisProperties.getAppKey());
        headers.add("appsecret", kisProperties.getAppSecret());
        headers.add("tr_id", trId);
        headers.add("custtype", "P");

        return headers;
    }

    private List<RankStockDto> getRankStocks(JsonNode node, Map<String, String> body){
        List<RankStockDto> rankStockDtos = new ArrayList<>();

        for (JsonNode n : node) {
            RankStockDto rankStockDto = RankStockDto.builder()
                    .name(n.get(body.get("name")).asText())
                    .code(n.get(body.get("code")).asText())
                    .rank(n.get(body.get("rank")).asInt())
                    .price(n.get(body.get("price")).asLong())
                    .prdyVrss(n.get(body.get("prdyVrss")).asLong())
                    .prdyCtrt(n.get(body.get("prdyCtrt")).asInt())
                    .avrgVol(n.get(body.get("avgrVold")).asLong())
                    .build();

            rankStockDtos.add(rankStockDto);
        }
        return rankStockDtos;
    }
}