package muzusi.application.trade.service;

import muzusi.application.trade.dto.TradeReqDto;
import muzusi.domain.trade.entity.TradeReservation;
import muzusi.domain.trade.service.TradeReservationService;
import muzusi.domain.trade.type.TradeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradeReservationHandlerTest {

    @InjectMocks
    private TradeReservationHandler tradeReservationHandler;

    @Mock
    private TradeReservationService tradeReservationService;

    private TradeReqDto tradeReqDto;

    @BeforeEach
    void setUp() {
        tradeReqDto = new TradeReqDto(3000L, 2900L, 5, "000610", TradeType.BUY);
    }

    @Test
    @DisplayName("거래 예약 저장 테스트")
    void saveTradeReservationSuccess() {
        // given

        // when
        tradeReservationHandler.saveTradeReservation(1L, tradeReqDto);

        // then
        verify(tradeReservationService, times(1)).save(any(TradeReservation.class));
    }
}
