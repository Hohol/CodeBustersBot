package game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static game.Utils.*;

public class PhantomUpdater {
    private final GameParameters gameParameters;

    public PhantomUpdater(GameParameters gameParameters) {
        this.gameParameters = gameParameters;
    }

    public List<Buster> updatePhantomEnemies(List<Buster> allies, List<Buster> phantomEnemies, List<Buster> enemies, Point enemyBase) {
        List<Buster> r = new ArrayList<>();
        r.addAll(enemies);
        for (Buster phantomEnemy : phantomEnemies) {
            if (containsWithId(enemies, phantomEnemy.id)) {
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
        return new Buster(pe.id, newPosition.x, newPosition.y, pe.isCarryingGhost, pe.remainingStunDuration, pe.remainingStunCooldown, pe.ghostId);
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
        to = getNewPosition(target.x, target.y, to.x, to.y, gameParameters);
        return new Ghost(target.ghostId, to.x, to.y, 0, 0);
    }
}
