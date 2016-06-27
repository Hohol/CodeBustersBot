package game;

public class Buster {
    public final int x;
    public final int y;
    public final boolean isCarryingGhost;
    public final int id;
    public final int remainingStunDuration;
    public final int remainingStunCooldown;

    public Buster(int id, int x, int y, boolean isCarryingGhost, int remainingStunDuration, int remainingStunCooldown) {
        this.x = x;
        this.y = y;
        this.isCarryingGhost = isCarryingGhost;
        this.id = id;
        this.remainingStunDuration = remainingStunDuration;
        this.remainingStunCooldown = remainingStunCooldown;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Buster{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", isCarryGh=" + isCarryingGhost +
                ", remStDur=" + remainingStunDuration +
                ", remStCd=" + remainingStunCooldown +
                '}';
    }
}
