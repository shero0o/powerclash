package at.fhv.shop_catalogue.repository;

import at.fhv.shop_catalogue.model.Brawler;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BrawlerRepository extends JpaRepository<Brawler, Long> {
    List<Brawler> findByIdNotIn(List<Long> ownedBrawlerIds);
}
