package game;

public class EvaluationState {
    private final boolean iCanBeStunned;
    private final boolean iHaveStun;

    public EvaluationState(boolean iCanBeStunned, boolean iHaveStun) {
        this.iCanBeStunned = iCanBeStunned;
        this.iHaveStun = iHaveStun;
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
        return false;
    }
}
