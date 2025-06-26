package at.fhv.spiel_service.service.game.manager.state;

import at.fhv.spiel_service.messaging.StateUpdateMessage;

public interface StateUpdateManager {
    StateUpdateMessage buildStateUpdate();
}
