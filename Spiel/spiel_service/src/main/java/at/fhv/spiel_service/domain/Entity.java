package at.fhv.spiel_service.domain;

public interface Entity {
    String getId();
    Position getPosition();
    void setPosition(Position position);
}
