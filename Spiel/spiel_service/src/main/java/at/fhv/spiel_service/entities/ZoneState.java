package at.fhv.spiel_service.entities;

public class ZoneState {
    private Position center;
    private float    radius;
    private long     timeMsRemaining;

    public ZoneState() {}

    public ZoneState(Position center, float radius, long timeMsRemaining) {
        this.center           = center;
        this.radius           = radius;
        this.timeMsRemaining  = timeMsRemaining;
    }

    public Position getCenter() { return center; }
    public void setCenter(Position center) { this.center = center; }

    public float getRadius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }

    public long getTimeMsRemaining() { return timeMsRemaining; }
    public void setTimeMsRemaining(long timeMsRemaining) {
        this.timeMsRemaining = timeMsRemaining;
    }
}

