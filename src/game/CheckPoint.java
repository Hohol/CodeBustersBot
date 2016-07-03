package game;

public class CheckPoint {
    public static final int NEVER = -2;
    public final Point p;
    public int lastSeen;

    public CheckPoint(Point p, int lastSeen) {
        this.p = p;
        this.lastSeen = lastSeen;
    }

    @Override
    public String toString() {
        return "CheckPoint{" +
                "p=" + p +
                ", lastSeen=" + lastSeen +
                '}';
    }
}
