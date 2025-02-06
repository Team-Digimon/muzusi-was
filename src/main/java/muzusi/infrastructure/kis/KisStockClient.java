package muzusi.infrastructure.kis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.application.kis.dto.KisAuthDto;
import muzusi.application.stock.dto.StockMinutesChartInfoDto;
import muzusi.global.exception.KisApiException;
import muzusi.global.redis.RedisService;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class KisStockClient {
    private final KisProperties kisProperties;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;
    private static final int MINUTES_GAP = 10;

    public StockMinutesChartInfoDto getStockMinutesChartInfo(String code, LocalDateTime time) {
        HttpHeaders headers = new HttpHeaders();
        KisAuthDto.AccessToken accessToken = (KisAuthDto.AccessToken) redisService.get(KisConstant.ACCESS_TOKEN_PREFIX.getValue());
        headers.add("authorization", accessToken.getValue());
        headers.add("appkey", kisProperties.getAppKey());
        headers.add("appsecret", kisProperties.getAppSecret());
        headers.add("tr_id", "FHKST03010200");

        String uri = UriComponentsBuilder.fromUriString(kisProperties.getUrl(KisUrlConstant.TIME_ITEM_CHART_PRICE))
                .queryParam("FID_ETC_CLS_CODE", "")
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_INPUT_ISCD", code)
                .queryParam("FID_INPUT_HOUR_1", time.format(DateTimeFormatter.ofPattern("HHmmss")))
                .queryParam("FID_PW_DATA_INCU_YN", "N")
                .build().toUriString();

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
            JsonNode output2 = rootNode.get("output2");

            long high = Long.MIN_VALUE, low = Long.MAX_VALUE;
            long open = 0L, close = 0L, volume = 0L;

            for (int i = 0 ; i < Math.min(MINUTES_GAP, output2.size()); i++) {
                JsonNode stockInfo = output2.get(i);
                if (i == 0) {
                    close = stockInfo.get("stck_prpr").asLong();
                }
                if (i == Math.min(MINUTES_GAP, output2.size())-1) {
                    open = stockInfo.get("stck_prpr").asLong();
                }
                low = Math.min(low, stockInfo.get("stck_prpr").asLong());
                high = Math.max(high, stockInfo.get("stck_prpr").asLong());
                volume += stockInfo.get("cntg_vol").asLong();
            }

            return StockMinutesChartInfoDto.builder()
                    .stockCode(code)
                    .start(time.minusMinutes(MINUTES_GAP))
                    .end(time)
                    .open(open)
                    .close(close)
                    .high(high)
                    .low(low)
                    .volume(volume)
                    .build();
        } catch (Exception e) {
            throw new KisApiException(e);
        }
    }
}