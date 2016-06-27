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
        testGameParameters.W = 16001;
        testGameParameters.H = 9001;
        testGameParameters.FOG_RANGE = 2200;
        testGameParameters.MAX_BUST_RANGE = 6;
        testGameParameters.STUN_RANGE = 5;
        testGameParameters.RELEASE_RANGE = 4;
        testGameParameters.MIN_BUST_RANGE = 3;
        testGameParameters.MOVE_DIST = 2;

        bestMoveFinder = new BestMoveFinder(testGameParameters);
        testBuilder = new TestBuilder();
    }

    static class TestBuilder {
        Point myBase = new Point(0, 0);
        List<BusterBuilder> allies = new ArrayList<>();
        List<BusterBuilder> enemies = new ArrayList<>();

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

        void build() {
        }
    }

    protected BusterBuilder ally(int x, int y) {
        return testBuilder.ally(x, y);
    }
    protected BusterBuilder enemy(int x, int y) {
        return testBuilder.enemy(x, y);
    }

    protected void checkMove(Move move) {
        testBuilder.build();

        assertEquals(
                bestMoveFinder.findBestMove(
                        testBuilder.allies.get(0).build(),
                        testBuilder.myBase,
                        buildBusters(testBuilder.enemies),
                        Collections.emptyList(),
                        Collections.emptyList(),
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
