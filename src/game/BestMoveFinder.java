package game;

import java.util.List;
import java.util.Set;

import static game.Utils.*;

public class BestMoveFinder {
    public Move findBestMove(Buster buster, Point myBase, List<Buster> enemies, List<Ghost> ghosts, List<CheckPoint> checkPoints, Set<Integer> alreadyStunnedEnemies) {
        if (buster.remainingStunDuration > 0) {
            return Move.release();
        }

        boolean iVeSeenItAll = checkIVeSeenItAll(checkPoints, myBase);

        Move move;
        if ((move = tryReleaseGhost(buster, myBase)) != null) {
            return move;
        }
        if ((move = tryStunEnemy(buster, enemies, alreadyStunnedEnemies)) != null) {
            return move;
        }
        if ((move = tryCarryGhost(buster, myBase)) != null) {
            return move;
        }
        if ((move = tryBustGhost(buster, ghosts, iVeSeenItAll)) != null) {
            return move;
        }
        if ((move = tryGoToNearestGhost(buster, ghosts, iVeSeenItAll, myBase)) != null) {
            return move;
        }

        Point dest = getCheckPoint(buster, checkPoints);
        return Move.move(dest);
    }

    private Move tryReleaseGhost(Buster buster, Point myBase) {
        if (!buster.isCarryingGhost) {
            return null;
        }
        if (dist(buster, myBase) <= RELEASE_RANGE) {
            return Move.release();
        }
        return null;
    }

    private Point getCheckPoint(Buster buster, List<CheckPoint> checkPoints) {
        int minLastSeen = Integer.MAX_VALUE;
        double minDist = Double.POSITIVE_INFINITY;
        Point r = null;
        for (CheckPoint checkPoint : checkPoints) {
            double dist = dist(buster, checkPoint.p);
            if (checkPoint.lastSeen < minLastSeen || checkPoint.lastSeen == minLastSeen && dist < minDist) {
                minLastSeen = checkPoint.lastSeen;
                minDist = dist;
                r = checkPoint.p;
            }
        }
        return r;
    }

    private Move tryStunEnemy(Buster buster, List<Buster> enemies, Set<Integer> alreadyStunnedEnemies) {
        if (buster.remainingStunCooldown > 0) {
            return null;
        }
        for (Buster enemy : enemies) {
            if (enemy.remainingStunDuration > 0) {
                continue; // todo stun if remainingStunDuration == 1?
            }
            if (alreadyStunnedEnemies.contains(enemy.id)) {
                continue;
            }
            if (dist(buster, enemy) <= STUN_RANGE) {
                return Move.stun(enemy.getId());
            }
        }
        return null;
    }

    private Move tryGoToNearestGhost(Buster buster, List<Ghost> ghosts, boolean iVeSeenItAll, Point myBase) {
        Ghost ghost = pickNearestGhost(buster, ghosts, iVeSeenItAll);
        if (ghost == null) {
            return null;
        }
        if (dist(buster, ghost) >= MAX_BUST_RANGE) {
            return Move.move(ghost.x, ghost.y);
        } else {
            return Move.move(myBase);
        }
    }

    private Move tryBustGhost(Buster buster, List<Ghost> ghosts, boolean iVeSeenItAll) {
        Ghost bustableGhost = pickBustableGhost(buster, ghosts, iVeSeenItAll);
        if (bustableGhost == null) {
            return null;
        }
        return Move.bust(bustableGhost.id);
    }

    private Move tryCarryGhost(Buster buster, Point myBasePosition) {
        if (!buster.isCarryingGhost) {
            return null;
        }
        return Move.move(
                moveToWithAllowedRange(buster.x, buster.y, myBasePosition.x, myBasePosition.y, MOVE_DIST, RELEASE_RANGE)
        );
    }

    private Ghost pickNearestGhost(Buster buster, List<Ghost> ghosts, boolean iVeSeenItAll) {
        Ghost nearest = null;
        double minDist = Double.POSITIVE_INFINITY;
        for (Ghost ghost : ghosts) {
            if (shouldSkipGhost(ghost, iVeSeenItAll)) { // magic
                continue;
            }
            double dist = dist(buster, ghost);
            if (dist < minDist) {
                minDist = dist;
                nearest = ghost;
            }
        }
        return nearest;
    }

    private boolean shouldSkipGhost(Ghost ghost, boolean iVeSeenItAll) {
        return ghost.stamina >= 30 && !iVeSeenItAll;
    }

    private boolean checkIVeSeenItAll(List<CheckPoint> checkPoints, Point myBase) {
        Point enemyBase = new Point(H - myBase.x, W - myBase.y);
        for (CheckPoint checkPoint : checkPoints) {
            if (dist(checkPoint.p, myBase) <= dist(checkPoint.p, enemyBase)) {
                if (checkPoint.lastSeen == CheckPoint.NEVER) {
                    return false;
                }
            }
        }
        return true;
    }

    private Ghost pickBustableGhost(Buster buster, List<Ghost> ghosts, boolean iVeSeenItAll) {
        for (Ghost ghost : ghosts) {
            if (shouldSkipGhost(ghost, iVeSeenItAll)) {
                continue;
            }
            double range = dist(buster, ghost);
            if (range >= MIN_BUST_RANGE && range <= MAX_BUST_RANGE) {
                return ghost;
            }
        }
        return null;
    }

}
