package at.fhv.wallet.repository;

import at.fhv.wallet.model.Gadget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GadgetRepository extends JpaRepository<Gadget, Long> {
    // Holt alle Gadgets, die nicht in der Liste der übergebenen IDs sind
    List<Gadget> findByIdNotIn(List<Long> ownedGadgetIds);
}
