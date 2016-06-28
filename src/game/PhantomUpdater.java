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
            if (weHaveVisionOverThisPlace(allies, newState)) {
                continue;
            }
            r.add(newState);
        }
        r.sort(Comparator.comparing(Buster::getId));
        return r;
    }

    private boolean weHaveVisionOverThisPlace(List<Buster> allies, Buster newState) {
        for (Buster ally : allies) {
            if (dist(ally, newState) <= gameParameters.FOG_RANGE) {
                return true;
            }
        }
        return false;
    }

    private Buster movePhantomEnemy(Buster pe, Point enemyBase) {
        if (!pe.isCarryingGhost) {
            return pe;
        }
        Point newPosition = getNewPosition(pe, Move.move(enemyBase), gameParameters);
        //noinspection ConstantConditions
        return new Buster(pe.id, newPosition.x, newPosition.y, pe.isCarryingGhost, pe.remainingStunDuration, pe.remainingStunCooldown);
    }

    private boolean containsWithId(List<Buster> busters, int id) {
        for (Buster buster : busters) {
            if (buster.id == id) {
                return true;
            }
        }
        return false;
    }
}
