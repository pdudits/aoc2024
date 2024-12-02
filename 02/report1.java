///usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.System.*;

/**
 * Fortunately, the first location The Historians want to search isn't a long walk from the Chief Historian's office.
 *
 * While the Red-Nosed Reindeer nuclear fusion/fission plant appears to contain no sign of the Chief Historian, the engineers there run up to you as soon as they see you. Apparently, they still talk about the time Rudolph was saved through molecular synthesis from a single electron.
 *
 * They're quick to add that - since you're already here - they'd really appreciate your help analyzing some unusual data from the Red-Nosed reactor. You turn to check if The Historians are waiting for you, but they seem to have already divided into groups that are currently searching every corner of the facility. You offer to help with the unusual data.
 *
 * The unusual data (your puzzle input) consists of many reports, one report per line. Each report is a list of numbers called levels that are separated by spaces. For example:
 *
 * 7 6 4 2 1
 * 1 2 7 8 9
 * 9 7 6 2 1
 * 1 3 2 4 5
 * 8 6 4 4 1
 * 1 3 6 7 9
 *
 * This example data contains six reports each containing five levels.
 *
 * The engineers are trying to figure out which reports are safe. The Red-Nosed reactor safety systems can only tolerate levels that are either gradually increasing or gradually decreasing. So, a report only counts as safe if both of the following are true:
 *
 *     The levels are either all increasing or all decreasing.
 *     Any two adjacent levels differ by at least one and at most three.
 *
 * In the example above, the reports can be found safe or unsafe by checking those rules:
 *
 *     7 6 4 2 1: Safe because the levels are all decreasing by 1 or 2.
 *     1 2 7 8 9: Unsafe because 2 7 is an increase of 5.
 *     9 7 6 2 1: Unsafe because 6 2 is a decrease of 4.
 *     1 3 2 4 5: Unsafe because 1 3 is increasing but 3 2 is decreasing.
 *     8 6 4 4 1: Unsafe because 4 4 is neither an increase or a decrease.
 *     1 3 6 7 9: Safe because the levels are all increasing by 1, 2, or 3.
 *
 * So, in this example, 2 reports are safe.
 *
 * Analyze the unusual data from the engineers. How many reports are safe?
 */
public class report1 {
    final static String SAMPLE = """
            7 6 4 2 1
            1 2 7 8 9
            9 7 6 2 1
            1 3 2 4 5
            8 6 4 4 1
            1 3 6 7 9
            """;

    public static void main(String... args) throws IOException {
        var reports = Files.lines(Path.of("input.txt"))
            //SAMPLE.lines()
                .filter(l -> l.length() > 0)
                .map(Report::parse)
                .filter(r -> r.isSafe(1)).count();
        out.println(reports);
    }
}

record Report(int[] levels) {
    boolean isSafe() {
        return isSafe(0);
    }

    @Override
    public String toString() {
        return Arrays.toString(levels);
    }
    boolean isSafe(int dampening) {
        int gradient = 0;
        for (int i=1; i<levels.length; i++) {
            var delta = levels[i] - levels[i-1];
            if (gradient == 0) {
                gradient = delta;
            }
            if (Math.abs(delta) < 1 || Math.abs(delta) > 3 || gradient * delta < 0) {
                if (dampening > 0) {
                    return without(i-1).isSafe(dampening-1) ||
                            without(i).isSafe(dampening-1) ||
                            (gradient * delta < 0 && without(0).isSafe(dampening-1));
                }
                return false;
            }
        }
        return true;
    }

    Report without(int index) {
        int[] newInput = new int[levels.length-1];
        for(int i=0,j=0; i<newInput.length; i++) {
            if (j == index) {
                j++;
            }
            newInput[i] = levels[j++];
        }
        return new Report(newInput);
    }

    static Report parse(String line) {
        var levels = Arrays.stream(line.trim().split("\\s+")).mapToInt(Integer::parseInt).toArray();
        return new Report(levels);
    }

}
