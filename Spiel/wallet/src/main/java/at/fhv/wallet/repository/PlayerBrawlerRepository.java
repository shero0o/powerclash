package at.fhv.wallet.repository;

import at.fhv.wallet.model.PlayerBrawler;
import at.fhv.wallet.model.PlayerBrawlerId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlayerBrawlerRepository extends JpaRepository<PlayerBrawler, PlayerBrawlerId> {
    List<PlayerBrawler> findByPlayerId(Long playerId);
    boolean existsByPlayerIdAndBrawlerId(Long playerId, Long brawlerId);
}
