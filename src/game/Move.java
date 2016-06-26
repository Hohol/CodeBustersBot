package game;

public class Move {
    public final MoveType type;
    public final int x, y;
    public final int targetId;

    public Move(MoveType type, int x, int y, int targetId) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.targetId = targetId;
    }

    public static Move move(int x, int y) {
        return new Move(MoveType.MOVE, x, y, -1);
    }

    public static Move release() {
        return new Move(MoveType.RELEASE, -1, -1, -1);
    }

    public static Move bust(int ghostId) {
        return new Move(MoveType.BUST, -1, -1, ghostId);
    }

    public static Move stun(int enemyId) {
        return new Move(MoveType.STUN, -1, -1, enemyId);
    }

    public static Move move(Point p) {
        return move(p.x, p.y);
    }

    public String toInteractorString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append(" ");
        //noinspection StatementWithEmptyBody
        if (type == MoveType.RELEASE) {
        } else if (type == MoveType.BUST) {
            sb.append(targetId);
        } else if (type == MoveType.MOVE) {
            sb.append(y).append(" ").append(x);

        } else if (type == MoveType.STUN) {
            sb.append(targetId);
        } else {
            throw new RuntimeException();
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        Move move = (Move) o;

        if (x != move.x) return false;
        if (y != move.y) return false;
        if (targetId != move.targetId) return false;
        if (type != move.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + targetId;
        return result;
    }

    @Override
    public String toString() {
        return "Move{" +
                "type=" + type +
                ", x=" + x +
                ", y=" + y +
                ", targetId=" + targetId +
                '}';
    }
}
