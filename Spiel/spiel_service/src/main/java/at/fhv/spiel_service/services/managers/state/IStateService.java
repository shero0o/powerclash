// src/main/java/at/fhv/spiel_service/services/managers/state/IStateService.java
package at.fhv.spiel_service.services.managers.state;

import at.fhv.spiel_service.messaging.StateUpdateMessage;

public interface IStateService {
    StateUpdateMessage buildStateUpdate();
}
