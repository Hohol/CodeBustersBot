package game;

import org.testng.annotations.Test;

import static game.Move.*;
import static org.testng.Assert.assertEquals;

@Test
public class BestMoveFinderTest extends AbstractBestMoveFinderTest {

    @Test
    void testMoveWithAllowedRange() {
        assertEquals(Utils.moveToWithAllowedRange(0, 0, 0, 0, 10, 5), new Point(0, 0));
        assertEquals(Utils.moveToWithAllowedRange(1, 0, 0, 0, 10, 5), new Point(1, 0));
        assertEquals(Utils.moveToWithAllowedRange(10, 0, 0, 0, 10, 5), new Point(5, 0));
        assertEquals(Utils.moveToWithAllowedRange(1, 1, 2, 2, 1, 1), new Point(2, 2));
    }

    @Test
    void testCarryGhostToBase() {
        ally(10, 0).carryingGhost();
        checkMove(
                move(4, 0)
        );
    }

    @Test
    void avoidEnemies() {
        ally(0, 10).carryingGhost().stunCooldown(20);
        enemy(7, 8);
        checkMove(move(0, 10));
    }

    @Test
    void dontFearStunnedEnemy() {
        ally(0, 10).carryingGhost().stunCooldown(20);
        enemy(7, 8).stunDuration(10);
        checkMove(move(0, 4));
    }

    @Test
    void dontFearIfYouHaveStun() {
        ally(0, 10).carryingGhost();
        enemy(7, 8);
        checkMove(move(0, 4));
    }

    @Test
    void runAway() {
        ally(0, 10).carryingGhost().stunCooldown(20);
        enemy(0, 3);
        checkMove(move(25, 25));
    }

    @Test
    void testBust() {
        ally(0, 10);
        ghost(0, 14, 3);
        checkMove(bust(0));
    }

    @Test
    void testCatch() {
        ally(0, 10);
        ghost(0, 14, 0);
        checkMove(bust(0));
    }

    @Test
    void goToGhost() {
        ally(0, 10);
        ghost(0, 50, 3);
        checkMove(move(0, 50));
    }

    @Test
    void testCheckpoints() {
        ally(0, 10);
        checkMove(move(25, 25));
    }

    @Test
    void avoidFatGhosts() {
        ally(0, 10);
        ghost(0, 20, 40);
        checkMove(move(25, 25));
    }

    @Test
    void testMinBustRange() {
        ally(0, 10);
        ghost(0, 11, 1);
        checkMove(move(0, 4));
    }

    @Test
    void testBug() {
        ally(0, 5).carryingGhost().stunCooldown(20);
        enemy(0, 50);
        checkMove(move(0, 4));
    }

    @Test
    void dontFearEnemyIfYouHaveNoGhost() {
        ally(0, 25).stunCooldown(20);
        enemy(6, 25);
        checkMove(move(25, 25));
    }

    @Test
    void testGhostPriority() {
        ally(0, 10);
        ghost(0, 18, 0);
        ghost(3, 10, 20);
        checkMove(move(0, 18));
    }

    @Test
    void ghostRunsAway() {
        ally(0, 10);
        ghost(0, 12, 3);
        checkMove(move(0, 10));
    }

    @Test
    void ghostDoesntRunAwayIfHeWasBustedOnPreviousMove() {
        ally(0, 10);
        ghost(0, 12, 3, 1);
        checkMove(move(0, 4));
    }

    @Test
    void ghostDoesntRunAwayIfSomeAllyStartedToBustHimOnThisMove() {
        ally(0, 10);
        ghost(0, 12, 3);
        alreadyBusted(0);
        checkMove(move(0, 4));
    }

    @Test
    void minimizeDistToGhost() {
        ally(24, 25);
        ghost(31, 25, 3);
        alreadyBusted(0);
        checkMove(move(31, 25));
    }
}