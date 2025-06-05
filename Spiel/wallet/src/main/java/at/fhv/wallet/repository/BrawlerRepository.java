package at.fhv.wallet.repository;

import at.fhv.wallet.model.Brawler;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BrawlerRepository extends JpaRepository<Brawler, Long> {
    // Holt alle Brawler, die nicht in der Liste der übergebenen IDs sind
    List<Brawler> findByIdNotIn(List<Long> ownedBrawlerIds);
}
