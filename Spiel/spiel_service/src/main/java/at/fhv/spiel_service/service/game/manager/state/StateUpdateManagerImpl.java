package at.fhv.spiel_service.service.game.manager.state;

import at.fhv.spiel_service.domain.Crate;
import at.fhv.spiel_service.domain.NPC;
import at.fhv.spiel_service.domain.ProjectileType;
import at.fhv.spiel_service.domain.Zone;
import at.fhv.spiel_service.messaging.CrateState;
import at.fhv.spiel_service.messaging.PlayerState;
import at.fhv.spiel_service.messaging.StateUpdateMessage;
import at.fhv.spiel_service.service.game.manager.player.IPlayerService;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileManager;
import at.fhv.spiel_service.service.game.manager.zone.ZoneManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StateUpdateManagerImpl implements StateUpdateManager {
    private final IPlayerService playerService;
    private final ProjectileManager projectileManager;
    private final Map<String, Crate> crates;
    private final ZoneManager zoneManager;
    private final List<NPC> npcs;

    public StateUpdateManagerImpl(
            IPlayerService playerService,
            ProjectileManager projectileManager,
            Map<String, Crate> crates,
            ZoneManager zoneManager,
            List<NPC> npcs
    ) {
        this.playerService      = playerService;
        this.projectileManager  = projectileManager;
        this.crates             = crates;
        this.zoneManager        = zoneManager;
        this.npcs               = npcs;
    }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        List<PlayerState> ps = playerService.getAllPlayers().stream().map(p -> {
            String pid = p.getId();
            ProjectileType wep = projectileManager.getCurrentWeapon(pid);
            int ammo = projectileManager.getCurrentAmmo(pid);
            return new PlayerState(
                    pid,
                    p.getPosition(),
                    p.getCurrentHealth(),
                    p.isVisible(),
                    ammo,
                    wep,
                    playerService.getPlayerBrawler().getOrDefault(pid, "sniper"),
                    playerService.getPlayerNames().getOrDefault(pid, "Player"),
                    playerService.getPlayerCoins().getOrDefault(pid, 0),
                    p.getMaxHealth()
            );
        }).collect(Collectors.toList());

        StateUpdateMessage msg = new StateUpdateMessage();
        msg.setPlayers(ps);
        msg.setEvents(Collections.emptyList());
        msg.setProjectiles(projectileManager.getProjectiles());

        List<CrateState> crateStates = crates.values().stream()
                .map(c -> new CrateState(
                        c.getId(),
                        (int) c.getPosition().getX(),
                        (int) c.getPosition().getY(),
                        c.getCurrentHealth()
                ))
                .collect(Collectors.toList());
        msg.setCrates(crateStates);

        msg.setGadgets(playerService.getAllGadgets());

        if (zoneManager.isZoneActive()) {
            long rem = zoneManager.getRemainingTimeMs();
            msg.setZone(new Zone(
                    zoneManager.getCenter(),
                    zoneManager.getZoneRadius(),
                    rem
            ));
        }

        msg.setNpcs(new ArrayList<>(npcs));
        return msg;
    }

}
