package game;

import java.util.Scanner;

public class Repeater {
    public static void main(String[] args) {
        Interactor interactor = new Interactor();
        Scanner scanner = new Scanner("4 23 0 8 0 926 4979 0 3 10 1 7190 5217 0 2 7 2 1807 1861 0 1 17 3 3891 7859 0 0 -1 6 8235 4040 1 2 7 7 8736 4298 1 3 11 10 1549 6587 -1 13 1 19 2389 8814 -1 15 0 \n");
        interactor.solve(0, scanner, null);
    }
}
