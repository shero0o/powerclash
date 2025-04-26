package at.fhv.spiel_backend.command;

import at.fhv.spiel_backend.server.GameRoom;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MoveCommand implements ICommand {
    private final String playerId;
    private final float x;
    private final float y;

    @Override
    public void execute(GameRoom room) {
        room.getLogic().movePlayer(playerId, x, y);
    }
}
