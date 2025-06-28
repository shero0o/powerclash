package at.fhv.spiel_service.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.InputStream;
import java.util.*;

public class GameMap {
    @Getter
    private final String id;
    private final int width;
    private final int height;
    @Getter
    private final int tileWidth;
    @Getter
    private final int tileHeight;
    private final boolean[][] walls;
    private final Set<Position> bushPositions = new HashSet<>();
    private final Set<Position> poisonPositions = new HashSet<>();
    private final Set<Position> healPositions = new HashSet<>();
    private final Set<Position> cratePositions = new HashSet<>();

    public GameMap(String id) {
        this.id = id;

        String mapFile = switch (id) {
            case "level1" -> "map1.tmj";
            case "level2", "level3" -> "map2.tmj";
            default -> throw new IllegalArgumentException("Unknown Level: " + id);
        };

        String mapPath = "/map_tmj/" + mapFile;

        try (InputStream inputStream = getClass().getResourceAsStream(mapPath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Map-File not found: " + mapPath);
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(inputStream);

            this.width = root.get("width").asInt();
            this.height = root.get("height").asInt();
            this.tileWidth = root.get("tilewidth").asInt();
            this.tileHeight = root.get("tileheight").asInt();
            this.walls = new boolean[height][width];

            for (JsonNode layer : root.get("layers")) {
                String name = layer.get("name").asText();
                JsonNode data = layer.get("data");
                switch (name.toLowerCase()) {
                    case "wand" -> parseWalls(data);
                    case "kisten" -> parseCrates(data);
                    case "gebüsch, giftzone, energiezone" -> parseZones(data);
                    default -> {}
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading map '" + id + "'", e);
        }
    }

    private void parseWalls(JsonNode data) {
        for (int i = 0; i < data.size(); i++) {
            int gid = data.get(i).asInt();
            int x   = i % width;
            int y   = i / width;
            walls[y][x] = gid > 0;
        }
    }

    private void parseCrates(JsonNode data) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).asInt() == 129) {
                int x = i % width;
                int y = i / width;
                Position pos = new Position(x, y);
                cratePositions.add(pos);
            }
        }
    }

    private void parseZones(JsonNode data) {
        Set<Integer> bushGids   = Set.of(183);
        Set<Integer> poisonGids = Set.of(186);
        Set<Integer> healGids   = Set.of(19, 20);
        for (int i = 0; i < data.size(); i++) {
            int gid = data.get(i).asInt();
            int x = i % width;
            int y = i / width;
            Position pos = new Position(x, y);

            if (bushGids.contains(gid)) {
                bushPositions.add(pos);
            }
            if (poisonGids.contains(gid)) {
                poisonPositions.add(pos);
            }
            if (healGids.contains(gid)) {
                healPositions.add(pos);
            }
        }
    }


    public boolean isWallAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return true;
        return walls[y][x];
    }

    public boolean isBushTile(Position pos) {
        return bushPositions.contains(pos);
    }
    public boolean isPoisonTile(Position pos) {
        return poisonPositions.contains(pos);
    }
    public boolean isHealTile(Position pos) {
        return healPositions.contains(pos);
    }

    public Position[] getCratePositions() {
        return cratePositions.toArray(new Position[0]);
    }
    public int getWidthInPixels() {
        return width * tileWidth;
    }
    public int getHeightInPixels() {
        return height * tileHeight;
    }
}
