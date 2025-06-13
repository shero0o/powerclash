package at.fhv.spiel_service.entities;

public interface Entity {
    String getId();
    Position getPosition();
    void setPosition(Position position);
}
