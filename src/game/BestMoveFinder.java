package game;

import java.util.*;

import static game.Move.*;
import static game.Utils.*;
import static java.lang.Math.*;

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
            Set<Integer> alreadyBusted,
            boolean halfGhostsCollected,
            int[] prevMoveBustCnt,
            boolean iVeSeenItAll
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

        if (halfGhostsCollected) {
            ghosts = leaveOnlyClosestToBase(ghosts, myBase);
        } else {
            if (!iVeSeenItAll) {
                ghosts = removeFatGhosts(ghosts);
            }
        }
        Point checkPoint = getCheckPoint(buster, checkPoints);
        return trySomethingSmart(buster, myBase, allies, enemies, ghosts, checkPoint, alreadyBusted, checkPoints, halfGhostsCollected, prevMoveBustCnt);
    }

    private List<Ghost> leaveOnlyClosestToBase(List<Ghost> ghosts, Point myBase) {
        if (ghosts.isEmpty()) {
            return ghosts;
        }
        Ghost closestGhost = null;
        double minDistToBase = Double.POSITIVE_INFINITY;
        for (Ghost ghost : ghosts) {
            double dist = dist(ghost, myBase);
            if (dist < minDistToBase) {
                minDistToBase = dist;
                closestGhost = ghost;
            }
        }
        return Arrays.asList(closestGhost);
    }

    private List<Ghost> removeFatGhosts(List<Ghost> ghosts) {
        List<Ghost> r = new ArrayList<>();
        for (Ghost ghost : ghosts) {
            if (ghost.stamina < 15) { // magic
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
            List<CheckPoint> checkPoints,
            boolean halfGhostsCollected,
            int[] prevMoveBustCnt
    ) {
        Set<Move> possibleMoves = new LinkedHashSet<>();
        possibleMoves.add(move(moveToWithAllowedRange(buster.x, buster.y, myBase.x, myBase.y, gameParameters.RELEASE_RANGE)));
        possibleMoves.add(move(buster.x, buster.y));
        for (Buster enemy : enemies) {
            possibleMoves.add(move(runawayPoint(enemy.x, enemy.y, buster.x, buster.y, gameParameters.MOVE_RANGE)));
            possibleMoves.add(move(moveToBeOutsideRange(buster.x, buster.y, enemy.x, enemy.y, gameParameters.MIN_BUST_RANGE)));
        }
        Set<Integer> forbiddenGhosts = getForbiddenGhosts(ghosts, allies, enemies, prevMoveBustCnt);
        for (Ghost ghost : ghosts) {
            if (dist(buster, ghost) >= gameParameters.MIN_BUST_RANGE && dist(buster, ghost) <= gameParameters.MAX_BUST_RANGE
                    && !forbiddenGhosts.contains(ghost.id)) {
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
            if (!list.isEmpty()) {
                Buster afterOneMove = list.get(0);
                possibleMoves.add(move(moveToBeOutsideRange(buster.x, buster.y, afterOneMove.x, afterOneMove.y, gameParameters.MIN_BUST_RANGE)));
            }
        }
        boolean someOfUsCanCatchSomeEnemyWithGhost = checkSomeOfUsCanCatchEnemyWithGhost(allies, enemies, enemiesWithGhostNextPositions);
        List<Buster> alliesWhoNeedEscort = getAlliesWhoNeedEscort(buster, allies, enemies, myBase, halfGhostsCollected);
        List<Point> battles = getBattles(allies, enemies, ghosts);
        for (Buster ally : alliesWhoNeedEscort) {
            possibleMoves.add(move(ally.x, ally.y));
            Point nextPosition = getPositionAfterMovingToBase(ally, myBase, gameParameters);
            possibleMoves.add(move(nextPosition));
            possibleMoves.add(move(moveToBeOutsideRange(buster.x, buster.y, nextPosition.x, nextPosition.y, gameParameters.MIN_BUST_RANGE)));
            possibleMoves.add(move(moveToBeOutsideRange(buster.x, buster.y, ally.x, ally.y, gameParameters.MIN_BUST_RANGE)));
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
                    someOfUsCanCatchSomeEnemyWithGhost,
                    battles
            );
            if (evaluation.better(bestEvaluation)) {
                bestEvaluation = evaluation;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private List<Point> getBattles(List<Buster> allies, List<Buster> enemies, List<Ghost> ghosts) {
        List<Point> r = new ArrayList<>();
        for (Ghost ghost : ghosts) {
            if (thereIsBattleForThisGhost(ghost, allies, enemies)) {
                r.add(new Point(ghost.x, ghost.y));
            }
        }
        return r;
    }

    private boolean thereIsBattleForThisGhost(Ghost ghost, List<Buster> allies, List<Buster> enemies) {
        int alliesInBustRange = getInBustRange(ghost, allies);
        int enemiesInBustRange = getInBustRange(ghost, enemies);
        return alliesInBustRange > 0 && enemiesInBustRange > 0;
    }

    private int getInBustRange(Ghost ghost, List<Buster> busters) {
        int cnt = 0;
        for (Buster buster : busters) {
            double dist = dist(buster, ghost);
            if (inBustRange(dist, gameParameters)) {
                cnt++;
            }
        }
        return cnt;
    }

    private Set<Integer> getForbiddenGhosts(List<Ghost> ghosts, List<Buster> allies, List<Buster> enemies, int[] prevMoveBustCnt) {
        Set<Integer> r = new HashSet<>();
        for (Ghost ghost : ghosts) {
            int alliesInBustRange = getInBustRangeAndNotStunned(ghost, allies);
            if (alliesInBustRange == 0) {
                continue;
            }

            int enemiesInBustRange = getInBustRangeAndNotStunned(ghost, enemies);
            int movesToBust = divUp(ghost.stamina, alliesInBustRange);
            enemiesInBustRange += getStunnedButDangerousEnemies(ghost, enemies, movesToBust);

            enemiesInBustRange = max(enemiesInBustRange, ghost.bustCnt - prevMoveBustCnt[ghost.id]);
            if (enemiesInBustRange > alliesInBustRange) {
                r.add(ghost.id);
            }
        }
        return r;
    }

    private int getStunnedButDangerousEnemies(Ghost ghost, List<Buster> enemies, int movesToBust) {
        int cnt = 0;
        for (Buster enemy : enemies) {
            if (enemy.remainingStunDuration == 0) {
                continue;
            }
            double dist = dist(enemy, ghost);
            if (enemy.remainingStunDuration < movesToBust && inBustRange(dist, gameParameters)) {
                cnt++;
            }
        }
        return cnt;
    }

    private int divUp(int a, int b) {
        return (a + b - 1) / b;
    }

    private int getInBustRangeAndNotStunned(Ghost ghost, List<Buster> busters) {
        int cnt = 0;
        for (Buster buster : busters) {
            if (buster.remainingStunDuration > 0) {
                continue;
            }
            double dist = dist(buster, ghost);
            if (inBustRange(dist, gameParameters)) {
                cnt++;
            }
        }
        return cnt;
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
            double movesNeeded = (int) ceil(dist / gameParameters.MOVE_RANGE);
            movesNeeded += ally.remainingStunDuration;
            if (movesNeeded <= afterTicks && ally.remainingStunCooldown <= afterTicks) {
                return true;
            }
        }
        return false;
    }

    private List<Buster> getAlliesWhoNeedEscort(Buster me, List<Buster> allies, List<Buster> enemies, Point myBase, boolean halfGhostsCollected) {
        List<Buster> r = new ArrayList<>();
        for (Buster ally : allies) {
            if (needsEscort(me, ally, enemies, myBase, halfGhostsCollected, allies)) {
                r.add(ally);
            }
        }
        return r;
    }

    private boolean needsEscort(Buster me, Buster courier, List<Buster> enemies, Point myBase, boolean halfGhostsCollected, List<Buster> allies) {
        if (!courier.isCarryingGhost) {
            return false;
        }
        if (halfGhostsCollected) {
            return true;
        }
        Point newCourierPosition = getPositionAfterMovingToBase(courier, myBase, gameParameters);
        int dangerousEnemiesCnt = 0;
        for (Buster enemy : enemies) {
            if (isDangerous(courier, myBase, newCourierPosition, enemy)) {
                dangerousEnemiesCnt++;
            }
        }
        int escortersCloserThanMeCnt = 0;
        for (Buster ally : allies) {
            if (ally.id == courier.id) {
                continue;
            }
            if (ally.isCarryingGhost) {
                continue;
            }
            if (ally.remainingStunDuration > 0) {
                continue;
            }
            if (dist(ally, courier) >= dist(ally, me)) {
                continue;
            }
            if (dist(ally, courier) <= gameParameters.STUN_RANGE + gameParameters.MOVE_RANGE) {
                escortersCloserThanMeCnt++;
            }
        }
        return dangerousEnemiesCnt > escortersCloserThanMeCnt;
    }

    private boolean isDangerous(Buster courier, Point myBase, Point newCourierPosition, Buster enemy) {
        if (enemy.remainingStunDuration > 1) {
            return false;
        }
        if (enemy.remainingStunDuration == 1) {
            return dist(enemy, newCourierPosition) <= gameParameters.STUN_RANGE;
        }
        if (dist(enemy, courier) <= gameParameters.STUN_RANGE) {
            return true;
        }
        if (dist(enemy, myBase) <= dist(courier, myBase)) {
            return true;
        }
        if (dist(enemy, newCourierPosition) <= gameParameters.MOVE_RANGE + gameParameters.STUN_RANGE) {
            return true;
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
            if (betterTarget(buster, enemy, bestTarget)) {
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

    private boolean betterTarget(Buster me, Buster newTarget, Buster oldTarget) {
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
        boolean newCanTakeGhost = inBustRange(dist(newTarget, me), gameParameters);
        boolean oldCanTakeGhost = inBustRange(dist(oldTarget, me), gameParameters);
        if (newCanTakeGhost != oldCanTakeGhost) {
            return newCanTakeGhost;
        }
        return false;
    }

    public Move findExploringMove(Buster buster, List<Buster> allies, Point myBase, boolean weSawCenter) {
        int lessXCnt = 0;
        for (Buster ally : allies) {
            if (ally.x < buster.x) {
                lessXCnt++;
            }
        }
        double d = gameParameters.H / (allies.size() + 1);
        int x = (int) (d * (lessXCnt + 1));
        int y = gameParameters.W / 2;

        int s = 800;
        if (myBase.x == 0) {
            y += s;
            if (lessXCnt == 0) {
                x = gameParameters.FOG_RANGE;
            }
        } else {
            y -= s;
            if (lessXCnt == allies.size() - 1) {
                x = gameParameters.H - gameParameters.FOG_RANGE - 1;
            }
        }

        return move(x, y);
    }
}
