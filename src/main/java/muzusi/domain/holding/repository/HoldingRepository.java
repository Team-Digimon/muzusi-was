package muzusi.domain.holding.repository;

import muzusi.domain.holding.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long>, CustomHoldingRepository {
    List<Holding> findByAccount_Id(Long accountId);
}
