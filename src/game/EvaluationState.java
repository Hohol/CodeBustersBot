package game;

public class EvaluationState {
    private final boolean iCanBeStunned;
    private final boolean iHaveStun;
    private final int totalGhostStamina;
    private final boolean isCarryingGhost;
    private final double pseudoDistToNearestGhost;
    private final double distToCheckPoint;
    private final double distToBase;
    private final boolean inReleaseRange;

    public EvaluationState(
            boolean iCanBeStunned,
            boolean iHaveStun,
            int totalGhostStamina,
            boolean isCarryingGhost,
            double pseudoDistToNearestGhost,
            double distToCheckPoint,
            double distToBase,
            boolean inReleaseRange
    ) {
        this.iCanBeStunned = iCanBeStunned;
        this.iHaveStun = iHaveStun;
        this.totalGhostStamina = totalGhostStamina;
        this.isCarryingGhost = isCarryingGhost;
        this.pseudoDistToNearestGhost = pseudoDistToNearestGhost;
        this.distToCheckPoint = distToCheckPoint;
        this.distToBase = distToBase;
        this.inReleaseRange = inReleaseRange;
    }

    public boolean better(EvaluationState st) {
        if (st == null) {
            return true;
        }
        if (iHaveStun != st.iHaveStun) {
            return iHaveStun;
        }
        if (!iHaveStun) {
            if (iCanBeStunned != st.iCanBeStunned) {
                return !iCanBeStunned;
            }
        }
        if (isCarryingGhost != st.isCarryingGhost) {
            return isCarryingGhost;
        }
        if (isCarryingGhost) {
            if (inReleaseRange != st.inReleaseRange) {
                return inReleaseRange;
            }
            if (!inReleaseRange) {
                if (distToBase != st.distToBase) {
                    return distToBase < st.distToBase;
                }
            }
        }
        if (totalGhostStamina != st.totalGhostStamina) {
            return totalGhostStamina < st.totalGhostStamina;
        }
        if (pseudoDistToNearestGhost != st.pseudoDistToNearestGhost) {
            return pseudoDistToNearestGhost < st.pseudoDistToNearestGhost;
        }
        if (distToCheckPoint != st.distToCheckPoint) {
            return distToCheckPoint < st.distToCheckPoint;
        }
        return false;
    }
}
