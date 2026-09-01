package muzusi.infrastructure.stockchart.client.kis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muzusi.application.stockcandle.dto.StockMinuteCandleDto;
import muzusi.infrastructure.kis.KisRequestFactory;
import muzusi.infrastructure.kis.aop.KisRateLimit;
import muzusi.infrastructure.kis.constant.KisUrlConstant;
import muzusi.infrastructure.kis.exception.KisApiException;
import muzusi.infrastructure.properties.KisProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisStockChartClient {
    private final KisProperties kisProperties;
    private final KisRequestFactory requestFactory;
    
    private static final String STOCK_MINUTES_CHART_TR_ID = "FHKST03010200";
    private static final DateTimeFormatter HHMMSS_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter YYYYMMDD_HHMMSS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    @KisRateLimit
    public StockMinuteCandleDto getStockMinuteChart(String stockCode, LocalDateTime time, int gap) {
        HttpHeaders headers = requestFactory.getHttpHeader(STOCK_MINUTES_CHART_TR_ID);

        LocalDateTime bucketEnd = time.truncatedTo(ChronoUnit.MINUTES)
                .minusMinutes(time.getMinute() % gap);

        String uri = UriComponentsBuilder.fromUriString(kisProperties.getUrl(KisUrlConstant.TIME_ITEM_CHART_PRICE))
                .queryParam("FID_ETC_CLS_CODE", "")
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_INPUT_ISCD", stockCode)
                .queryParam("FID_INPUT_HOUR_1", bucketEnd.format(HHMMSS_FORMATTER))
                .queryParam("FID_PW_DATA_INCU_YN", "N")
                .build().toUriString();

        HttpEntity<String> requestInfo = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            StockMinuteChartResponse response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestInfo,
                    StockMinuteChartResponse.class
            ).getBody();

            return parseResponseToStockMinuteCandleDto(response, gap, stockCode, bucketEnd);
        } catch (KisApiException e) {
            throw e;
        } catch (Exception e) {
            throw new KisApiException("한국투자증권 당일분봉조회 API 호출 중 에러가 발생하였습니다.", e);
        }
    }

    /**
     * 한국투자증권 당일분봉조회 API 응답({@link StockMinuteChartResponse})에서 {@code [bucketEnd - gap, bucketEnd)} 구간의
     * 1분봉을 집계해 하나의 {@code gap}분봉 DTO로 변환하는 메서드
     *
     * <p> 응답의 1분봉 중 대상 버킷 시각 범위에 속하는 것만 필터링하므로, 스케줄러 실행 지연이나
     * 진행 중인 분봉({@code stck_cntg_hour == bucketEnd})의 포함 여부와 무관하게 결정적인 결과를 만든다.
     *
     * @param response      한국투자증권 당일분봉조회 API 조회 응답 DTO
     * @param gap           분봉 차트 간격(분)
     * @param stockCode     주식 종목 코드
     * @param bucketEnd     대상 버킷의 종료 시각(exclusive). 봉의 시각은 {@code bucketEnd - gap}으로 라벨링된다.
     * @return              분봉 정보 DTO
     */
    private StockMinuteCandleDto parseResponseToStockMinuteCandleDto(
            StockMinuteChartResponse response,
            int gap,
            String stockCode,
            LocalDateTime bucketEnd
    ) {
        if (response == null || response.output() == null || response.output().isEmpty()) {
            throw new KisApiException("한국투자증권 당일분봉조회 API 조회 결과가 비어있습니다.");
        }

        LocalDateTime bucketStart = bucketEnd.minusMinutes(gap);

        // output2는 최신순 정렬 → 필터링 후에도 최신순 유지
        List<StockMinuteChartResponse.Output2> bucket = response.output().stream()
                .filter(output -> {
                    LocalDateTime contractedAt = LocalDateTime.parse(output.date() + output.hour(), YYYYMMDD_HHMMSS_FORMATTER);
                    return !contractedAt.isBefore(bucketStart) && contractedAt.isBefore(bucketEnd);
                })
                .toList();

        if (bucket.isEmpty()) {
            throw new KisApiException("한국투자증권 당일분봉조회 응답에 '%s'의 %s~%s 구간 분봉이 없습니다.".formatted(stockCode, bucketStart, bucketEnd));
        }

        if (bucket.size() < gap) {
            log.warn("[StockMinuteCandle] '{}' {}~{} 구간 분봉이 {}개로 부족합니다. (기대: {}개)",
                    stockCode, bucketStart, bucketEnd, bucket.size(), gap);
        }

        StockMinuteChartResponse.Output2 latestOutput = bucket.get(0);
        StockMinuteChartResponse.Output2 earliestOutput = bucket.get(bucket.size() - 1);

        long high = Long.MIN_VALUE, low = Long.MAX_VALUE, volume = 0L;
        for (StockMinuteChartResponse.Output2 output : bucket) {
            high = Math.max(high, output.high());
            low = Math.min(low, output.low());
            volume += output.volume();
        }

        return StockMinuteCandleDto.builder()
                .stockCode(stockCode)
                .dateTime(bucketStart)
                .open(earliestOutput.open())
                .high(high)
                .low(low)
                .close(latestOutput.price())
                .volume(volume)
                .build();
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record StockMinuteChartResponse(
            @JsonProperty("rt_cd") String rtCd,
            @JsonProperty("msg_cd") String msgCd,
            @JsonProperty("msg1") String msg,
            @JsonProperty("output2") List<Output2> output
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Output2(
                @JsonProperty("stck_bsop_date") String date,
                @JsonProperty("stck_cntg_hour") String hour,
                @JsonProperty("stck_hgpr") long high,
                @JsonProperty("stck_lwpr") long low,
                @JsonProperty("stck_oprc") long open,
                @JsonProperty("stck_prpr") long price,
                @JsonProperty("cntg_vol") long volume
        ) { }
    }
}
