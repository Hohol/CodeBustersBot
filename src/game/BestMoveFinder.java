package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static game.Move.*;
import static game.Utils.*;

public class BestMoveFinder {

    private final GameParameters gameParameters;
    private final Evaluator evaluator;

    public BestMoveFinder(GameParameters gameParameters) {
        this.gameParameters = gameParameters;
        evaluator = new Evaluator(gameParameters);
    }

    public Move findBestMove(
            Buster buster,
            Point myBase,
            List<Buster> allies,
            List<Buster> enemies,
            List<Ghost> ghosts,
            List<CheckPoint> checkPoints,
            Set<Integer> alreadyStunnedEnemies,
            Set<Integer> alreadyBusted
    ) {
        if (buster.remainingStunDuration > 0) {
            return release();
        }

        Move move;
        if ((move = tryReleaseGhost(buster, myBase)) != null) {
            return move;
        }
        if ((move = tryStunEnemy(buster, enemies, alreadyStunnedEnemies)) != null) {
            return move;
        }
        boolean iVeSeenItAll = checkIVeSeenItAll(checkPoints, myBase);
        if (!iVeSeenItAll) {
            ghosts = removeFatGhosts(ghosts);
        }
        Point checkPoint = getCheckPoint(buster, checkPoints);
        return trySomethingSmart(buster, myBase, allies, enemies, ghosts, checkPoint, alreadyBusted);
    }

    private List<Ghost> removeFatGhosts(List<Ghost> ghosts) {
        List<Ghost> r = new ArrayList<>();
        for (Ghost ghost : ghosts) {
            if (ghost.stamina < 30) {
                r.add(ghost);
            }
        }
        return r;
    }

    private Move trySomethingSmart(Buster buster, Point myBase, List<Buster> allies, List<Buster> enemies, List<Ghost> ghosts, Point checkPoint, Set<Integer> alreadyBusted) {
        List<Move> possibleMoves = new ArrayList<>();
        possibleMoves.add(move(moveToWithAllowedRange(buster.x, buster.y, myBase.x, myBase.y, gameParameters.MOVE_RANGE, gameParameters.RELEASE_RANGE)));
        possibleMoves.add(move(buster.x, buster.y));
        for (Buster enemy : enemies) {
            possibleMoves.add(move(runawayPoint(enemy.x, enemy.y, buster.x, buster.y, gameParameters.MOVE_RANGE)));
        }
        for (Ghost ghost : ghosts) {
            if (dist(buster, ghost) >= gameParameters.MIN_BUST_RANGE && dist(buster, ghost) <= gameParameters.MAX_BUST_RANGE) {
                possibleMoves.add(bust(ghost.id));
            }
            possibleMoves.add(move(ghost.x, ghost.y));
        }
        possibleMoves.add(move(checkPoint));

        Move bestMove = null;
        EvaluationState bestEvaluation = null;
        for (Move move : possibleMoves) {
            Point newPosition = getNewPosition(buster, move);
            EvaluationState evaluation = evaluator.evaluate(buster, newPosition, myBase, allies, enemies, ghosts, move, checkPoint, alreadyBusted);
            if (evaluation.better(bestEvaluation)) {
                bestEvaluation = evaluation;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private Point getNewPosition(Buster buster, Move move) {
        if (move.type != MoveType.MOVE) {
            return new Point(buster.x, buster.y);
        }
        double dist = dist(buster.x, buster.y, move.x, move.y);
        if (dist <= gameParameters.MOVE_RANGE) {
            return new Point(move.x, move.y);
        }
        double w = gameParameters.MOVE_RANGE / dist;
        double dx = (move.x - buster.x) * w;
        double dy = (move.y - buster.y) * w;
        return Point.round(buster.x + dx, buster.y + dy);
    }

    private Move tryReleaseGhost(Buster buster, Point myBase) {
        if (!buster.isCarryingGhost) {
            return null;
        }
        if (dist(buster, myBase) <= gameParameters.RELEASE_RANGE) {
            return release();
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
            if (enemy.remainingStunDuration > 1) {
                continue;
            }
            if (alreadyStunnedEnemies.contains(enemy.id)) {
                continue;
            }
            if (dist(buster, enemy) <= gameParameters.STUN_RANGE) {
                return stun(enemy.getId());
            }
        }
        return null;
    }

    private Move tryGoToNearestGhost(Buster buster, List<Ghost> ghosts, boolean iVeSeenItAll, Point myBase) {
        Ghost ghost = pickNearestGhost(buster, ghosts, iVeSeenItAll);
        if (ghost == null) {
            return null;
        }
        if (dist(buster, ghost) >= gameParameters.MAX_BUST_RANGE) {
            return move(ghost.x, ghost.y);
        } else {
            return move(myBase);
        }
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
        Point enemyBase = new Point(gameParameters.H - myBase.x, gameParameters.W - myBase.y);
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
            if (range >= gameParameters.MIN_BUST_RANGE && range <= gameParameters.MAX_BUST_RANGE) {
                return ghost;
            }
        }
        return null;
    }

}
