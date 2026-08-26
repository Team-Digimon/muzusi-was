package muzusi.infrastructure.stockquote.websocket.kis;

import muzusi.application.trade.dto.TradeNotificationDto;
import muzusi.application.websocket.service.TradeNotificationPublisher;
import muzusi.domain.trade.type.TradeType;
import muzusi.infrastructure.kis.websocket.dto.KisWebSocketResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class KisStockQuoteWebSocketHandlerTest {

    private static final String TR_ID = "H0STCNT0";
    private static final int FIELD_COUNT = 46;

    @Mock
    private TradeNotificationPublisher tradeNotificationPublisher;

    @InjectMocks
    private KisStockQuoteWebSocketHandler kisStockQuoteWebSocketHandler;

    @Test
    @DisplayName("이 핸들러가 처리하는 TR_ID를 반환한다")
    void successReturnTrId() {
        // when & then
        assertThat(kisStockQuoteWebSocketHandler.getTrId()).isEqualTo(TR_ID);
    }

    @Nested
    @DisplayName("실시간 체결가 응답 처리")
    class Handle {
        @Test
        @DisplayName("체결 구분이 매수(1)인 데이터를 거래 체결 알림으로 발행한다")
        void successPublishBuyTradeNotification() {
            // given
            String data = buildData(item("005930", "091530", "70000", "1.23", "10", "1000", "1"));
            KisWebSocketResponseDto response = buildResponse(1, data);

            // when
            kisStockQuoteWebSocketHandler.handle(response);

            // then
            List<TradeNotificationDto> published = capturePublished();
            assertThat(published).hasSize(1);
            TradeNotificationDto notification = published.get(0);
            assertThat(notification.stockCode()).isEqualTo("005930");
            assertThat(notification.time()).isEqualTo("09:15:30");
            assertThat(notification.price()).isEqualTo(70000L);
            assertThat(notification.contingentVolume()).isEqualTo(10L);
            assertThat(notification.accumulatedVolume()).isEqualTo(1000L);
            assertThat(notification.tradeType()).isEqualTo(TradeType.BUY);
            assertThat(notification.changeRate()).isEqualTo(1.23);
        }

        @Test
        @DisplayName("체결 구분이 매도(5)인 데이터를 거래 체결 알림으로 발행한다")
        void successPublishSellTradeNotification() {
            // given
            String data = buildData(item("000660", "133000", "150000", "-2.5", "5", "500", "5"));
            KisWebSocketResponseDto response = buildResponse(1, data);

            // when
            kisStockQuoteWebSocketHandler.handle(response);

            // then
            List<TradeNotificationDto> published = capturePublished();
            assertThat(published).hasSize(1);
            assertThat(published.get(0).tradeType()).isEqualTo(TradeType.SELL);
        }

        @Test
        @DisplayName("여러 건의 데이터가 포함되어 있으면 모두 파싱해 순서대로 발행한다")
        void successPublishMultipleTradeNotifications() {
            // given
            String data = buildData(
                    item("005930", "091530", "70000", "1.23", "10", "1000", "1"),
                    item("000660", "091531", "150000", "-0.5", "3", "300", "5")
            );
            KisWebSocketResponseDto response = buildResponse(2, data);

            // when
            kisStockQuoteWebSocketHandler.handle(response);

            // then
            List<TradeNotificationDto> published = capturePublished();
            assertThat(published).hasSize(2);
            assertThat(published.get(0).stockCode()).isEqualTo("005930");
            assertThat(published.get(1).stockCode()).isEqualTo("000660");
        }

        @Test
        @DisplayName("체결 구분이 매수/매도로 판별되지 않는 데이터(예: 장전 3)는 발행 대상에서 제외한다")
        void excludeUnknownTradeTypeFromNotification() {
            // given
            String data = buildData(
                    item("005930", "091530", "70000", "1.23", "10", "1000", "3"),
                    item("000660", "091531", "150000", "-0.5", "3", "300", "5")
            );
            KisWebSocketResponseDto response = buildResponse(2, data);

            // when
            kisStockQuoteWebSocketHandler.handle(response);

            // then
            List<TradeNotificationDto> published = capturePublished();
            assertThat(published).hasSize(1);
            assertThat(published.get(0).stockCode()).isEqualTo("000660");
        }

        @Test
        @DisplayName("모든 데이터의 체결 구분을 판별할 수 없으면 빈 목록을 발행한다")
        void publishEmptyListWhenAllTradeTypesUnknown() {
            // given
            String data = buildData(item("005930", "091530", "70000", "1.23", "10", "1000", "3"));
            KisWebSocketResponseDto response = buildResponse(1, data);

            // when
            kisStockQuoteWebSocketHandler.handle(response);

            // then
            assertThat(capturePublished()).isEmpty();
        }

        @Test
        @DisplayName("데이터 건수가 실제 데이터보다 많아 파싱에 실패하면 발행하지 않는다")
        void doNotPublishWhenDataIsMalformed() {
            // given: dataCount는 2건이지만 실제 데이터는 1건 분량만 존재
            String data = buildData(item("005930", "091530", "70000", "1.23", "10", "1000", "1"));
            KisWebSocketResponseDto response = buildResponse(2, data);

            // when
            kisStockQuoteWebSocketHandler.handle(response);

            // then
            verifyNoInteractions(tradeNotificationPublisher);
        }

        @Test
        @DisplayName("체결 시간 형식이 올바르지 않아 변환에 실패하면 발행하지 않는다")
        void doNotPublishWhenTimeFormatIsInvalid() {
            // given: 체결 시간이 HHmmss(6자리) 형식이 아님
            String data = buildData(item("005930", "12", "70000", "1.23", "10", "1000", "1"));
            KisWebSocketResponseDto response = buildResponse(1, data);

            // when
            kisStockQuoteWebSocketHandler.handle(response);

            // then
            verifyNoInteractions(tradeNotificationPublisher);
        }
    }

    @SuppressWarnings("unchecked")
    private List<TradeNotificationDto> capturePublished() {
        ArgumentCaptor<List<TradeNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(tradeNotificationPublisher).publishTradeNotification(captor.capture());
        return captor.getValue();
    }

    private KisWebSocketResponseDto buildResponse(int count, String data) {
        return KisWebSocketResponseDto.builder()
                .isEncoded(true)
                .trId(TR_ID)
                .dataCount(count)
                .data(data)
                .build();
    }

    /**
     * 실시간 체결가 데이터 한 건({@value #FIELD_COUNT}개 필드)을 생성한다.
     * 테스트에서 검증하지 않는 필드는 빈 문자열로 채운다.
     */
    private String[] item(String stockCode, String time, String price, String changeRate,
                           String contingentVolume, String accumulatedVolume, String ccldDvsn) {
        String[] fields = new String[FIELD_COUNT];
        Arrays.fill(fields, "");
        fields[0] = stockCode;             // MKSC_SHRN_ISCD
        fields[1] = time;                  // STCK_CNTG_HOUR
        fields[2] = price;                 // STCK_PRPR
        fields[5] = changeRate;            // PRDY_CTRT
        fields[12] = contingentVolume;     // CNTG_VOL
        fields[13] = accumulatedVolume;    // ACML_VOL
        fields[21] = ccldDvsn;             // CCLD_DVSN
        return fields;
    }

    private String buildData(String[]... items) {
        List<String> allFields = new ArrayList<>();
        for (String[] fields : items) {
            allFields.addAll(Arrays.asList(fields));
        }
        return String.join("^", allFields);
    }
}
