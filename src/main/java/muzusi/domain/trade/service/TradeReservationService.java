package muzusi.domain.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.trade.entity.TradeReservation;
import muzusi.domain.trade.repository.TradeReservationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeReservationService {
    private final TradeReservationRepository tradeReservationRepository;

    public void save(TradeReservation reservation) {
        tradeReservationRepository.save(reservation);
    }
}
