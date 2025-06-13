// src/main/java/at/fhv/spiel_service/services/managers/player/PlayerServiceImpl.java
package at.fhv.spiel_service.services.managers.player;

import at.fhv.spiel_service.entities.Gadget;
import at.fhv.spiel_service.entities.Player;
import at.fhv.spiel_service.entities.Position;
import at.fhv.spiel_service.services.managers.equipment.IEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerServiceImpl implements IPlayerService {
    private static final int DEFAULT_LEVEL      = 1;
    private static final int DEFAULT_MAX_HEALTH = 100;

    private final Map<String, Player> players = new ConcurrentHashMap<>();

    private final IEquipmentService equipmentService;

    @Autowired
    public PlayerServiceImpl(IEquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @Override
    public Player addPlayer(String playerId, String brawlerId, String playerName) {
        int index = players.size();
        Position spawn = switch (index) {
            case 0 -> new Position(1200, 1200, 0);
            case 1 -> new Position(6520, 1200, 180);
            case 2 -> new Position(1200, 6520, 0);
            default -> new Position(6520, 6520, 180);
        };

        Player p = new Player(
                playerId,
                brawlerId,
                playerName,
                DEFAULT_LEVEL,
                DEFAULT_MAX_HEALTH,
                spawn
        );
        players.put(playerId, p);
        return p;
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
        equipmentService.removeWeapon(playerId);
        equipmentService.removeGadget(playerId);
    }

    @Override
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    @Override
    public Gadget getGadget(String playerId) {
        return equipmentService.getGadget(playerId);
    }

    @Override
    public Map<String, Player> getAllPlayers() {
        return players;
    }
}
