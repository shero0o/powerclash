package at.fhv.spiel_backend.command;

import at.fhv.spiel_backend.server.GameRoom;

public interface ICommand {
    void execute(GameRoom room);
}
