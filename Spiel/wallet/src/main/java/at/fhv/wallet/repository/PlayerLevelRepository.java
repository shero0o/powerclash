package at.fhv.wallet.repository;

import at.fhv.wallet.model.PlayerBrawler;
import at.fhv.wallet.model.PlayerBrawlerId;
import at.fhv.wallet.model.PlayerLevel;
import at.fhv.wallet.model.PlayerLevelId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerLevelRepository extends JpaRepository<PlayerLevel, PlayerLevelId> {
    List<PlayerLevel> findByPlayerId(Long playerId);
    boolean existsByPlayerIdAndLevelId(Long playerId, Long levelId);
}
