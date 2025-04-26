package at.fhv.spiel_backend.handler;

import at.fhv.spiel_backend.command.ICommand;
import at.fhv.spiel_backend.server.GameRoom;

public interface ICommandHandler {
    /**
     * @return true if this handler supports the given command
     */
    boolean supports(ICommand cmd);

    /**
     * Handle execution of the given command against the room
     */
    void handle(ICommand cmd, GameRoom room);
}
