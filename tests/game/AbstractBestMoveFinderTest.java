package game;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static game.Utils.*;
import static org.testng.Assert.assertEquals;

@Test
public class AbstractBestMoveFinderTest {
    protected BestMoveFinder bestMoveFinder;
    protected GameParameters testGameParameters;
    private TestBuilder testBuilder;

    @BeforeMethod
    void init() {
        testGameParameters = createTestGameParameters();
        bestMoveFinder = new BestMoveFinder(testGameParameters);
        testBuilder = new TestBuilder();
    }

    public static GameParameters createTestGameParameters() {
        GameParameters r = new GameParameters();
        r.W = 51;
        r.H = 51;
        r.FOG_RANGE = 7;
        r.MAX_BUST_RANGE = 6;
        r.STUN_RANGE = 5;
        r.RELEASE_RANGE = 4;
        r.MIN_BUST_RANGE = 3;
        r.MOVE_RANGE = 2;
        r.GHOST_MOVE_RANGE = 1;
        return r;
    }

    static class TestBuilder {
        Point myBase = new Point(0, 0);
        List<BusterBuilder> allies = new ArrayList<>();
        List<BusterBuilder> enemies = new ArrayList<>();
        List<Ghost> ghosts = new ArrayList<>();
        Set<Integer> alreadyBusted = new HashSet<>();

        BusterBuilder ally(int x, int y) {
            int id = allies.size() + enemies.size();
            BusterBuilder builder = new BusterBuilder(x, y, id);
            allies.add(builder);
            return builder;
        }

        BusterBuilder enemy(int x, int y) {
            int id = allies.size() + enemies.size();
            BusterBuilder builder = new BusterBuilder(x, y, id);
            enemies.add(builder);
            return builder;
        }
    }

    protected BusterBuilder ally(int x, int y) {
        return testBuilder.ally(x, y);
    }

    protected BusterBuilder enemy(int x, int y) {
        return testBuilder.enemy(x, y);
    }

    protected void ghost(int x, int y, int stamina) {
        ghost(x, y, stamina, 0);
    }

    protected void ghost(int x, int y, int stamina, int bustCnt) {
        int id = testBuilder.ghosts.size();
        testBuilder.ghosts.add(new Ghost(id, x, y, stamina, bustCnt));
    }

    protected void alreadyBusted(int ghostId) {
        testBuilder.alreadyBusted.add(ghostId);
    }

    protected void checkMove(Move expected) {
        Buster buster = testBuilder.allies.get(0).build();
        Move actual = bestMoveFinder.findBestMove(
                buster,
                testBuilder.myBase,
                buildBusters(testBuilder.allies),
                buildBusters(testBuilder.enemies),
                testBuilder.ghosts,
                Collections.singletonList(
                        new CheckPoint(new Point(testGameParameters.H / 2, testGameParameters.W / 2))
                ),
                Collections.emptySet(),
                testBuilder.alreadyBusted
        );
        expected = simplify(buster, expected, testGameParameters);
        actual = simplify(buster, actual, testGameParameters);
        assertEquals(actual, expected);
    }

    public static List<Buster> buildBusters(List<BusterBuilder> busters) {
        List<Buster> r = new ArrayList<>();
        for (BusterBuilder buster : busters) {
            r.add(buster.build());
        }
        return r;
    }
}
