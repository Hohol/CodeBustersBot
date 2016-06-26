package game;

import java.util.List;
import java.util.Random;

import static game.Utils.*;
import static game.Utils.dist;

public class BestMoveFinder {
    static final int MAX_BUST_RANGE = 1760;
    static final int MIN_BUST_RANGE = 900;
    static final int RELEASE_RANGE = 1600;
    static final int FOG_RANGE = 2200;
    static final int STUN_RANGE = 1760;
    static final int STUN_COOLDOWN = 20;
    static final int STUN_DURATION = 10;

    Random rnd = new Random();

    public Move findBestMove(Buster buster, Point myBasePosition, List<Buster> enemies, List<Ghost> ghosts, Point[] destinations) {
        if (buster.remainingStunDuration > 0) {
            return Move.release();
        }

        Move move;
        if ((move = tryCarryGhost(buster, myBasePosition)) != null) {
            return move;
        }
        if ((move = tryStunEnemy(buster, enemies)) != null) {
            return move;
        }
        if ((move = tryBustGhost(buster, ghosts)) != null) {
            return move;
        }
        if ((move = tryGoToNearestGhost(buster, ghosts)) != null) {
            return move;
        }

        Point dest = getRandomDestination(buster, destinations);
        return Move.move(dest);
    }

    private Move tryStunEnemy(Buster buster, List<Buster> enemies) {
        for (Buster enemy : enemies) {
            if (enemy.remainingStunDuration > 0) {
                continue; // todo stun if remainingStunDuration == 1?
            }
            if (dist(buster, enemy) <= STUN_RANGE) {
                return Move.stun(enemy.getId());
            }
        }
        return null;
    }

    private Move tryGoToNearestGhost(Buster buster, List<Ghost> ghosts) {
        if (ghosts.isEmpty()) {
            return null;
        }
        Ghost ghost = pickNearestGhost(buster, ghosts);
        return Move.move(ghost.x, ghost.y);
    }

    private Move tryBustGhost(Buster buster, List<Ghost> ghosts) {
        Ghost bustableGhost = pickBustableGhost(buster, ghosts);
        if (bustableGhost == null) {
            return null;
        }
        return Move.bust(bustableGhost.id);
    }

    private Move tryCarryGhost(Buster buster, Point myBasePosition) {
        if (!buster.isCarryingGhost) {
            return null;
        }
        if (dist(buster, myBasePosition) <= RELEASE_RANGE) {
            return Move.release();
        } else {
            return Move.move(myBasePosition);
        }
    }

    private Ghost pickNearestGhost(Buster buster, List<Ghost> ghosts) {
        Ghost nearest = null;
        double minDist = Double.POSITIVE_INFINITY;
        for (Ghost ghost : ghosts) {
            double dist = dist(buster, ghost);
            if (dist < minDist) {
                minDist = dist;
                nearest = ghost;
            }
        }
        return nearest;
    }

    private Point getRandomDestination(Buster buster, Point[] destinations) {
        Point oldDestination = destinations[buster.id];
        if (oldDestination == null || oldDestination.x == buster.x && oldDestination.y == buster.y) {
            destinations[buster.id] = new Point(rnd.nextInt(H + 1), rnd.nextInt(W + 1));
        }
        return destinations[buster.id];
    }

    private Ghost pickBustableGhost(Buster buster, List<Ghost> ghosts) {
        for (Ghost ghost : ghosts) {
            double range = dist(buster, ghost);
            if (range >= MIN_BUST_RANGE && range <= MAX_BUST_RANGE) {
                return ghost;
            }
        }
        return null;
    }
}
