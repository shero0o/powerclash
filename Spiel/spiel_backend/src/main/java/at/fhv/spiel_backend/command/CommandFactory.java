package at.fhv.spiel_backend.command;

import at.fhv.spiel_backend.ws.AttackPayload;
import at.fhv.spiel_backend.ws.InputMessage;
import at.fhv.spiel_backend.ws.MovePayload;

import java.util.Map;

public class CommandFactory {

    public static ICommand move(String playerId, float x, float y) {
        return new MoveCommand(playerId, x, y);
    }

    public static ICommand attack(String playerId, float targetX, float targetY) {
        return new AttackCommand(playerId, targetX, targetY);
    }

    public static ICommand useGadget(String playerId) {
        return new UseGadgetCommand(playerId);
    }

    /**
     * Builds a command from a WebSocket InputMessage.
     */
    public static ICommand from(InputMessage im) {
        return switch (im.getType()) {
            case MOVE -> {
                MovePayload mp = (MovePayload) im.getPayload();
                yield move(im.getPlayerId(), mp.getX(), mp.getY());
            }
            case ATTACK -> {
                AttackPayload ap = (AttackPayload) im.getPayload();
                yield attack(im.getPlayerId(), ap.getTargetX(), ap.getTargetY());
            }
            case USE_GADGET -> useGadget(im.getPlayerId());
            default -> throw new IllegalArgumentException("Unknown action type: " + im.getType());
        };
    }
}

