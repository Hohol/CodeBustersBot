package game;

public class BusterBuilder {
    private int x;
    private int y;
    private boolean isCarryingGhost = false;
    private int id;
    private int remainingStunDuration = 0;
    private int remainingStunCooldown = 0;

    BusterBuilder(int x, int y, int id) {
        this.x = x;
        this.y = y;
    }

    Buster build() {
        return new Buster(id, x, y, isCarryingGhost, remainingStunDuration, remainingStunCooldown);
    }

    public BusterBuilder carryingGhost() {
        isCarryingGhost = true;
        return this;
    }

    public BusterBuilder stunCooldown(int cooldown) {
        this.remainingStunCooldown = cooldown;
        return this;
    }

    public BusterBuilder stunDuration(int duration) {
        this.remainingStunDuration = duration;
        return this;
    }
}
