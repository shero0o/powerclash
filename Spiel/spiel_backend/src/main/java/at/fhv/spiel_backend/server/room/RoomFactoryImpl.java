package at.fhv.spiel_backend.server.room;

import at.fhv.spiel_backend.logic.DefaultGameLogic;
import at.fhv.spiel_backend.logic.GameLogic;
import at.fhv.spiel_backend.server.EventPublisher;
import at.fhv.spiel_backend.server.game.GameRoomImpl;
import at.fhv.spiel_backend.server.game.IGameRoom;
import at.fhv.spiel_backend.server.map.IMapFactory;
import org.springframework.stereotype.Component;

@Component
public class RoomFactoryImpl implements IRoomFactory {
    private final IMapFactory mapFactory;
    private final EventPublisher eventPublisher;

    public RoomFactoryImpl(IMapFactory mapFactory, EventPublisher eventPublisher) {
        this.mapFactory = mapFactory;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public IGameRoom createRoom(String levelId) {
        return new GameRoomImpl(mapFactory, new DefaultGameLogic(), eventPublisher, levelId);
    }

}
