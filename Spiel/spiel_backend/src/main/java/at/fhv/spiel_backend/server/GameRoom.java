package at.fhv.spiel_backend.server;

import at.fhv.spiel_backend.command.ICommand;
import at.fhv.spiel_backend.logic.GameLogic;
import org.springframework.web.socket.WebSocketSession;

public interface GameRoom {
    void addPlayer(WebSocketSession session);
    void removePlayer(WebSocketSession session);
    void handleInput(ICommand cmd);
    void start();
    GameLogic getLogic();
}
