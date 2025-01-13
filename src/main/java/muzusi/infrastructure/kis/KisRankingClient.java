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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisRankingClient {
    private final KisProperties kisProperties;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;
    private final StockService stockService;

    public List<RankStockDto> getVolumeRank() {
        HttpHeaders headers = getHttpHeaders();

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

            return getRankStocks(rootNode.get("output"));
        } catch (Exception e) {
            log.error("[KIS ERROR] " + e.getMessage());
            return null;
        }
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        KisAuthDto.AccessToken accessToken = (KisAuthDto.AccessToken) redisService.get(KisConstant.ACCESS_TOKEN_PREFIX.getValue());
        headers.add("authorization", accessToken.getValue());
        headers.add("appkey", kisProperties.getAppKey());
        headers.add("appsecret", kisProperties.getAppSecret());
        headers.add("tr_id", "FHPST01710000");
        headers.add("custtype", "P");

        return headers;
    }

    private List<RankStockDto> getRankStocks(JsonNode node) {
        List<RankStockDto> rankStockDtos = new ArrayList<>();

        for(JsonNode n : node) {
            Optional<Long> stockId = stockService.readByStockName(n.get("hts_kor_isnm").asText())
                            .map(stock -> stock.getId());

            if(stockId.isPresent()) {
                RankStockDto rankStockDto = RankStockDto.builder()
                        .id(stockId.get())
                        .name(n.get("hts_kor_isnm").asText())
                        .code(n.get("mksc_shrn_iscd").asInt())
                        .rank(n.get("data_rank").asInt())
                        .price(n.get("stck_prpr").asLong())
                        .prdyVrss(n.get("prdy_vrss").asLong())
                        .prdyCtrt(n.get("prdy_ctrt").asInt())
                        .avrgVol(n.get("avrg_vol").asLong())
                        .acmlTrPbmn(n.get("acml_tr_pbmn").asLong())
                        .build();

                rankStockDtos.add(rankStockDto);
            } else {
                log.error("[KIS ERROR] " + n.get("hts_kor_isnm").asText() + "는 데이터베이스 상 존재하지 않는 주식입니다.");
            }
        }
        return rankStockDtos;
    }
}