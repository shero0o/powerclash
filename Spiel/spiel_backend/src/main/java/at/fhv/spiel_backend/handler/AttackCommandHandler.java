package at.fhv.spiel_backend.handler;

import at.fhv.spiel_backend.command.AttackCommand;
import at.fhv.spiel_backend.command.ICommand;
import at.fhv.spiel_backend.server.game.IGameRoom;
import org.springframework.stereotype.Component;

@Component
public class AttackCommandHandler implements ICommandHandler {
    @Override
    public boolean supports(ICommand cmd) {
        return cmd instanceof AttackCommand;
    }

    @Override
    public void handle(ICommand cmd, IGameRoom room) {
        AttackCommand ac = (AttackCommand) cmd;
        room.getLogic().playerAttack(ac.getPlayerId(), ac.getTargetX(), ac.getTargetY());
    }
}

