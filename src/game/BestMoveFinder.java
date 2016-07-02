package game;

import java.util.*;

import static game.Move.*;
import static game.Utils.*;

public class BestMoveFinder {

    private final GameParameters gameParameters;
    private final Evaluator evaluator;
    private final PhantomUpdater phantomUpdater;

    public BestMoveFinder(GameParameters gameParameters) {
        this.gameParameters = gameParameters;
        evaluator = new Evaluator(gameParameters);
        phantomUpdater = new PhantomUpdater(gameParameters);
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
        return trySomethingSmart(buster, myBase, allies, enemies, ghosts, checkPoint, alreadyBusted, checkPoints);
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

    private Move trySomethingSmart(
            Buster buster,
            Point myBase,
            List<Buster> allies,
            List<Buster> enemies,
            List<Ghost> ghosts,
            Point checkPoint,
            Set<Integer> alreadyBusted,
            List<CheckPoint> checkPoints
    ) {
        Set<Move> possibleMoves = new LinkedHashSet<>();
        possibleMoves.add(move(moveToWithAllowedRange(buster.x, buster.y, myBase.x, myBase.y, gameParameters.RELEASE_RANGE)));
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
        for (CheckPoint point : checkPoints) {
            possibleMoves.add(move(point.p));
        }
        for (Buster enemy : enemies) {
            possibleMoves.add(move(enemy.x, enemy.y));
        }
        List<List<Buster>> enemiesWithGhostNextPositions = getEnemiesWithGhostNextPositions(enemies, getEnemyBase(myBase, gameParameters));
        for (List<Buster> list : enemiesWithGhostNextPositions) {
            for (Buster enemyPosition : list) {
                possibleMoves.add(move(enemyPosition.x, enemyPosition.y));
            }
        }
        boolean someOfUsCanCatchSomeEnemyWithGhost = checkSomeOfUsCanCatchEnemyWithGhost(allies, enemies, enemiesWithGhostNextPositions);
        List<Buster> alliesWhoNeedEscort = getAlliesWhoNeedEscort(allies, enemies, myBase);
        for (Buster ally : alliesWhoNeedEscort) {
            possibleMoves.add(move(ally.x, ally.y));
            Point nextPosition = getPositionAfterMovingToBase(ally, myBase, gameParameters);
            possibleMoves.add(move(nextPosition));
            possibleMoves.add(move(moveToBeOutsideRange(buster.x, buster.y, nextPosition.x, nextPosition.y, gameParameters.MIN_BUST_RANGE)));
        }

        Move bestMove = null;
        EvaluationState bestEvaluation = null;
        for (Move move : possibleMoves) {
            Point newPosition = getNewPosition(buster, move, this.gameParameters);
            EvaluationState evaluation = evaluator.evaluate(
                    buster,
                    newPosition,
                    myBase,
                    allies,
                    enemies,
                    ghosts,
                    move,
                    checkPoint,
                    alreadyBusted,
                    enemiesWithGhostNextPositions,
                    alliesWhoNeedEscort,
                    someOfUsCanCatchSomeEnemyWithGhost
            );
            if (evaluation.better(bestEvaluation)) {
                bestEvaluation = evaluation;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private boolean checkSomeOfUsCanCatchEnemyWithGhost(List<Buster> allies, List<Buster> enemies, List<List<Buster>> enemiesWithGhostNextPositions) {
        for (Buster enemy : enemies) {
            if (!enemy.isCarryingGhost) {
                continue;
            }
            for (Buster ally : allies) {
                if (ally.remainingStunDuration == 0 && ally.hasStun() && dist(ally, enemy) <= gameParameters.STUN_RANGE) {
                    return true;
                }
            }
        }
        for (List<Buster> enemiesWithGhostNextPosition : enemiesWithGhostNextPositions) {
            if (enemiesWithGhostNextPosition.isEmpty()) {
                continue;
            }
            Buster lastPosition = enemiesWithGhostNextPosition.get(enemiesWithGhostNextPosition.size() - 1);
            if (someoneCanCatch(allies, lastPosition, enemiesWithGhostNextPosition.size())) {
                return true;
            }
        }
        return false;
    }

    private boolean someoneCanCatch(List<Buster> allies, Buster lastPosition, int afterTicks) {
        for (Buster ally : allies) {
            double dist = dist(ally, lastPosition);
            dist -= gameParameters.STUN_RANGE;
            double movesNeeded = (int) Math.ceil(dist / gameParameters.MOVE_RANGE);
            movesNeeded += ally.remainingStunDuration;
            if (movesNeeded <= afterTicks && ally.remainingStunCooldown <= afterTicks) {
                return true;
            }
        }
        return false;
    }

    private List<Buster> getAlliesWhoNeedEscort(List<Buster> allies, List<Buster> enemies, Point myBase) {
        List<Buster> r = new ArrayList<>();
        for (Buster ally : allies) {
            if (needsEscort(ally, enemies, myBase)) {
                r.add(ally);
            }
        }
        return r;
}

    private boolean needsEscort(Buster ally, List<Buster> enemies, Point myBase) {
        if (!ally.isCarryingGhost) {
            return false;
        }
        Point newCourierPosition = getPositionAfterMovingToBase(ally, myBase, gameParameters);
        for (Buster enemy : enemies) {
            if (enemy.remainingStunDuration > 0) {
                continue;
            }
            if (dist(enemy, ally) <= gameParameters.STUN_RANGE) {
                return true;
            }
            if (dist(enemy, myBase) <= dist(ally, myBase)) {
                return true;
            }
            if (dist(enemy, newCourierPosition) <= gameParameters.MOVE_RANGE + gameParameters.STUN_RANGE) {
                return true;
            }
        }
        return false;
    }

    private List<List<Buster>> getEnemiesWithGhostNextPositions(List<Buster> enemies, Point enemyBase) {
        List<List<Buster>> r = new ArrayList<>();
        for (Buster enemy : enemies) {
            if (enemy.isCarryingGhost) {
                r.add(getNextPositionsOnWayToBase(enemy, enemyBase));
            }
        }
        return r;
    }

    private List<Buster> getNextPositionsOnWayToBase(Buster enemy, Point enemyBase) {
        List<Buster> r = new ArrayList<>();
        boolean first = true;
        while (enemy != null) {
            if (!first) {
                r.add(enemy);
            }
            first = false;
            enemy = phantomUpdater.movePhantomEnemy(enemy, enemyBase);
        }
        for (int i = 0; i < 2; i++) {
            if (!r.isEmpty()) {
                r.remove(r.size() - 1);
            }
        }
        return r;
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
        Buster bestTarget = null;
        for (Buster enemy : enemies) {
            if (shouldNotUseStun(buster, enemy, alreadyStunnedEnemies)) {
                continue;
            }
            if (betterTarget(enemy, bestTarget)) {
                bestTarget = enemy;
            }
        }
        if (bestTarget == null) {
            return null;
        }
        return stun(bestTarget.id);
    }

    private boolean shouldNotUseStun(Buster buster, Buster enemy, Set<Integer> alreadyStunnedEnemies) {
        if (alreadyStunnedEnemies.contains(enemy.id)) {
            return true;
        }
        if (dist(buster, enemy) > gameParameters.STUN_RANGE) {
            return true;
        }
        if (enemy.remainingStunDuration > 1) {
            return true;
        }
        return false;
    }

    private boolean betterTarget(Buster newTarget, Buster oldTarget) {
        if (oldTarget == null) {
            return true;
        }
        if (newTarget.isCarryingGhost != oldTarget.isCarryingGhost) {
            return newTarget.isCarryingGhost;
        }
        if (newTarget.hasStun() != oldTarget.hasStun()) {
            return !newTarget.hasStun();
        }
        if (newTarget.remainingStunCooldown != oldTarget.remainingStunCooldown) {
            return newTarget.remainingStunCooldown < oldTarget.remainingStunCooldown;
        }
        return false;
    }

    private boolean checkIVeSeenItAll(List<CheckPoint> checkPoints, Point myBase) {
        GameParameters gameParameters = this.gameParameters;
        Point enemyBase = getEnemyBase(myBase, gameParameters);
        for (CheckPoint checkPoint : checkPoints) {
            if (dist(checkPoint.p, myBase) <= dist(checkPoint.p, enemyBase) + 5) {
                if (checkPoint.lastSeen == CheckPoint.NEVER) {
                    return false;
                }
            }
        }
        return true;
    }

    public Move findExploringMove(Buster buster, List<Buster> allies, Point myBasePosition, boolean weSawCenter) {
        int lessXCnt = 0;
        for (Buster ally : allies) {
            if (ally.x < buster.x) {
                lessXCnt++;
            }
        }
        double d = gameParameters.H / (allies.size() + 1);
        int x = (int) (d * (lessXCnt + 1));
        int y = gameParameters.W / 2;

        if (!weSawCenter && buster.y == y) {
            x = gameParameters.H / 2;
        }

        return move(x, y);
    }
}
