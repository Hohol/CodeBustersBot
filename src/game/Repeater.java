package game;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Repeater {
    public static void main(String[] args) {
        Interactor interactor = new Interactor();
        Scanner scanner = new Scanner("3 17 0 9 0 7204 3960 0 0 -1 1 9287 322 0 0 -1 2 3808 834 0 0 -1 3 4350 1623 1 2 10 0 6763 5032 -1 40 0 1 10773 402 -1 3 0 3 9736 1074 -1 40 0 12 3808 834 -1 0 0 14 10142 1906 -1 3 0 \n");
        try {
            interactor.solve(0, scanner, null);
        } catch (NoSuchElementException ignored) {
        }
    }
}
