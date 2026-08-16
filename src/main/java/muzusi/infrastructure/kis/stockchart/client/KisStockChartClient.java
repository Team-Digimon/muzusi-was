package muzusi.infrastructure.kis.stockchart.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import muzusi.application.stockchart.dto.StockChartDto;
import muzusi.global.exception.ExternalApiException;
import muzusi.global.util.datetime.DateTimeFormatterUtil;
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
public class KisStockChartClient {
    private final KisProperties kisProperties;
    private final KisRequestFactory requestFactory;
    private final ObjectMapper objectMapper;
    
    private static final String STOCK_MINUTES_CHART_TR_ID = "FHKST03010200";
    private static final DateTimeFormatter HHMMSS_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");
    
    public StockChartDto getStockMinutesChart(String stockCode, LocalDateTime time, int gap) {
        HttpHeaders headers = requestFactory.getHttpHeader(STOCK_MINUTES_CHART_TR_ID);
        
        String uri = UriComponentsBuilder.fromUriString(kisProperties.getUrl(KisUrlConstant.TIME_ITEM_CHART_PRICE))
                .queryParam("FID_ETC_CLS_CODE", "")
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_INPUT_ISCD", stockCode)
                .queryParam("FID_INPUT_HOUR_1", time.format(HHMMSS_FORMATTER))
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
            
            return parseStockChartResponse(rootNode, gap, stockCode, time);
        } catch (Exception e) {
            throw new ExternalApiException(e, "한국투자증권 당일분봉조회 API 호출 중 에러가 발생하였습니다.");
        }
    }
    
    /**
     * 한국투자증권 당일분봉조회 API 조회 결과({@code JsonNode})에서 주식 차트 정보를 파싱하는 메서드
     *
     * @param rootNode      한국투자증권 당일분봉조회 API 조회 결과
     * @param gap           분봉 차트 간격
     * @param stockCode     주식 종목 코드
     * @param time          조회 시각
     * @return              주식 차트 정보 DTO
     */
    private StockChartDto parseStockChartResponse(JsonNode rootNode, int gap, String stockCode, LocalDateTime time) {
        JsonNode output = rootNode.get("output2");
        
        if (output == null || output.isEmpty()) {
            throw new ExternalApiException("한국투자증권 당일분봉조회 API 조회 결과가 비어있습니다.");
        }
        
        int windowSize = Math.min(gap, output.size());
        long high = Long.MIN_VALUE, low = Long.MAX_VALUE, volume = 0L;
        
        for (int i = 0; i < windowSize; i++) {
            JsonNode stockChart = output.get(i);
            high = Math.max(high, stockChart.get("stck_hgpr").asLong());
            low = Math.min(low, stockChart.get("stck_lwpr").asLong());
            volume += stockChart.get("cntg_vol").asLong();
        }
        
        long close = output.get(0).get("stck_prpr").asLong();
        long open = output.get(windowSize - 1).get("stck_oprc").asLong();
        
        return StockChartDto.builder()
                .stockCode(stockCode)
                .dateTime(DateTimeFormatterUtil.parseToString(time))
                .open(open)
                .close(close)
                .high(high)
                .low(low)
                .volume(volume)
                .build();
    }
}
