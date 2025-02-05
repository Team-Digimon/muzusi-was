package muzusi.application.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.application.trade.dto.ReservationInfoDto;
import muzusi.domain.trade.service.TradeReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTradeReservationService {
    private final TradeReservationService tradeReservationService;

    @Transactional(readOnly = true)
    public List<ReservationInfoDto> getReservationsByUserId(Long userId) {
        return tradeReservationService.readByUserId(userId)
                .stream().map(ReservationInfoDto::fromEntity)
                .toList();
    }
}
