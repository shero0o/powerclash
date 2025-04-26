package at.fhv.spiel_backend.handler;

import at.fhv.spiel_backend.command.ICommand;
import at.fhv.spiel_backend.command.MoveCommand;
import at.fhv.spiel_backend.server.GameRoom;
import org.springframework.stereotype.Component;

@Component
public class MoveCommandHandler implements ICommandHandler {
    @Override
    public boolean supports(ICommand cmd) {
        return cmd instanceof MoveCommand;
    }

    @Override
    public void handle(ICommand cmd, GameRoom room) {
        MoveCommand mc = (MoveCommand) cmd;
        room.getLogic().movePlayer(mc.getPlayerId(), mc.getX(), mc.getY());
    }
}
