package game;

import java.util.List;

import static game.Utils.*;
import static java.lang.Math.*;

public class Evaluator {
    private final GameParameters gameParameters;

    public Evaluator(GameParameters gameParameters) {
        this.gameParameters = gameParameters;
    }

    EvaluationState evaluate(Buster buster, Point newMyPosition, Point myBase, List<Buster> enemies, List<Ghost> ghosts, Move move, Point checkPoint) {
        boolean canBeStunned = checkCanBeStunned(newMyPosition, enemies);
        boolean iHaveStun = buster.remainingStunCooldown == 0;
        int totalGhostStamina = getTotalGhostStamina(ghosts, move);
        boolean isCarryingGhost = checkIsCarryingGhost(buster, move, ghosts);
        double distToNearestGhost = getPseudoDistToNearestGhost(newMyPosition, ghosts);
        double distToCheckPoint = dist(newMyPosition, checkPoint);
        double distToBase = dist(newMyPosition, myBase);
        boolean inReleaseRange = distToBase <= gameParameters.RELEASE_RANGE;
        return new EvaluationState(canBeStunned, iHaveStun, totalGhostStamina, isCarryingGhost, distToNearestGhost, distToCheckPoint, distToBase, inReleaseRange);
    }

    private double getPseudoDistToNearestGhost(Point newMyPosition, List<Ghost> ghosts) {
        double mi = Double.POSITIVE_INFINITY;
        for (Ghost ghost : ghosts) {
            mi = min(mi, getPseudoDist(newMyPosition, ghost));
        }
        return mi;
    }

    private double getPseudoDist(Point newMyPosition, Ghost ghost) {
        double dist = dist(newMyPosition, ghost);
        if (dist >= gameParameters.MAX_BUST_RANGE) {
            return dist - gameParameters.MAX_BUST_RANGE;
        }
        if (dist >= gameParameters.MIN_BUST_RANGE) {
            return 0;
        }
        return gameParameters.MIN_BUST_RANGE - dist;
    }

    private boolean checkIsCarryingGhost(Buster buster, Move move, List<Ghost> ghosts) {
        if (buster.isCarryingGhost) {
            return true;
        }
        if (move.type != MoveType.BUST) {
            return false;
        }
        for (Ghost ghost : ghosts) {
            if (ghost.id == move.targetId) {
                return ghost.stamina == 0; // todo or <= 1?
            }
        }
        throw new RuntimeException();
    }

    private int getTotalGhostStamina(List<Ghost> ghosts, Move move) {
        int r = 0;
        for (Ghost ghost : ghosts) {
            int stamina = ghost.stamina;
            if (move.type == MoveType.BUST && move.targetId == ghost.id && stamina > 0) {
                stamina--;
            }
            r += stamina;
        }
        return r;
    }

    private boolean checkCanBeStunned(Point myPosition, List<Buster> enemies) {
        for (Buster enemy : enemies) {
            if (enemy.remainingStunDuration > 0) {
                continue;
            }
            if (dist(enemy, myPosition) <= gameParameters.STUN_RANGE + gameParameters.MOVE_RANGE) {
                return true;
            }
        }
        return false;
    }
}
