package game;

import java.util.*;
import java.io.PrintWriter;

import static game.Utils.dist;

public class Interactor {

    static final int W = 16001;
    static final int H = 9001;
    static final int MAX_BUST_RANGE = 1760;
    static final int MIN_BUST_RANGE = 900;
    static final int RELEASE_RANGE = 1600;
    static final int FOG_RANGE = 2200;

    Random rnd = new Random();

    public void solve(int testNumber, Scanner in, PrintWriter out) {
        int bustersPerPlayer = in.nextInt(); // the amount of busters you control
        int ghostCount = in.nextInt(); // the amount of ghosts on the map
        int myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right
        Point myBasePosition = myTeamId == 0 ? new Point(0, 0) : new Point(H, W);


        Point[] destinations = new Point[bustersPerPlayer * 2];
        while (true) {
            List<Buster> myBusters = new ArrayList<>();
            List<Ghost> ghosts = new ArrayList();
            int entities = in.nextInt(); // the number of busters and ghosts visible to you
            for (int i = 0; i < entities; i++) {
                int entityId = in.nextInt(); // buster id or ghost id
                int y = in.nextInt();
                int x = in.nextInt();
                int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
                int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost.
                int value = in.nextInt(); // For busters: Ghost id being carried. For ghosts: number of busters attempting to trap this ghost.

                if (entityType == myTeamId) {
                    myBusters.add(new Buster(x, y, state == 1, entityId));
                } else if (entityType == -1) {
                    ghosts.add(new Ghost(x, y, entityId));
                }
            }
            myBusters.sort(Comparator.comparing(Buster::getId));
            for (Buster buster : myBusters) {
                if (buster.isCarryingGhost) {
                    if (dist(buster, myBasePosition) <= RELEASE_RANGE) {
                        System.out.println("RELEASE");
                    } else {
                        System.out.println(move(myBasePosition));
                    }
                } else {
                    Ghost targetGhost = pickGhost(buster, ghosts);
                    if (targetGhost == null) {
                        Point dest = getDestination(buster, destinations);
                        System.out.println(move(dest));
                    } else {
                        System.out.println("BUST " + targetGhost.id);
                    }
                }
            }
        }
    }

    private Point getDestination(Buster buster, Point[] destinations) {
        Point oldDestination = destinations[buster.id];
        if (oldDestination == null || oldDestination.x == buster.x && oldDestination.y == buster.y) {
            destinations[buster.id] = new Point(rnd.nextInt(H + 1), rnd.nextInt(W + 1));
        }
        return destinations[buster.id];
    }

    private Ghost pickGhost(Buster buster, List<Ghost> ghosts) {
        for (Ghost ghost : ghosts) {
            double range = Utils.dist(buster, ghost);
            if (range >= MIN_BUST_RANGE && range <= MAX_BUST_RANGE) {
                return ghost;
            }
        }
        return null;
    }

    private String move(Point p) {
        return move(p.x, p.y);
    }

    private String move(int x, int y) {
        return "MOVE " + y + " " + x;
    }
}
