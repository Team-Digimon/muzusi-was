package muzusi.domain.trade.repository;

import muzusi.domain.trade.entity.TradeReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradeReservationRepository extends JpaRepository<TradeReservation, Long> {
    List<TradeReservation> findByStockCode(String stockCode);
    boolean existsByStockCode(String stockCode);
    List<TradeReservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("DELETE FROM trade_reservation t WHERE t.id IN :ids")
    void deleteAllByIds(@Param("ids") List<Long> ids);

}
