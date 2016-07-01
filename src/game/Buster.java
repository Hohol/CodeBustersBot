package game;

public class Buster {
    public final int id;
    public final int x;
    public final int y;
    public final boolean isCarryingGhost;
    public final int remainingStunDuration;
    public final int remainingStunCooldown;
    public final int ghostId;
    public final int lastSeen;

    public Buster(int id, int x, int y, boolean isCarryingGhost, int remainingStunDuration, int remainingStunCooldown, int ghostId, int lastSeen) {
        this.x = x;
        this.y = y;
        this.isCarryingGhost = isCarryingGhost;
        this.id = id;
        this.remainingStunDuration = remainingStunDuration;
        this.remainingStunCooldown = remainingStunCooldown;
        this.ghostId = ghostId;
        this.lastSeen = lastSeen;
    }

    public int getId() {
        return id;
    }

    public boolean hasStun() {
        return remainingStunCooldown == 0;
    }

    @Override
    public String toString() {
        return "Buster{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", carrGh=" + isCarryingGhost +
                ", stDur=" + remainingStunDuration +
                ", stCd=" + remainingStunCooldown +
                ", ghId=" + ghostId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        Buster buster = (Buster) o;

        if (x != buster.x) return false;
        if (y != buster.y) return false;
        if (isCarryingGhost != buster.isCarryingGhost) return false;
        if (id != buster.id) return false;
        if (remainingStunDuration != buster.remainingStunDuration) return false;
        if (remainingStunCooldown != buster.remainingStunCooldown) return false;
        if (ghostId != buster.ghostId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + (isCarryingGhost ? 1 : 0);
        result = 31 * result + id;
        result = 31 * result + remainingStunDuration;
        result = 31 * result + remainingStunCooldown;
        result = 31 * result + ghostId;
        return result;
    }
}
