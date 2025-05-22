// GameMap.java
package at.fhv.spiel_backend.server.map;

import at.fhv.spiel_backend.model.Position;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class GameMap {
    private final String id;
    private final int width;
    private final int height;
    private final int tileWidth;
    private final int tileHeight;
    private final boolean[][] walls;
    private Set<Position> bushPositions = new HashSet<>();
    private Set<Position> poisonPositions = new HashSet<>();
    private Set<Position> healPositions = new HashSet<>();


    public GameMap(String id) {
        this.id = id;

        // Dynamisch den richtigen Map-Dateinamen wählen
        String mapFile = switch (id) {
            case "level1" -> "map1.tmj";
            case "level2", "level3" -> "map2.0.tmj";
            default -> throw new IllegalArgumentException("Unknown Level: " + id);
        };

        try (InputStream is = getClass().getResourceAsStream("/" + mapFile)) {
            if (is == null) {
                throw new RuntimeException("Map-File not found: " + mapFile);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(is);

            // read dimensions dynamically
            this.width      = root.get("width").asInt();
            this.height     = root.get("height").asInt();
            this.tileWidth  = root.get("tilewidth").asInt();
            this.tileHeight = root.get("tileheight").asInt();
            this.walls      = new boolean[height][width];

            for (JsonNode layer : root.get("layers")) {
                if ("Wand".equalsIgnoreCase(layer.get("name").asText())) {
                    JsonNode data = layer.get("data");
                    for (int i = 0; i < data.size(); i++) {
                        int gid = data.get(i).asInt();
                        int x   = i % width;
                        int y   = i / width;
                        walls[y][x] = gid > 0;
                    }
                    break;
                }
            }

            Set<Integer> bushGids = Set.of(183);

            for (JsonNode layer : root.get("layers")) {
                if ("Gebüsch, Giftzone, Energiezone".equalsIgnoreCase(layer.get("name").asText())) {
                    JsonNode data = layer.get("data");
                    for (int i = 0; i < data.size(); i++) {
                        int gid = data.get(i).asInt();
                        int x = i % width;
                        int y = i / width;

                        if (bushGids.contains(gid)) {
                            bushPositions.add(new Position(x, y));
                        }
                    }
                    break;
                }
            }

            Set<Integer> poisonGids = Set.of(186);

            for (JsonNode layer : root.get("layers")) {
                if ("Gebüsch, Giftzone, Energiezone".equalsIgnoreCase(layer.get("name").asText())) {
                    JsonNode data = layer.get("data");
                    for (int i = 0; i < data.size(); i++) {
                        int gid = data.get(i).asInt();
                        int x = i % width;
                        int y = i / width;

                        Position pos = new Position(x, y);
                        if (poisonGids.contains(gid)) {
                            poisonPositions.add(pos);
                        }
                    }
                }
            }

            Set<Integer> HealGids = Set.of(19, 20);

            for (JsonNode layer : root.get("layers")) {
                if ("Gebüsch, Giftzone, Energiezone".equalsIgnoreCase(layer.get("name").asText())) {
                    JsonNode data = layer.get("data");
                    for (int i = 0; i < data.size(); i++) {
                        int gid = data.get(i).asInt();
                        int x = i % width;
                        int y = i / width;

                        Position pos = new Position(x, y);
                        if (HealGids.contains(gid)) {
                            healPositions.add(pos);
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error by loading the map '" + id + "'", e);
        }
    }


    public String getId()         { return id; }
    public int     getTileWidth() { return tileWidth; }
    public int     getTileHeight(){ return tileHeight; }
    public Set<Position> getBushPositions() {
        return bushPositions;
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
}
