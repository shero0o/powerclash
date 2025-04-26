package at.fhv.spiel_backend.server.room;

import at.fhv.spiel_backend.handler.CommandProcessor;
import at.fhv.spiel_backend.logic.GameLogic;
import at.fhv.spiel_backend.server.EventPublisher;
import at.fhv.spiel_backend.server.game.GameRoomImpl;
import at.fhv.spiel_backend.server.game.IGameRoom;
import at.fhv.spiel_backend.server.map.IMapFactory;
import org.springframework.stereotype.Component;


@Component
public class RoomFactoryImpl implements IRoomFactory{
    private final IMapFactory mapFactory;
    private final CommandProcessor commandProcessor;
    private final GameLogic gameLogic;
    private final EventPublisher eventPublisher;

    public RoomFactoryImpl(IMapFactory mapFactory,
                           CommandProcessor commandProcessor,
                           GameLogic gameLogic,
                           EventPublisher eventPublisher) {
        this.mapFactory = mapFactory;
        this.commandProcessor = commandProcessor;
        this.gameLogic = gameLogic;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public IGameRoom createRoom() {
        return new GameRoomImpl(mapFactory, commandProcessor, gameLogic, eventPublisher);
    }
}
