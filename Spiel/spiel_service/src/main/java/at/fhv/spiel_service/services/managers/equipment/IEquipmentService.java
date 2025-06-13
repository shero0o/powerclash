// src/main/java/at/fhv/spiel_service/services/managers/equipment/IEquipmentService.java
package at.fhv.spiel_service.services.managers.equipment;

import at.fhv.spiel_service.entities.Gadget;

import java.util.Collection;

public interface IEquipmentService {
    void assignWeapon(String playerId, String weaponType);
    String getWeaponType(String playerId);
    void removeWeapon(String playerId);

    void assignGadget(String playerId, String gadgetType);
    Gadget getGadget(String playerId);
    void removeGadget(String playerId);

    /** liefert alle aktiven Gadgets zurück */
    Collection<Gadget> getAllGadgets();
}
