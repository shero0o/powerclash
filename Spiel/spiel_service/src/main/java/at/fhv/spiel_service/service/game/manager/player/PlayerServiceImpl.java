package at.fhv.spiel_service.service.game.manager.player;

import at.fhv.spiel_service.domain.*;
import at.fhv.spiel_service.service.game.manager.projectile.ProjectileManager;
import lombok.Getter;

import java.util.*;

import static at.fhv.spiel_service.domain.ProjectileType.RIFLE_BULLET;

public class PlayerServiceImpl implements IPlayerService {
    private final Map<String, Player> players;
    @Getter
    private final Map<String, String> playerBrawler;
    @Getter
    private final Map<String, String> playerNames;
    @Getter
    private final Map<String, Gadget> playerGadget;
    @Getter
    private final Map<String, Integer> playerCoins;
    @Getter
    private final ProjectileManager projectileManager;

    public PlayerServiceImpl(Map<String, Player> players,
                             Map<String, String> playerBrawler,
                             Map<String, String> playerNames,
                             Map<String, Gadget> playerGadget,
                             Map<String, Integer> playerCoins,
                             ProjectileManager projectileManager) {
        this.players = players;
        this.playerBrawler = playerBrawler;
        this.playerNames   = playerNames;
        this.playerGadget  = playerGadget;
        this.playerCoins   = playerCoins;
        this.projectileManager = projectileManager;
    }

    @Override
    public void addPlayer(String playerId, String brawlerId, String playerName) {
        if (brawlerId == null || brawlerId.isBlank()) {
            brawlerId = "hitman";
        }
        int index = players.size();
        Position spawn;
        switch (index) {
            case 0 -> spawn = new Position(1200, 1200, 0);
            case 1 -> spawn = new Position(6520, 1200, 0);
            case 2 -> spawn = new Position(1200, 6480, 0);
            case 3 -> spawn = new Position(6520, 6480, 0);
            default -> spawn = new Position(3860, 2700, 0);
        }
        Brawler br = new Brawler(playerId, 1, 100, spawn);
        Player player = new Player(br.getId(), br.getLevel(), br.getMaxHealth(), br.getPosition());
        players.put(playerId, player);
        projectileManager.initPlayer(playerId, RIFLE_BULLET);
        playerBrawler.put(playerId, brawlerId);
        playerNames.put(playerId, playerName);
        playerGadget.put(playerId, new Gadget(null, playerId));
        playerCoins.put(playerId, 0);
    }

    @Override
    public void removePlayer(String playerId) {
        players.remove(playerId);
        projectileManager.removeProjectileById(playerId);
        playerBrawler.remove(playerId);
        playerNames.remove(playerId);
        playerGadget.remove(playerId);
        playerCoins.remove(playerId);
    }

    @Override
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    @Override
    public Position getPlayerPosition(String playerId) {
        Player p = players.get(playerId);
        return p != null ? p.getPosition() : null;
    }

    @Override
    public Gadget getGadget(String playerId) {
        return playerGadget.get(playerId);
    }

    @Override
    public Map<String, Player> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    @Override
    public List<Player> getAllPlayers() {
        return new ArrayList<>(players.values());
    }

    public List<Gadget> getAllGadgets() {
        return new ArrayList<>(this.playerGadget.values());
    }

    @Override
    public void setGadget(String playerId, GadgetType type) {
        playerGadget.put(playerId, new Gadget(type, playerId));
    }


}
