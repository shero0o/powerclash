// src/main/java/at/fhv/spiel_service/services/managers/equipment/EquipmentServiceImpl.java
package at.fhv.spiel_service.services.managers.equipment;

import at.fhv.spiel_service.entities.Gadget;
import at.fhv.spiel_service.entities.GadgetType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class EquipmentServiceImpl implements IEquipmentService {
    private static final String DEFAULT_WEAPON = "RIFLE_BULLET";

    private final Map<String, String> weaponMap = new ConcurrentHashMap<>();
    private final Map<String, Gadget> gadgetMap = new ConcurrentHashMap<>();

    @Override
    public void assignWeapon(String playerId, String weaponType) {
        weaponMap.put(playerId, weaponType);
    }

    @Override
    public String getWeaponType(String playerId) {
        return weaponMap.getOrDefault(playerId, DEFAULT_WEAPON);
    }

    @Override
    public void removeWeapon(String playerId) {
        weaponMap.remove(playerId);
    }

    @Override
    public void assignGadget(String playerId, String gadgetType) {
        GadgetType type = GadgetType.valueOf(gadgetType);
        Gadget g = new Gadget(type, playerId);
        gadgetMap.put(playerId, g);
    }

    @Override
    public Gadget getGadget(String playerId) {
        return gadgetMap.get(playerId);
    }

    @Override
    public void removeGadget(String playerId) {
        gadgetMap.remove(playerId);
    }

    /**
     * Liefert alle aktuell zugewiesenen Gadgets.
     */
    @Override
    public Collection<Gadget> getAllGadgets() {
        // Rückgabe als List, da viele Clients ein List erwarten
        return new ArrayList<>(gadgetMap.values());
    }
}
