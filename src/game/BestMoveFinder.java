package game;

import java.util.*;

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
            if (ghost.stamina < 30) { // magic
                r.add(ghost);
            }
        }
        return r;
    }

    private Move trySomethingSmart(Buster buster, Point myBase, List<Buster> allies, List<Buster> enemies, List<Ghost> ghosts, Point checkPoint, Set<Integer> alreadyBusted) {
        Set<Move> possibleMoves = new LinkedHashSet<>();
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
        for (Buster enemy : enemies) {
            possibleMoves.add(move(enemy.x, enemy.y));
        }

        Move bestMove = null;
        EvaluationState bestEvaluation = null;
        for (Move move : possibleMoves) {
            Point newPosition = getNewPosition(buster, move, this.gameParameters);
            EvaluationState evaluation = evaluator.evaluate(buster, newPosition, myBase, allies, enemies, ghosts, move, checkPoint, alreadyBusted);
            if (evaluation.better(bestEvaluation)) {
                bestEvaluation = evaluation;
                bestMove = move;
            }
        }
        return bestMove;
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

    private boolean checkIVeSeenItAll(List<CheckPoint> checkPoints, Point myBase) {
        GameParameters gameParameters = this.gameParameters;
        Point enemyBase = getEnemyBase(myBase, gameParameters);
        for (CheckPoint checkPoint : checkPoints) {
            if (dist(checkPoint.p, myBase) <= dist(checkPoint.p, enemyBase)) {
                if (checkPoint.lastSeen == CheckPoint.NEVER) {
                    return false;
                }
            }
        }
        return true;
    }

}
