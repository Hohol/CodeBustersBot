package game;

import java.util.*;
import java.io.PrintWriter;

import static game.Utils.*;
import static game.Utils.dist;

public class Interactor {

    public void solve(int testNumber, Scanner in, PrintWriter out) {
        int bustersPerPlayer = in.nextInt(); // the amount of busters you control
        int ghostCount = in.nextInt(); // the amount of ghosts on the map
        int myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right
        Point myBasePosition = myTeamId == 0 ? new Point(0, 0) : new Point(H, W);

        BestMoveFinder bestMoveFinder = new BestMoveFinder();
        Point[] destinations = new Point[bustersPerPlayer * 2];
        while (true) {
            List<Buster> myBusters = new ArrayList<>();
            List<Ghost> ghosts = new ArrayList<>();
            int entities = in.nextInt(); // the number of busters and ghosts visible to you
            for (int i = 0; i < entities; i++) {
                int entityId = in.nextInt(); // buster id or ghost id
                int y = in.nextInt();
                int x = in.nextInt();
                int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
                int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost.
                int value = in.nextInt(); // For busters: Ghost id being carried. For ghosts: number of busters attempting to trap this ghost.

                if (entityType == myTeamId) {
                    myBusters.add(buildBuster(entityId, x, y, state, value));
                } else if (entityType == -1) {
                    ghosts.add(new Ghost(x, y, entityId));
                }
            }
            myBusters.sort(Comparator.comparing(Buster::getId));
            for (Buster buster : myBusters) {
                Move move = bestMoveFinder.findBestMove(buster, myBasePosition, ghosts, destinations);
                System.out.println(move.toInteractorString());
            }
        }
    }

    private Buster buildBuster(int id, int x, int y, int state, int value) {
        int remainingStunDuration = 0;
        boolean isCarryingGhost = false;
        if (state == 1) {
            isCarryingGhost = true;
        } else if (state == 2) {
            remainingStunDuration = value;
        }
        return new Buster(id, x, y, isCarryingGhost, remainingStunDuration);
    }



    private String move(int x, int y) {
        return "MOVE " + y + " " + x;
    }
}
