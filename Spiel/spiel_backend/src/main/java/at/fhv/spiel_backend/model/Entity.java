package at.fhv.spiel_backend.model;

public interface Entity {
    String getId();
    Position getPosition();
    void setPosition(Position position);
}
