package at.fhv.spiel_backend.handler;

import at.fhv.spiel_backend.command.ICommand;
import at.fhv.spiel_backend.server.game.IGameRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommandProcessor {
    private final List<ICommandHandler> handlers;

    /**
     * Process a command within the given room.
     */
    public void process(ICommand cmd, IGameRoom room) {
        handlers.stream()
                .filter(h -> h.supports(cmd))
                .findFirst()
                .ifPresent(h -> h.handle(cmd, room));
    }
}
