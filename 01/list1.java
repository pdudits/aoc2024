///usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.*;

/*
Throughout the Chief's office, the historically significant locations are listed not by name but by a unique number called the location ID. To make sure they don't miss anything, The Historians split into two groups, each searching the office and trying to create their own complete list of location IDs.

There's just one problem: by holding the two lists up side by side (your puzzle input), it quickly becomes clear that the lists aren't very similar. Maybe you can help The Historians reconcile their lists?

For example:

3   4
4   3
2   5
1   3
3   9
3   3

Maybe the lists are only off by a small amount! To find out, pair up the numbers and measure how far apart they are. Pair up the smallest number in the left list with the smallest number in the right list, then the second-smallest left number with the second-smallest right number, and so on.
 */
public class list1 {

    public static void main(String... args) throws IOException {
        var input = Input.parse(Files.readString(Path.of("input1.txt")));
        out.println(input.diff());
        out.println(input.similarityScore());
    }
}

record Input(int[] left, int[] right) {
    Input {
        Arrays.sort(left);
        Arrays.sort(right);
    }

    public static Input parse(String input) {
        String[] lines = input.split("\n");
        int[] left = new int[lines.length];
        int[] right = new int[lines.length];
        for (int i = 0; i < lines.length; i++) {
            String[] parts = lines[i].split("\\s+");
            left[i] = Integer.parseInt(parts[0]);
            right[i] = Integer.parseInt(parts[1]);
        }
        return new Input(left, right);
    }

    int diff() {
        int diff = 0;
        for (int i = 0; i < left.length; i++) {
            diff += Math.abs(left[i] - right[i]);
        }
        return diff;
    }

    Map<Integer, BigInteger> rightFrequency() {
        var result = new HashMap<Integer, BigInteger>();
        // it is already sorted so we can just iterate
        for (int i = 0; i < right.length; i++) {
            result.put(right[i], result.getOrDefault(right[i], BigInteger.ZERO).add(BigInteger.ONE));
        }
        return result;
    }
    /*
    The Historians can't agree on which group made the mistakes or how to read most of the Chief's handwriting, but in the commotion you notice an interesting detail: a lot of location IDs appear in both lists! Maybe the other numbers aren't location IDs at all but rather misinterpreted handwriting.

    This time, you'll need to figure out exactly how often each number from the left list appears in the right list. Calculate a total similarity score by adding up each number in the left list after multiplying it by the number of times that number appears in the right list.
     */
    BigInteger similarityScore() {
        BigInteger score = BigInteger.ZERO;
        var rightFrequency = rightFrequency();
        for (int i = 0; i < left.length; i++) {
            score = score.add(BigInteger.valueOf(left[i])
                    .multiply(rightFrequency.getOrDefault(left[i], BigInteger.ZERO)));
        }
        return score;
    }
}
