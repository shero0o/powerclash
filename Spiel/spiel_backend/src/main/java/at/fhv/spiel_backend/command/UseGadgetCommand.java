package at.fhv.spiel_backend.command;


import at.fhv.spiel_backend.server.game.IGameRoom;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UseGadgetCommand implements ICommand {
    private final String playerId;

    @Override
    public void execute(IGameRoom room) {
        room.getLogic().useGadget(playerId);
    }
}
