package game;

public class Buster {
    public final int x;
    public final int y;
    public final boolean isCarryingGhost;
    public final int id;

    public Buster(int x, int y, boolean isCarryingGhost, int id) {
        this.x = x;
        this.y = y;
        this.isCarryingGhost = isCarryingGhost;
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
