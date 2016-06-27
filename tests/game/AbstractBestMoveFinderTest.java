package game;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;

@Test
public class AbstractBestMoveFinderTest {
    protected BestMoveFinder bestMoveFinder;
    protected GameParameters testGameParameters;
    TestBuilder testBuilder;

    @BeforeMethod
    void init() {
        testGameParameters = new GameParameters();
        testGameParameters.W = 50;
        testGameParameters.H = 50;
        testGameParameters.FOG_RANGE = 2200;
        testGameParameters.MAX_BUST_RANGE = 6;
        testGameParameters.STUN_RANGE = 5;
        testGameParameters.RELEASE_RANGE = 4;
        testGameParameters.MIN_BUST_RANGE = 3;
        testGameParameters.MOVE_RANGE = 2;

        bestMoveFinder = new BestMoveFinder(testGameParameters);
        testBuilder = new TestBuilder();
    }

    static class TestBuilder {
        Point myBase = new Point(0, 0);
        List<BusterBuilder> allies = new ArrayList<>();
        List<BusterBuilder> enemies = new ArrayList<>();
        List<Ghost> ghosts = new ArrayList<>();

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
        int id = testBuilder.ghosts.size();
        testBuilder.ghosts.add(new Ghost(id, x, y, stamina));
    }

    protected void checkMove(Move move) {
        assertEquals(
                bestMoveFinder.findBestMove(
                        testBuilder.allies.get(0).build(),
                        testBuilder.myBase,
                        buildBusters(testBuilder.enemies),
                        testBuilder.ghosts,
                        Collections.singletonList(
                                new CheckPoint(new Point(testGameParameters.H / 2, testGameParameters.W / 2))
                        ),
                        Collections.emptySet()
                ),
                move
        );
    }

    private List<Buster> buildBusters(List<BusterBuilder> busters) {
        List<Buster> r = new ArrayList<>();
        for (BusterBuilder buster : busters) {
            r.add(buster.build());
        }
        return r;
    }
}
