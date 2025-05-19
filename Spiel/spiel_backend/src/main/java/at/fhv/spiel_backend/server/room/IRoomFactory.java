package at.fhv.spiel_backend.server.room;

import at.fhv.spiel_backend.server.game.IGameRoom;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;



public interface IRoomFactory {
    IGameRoom createRoom(String levelId);
}

