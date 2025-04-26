package at.fhv.spiel_backend.server.map;

import java.util.List;

public class GameMap {
    private final String id;
    private final int[][] walls;

    public GameMap(String id) {
        this.id = id;
        this.walls = new int[0][0]; // Platzhalter
    }

    public String getId() {
        return id;
    }
}
