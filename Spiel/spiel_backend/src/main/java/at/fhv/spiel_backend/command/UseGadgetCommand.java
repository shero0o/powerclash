package at.fhv.spiel_backend.command;


import at.fhv.spiel_backend.server.GameRoom;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UseGadgetCommand implements ICommand {
    private final String playerId;

    @Override
    public void execute(GameRoom room) {
        room.getLogic().useGadget(playerId);
    }
}
