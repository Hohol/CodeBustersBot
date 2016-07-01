package game;

public class CheckPoint {
    public static final int NEVER = -1;
    public final Point p;
    public int lastSeen;

    public CheckPoint(Point p) {
        this.p = p;
        lastSeen = NEVER;
    }

    @Override
    public String toString() {
        return "CheckPoint{" +
                "p=" + p +
                ", lastSeen=" + lastSeen +
                '}';
    }
}
