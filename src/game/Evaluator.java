package game;

import java.util.List;

public class Evaluator {
    private final GameParameters gameParameters;

    public Evaluator(GameParameters gameParameters) {
        this.gameParameters = gameParameters;
    }

    EvaluationState evaluate(Buster buster, Point newMyPosition, Point myBase, List<Buster> enemies) {
        boolean canBeStunned = checkCanBeStunned(newMyPosition, enemies);
        boolean iHaveStun = buster.remainingStunCooldown == 0;
        return new EvaluationState(canBeStunned, iHaveStun);
    }

    private boolean checkCanBeStunned(Point myPosition, List<Buster> enemies) {
        for (Buster enemy : enemies) {
            if (enemy.remainingStunDuration > 0) {
                continue;
            }
            if (Utils.dist(enemy, myPosition) <= gameParameters.STUN_RANGE + gameParameters.MOVE_RANGE) {
                return true;
            }
        }
        return false;
    }
}
