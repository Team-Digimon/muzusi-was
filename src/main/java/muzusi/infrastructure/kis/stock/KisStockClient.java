package muzusi.infrastructure.kis.stock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.application.stock.dto.StockChartInfoDto;
import muzusi.global.exception.KisApiException;
import muzusi.infrastructure.kis.KisRequestFactory;
import muzusi.infrastructure.kis.constant.KisUrlConstant;
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
    private final KisRequestFactory kisRequestFactory;
    private static final int MINUTES_GAP = 10;
    private static final String MINUTES_CHART_TR_ID = "FHKST03010200";
    private static final String INQUIRE_PRICE_TR_ID = "FHKST01010100";

    public StockChartInfoDto getStockMinutesChartInfo(String code, LocalDateTime time) {
        HttpHeaders headers = kisRequestFactory.getHttpHeader(MINUTES_CHART_TR_ID);

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

            for (int i = 0; i < Math.min(MINUTES_GAP, output2.size()); i++) {
                JsonNode stockInfo = output2.get(i);
                if (i == 0) {
                    close = stockInfo.get("stck_prpr").asLong();
                }
                if (i == Math.min(MINUTES_GAP, output2.size()) - 1) {
                    open = stockInfo.get("stck_prpr").asLong();
                }
                low = Math.min(low, stockInfo.get("stck_prpr").asLong());
                high = Math.max(high, stockInfo.get("stck_prpr").asLong());
                volume += stockInfo.get("cntg_vol").asLong();
            }

            return StockChartInfoDto.builder()
                    .stockCode(code)
                    .date(time)
                    .low(low)
                    .high(high)
                    .open(open)
                    .close(close)
                    .volume(volume)
                    .build();
        } catch (Exception e) {
            throw new KisApiException(e);
        }
    }

    public Long getStockInquirePrice(String stockCode) {
        HttpHeaders headers = kisRequestFactory.getHttpHeader(INQUIRE_PRICE_TR_ID);

        String uri = UriComponentsBuilder.fromUriString(kisProperties.getUrl(KisUrlConstant.INQUIRE_PRICE))
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_INPUT_ISCD", stockCode)
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
            JsonNode output = rootNode.get("output");

            return output.get("stck_prpr").asLong();
        } catch (Exception e) {
            throw new KisApiException(e);
        }
    }
}