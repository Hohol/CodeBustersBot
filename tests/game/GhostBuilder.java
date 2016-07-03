package game;

public class GhostBuilder {
    int x, y;
    int id;
    int stamina;
    int bustCnt;

    public GhostBuilder(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public Ghost build() {
        return new Ghost(id, x, y, stamina, bustCnt);
    }
    public GhostBuilder stamina(int stamina) {
        this.stamina = stamina;
        return this;
    }
}
