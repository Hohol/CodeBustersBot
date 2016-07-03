package game;

import java.io.PrintWriter;
import java.util.*;

import static game.MoveType.*;
import static game.Utils.*;

public class Interactor {
    GameParameters gameParameters = new GameParameters();

    public void solve(int testNumber, Scanner scanner, PrintWriter out) {
        BestMoveFinder bestMoveFinder = new BestMoveFinder(gameParameters);
        PhantomUpdater phantomUpdater = new PhantomUpdater(gameParameters);
        Investigator investigator = new Investigator(gameParameters);

        IntReader in = new IntReader(scanner);
        int bustersPerPlayer = in.nextInt(); // the amount of busters you control
        int ghostCnt = in.nextInt(); // the amount of ghosts on the map
        int myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right
        Point topLeftCorner = new Point(0, 0);
        Point myBase = myTeamId == 0 ? topLeftCorner : Utils.getEnemyBase(topLeftCorner, gameParameters);
        Point enemyBase = Utils.getEnemyBase(myBase, gameParameters);
        List<CheckPoint> checkPoints = genCheckPoints(gameParameters, enemyBase);

        int[] lastStunUsed = new int[bustersPerPlayer * 2];
        Arrays.fill(lastStunUsed, -gameParameters.STUN_COOLDOWN - 5);
        int round = 0;
        List<Buster> phantomEnemies = new ArrayList<>();
        List<Ghost> phantomGhosts = new ArrayList<>();
        List<Buster> prevEnemies = new ArrayList<>();
        List<Buster> prevAllies = new ArrayList<>();
        boolean exploring = true;
        boolean weSawCenter = false;
        int ghostsCollectedCnt = 0;
        boolean halfGhostsCollected = false;
        int[] prevMoveBustCnt = new int[ghostCnt];
        Set<Integer> seenGhosts = new HashSet<>();
        Set<Point> allMyPreviousPositions = new HashSet<>();
        List<Point> initialEnemyPositions = null;
        Set<Integer> knownGhostType = new HashSet<>();
        while (true) {
            List<Buster> allies = new ArrayList<>();
            List<Buster> enemies = new ArrayList<>();
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
                    ghosts.add(new Ghost(entityId, x, y, state, value));
                } else {
                    Buster buster = buildBuster(entityId, x, y, state, value, lastStunUsed[entityId], round, gameParameters);
                    if (entityType == myTeamId) {
                        allies.add(buster);
                    } else {
                        enemies.add(buster);
                    }
                }
            }
            if (initialEnemyPositions == null) {
                initialEnemyPositions = getInitialEnemyPositions(allies);
            }
            System.err.println("Round: " + round);
            Set<Integer> whoUsedStunOnPrevMove = investigator.whoUsedStunOnPrevMove(allies, prevAllies, enemies, prevEnemies);
            for (int enemyId : whoUsedStunOnPrevMove) {
                lastStunUsed[enemyId] = round - 1;
            }
            enemies = updateStunCd(enemies, lastStunUsed, round);
            allies.sort(Comparator.comparing(Buster::getId));
            updateCheckpoints(allies, checkPoints, round);
            if (inVisionRange(new Point(gameParameters.H / 2, gameParameters.W / 2), allies)) {
                weSawCenter = true;
            }

            for (Buster ally : allies) {
                allMyPreviousPositions.add(new Point(ally.x, ally.y));
            }
            for (Buster enemy : enemies) {
                if (enemy.isCarryingGhost) {
                    seenGhosts.add(enemy.ghostId);
                }
            }

            phantomEnemies = phantomUpdater.updatePhantomEnemies(allies, phantomEnemies, enemies, enemyBase, round);
            phantomGhosts = phantomUpdater.updatePhantomGhosts(ghosts, phantomGhosts, allies, enemies, seenGhosts, allMyPreviousPositions, enemyBase);
            for (Ghost ghost : ghosts) {
                seenGhosts.add(ghost.id);
                knownGhostType.add(ghost.id);
            }

//            print(ghosts, "Ghosts");
//            print(phantomGhosts, "Phantom ghosts");
//            print(enemies, "Enemies");
//            print(allies, "Allies");
//            print(phantomEnemies, "Phantom enemies");

            if (!enemies.isEmpty() || round >= 8) {
                exploring = false;
            }
            Set<Integer> alreadyStunnedEnemies = new HashSet<>();
            Set<Integer> alreadyBusted = new HashSet<>();

            List<Move> moves = new ArrayList<>();
            for (Buster buster : allies) {
                Move move;
                if ((exploring || !weSawCenter) && !buster.isCarryingGhost && !seeSomeSmallGhostNearCenter(buster, ghosts, initialEnemyPositions, round)) {
                    move = bestMoveFinder.findExploringMove(buster, allies, myBase, weSawCenter);
                } else {
                    boolean iVeSeenItAll = checkIVeSeenItAll(checkPoints, myBase, knownGhostType, ghostCnt);
                    move = bestMoveFinder.findBestMove(buster, myBase, allies, phantomEnemies, phantomGhosts, checkPoints, alreadyStunnedEnemies, alreadyBusted, halfGhostsCollected, prevMoveBustCnt, iVeSeenItAll);
                }

                moves.add(move);
                if (move.type == STUN) {
                    lastStunUsed[buster.id] = round;
                    alreadyStunnedEnemies.add(move.targetId);
                }
                if (move.type == BUST) {
                    alreadyBusted.add(move.targetId);
                }
                printMove(buster, move);
            }

            Arrays.fill(prevMoveBustCnt, 0);
            for (int i = 0; i < allies.size(); i++) {
                Buster buster = allies.get(i);
                Move move = moves.get(i);
                if (move.type == RELEASE && dist(buster, myBase) <= gameParameters.RELEASE_RANGE) {
                    ghostsCollectedCnt++;
                    if (ghostsCollectedCnt >= ghostCnt / 2) {
                        halfGhostsCollected = true;
                    }
                }
                if (move.type == BUST) {
                    prevMoveBustCnt[move.targetId]++;
                }
            }
            phantomUpdater.updateAfterMoves(phantomEnemies, phantomGhosts, allies, enemies, moves);
            prevEnemies = enemies;
            prevAllies = allies;
            in.dump();
            round++;
        }
    }

    private List<Point> getInitialEnemyPositions(List<Buster> allies) {
        List<Point> r = new ArrayList<>();
        for (Buster ally : allies) {
            r.add(new Point(gameParameters.H - ally.x - 1, gameParameters.W - ally.y - 1));
        }
        return r;
    }

    private boolean seeSomeSmallGhostNearCenter(Buster buster, List<Ghost> ghosts, List<Point> initialEnemyPositions, int round) {
        for (Ghost ghost : ghosts) {
            if (ghost.stamina <= 3 && dist(buster, ghost) <= gameParameters.FOG_RANGE && enemiesAlreadyCanSee(initialEnemyPositions, round, ghost)) {
                return true;
            }
        }
        return false;
    }

    private boolean enemiesAlreadyCanSee(List<Point> initialEnemyPositions, int round, Ghost ghost) {
        for (Point p : initialEnemyPositions) {
            if (dist(p, ghost) <= round * gameParameters.MOVE_RANGE + gameParameters.FOG_RANGE) {
                return true;
            }
        }
        return false;
    }

    private List<Buster> updateStunCd(List<Buster> enemies, int[] lastStunUsed, int round) {
        List<Buster> r = new ArrayList<>();
        for (Buster enemy : enemies) {
            int cd = getCd(lastStunUsed[enemy.id], round, gameParameters);
            r.add(new Buster(enemy.id, enemy.x, enemy.y, enemy.isCarryingGhost, enemy.remainingStunDuration, cd, enemy.ghostId, enemy.lastSeen));
        }
        return r;
    }

    private <T> void print(List<T> list, final String message) {
        System.err.println(message + ":");
        for (T enemy : list) {
            System.err.println(enemy);
        }
    }

    private void updateCheckpoints(List<Buster> myBusters, List<CheckPoint> checkPoints, int round) {
        for (CheckPoint checkPoint : checkPoints) {
            if (inVisionRange(checkPoint.p, myBusters)) {
                checkPoint.lastSeen = round;
            }
        }
    }

    private boolean inVisionRange(Point p, List<Buster> myBusters) {
        for (Buster myBuster : myBusters) {
            if (dist(myBuster, p) <= gameParameters.FOG_RANGE) {
                return true;
            }
        }
        return false;
    }

    private List<CheckPoint> genCheckPoints(GameParameters gameParameters, Point enemyBase) {
        List<CheckPoint> r = new ArrayList<>();
        int n = 4;
        int m = 6;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (i == 0 && j == 0 || i == n - 1 && j == m - 1) {
                    continue;
                }
                int x = (int) Math.round((double) i * (gameParameters.H - 1) / (n - 1));
                int y = (int) Math.round((double) j * (gameParameters.W - 1) / (m - 1));
                int lastSeen;
                if (dist(x, y, enemyBase.x, enemyBase.y) <= gameParameters.H / 2) {
                    lastSeen = -1;
                } else {
                    lastSeen = CheckPoint.NEVER;
                }
                r.add(new CheckPoint(new Point(x, y), lastSeen));
            }
        }
        return r;
    }

    private void printMove(Buster buster, Move move) {
        System.out.println(move.toInteractorString() + " " + getMessage(buster));
    }

    private String getMessage(Buster buster) {
        return buster.remainingStunCooldown == 0 ? "" : ("" + buster.remainingStunCooldown);
    }

    private Buster buildBuster(int id, int x, int y, int state, int value, int lastStunUsed, int round, GameParameters gameParameters) {
        int remainingStunCooldown = getCd(lastStunUsed, round, gameParameters);
        int remainingStunDuration = 0;
        boolean isCarryingGhost = false;
        int ghostId = -1;
        if (state == 1) {
            isCarryingGhost = true;
            ghostId = value;
        } else if (state == 2) {
            remainingStunDuration = value;
        }
        return new Buster(id, x, y, isCarryingGhost, remainingStunDuration, remainingStunCooldown, ghostId, round);
    }

    private int getCd(int lastStunUsed, int round, GameParameters gameParameters) {
        int stunDelta = round - lastStunUsed;
        return stunDelta < gameParameters.STUN_COOLDOWN ? gameParameters.STUN_COOLDOWN - stunDelta : 0;
    }

    private boolean checkIVeSeenItAll(List<CheckPoint> checkPoints, Point myBase, Set<Integer> knownGhostTypes, int ghostCnt) {
        if (allGhostTypesAreKnown(knownGhostTypes, ghostCnt)) {
            return true;
        }
        Point enemyBase = getEnemyBase(myBase, gameParameters);
        for (CheckPoint checkPoint : checkPoints) {
            if (shouldBeeSeen(myBase, enemyBase, checkPoint.p)) {
                if (checkPoint.lastSeen == CheckPoint.NEVER) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean allGhostTypesAreKnown(Set<Integer> knownGhostTypes, int ghostCnt) {
        if (!knownGhostTypes.contains(0)) {
            return false;
        }
        for (int id = 1; id < ghostCnt; id += 2) {
            if (!knownGhostTypes.contains(id) && !knownGhostTypes.contains(id + 1)) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldBeeSeen(Point myBase, Point enemyBase, Point p) {
        if (dist(p, myBase) <= dist(p, enemyBase) + 5) {
            return true;
        }
        /*if (p.x == 0 && p.y == gameParameters.W - 1) {
            return true;
        }
        if (p.x == gameParameters.H - 1 && p.y == 0) {
            return true;
        }*/
        return false;
    }

    static class IntReader {
        private final Scanner in;
        List<Integer> input = new ArrayList<>();

        IntReader(Scanner in) {
            this.in = in;
        }

        public int nextInt() {
            int r = in.nextInt();
            input.add(r);
            return r;
        }

        public void dump() {
            System.err.println("input dump:");
            for (int v : input) {
                System.err.print(v + " ");
            }
            System.err.println();
            input.clear();
        }
    }
}
