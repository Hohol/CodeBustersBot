package game;

public class Ghost {
    public final int x, y;
    public final int id;
    public final int stamina;
    public final int bustCnt;

    public Ghost(int id, int x, int y, int stamina, int bustCnt) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.stamina = stamina;
        this.bustCnt = bustCnt;
    }

    @Override
    public String toString() {
        return "Ghost{" +
                "x=" + x +
                ", y=" + y +
                ", id=" + id +
                ", stamina=" + stamina +
                ", bustCnt=" + bustCnt +
                '}';
    }
}
