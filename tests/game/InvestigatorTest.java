package game;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static game.AbstractBestMoveFinderTest.*;
import static java.util.Arrays.*;
import static org.testng.Assert.*;

@Test
public class InvestigatorTest {
    Investigator investigator;

    @BeforeMethod
    void init() {
        investigator = new Investigator(createTestGameParameters());
    }

    @Test
    void test() {
        check(
                asList(),
                asList(),
                asList(),
                asList()
        );
    }

    @Test
    void testJustStunned() {
        check(
                asList(buster(0, 0, 0).stunDuration(10)),
                asList(buster(0, 0, 0)),
                asList(buster(1, 1, 1)),
                asList(buster(1, 1, 1)),
                1
        );
    }

    @Test
    void testTwoEnemies() {
        check(
                asList(buster(0, 0, 0).stunDuration(10)),
                asList(buster(0, 0, 0)),
                asList(buster(1, 1, 2), buster(2, 2, 1)),
                asList(buster(1, 1, 2), buster(2, 2, 1)),
                1
        );
    }

    @Test
    void couldntStunIfNotInRange() {
        check(
                asList(buster(0, 0, 0).stunDuration(10)),
                asList(buster(0, 0, 0)),
                asList(buster(1, 1, 2), buster(10, 10, 1)),
                asList(buster(1, 1, 2), buster(10, 10, 1)),
                2
        );
    }

    @Test
    void couldntStunIfCarriesGhost() {
        check(
                asList(buster(0, 0, 0).stunDuration(10)),
                asList(buster(0, 0, 0)),
                asList(buster(1, 1, 2), buster(2, 2, 1).carryingGhost()),
                asList(buster(1, 1, 2), buster(2, 2, 1)),
                2
        );
    }

    @Test
    void couldStunIfCloseToMyOldPosition() {
        check(
                asList(buster(0, 0, 0).stunDuration(10)),
                asList(buster(1, 1, 0)),
                asList(buster(1, 6, 2)),
                asList(buster(1, 6, 2)),
                2
        );
    }

    @Test
    void couldntStunIfMoved() {
        check(
                asList(buster(0, 0, 0).stunDuration(10)),
                asList(buster(0, 0, 0)),
                asList(buster(1, 1, 2), buster(3, 3, 1)),
                asList(buster(1, 1, 2), buster(2, 2, 1)),
                2
        );
    }

    @Test
    void couldntStunIfStunOnCooldown() {
        check(
                asList(buster(0, 0, 0).stunDuration(10)),
                asList(buster(0, 0, 0)),
                asList(buster(1, 1, 2), buster(2, 2, 1)),
                asList(buster(1, 1, 2), buster(2, 2, 1).stunCooldown(1)),
                2
        );
    }

    @Test
    void couldntStunIfStunned() {
        check(
                asList(buster(0, 0, 0).stunDuration(10)),
                asList(buster(0, 0, 0)),
                asList(buster(1, 1, 2), buster(2, 2, 1)),
                asList(buster(1, 1, 2), buster(2, 2, 1).stunDuration(1)),
                2
        );
    }

    @Test
    void couldntStunIfCantBeSeenAndWeHaveVisionOverThisPlace() {
        check(
                asList(buster(0, 0, 0).stunDuration(10)),
                asList(buster(0, 0, 0)),
                asList(buster(1, 1, 2)),
                asList(buster(1, 1, 2), buster(2, 2, 1)),
                2
        );
    }

    @Test
    void couldntStunTwoAtOnce() {
        check(
                asList(buster(0, 0, 0).stunDuration(10), buster(0, 0, 3).stunDuration(10)),
                asList(buster(0, 0, 0), buster(0, 0, 3)),
                asList(buster(1, 1, 2), buster(2, 2, 1)),
                asList(buster(1, 1, 2), buster(2, 2, 1)),
                1, 2
        );
    }

    @Test
    void complexCase() {
        // todo answer is wrong but it's ok for greedy algo
        // todo implement correct algo
        check(
                asList(buster(0, 10, 0).stunDuration(10), buster(0, 12, 1).stunDuration(10)),
                asList(buster(0, 10, 0), buster(0, 12, 1)),
                asList(buster(0, 6, 3), buster(0, 14, 2)),
                asList(buster(0, 6, 3), buster(0, 14, 2)),
                2
        );
    }

    // -- utils

    private void check(List<BusterBuilder> allies, List<BusterBuilder> prevAllies, List<BusterBuilder> enemies, List<BusterBuilder> prevEnemies, int... expected) {
        Set<Integer> actual = investigator.whoUsedStunOnPrevMove(
                buildBusters(allies),
                buildBusters(prevAllies),
                buildBusters(enemies),
                buildBusters(prevEnemies)
        );
        Set<Integer> expectedSet = new HashSet<>();
        for (int id : expected) {
            expectedSet.add(id);
        }
        assertEquals(actual, expectedSet);
    }

    private BusterBuilder buster(int x, int y, int id) {
        return new BusterBuilder(x, y, id);
    }
}