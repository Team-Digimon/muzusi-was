package muzusi.infrastructure.kis.stockprice.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import muzusi.infrastructure.kis.KisRequestFactory;
import muzusi.infrastructure.kis.constant.KisUrlConstant;
import muzusi.infrastructure.kis.exception.KisApiException;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KisMultiStockPriceClient {
    private final KisRequestFactory kisRequestFactory;
    private final KisProperties kisProperties;
    private final ObjectMapper objectMapper;
    private final RateLimiter kisRateLimiter;

    /**
     * 한국투자증권 멀티종목 시세조회 API 당 조회 가능 주목 종목 수는 30
     */
    private static final int BATCH_SIZE = 30;
    private static final String TR_ID = "FHKST11300006";
    
    public Map<String, Long> getMultiStockPrice(List<String> stockCodes) {
        Map<String, Long> stockPriceMap = new HashMap<>();
        List<String> targetStockCodes = new ArrayList<>();
        
        
        for (String stockCode : stockCodes) {
            targetStockCodes.add(stockCode);
            
            if (targetStockCodes.size() >= BATCH_SIZE) {
                Map<String, Long> result = getMultiStockPriceInBatch(targetStockCodes);
                stockPriceMap.putAll(result);
                
                targetStockCodes.clear();
            }
        }
        
        if (!targetStockCodes.isEmpty()) {
            Map<String, Long> result = getMultiStockPriceInBatch(targetStockCodes);
            stockPriceMap.putAll(result);
        }
        
        return stockPriceMap;
    }
    
    /**
     * 최대 {@link #BATCH_SIZE}개의 종목 코드에 대한 시세를 한국투자증권 API로 조회하는 메서드
     *
     * <p> {@link #buildUri(String, List)}를 통해 인자로 전달된 주식 코드를 쿼리 파라미터로 포함한 URI를 생성한다.
     * <p> 유량 제한({@link #kisRateLimiter})을 획득한 뒤 해당 URI로 한국투자증권 멀티종목 시세조회 API를 호출한다.
     *
     * @param stockCodes 시세를 조회할 종목 코드 목록 (최대 {@link #BATCH_SIZE}개)
     * @return           주식 종목 별 현재가 Map
     */
    private Map<String, Long> getMultiStockPriceInBatch(List<String> stockCodes) {
        HttpHeaders header = kisRequestFactory.getHttpHeader(TR_ID);

        String uri = buildUri(kisProperties.getUrl(KisUrlConstant.MULTI_PRICE), stockCodes);

        HttpEntity<String> requestInfo = new HttpEntity<>(header);

        RestTemplate restTemplate = new RestTemplate();

        kisRateLimiter.acquire();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestInfo,
                    String.class
            );
            
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            
            return parseMultiStockPrice(rootNode);
        } catch (Exception e) {
            throw new KisApiException("한국투자증권 멀티종목 시세 조회 API 조회 중 오류가 발생하였습니다.", e);
        }
    }
    
    /**
     * 한국투자증권 멀티종목 시세 조회 API 응답을 주식 종목 별 현재가 Map으로 변환하는 메서드
     *
     * @param rootNode 한국투자증권 멀티종목 시세 조회 API 응답({@code JsonNode})
     * @return         주식 종목 별 현재가 Map
     */
    private Map<String, Long> parseMultiStockPrice(JsonNode rootNode) {
        JsonNode output = rootNode.get("output");
        Map<String, Long> stockPriceMap = new HashMap<>(BATCH_SIZE);
        
        
        for (int i = 0; i < output.size(); i++) {
            JsonNode node = output.get(i);
            String stockCode = node.get("inter_shrn_iscd").asText();
            long price = node.get("inter2_prpr").asLong();
            
            if (stockCode == null || stockCode.isEmpty()) continue;
            
            stockPriceMap.put(stockCode, price);
        }
        
        return stockPriceMap;
    }
    
    /**
     * 멀티종목 시세 조회 API 요청 URI를 생성하는 메서드
     *
     * <p> 한국투자증권 멀티종목 시세조회 API는 쿼리 파라미터로 {@link #BATCH_SIZE}개의 항목을 모두 채워서 요청을 해야한다.
     * <p> 쿼리 파라미터의 각 항목 인덱스는 1부터 시작한다.
     * <p> 인자로 주어진 주식 종목 코드 목록({@code stockCodes})이 {@link #BATCH_SIZE}보다 작을 경우 공백을 채워서 전달한다.
     *
     * @param uri        멀티종목 시세조회 API URI
     * @param stockCodes 시세를 조회할 종목 코드 목록 (최대 {@link #BATCH_SIZE}개)
     * @return           쿼리 파라미터가 포함된 완성된 요청 URI 문자열
     */
    private String buildUri(String uri, List<String> stockCodes) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uri);
        
        for (int i = 1; i <= BATCH_SIZE; i++) {
            String stockCode = (i - 1 < stockCodes.size()) ? stockCodes.get(i - 1) : "";
            
            uriBuilder.queryParam("FID_COND_MRKT_DIV_CODE_" + i, "J");
            uriBuilder.queryParam("FID_INPUT_ISCD_" + i, stockCode);
        }
        
        return uriBuilder.build().toUriString();
    }
}
