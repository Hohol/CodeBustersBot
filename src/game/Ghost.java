package game;

public class Ghost {
    public final int x, y;
    public final int id;
    public final int stamina;

    public Ghost(int id, int x, int y, int stamina) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.stamina = stamina;
    }
}
