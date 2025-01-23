package muzusi.domain.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.trade.entity.TradeReservation;
import muzusi.domain.trade.repository.TradeReservationRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TradeReservationService {
    private final TradeReservationRepository tradeReservationRepository;

    public void save(TradeReservation reservation) {
        tradeReservationRepository.save(reservation);
    }

    public Optional<TradeReservation> readById(String id) {
        return tradeReservationRepository.findById(id);
    }

    public void deleteById(String id) {
        tradeReservationRepository.deleteById(id);
    }
}
