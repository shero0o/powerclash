package at.fhv.spiel_backend.handler;

import at.fhv.spiel_backend.command.ICommand;
import at.fhv.spiel_backend.command.UseGadgetCommand;
import at.fhv.spiel_backend.server.GameRoom;
import org.springframework.stereotype.Component;

@Component
public class UseGadgetCommandHandler implements ICommandHandler {
    @Override
    public boolean supports(ICommand cmd) {
        return cmd instanceof UseGadgetCommand;
    }

    @Override
    public void handle(ICommand cmd, GameRoom room) {
        UseGadgetCommand ug = (UseGadgetCommand) cmd;
        room.getLogic().useGadget(ug.getPlayerId());
    }
}
