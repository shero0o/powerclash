package at.fhv.wallet.repository;

import at.fhv.wallet.model.Gadget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GadgetRepository extends JpaRepository<Gadget, Long> {
    List<Gadget> findByIdNotIn(List<Long> ownedGadgetIds);
}
