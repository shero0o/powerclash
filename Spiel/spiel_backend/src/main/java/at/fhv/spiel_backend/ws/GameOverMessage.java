// File: at/fhv/spiel_backend/ws/GameOverMessage.java
package at.fhv.spiel_backend.ws;

public class GameOverMessage {
    private String winnerId;

    public GameOverMessage() { }

    public GameOverMessage(String winnerId) {
        this.winnerId = winnerId;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }
}
