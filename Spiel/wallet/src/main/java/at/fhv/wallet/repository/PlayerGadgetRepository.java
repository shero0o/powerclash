package at.fhv.wallet.repository;

import at.fhv.wallet.model.PlayerGadget;
import at.fhv.wallet.model.PlayerGadgetId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlayerGadgetRepository extends JpaRepository<PlayerGadget, PlayerGadgetId> {
    List<PlayerGadget> findByPlayerId(Long playerId);
    boolean existsByPlayerIdAndGadgetId(Long playerId, Long gadgetId);
    void deleteByPlayerIdAndGadgetId(Long playerId, Long gadgetId);
}
