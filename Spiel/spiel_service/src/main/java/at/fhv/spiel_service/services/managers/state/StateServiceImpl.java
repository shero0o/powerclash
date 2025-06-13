// src/main/java/at/fhv/spiel_service/services/managers/state/StateServiceImpl.java
package at.fhv.spiel_service.services.managers.state;

import at.fhv.spiel_service.messaging.StateUpdateMessage;
import at.fhv.spiel_service.messaging.PlayerState;
import at.fhv.spiel_service.messaging.CrateState;
import at.fhv.spiel_service.entities.Projectile;
import at.fhv.spiel_service.entities.NPC;
import at.fhv.spiel_service.entities.ZoneState;
import at.fhv.spiel_service.entities.ProjectileType;
import at.fhv.spiel_service.services.managers.player.IPlayerService;
import at.fhv.spiel_service.services.managers.crate.ICrateService;
import at.fhv.spiel_service.services.managers.projectile.IProjectileService;
import at.fhv.spiel_service.services.managers.equipment.IEquipmentService;
import at.fhv.spiel_service.services.managers.zone.IZoneService;
import at.fhv.spiel_service.services.managers.npc.INPCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

@Service
public class StateServiceImpl implements IStateService {

    private final IPlayerService      playerSvc;
    private final ICrateService       crateSvc;
    private final IProjectileService  projectileSvc;
    private final IEquipmentService   equipmentSvc;
    private final IZoneService        zoneSvc;
    private final INPCService         npcSvc;

    @Autowired
    public StateServiceImpl(IPlayerService playerSvc,
                            ICrateService crateSvc,
                            IProjectileService projectileSvc,
                            IEquipmentService equipmentSvc,
                            IZoneService zoneSvc,
                            INPCService npcSvc) {
        this.playerSvc     = playerSvc;
        this.crateSvc      = crateSvc;
        this.projectileSvc = projectileSvc;
        this.equipmentSvc  = equipmentSvc;
        this.zoneSvc       = zoneSvc;
        this.npcSvc        = npcSvc;
    }

    @Override
    public StateUpdateMessage buildStateUpdate() {
        StateUpdateMessage msg = new StateUpdateMessage();

        // 1) Spieler-Status
        List<PlayerState> players = playerSvc.getAllPlayers().values().stream()
                .map(p -> new PlayerState(
                        p.getId(),
                        p.getPosition(),
                        p.getCurrentHealth(),
                        p.isVisible(),
                        /* ammo */ 0,
                        ProjectileType.valueOf(equipmentSvc.getWeaponType(p.getId())),
                        p.getBrawlerId(),
                        p.getPlayerName(),
                        p.getCoinCount(),
                        p.getMaxHealth()
                ))
                .collect(Collectors.toList());
        msg.setPlayers(players);

        // 2) Projektile
        msg.setProjectiles(new ArrayList<>(projectileSvc.getAllProjectiles()));

        // 3) NPCs
        msg.setNpcs(new ArrayList<>(npcSvc.getAllNpcs()));

        // 4) Kisten-Status
        List<CrateState> crates = crateSvc.getAllCrates().stream()
                .map(c -> new CrateState(
                        c.getId(),
                        (int)c.getPosition().getX(),
                        (int)c.getPosition().getY(),
                        c.getCurrentHealth()
                ))
                .collect(Collectors.toList());
        msg.setCrates(crates);

        // 5) Gadgets
        msg.setGadgets(new ArrayList<>(equipmentSvc.getAllGadgets()));

        // 6) Safe-Zone
        if (zoneSvc.isZoneActive()) {
            msg.setZoneState(new ZoneState(
                    zoneSvc.getCenter(),
                    zoneSvc.getZoneRadius(),
                    zoneSvc.getRemainingTimeMs()
            ));
        }

        return msg;
    }
}
