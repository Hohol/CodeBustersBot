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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        Ghost ghost = (Ghost) o;

        if (x != ghost.x) return false;
        if (y != ghost.y) return false;
        if (id != ghost.id) return false;
        if (stamina != ghost.stamina) return false;
        if (bustCnt != ghost.bustCnt) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + id;
        result = 31 * result + stamina;
        result = 31 * result + bustCnt;
        return result;
    }
}
