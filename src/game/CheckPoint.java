package game;

public class CheckPoint {
    public final Point p;
    public int lastSeen;

    public CheckPoint(Point p) {
        this.p = p;
    }

    @Override
    public String toString() {
        return "CheckPoint{" +
                "p=" + p +
                ", lastSeen=" + lastSeen +
                '}';
    }
}
