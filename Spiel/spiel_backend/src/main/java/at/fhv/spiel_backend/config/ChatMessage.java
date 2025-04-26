package at.fhv.spiel_backend.config;

public class ChatMessage {
    private String playerId;
    private String text;

    // Getter und Setter
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

