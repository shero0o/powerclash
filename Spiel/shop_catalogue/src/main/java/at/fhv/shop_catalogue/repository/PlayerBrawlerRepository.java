package at.fhv.shop_catalogue.repository;

import at.fhv.shop_catalogue.model.PlayerBrawler;
import at.fhv.shop_catalogue.model.PlayerBrawlerId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlayerBrawlerRepository extends JpaRepository<PlayerBrawler, PlayerBrawlerId> {
    List<PlayerBrawler> findByPlayerId(Long playerId);
    boolean existsByPlayerIdAndBrawlerId(Long playerId, Long brawlerId);
    void deleteByPlayerIdAndBrawlerId(Long playerId, Long brawlerId);
}
