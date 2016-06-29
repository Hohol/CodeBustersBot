package game;

import java.util.*;

import static game.Utils.*;

public class Investigator {
    private final GameParameters gameParameters;

    public Investigator(GameParameters gameParameters) {
        this.gameParameters = gameParameters;
    }

    public Set<Integer> whoUsedStunOnPrevMove(List<Buster> allies, List<Buster> prevAllies, List<Buster> enemies, List<Buster> prevEnemies) {
        prevEnemies = new ArrayList<>(prevEnemies);
        prevEnemies.sort(Comparator.comparing(Buster::getId));


        Set<Integer> r = new HashSet<>();
        for (Buster ally : allies) {
            if (ally.remainingStunDuration == gameParameters.STUN_DURATION) {
                Buster prevAlly = getWithId(prevAllies, ally.id);
                int whoStunned = whoStunned(prevAlly, enemies, prevEnemies, allies, r);
                if (whoStunned != -1) {
                    r.add(whoStunned);
                }
            }
        }
        return r;
    }

    private int whoStunned(Buster prevAlly, List<Buster> enemies, List<Buster> prevEnemies, List<Buster> allies, Set<Integer> alreadyUsedStun) {
        for (Buster prevEnemy : prevEnemies) {
            Buster enemy = getWithId(enemies, prevEnemy.id);
            if (couldStun(prevAlly, enemy, prevEnemy, allies, alreadyUsedStun)) {
                return prevEnemy.id;
            }
        }
        return -1;
    }

    private boolean couldStun(Buster prevAlly, Buster enemy, Buster prevEnemy, List<Buster> allies, Set<Integer> alreadyUsedStun) {
        if (alreadyUsedStun.contains(prevEnemy.id)) {
            return false;
        }
        if (dist(prevAlly, prevEnemy) > gameParameters.STUN_RANGE) {
            return false;
        }
        if (enemy != null) {
            if (enemy.isCarryingGhost) {
                return false;
            }
            if (enemy.x != prevEnemy.x || enemy.y != prevEnemy.y) {
                return false;
            }
        }
        if (prevEnemy.remainingStunCooldown > 0) {
            return false;
        }
        if (prevEnemy.remainingStunDuration > 0) {
            return false;
        }
        if (enemy == null && weCanSeePosition(prevEnemy, allies)) {
            return false;
        }

        return true;
    }

    private boolean weCanSeePosition(Buster prevEnemy, List<Buster> allies) {
        for (Buster ally : allies) {
            if (dist(ally, prevEnemy) <= gameParameters.FOG_RANGE) {
                return true;
            }
        }
        return false;
    }
}
