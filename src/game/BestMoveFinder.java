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

    public Move findBestMove(Buster buster, Point myBasePosition, List<Ghost> ghosts, Point[] destinations) {
        if (buster.remainingStunDuration > 0) {
            return Move.release();
        }
        if (buster.isCarryingGhost) {
            if (dist(buster, myBasePosition) <= RELEASE_RANGE) {
                return Move.release();
            } else {
                return Move.move(myBasePosition);
            }
        } else {
            Ghost targetGhost = pickGhost(buster, ghosts);
            if (targetGhost == null) {
                Point dest = getDestination(buster, destinations);
                return Move.move(dest);
            } else {
                return Move.bust(targetGhost.id);
            }
        }
    }

    private Point getDestination(Buster buster, Point[] destinations) {
        Point oldDestination = destinations[buster.id];
        if (oldDestination == null || oldDestination.x == buster.x && oldDestination.y == buster.y) {
            destinations[buster.id] = new Point(rnd.nextInt(H + 1), rnd.nextInt(W + 1));
        }
        return destinations[buster.id];
    }

    private Ghost pickGhost(Buster buster, List<Ghost> ghosts) {
        for (Ghost ghost : ghosts) {
            double range = dist(buster, ghost);
            if (range >= MIN_BUST_RANGE && range <= MAX_BUST_RANGE) {
                return ghost;
            }
        }
        return null;
    }
}
