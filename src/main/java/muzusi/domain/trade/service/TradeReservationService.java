package muzusi.domain.trade.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.trade.entity.TradeReservation;
import muzusi.domain.trade.repository.TradeReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TradeReservationService {
    private final TradeReservationRepository tradeReservationRepository;

    public void save(TradeReservation reservation) {
        tradeReservationRepository.save(reservation);
    }

    public List<TradeReservation> readAll() {
        return tradeReservationRepository.findAll();
    }

    public Optional<TradeReservation> readById(String id) {
        return tradeReservationRepository.findById(id);
    }

    public List<TradeReservation> readByStockCode(String stockCode) {
        return tradeReservationRepository.findByStockCode(stockCode);
    }

    public List<TradeReservation> readByUserId(Long userId) {
        return tradeReservationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void deleteById(String id) {
        tradeReservationRepository.deleteById(id);
    }

    public void deleteAll() {
        tradeReservationRepository.deleteAll();
    }
}
