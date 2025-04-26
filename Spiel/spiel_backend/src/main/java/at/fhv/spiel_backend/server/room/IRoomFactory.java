package at.fhv.spiel_backend.server.room;

import at.fhv.spiel_backend.server.game.IGameRoom;

public interface IRoomFactory {
    IGameRoom createRoom();
}
