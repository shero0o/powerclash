package at.fhv.shop_catalogue.repository;

import at.fhv.shop_catalogue.model.Gadget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GadgetRepository extends JpaRepository<Gadget, Long> {
    List<Gadget> findByIdNotIn(List<Long> ownedGadgetIds);
}
