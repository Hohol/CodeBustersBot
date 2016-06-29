package game;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Repeater {
    public static void main(String[] args) throws FileNotFoundException {
        String input = readInput();
        Scanner scanner = new Scanner(input);
        Interactor interactor = new Interactor();
        try {
            interactor.solve(0, scanner, null);
        } catch (NoSuchElementException ignored) {
        }
    }

    private static String readInput() throws FileNotFoundException {
        Scanner in = new Scanner(new FileInputStream("input.txt"));
        StringBuilder sb = new StringBuilder();
        while (in.hasNext()) {
            String s = in.nextLine();
            if (s.contains(".")) {
                throw new RuntimeException("broken input");
            }
            if (s.equals("input dump:") || s.equals("Standard Error Stream:")) {
                s = in.nextLine();
                if (s.contains(".")) {
                    throw new RuntimeException("broken input");
                }
                if (!s.startsWith("Round:")) {
                    sb.append(s).append(" ");
                }
            }
        }
        return sb.toString();
    }
}
