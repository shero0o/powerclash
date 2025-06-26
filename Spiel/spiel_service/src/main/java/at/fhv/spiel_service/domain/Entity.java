package at.fhv.spiel_service.domain;

public interface Entity {
    String getId();
    Position getPosition();
    default void setPosition(Position position){}
}
