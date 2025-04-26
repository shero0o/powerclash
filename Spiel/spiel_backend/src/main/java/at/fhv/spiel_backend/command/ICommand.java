package at.fhv.spiel_backend.command;

import at.fhv.spiel_backend.server.game.IGameRoom;

public interface ICommand {
    void execute(IGameRoom room);
}
