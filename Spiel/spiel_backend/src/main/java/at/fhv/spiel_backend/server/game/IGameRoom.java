package at.fhv.spiel_backend.server.game;

public interface IGameRoom {

    String getId();
    /**
     * Spieler-ID registrieren
     */
    void addPlayer(String playerId);

    /**
     * Spieler-ID entfernen
     */
    void removePlayer(String playerId);

    /**
     * Gibt die aktuelle Teilnehmerzahl zurück
     */
    int getPlayerCount();

    /**
     * Domain-Logik für den Start eines Spiels
     */
    void start();



//    void addPlayer(WebSocketSession session);
//    void removePlayer(WebSocketSession session);
//    void handleInput(ICommand cmd);
//    void start();
//    GameLogic getLogic();
}
