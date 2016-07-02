package game;

import org.testng.annotations.Test;

import static game.Move.*;
import static org.testng.Assert.assertEquals;

@Test
public class BestMoveFinderTest extends AbstractBestMoveFinderTest {

    @Test
    void testMoveWithAllowedRange() {
        assertEquals(Utils.moveToWithAllowedRange(0, 0, 0, 0, 5), new Point(0, 0));
        assertEquals(Utils.moveToWithAllowedRange(1, 0, 0, 0, 5), new Point(1, 0));
        assertEquals(Utils.moveToWithAllowedRange(10, 0, 0, 0, 5), new Point(5, 0));
        assertEquals(Utils.moveToWithAllowedRange(1, 1, 2, 2, 1), new Point(2, 2));
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

    @Test
    void chaseEnemy() {
        ally(50, 45);
        enemy(50, 36).carryingGhost();
        checkMove(move(50, 36));
    }

    @Test
    void beCloserToBaseWhenBusting() {
        ally(0, 25);
        ghost(0, 25, 3);
        checkMove(move(0, 4));
    }

    @Test
    void extendedChaseTooLate() {
        ally(41, 46);
        enemy(50, 42).carryingGhost();
        checkMove(move(25, 25));
    }

    @Test
    void extendedChase2() {
        ally(41, 42);
        enemy(50, 38).carryingGhost();
        checkMove(move(50, 40)); // it looks strange but it's correct
    }

    @Test
    void considerStunCooldownWhenChasing() {
        ally(41, 42).stunCooldown(2);
        enemy(50, 38).carryingGhost();
        checkMove(move(50, 40));
    }

    @Test
    void considerStunCooldownWhenChasing2() {
        ally(41, 42).stunCooldown(3);
        enemy(50, 38).carryingGhost();
        checkMove(move(2, 2));
    }

    @Test
    void chaseBug() {
        ally(0, 0);
        enemy(50, 46).carryingGhost();
        checkMove(move(25, 25)); // just no exception
    }

    @Test
    void chaseBug2() {
        ally(25, 30);
        enemy(50, 44).carryingGhost();
        checkMove(move(25, 25));
    }

    @Test
    void chaseBug3() {
        ally(43, 44);
        enemy(50, 42).carryingGhost();
        checkMove(move(2, 2));
    }

    @Test
    void preferStunningEnemyWithGhost() {
        ally(0, 10);
        enemy(0, 11);
        enemy(0, 12).carryingGhost();
        checkMove(stun(2));
    }

    @Test
    void preferStunEnemyWithLessStunCooldown() {
        ally(0, 10);
        enemy(0, 11).stunCooldown(10);
        enemy(0, 12).stunCooldown(9);
        checkMove(stun(2));
    }

    @Test
    void ghostCantMoveThroughWall() {
        testGameParameters.MOVE_RANGE = 3;
        ally(2, 25);
        ghost(0, 25, 3);
        checkMove(move(0, 3));
    }

    @Test
    void testEscortSimple() {
        ally(0, 10);
        ally(0, 20).carryingGhost();
        enemy(1, 20);
        checkMove(move(0, 20));
    }

    @Test
    void dontStayTooCloseToCourier() {
        ally(4, 8);
        ally(0, 10).carryingGhost();
        enemy(0, 4);
        checkMove(move(3, 8));
    }

    @Test
    void whenChasingTryAlwaysSeeEnemy() {
        ally(50, 20).stunCooldown(10);
        enemy(50, 10).carryingGhost();
        checkMove(move(50, 10));
    }

    @Test
    void preferNotStunEnemyWithStunReady() {
        ally(0, 10);
        enemy(0, 11).stunCooldown(1);
        enemy(0, 12);
        checkMove(stun(1));
    }

    @Test
    void chaseEnemyIfSomeAllyCanCatchHim() {
        ally(50, 0);
        ally(50, 31);
        enemy(50, 36).carryingGhost();
        checkMove(move(50, 36));
    }

    @Test
    void chaseEnemyIfSomeAllyCanCatchHim2() {
        ally(50, 0);
        ally(43, 32);
        enemy(50, 30).carryingGhost();
        checkMove(move(50, 30));
    }

    @Test
    void chaseEnemyIfSomeAllyCanCatchHim3() {
        ally(50, 0);
        ally(43, 42);
        enemy(50, 40).carryingGhost();
        checkMove(move(50, 40));
    }

    @Test
    void chaseEnemyIfSomeAllyCanCatchHim4() {
        ally(50, 0);
        ally(42, 42);
        enemy(50, 40).carryingGhost();
        checkMove(move(25, 25));
    }

    @Test
    void chaseEnemyIfSomeAllyCanCatchHim5() {
        ally(50, 0);
        ally(43, 42).stunCooldown(1);
        enemy(50, 40).carryingGhost();
        checkMove(move(50, 40));
    }

    @Test
    void chaseEnemyIfSomeAllyCanCatchHim6() {
        ally(50, 0);
        ally(43, 42).stunCooldown(2);
        enemy(50, 40).carryingGhost();
        checkMove(move(25, 25));
    }

    @Test
    void chaseEnemyIfSomeAllyCanCatchHim7() {
        ally(50, 0);
        ally(50, 25).stunCooldown(2);
        enemy(50, 30).carryingGhost();
        checkMove(move(50, 30));
    }

    @Test
    void chaseEnemyIfSomeAllyCanCatchHimPriority() {
        ally(50, 0);
        ally(50, 31);
        enemy(50, 36).carryingGhost();
        ghost(10, 10, 3);
        checkMove(move(50, 36));
    }

    @Test
    void improvedEscort() {
        ally(0, 30);
        ally(0, 20).carryingGhost();
        enemy(0, 14);
        checkMove(move(0, 20));
    }

    @Test
    void improvedEscort2() {
        ally(0, 30);
        ally(0, 9).carryingGhost();
        enemy(7, 7);
        checkMove(move(0, 9));
    }

    @Test
    void improvedEscort3() {
        ally(0, 30);
        ally(0, 9).carryingGhost();
        enemy(8, 7);
        checkMove(move(25, 25));
    }

    @Test
    void chaseConsiderStunnedAlly() {
        ally(50, 0);
        ally(50, 30).stunDuration(2);
        enemy(50, 30).carryingGhost();
        checkMove(move(50, 30));
    }

    @Test
    void chaseConsiderStunnedAlly2() {
        ally(50, 0);
        ally(50, 30).stunDuration(3);
        enemy(50, 30).carryingGhost();
        checkMove(move(25, 25));
    }

    @Test
    void courierIsGonnaUseStun() {
        ally(0, 14);
        ally(0, 10).carryingGhost();
        enemy(5, 10);
        checkMove(move(0, 13));
    }

    @Test
    void dontHelpEnemyBust() {
        ally(0, 10).stunCooldown(20);
        enemy(0, 10);
        enemy(0, 10);
        ghost(3, 10, 15);
        checkMove(move(0, 10));
    }

    @Test
    void dontHelpEnemyBustEvenWhenWeCantSeeThem() {
        ally(0, 10);
        ghost(3, 10, 15, 3);
        checkMove(move(0, 10));
    }

    @Test
    void dontHelpEnemyBustEvenWhenTheyAreStunned() {
        ally(0, 10).stunCooldown(20);
        enemy(0, 10);
        enemy(0, 10).stunDuration(3);
        ghost(3, 10, 15);
        checkMove(move(0, 10));
    }

    @Test
    void chasedEnemyIsGonnaUseStun() {
        ally(50, 7).stunCooldown(20);
        ally(50, 10);
        enemy(50, 10).carryingGhost();
        checkMove(move(50, 7));
    }

    @Test
    void chasedEnemyIsGonnaUseStun2() {
        ally(50, 6).stunCooldown(20);
        ally(50, 10);
        enemy(50, 10).carryingGhost();
        checkMove(move(50, 7));
    }

    @Test
    void chasedEnemyIsGonnaUseStun3() {
        ally(50, 9).stunCooldown(1);
        enemy(50, 10).carryingGhost().stunCooldown(1);
        checkMove(move(50, 9));
    }

    @Test
    void chasedEnemyIsGonnaUseStun4() {
        ally(50, 8).stunCooldown(1);
        enemy(50, 10).carryingGhost().stunCooldown(1);
        checkMove(move(50, 9));
    }
}