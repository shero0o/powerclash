package at.fhv.shop_catalogue.repository;

import at.fhv.shop_catalogue.model.PlayerGadget;
import at.fhv.shop_catalogue.model.PlayerGadgetId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlayerGadgetRepository extends JpaRepository<PlayerGadget, PlayerGadgetId> {
    List<PlayerGadget> findByPlayerId(Long playerId);
    boolean existsByPlayerIdAndGadgetId(Long playerId, Long gadgetId);
    void deleteByPlayerIdAndGadgetId(Long playerId, Long gadgetId);
}
