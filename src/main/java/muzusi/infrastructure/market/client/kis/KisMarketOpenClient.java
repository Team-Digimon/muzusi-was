package muzusi.infrastructure.market.client.kis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisMarketOpenClient {
    private final KisProperties kisProperties;
    private final KisRequestFactory kisRequestFactory;
    
    private static final String TR_ID = "CTCA0903R";
    
    @KisRateLimit
    public boolean isMarketOpen() {
        HttpHeaders header = kisRequestFactory.getHttpHeader(TR_ID);
        
        HttpEntity<Void> requestInfo = new HttpEntity<>(header);
        
        String uri = UriComponentsBuilder.fromUriString(kisProperties.getUrl(KisUrlConstant.MARKET_OPEN))
                .queryParam("BASS_DT", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))
                .queryParam("CTX_AREA_NK", "")
                .queryParam("CTX_AREA_FK", "")
                .toUriString();
        
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            KisMarketOpenResponse response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestInfo,
                    KisMarketOpenResponse.class
            ).getBody();
            
            KisErrorParser.validate(response);
            
            return response.isMarketOpen();
        } catch (Exception e) {
            throw new KisApiException("한국투자증권 국내휴장일 조회 API 호출 중 에러가 발생하였습니다.", e);
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KisMarketOpenResponse(
            @JsonProperty(value = "rt_cd") String rtCd,
            @JsonProperty(value = "msg_cd") String msgCd,
            @JsonProperty(value = "msg1") String msg1,
            @JsonProperty(value = "output") List<Output> output
    ) implements KisResponse {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Output(
                @JsonProperty(value = "opnd_yn") String isOpen
        ) { }
        
        private boolean isMarketOpen() {
            return "Y".equals(output.get(0).isOpen());
        }
    }
}
