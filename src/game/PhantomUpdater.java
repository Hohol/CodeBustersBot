package game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static game.MoveType.STUN;
import static game.Utils.*;

public class PhantomUpdater {
    private final GameParameters gameParameters;

    public PhantomUpdater(GameParameters gameParameters) {
        this.gameParameters = gameParameters;
    }

    public List<Buster> updatePhantomEnemies(List<Buster> allies, List<Buster> phantomEnemies, List<Buster> enemies, Point enemyBase, int round) {
        List<Buster> r = new ArrayList<>();
        r.addAll(enemies);
        for (Buster phantomEnemy : phantomEnemies) {
            if (containsWithId(enemies, phantomEnemy.id)) {
                continue;
            }
            if (!phantomEnemy.isCarryingGhost && round - phantomEnemy.lastSeen >= 20) {
                continue;
            }
            Buster newState = movePhantomEnemy(phantomEnemy, enemyBase);
            if (newState == null) {
                continue;
            }
            if (weHaveVisionOverThisPlace(allies, newState.x, newState.y)) {
                continue;
            }
            r.add(newState);
        }
        r.sort(Comparator.comparing(Buster::getId));
        return r;
    }

    private boolean weHaveVisionOverThisPlace(List<Buster> allies, int x, int y) {
        for (Buster ally : allies) {
            if (dist(ally.x, ally.y, x, y) <= gameParameters.FOG_RANGE) {
                return true;
            }
        }
        return false;
    }

    public Buster movePhantomEnemy(Buster pe, Point enemyBase) {
        if (!pe.isCarryingGhost) {
            return pe;
        }
        if (dist(pe, enemyBase) <= gameParameters.RELEASE_RANGE) {
            return null;
        }
        Point newPosition = getNewPosition(pe, Move.move(enemyBase), gameParameters);
        //noinspection ConstantConditions
        return new Buster(pe.id, newPosition.x, newPosition.y, pe.isCarryingGhost, pe.remainingStunDuration, pe.remainingStunCooldown, pe.ghostId, pe.lastSeen);
    }

    private boolean containsWithId(List<Buster> busters, int id) {
        for (Buster buster : busters) {
            if (buster.id == id) {
                return true;
            }
        }
        return false;
    }

    public List<Ghost> updatePhantomGhosts(List<Ghost> ghosts, List<Ghost> phantomGhosts, List<Buster> allies, List<Buster> enemies) {
        ArrayList<Ghost> r = new ArrayList<>();
        r.addAll(ghosts);

        List<Buster> allBusters = new ArrayList<>(allies);
        allBusters.addAll(enemies);

        for (Ghost phantomGhost : phantomGhosts) {
            if (containsGhostWithId(ghosts, phantomGhost.id)) {
                continue;
            }
            if (carriesGhostWithId(allBusters, phantomGhost.id)) {
                continue;
            }
            Ghost newGhostState = moveGhost(phantomGhost, allBusters);
            if (weHaveVisionOverThisPlace(allies, newGhostState.x, newGhostState.y)) {
                continue;
            }
            r.add(newGhostState);
        }
        return r;
    }

    private boolean carriesGhostWithId(List<Buster> allBusters, int ghostId) {
        for (Buster buster : allBusters) {
            if (buster.ghostId == ghostId) {
                return true;
            }
        }
        return false;
    }

    boolean containsGhostWithId(List<Ghost> ghosts, int id) {
        for (Ghost ghost : ghosts) {
            if (ghost.id == id) {
                return true;
            }
        }
        return false;
    }

    public Ghost moveGhost(Ghost ghost, List<Buster> allBusters) {
        List<Buster> bustersWithMinDist = getBustersInRangeWithMinDist(ghost, allBusters);
        if (bustersWithMinDist.isEmpty()) {
            return ghost;
        }
        Point mean = getMeanPoint(bustersWithMinDist);
        Point p = runawayPoint(mean.x, mean.y, ghost.x, ghost.y, gameParameters.GHOST_MOVE_RANGE);
        p = getNewPosition(ghost.x, ghost.y, p.x, p.y, gameParameters.GHOST_MOVE_RANGE, gameParameters);
        return new Ghost(ghost.id, p.x, p.y, ghost.stamina, ghost.bustCnt);
    }

    private Point getMeanPoint(List<Buster> bustersWithMinDist) {
        double sumX = 0;
        double sumY = 0;
        for (Buster buster : bustersWithMinDist) {
            sumX += buster.x;
            sumY += buster.y;
        }
        int cnt = bustersWithMinDist.size();
        return Point.round(sumX / cnt, sumY / cnt);
    }

    private List<Buster> getBustersInRangeWithMinDist(Ghost ghost, List<Buster> allBusters) {
        double minDist = Double.POSITIVE_INFINITY;
        List<Buster> r = new ArrayList<>();
        for (Buster buster : allBusters) {
            double dist = dist(buster, ghost);
            if (dist > gameParameters.FOG_RANGE) {
                continue;
            }
            if (dist < minDist) {
                minDist = dist;
                r.clear();
                r.add(buster);
            } else if (dist == minDist) {
                r.add(buster);
            }
        }
        return r;
    }

    public Ghost dropGhostFromStunnedEnemy(Buster buster, Buster target) {
        Point to = runawayPoint(buster.x, buster.y, target.x, target.y, gameParameters.MOVE_RANGE);
        to = getNewPosition(target.x, target.y, to.x, to.y, gameParameters.MOVE_RANGE, gameParameters);
        return new Ghost(target.ghostId, to.x, to.y, 0, 0);
    }

    void updateAfterMoves(List<Buster> phantomEnemies, List<Ghost> phantomGhosts, List<Buster> allies, List<Buster> enemies, List<Move> moves) {
        for (int i = 0; i < allies.size(); i++) {
            Buster buster = allies.get(i);
            Move move = moves.get(i);
            if (move.type != STUN) {
                continue;
            }
            Buster target = getWithId(enemies, move.targetId);
            //noinspection ConstantConditions
            if (!target.isCarryingGhost) {
                continue;
            }
            Point newEnemyPosition = runawayPoint(buster.x, buster.y, target.x, target.y, gameParameters.MOVE_RANGE);
            newEnemyPosition = getNewPosition(target.x, target.y, newEnemyPosition.x, newEnemyPosition.y, gameParameters.MOVE_RANGE, gameParameters);

            Buster newTargetState = new Buster(target.id, newEnemyPosition.x, newEnemyPosition.y, false, gameParameters.STUN_DURATION, 0, -1, target.lastSeen);
            update(phantomEnemies, newTargetState);
            phantomGhosts.add(new Ghost(target.ghostId, newEnemyPosition.x, newEnemyPosition.y, 0, 0));
        }
    }

    private void update(List<Buster> phantomEnemies, Buster newState) {
        for (int i = 0; i < phantomEnemies.size(); i++) {
            Buster buster = phantomEnemies.get(i);
            if (buster.id == newState.id) {
                phantomEnemies.set(i, newState);
                return;
            }
        }
        throw new RuntimeException();
    }

}
