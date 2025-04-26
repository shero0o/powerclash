package at.fhv.spiel_backend.command;

import at.fhv.spiel_backend.server.GameRoom;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttackCommand implements ICommand {
    private final String playerId;
    private final float targetX;
    private final float targetY;

    @Override
    public void execute(GameRoom room) {
        room.getLogic().playerAttack(playerId, targetX, targetY);
    }
}


