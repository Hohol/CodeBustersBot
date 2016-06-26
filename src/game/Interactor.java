package game;

import java.io.PrintWriter;
import java.util.*;

import static game.MoveType.*;
import static game.Utils.*;

public class Interactor {

    public void solve(int testNumber, Scanner in, PrintWriter out) {
        List<CheckPoint> checkPoints = genCheckPoints();
        int bustersPerPlayer = in.nextInt(); // the amount of busters you control
        int ghostCount = in.nextInt(); // the amount of ghosts on the map
        int myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right
        Point myBasePosition = myTeamId == 0 ? new Point(0, 0) : new Point(H, W);

        BestMoveFinder bestMoveFinder = new BestMoveFinder();
        int[] lastStunUsed = new int[bustersPerPlayer * 2];
        Arrays.fill(lastStunUsed, -STUN_COOLDOWN - 5);
        int round = 0;
        while (true) {
            List<Buster> myBusters = new ArrayList<>();
            List<Buster> enemyBusters = new ArrayList<>();
            List<Ghost> ghosts = new ArrayList<>();

            int entities = in.nextInt(); // the number of busters and ghosts visible to you
            for (int i = 0; i < entities; i++) {
                int entityId = in.nextInt(); // buster id or ghost id
                int y = in.nextInt();
                int x = in.nextInt();
                int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
                int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost.
                int value = in.nextInt(); // For busters: Ghost id being carried. For ghosts: number of busters attempting to trap this ghost.

                if (entityType == -1) {
                    ghosts.add(new Ghost(entityId, x, y, state));
                } else {
                    Buster buster = buildBuster(entityId, x, y, state, value, lastStunUsed[entityId], round);
                    if (entityType == myTeamId) {
                        myBusters.add(buster);
                    } else {
                        enemyBusters.add(buster);
                    }
                }
            }
            myBusters.sort(Comparator.comparing(Buster::getId));
            updateCheckpoints(myBusters, checkPoints, round);
            for (Buster buster : myBusters) {
                Move move = bestMoveFinder.findBestMove(buster, myBasePosition, enemyBusters, ghosts, checkPoints);
                if (move.type == STUN) {
                    lastStunUsed[buster.id] = round;
                }
                printMove(move, ghosts);
            }
            round++;
        }
    }

    private void updateCheckpoints(List<Buster> myBusters, List<CheckPoint> checkPoints, int round) {
        for (Buster myBuster : myBusters) {
            for (CheckPoint checkPoint : checkPoints) {
                if (dist(myBuster, checkPoint.p) <= FOG_RANGE) {
                    checkPoint.lastSeen = round;
                }
            }
        }
    }

    private List<CheckPoint> genCheckPoints() {
        List<CheckPoint> r = new ArrayList<>();
        int n = 4;
        int m = 6;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (i == 0 && j == 0 || i == n - 1 && j == m - 1) {
                    continue;
                }
                int x = (int) Math.round((double) i * H / (n - 1));
                int y = (int) Math.round((double) j * W / (m - 1));
                r.add(new CheckPoint(new Point(x, y)));
            }
        }
        return r;
    }

    private void printMove(Move move, List<Ghost> ghosts) {
        System.out.println(move.toInteractorString() + " " + getMessage(move, ghosts));
    }

    private String getMessage(Move move, List<Ghost> ghosts) {
        return "";
    }

    private Buster buildBuster(int id, int x, int y, int state, int value, int lastStunUsed, int round) {
        int stunDelta = round - lastStunUsed;
        int remainingStunCooldown = stunDelta < STUN_COOLDOWN ? STUN_COOLDOWN - stunDelta : 0;
        int remainingStunDuration = 0;
        boolean isCarryingGhost = false;
        if (state == 1) {
            isCarryingGhost = true;
        } else if (state == 2) {
            remainingStunDuration = value;
        }
        return new Buster(id, x, y, isCarryingGhost, remainingStunDuration, remainingStunCooldown);
    }
}
