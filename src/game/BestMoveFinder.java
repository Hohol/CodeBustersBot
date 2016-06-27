package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static game.Utils.*;
import static java.lang.Math.*;

public class BestMoveFinder {

    private final GameParameters gameParameters;
    private final Evaluator evaluator;

    public BestMoveFinder(GameParameters gameParameters) {
        this.gameParameters = gameParameters;
        evaluator = new Evaluator(gameParameters);
    }

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
        if ((move = tryCarryGhostAndAvoidEnemies(buster, myBase, enemies)) != null) {
            return move;
        }
        /*if ((move = tryCarryGhost(buster, myBase)) != null) {
            return move;
        }*/
        if ((move = tryBustGhost(buster, ghosts, iVeSeenItAll)) != null) {
            return move;
        }
        if ((move = tryGoToNearestGhost(buster, ghosts, iVeSeenItAll, myBase)) != null) {
            return move;
        }

        Point dest = getCheckPoint(buster, checkPoints);
        return Move.move(dest);
    }

    private Move tryCarryGhostAndAvoidEnemies(Buster buster, Point myBase, List<Buster> enemies) {
        if (!buster.isCarryingGhost) {
            return null;
        }
        List<Move> possibleMoves = new ArrayList<>();
        possibleMoves.add(Move.move(moveToWithAllowedRange(buster.x, buster.y, myBase.x, myBase.y, gameParameters.MOVE_RANGE, gameParameters.RELEASE_RANGE)));
        possibleMoves.add(Move.move(buster.x, buster.y));
        for (Buster enemy : enemies) {
            possibleMoves.add(runawayMove(buster, enemy));
        }

        Move bestMove = null;
        EvaluationState bestEvaluation = null;
        for (Move move : possibleMoves) {
            Point newPosition = getNewPosition(buster, move);
            EvaluationState evaluation = evaluator.evaluate(buster, newPosition, myBase, enemies);
            if (evaluation.better(bestEvaluation)) {
                bestEvaluation = evaluation;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private Move runawayMove(Buster buster, Buster enemy) {
        if (buster.x == enemy.x && buster.y == enemy.y) {
            return Move.move(buster.x, buster.y);
        }
        double alpha = atan2(buster.y - enemy.y, buster.x - enemy.x);
        double newX = buster.x + gameParameters.MOVE_RANGE * cos(alpha);
        double newY = buster.y + gameParameters.MOVE_RANGE * sin(alpha);
        return Move.move(Point.round(newX, newY));
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
            if (dist(buster, enemy) <= gameParameters.STUN_RANGE) {
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
        if (dist(buster, ghost) >= gameParameters.MAX_BUST_RANGE) {
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
