package game;

public class Buster {
    public final int x;
    public final int y;
    public final boolean isCarryingGhost;
    public final int id;
    public final int remainingStunDuration;

    public Buster(int id, int x, int y, boolean isCarryingGhost, int remainingStunDuration) {
        this.x = x;
        this.y = y;
        this.isCarryingGhost = isCarryingGhost;
        this.id = id;
        this.remainingStunDuration = remainingStunDuration;
    }

    public int getId() {
        return id;
    }
}
