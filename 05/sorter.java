///usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

import static java.lang.System.*;

/**
 * The Elf must recognize you, because they waste no time explaining that the new sleigh launch safety manual updates won't print correctly. Failure to update the safety manuals would be dire indeed, so you offer your services.
 *
 * Safety protocols clearly indicate that new pages for the safety manuals must be printed in a very specific order. The notation X|Y means that if both page number X and page number Y are to be produced as part of an update, page number X must be printed at some point before page number Y.
 *
 * The Elf has for you both the page ordering rules and the pages to produce in each update (your puzzle input), but can't figure out whether each update has the pages in the right order.
 *
 * For example:
 *
 * 47|53
 * 97|13
 * 97|61
 * 97|47
 * 75|29
 * 61|13
 * 75|53
 * 29|13
 * 97|29
 * 53|29
 * 61|53
 * 97|53
 * 61|29
 * 47|13
 * 75|47
 * 97|75
 * 47|61
 * 75|61
 * 47|29
 * 75|13
 * 53|13
 *
 * 75,47,61,53,29
 * 97,61,53,29,13
 * 75,29,13
 * 75,97,47,61,53
 * 61,13,29
 * 97,13,75,29,47
 *
 * The first section specifies the page ordering rules, one per line. The first rule, 47|53, means that if an update includes both page number 47 and page number 53, then page number 47 must be printed at some point before page number 53. (47 doesn't necessarily need to be immediately before 53; other pages are allowed to be between them.)
 *
 * The second section specifies the page numbers of each update. Because most safety manuals are different, the pages needed in the updates are different too. The first update, 75,47,61,53,29, means that the update consists of page numbers 75, 47, 61, 53, and 29.
 *
 * To get the printers going as soon as possible, start by identifying which updates are already in the right order.
 *
 * In the above example, the first update (75,47,61,53,29) is in the right order:
 *
 *     75 is correctly first because there are rules that put each other page after it: 75|47, 75|61, 75|53, and 75|29.
 *     47 is correctly second because 75 must be before it (75|47) and every other page must be after it according to 47|61, 47|53, and 47|29.
 *     61 is correctly in the middle because 75 and 47 are before it (75|61 and 47|61) and 53 and 29 are after it (61|53 and 61|29).
 *     53 is correctly fourth because it is before page number 29 (53|29).
 *     29 is the only page left and so is correctly last.
 *
 * Because the first update does not include some page numbers, the ordering rules involving those missing page numbers are ignored.
 *
 * The second and third updates are also in the correct order according to the rules. Like the first update, they also do not include every page number, and so only some of the ordering rules apply - within each update, the ordering rules that involve missing page numbers are not used.
 *
 * The fourth update, 75,97,47,61,53, is not in the correct order: it would print 75 before 97, which violates the rule 97|75.
 *
 * The fifth update, 61,13,29, is also not in the correct order, since it breaks the rule 29|13.
 *
 * The last update, 97,13,75,29,47, is not in the correct order due to breaking several rules.
 *
 * For some reason, the Elves also need to know the middle page number of each update being printed. Because you are currently only printing the correctly-ordered updates, you will need to find the middle page number of each correctly-ordered update. In the above example, the correctly-ordered updates are:
 *
 * 75,47,61,53,29
 * 97,61,53,29,13
 * 75,29,13
 *
 * These have middle page numbers of 61, 53, and 29 respectively. Adding these page numbers together gives 143.
 *
 * Of course, you'll need to be careful: the actual list of page ordering rules is bigger and more complicated than the above example.
 *
 * Determine which updates are already in the correct order. What do you get if you add up the middle page number from those correctly-ordered updates?
 */
public class sorter {
    private static final String SAMPLE= """
            47|53
            97|13
            97|61
            97|47
            75|29
            61|13
            75|53
            29|13
            97|29
            53|29
            61|53
            97|53
            61|29
            47|13
            75|47
            97|75
            47|61
            75|61
            47|29
            75|13
            53|13
            
            75,47,61,53,29
            97,61,53,29,13
            75,29,13
            75,97,47,61,53
            61,13,29
            97,13,75,29,47
            """;

    public static void main(String... args) throws IOException {
        //var input = Input.fromString(SAMPLE);
        var input = Input.fromString(Files.readString(Path.of("input.txt")));
        out.println(input.sumMiddlePages());
        out.println(input.sumResortedMiddlePages());
    }


}

record Input(Sorting sorting, PrintJob[] jobs) {
    public static Input fromString(String s) {
        String[] parts = s.split("\r?\n\r?\n");
        return new Input(
                new Sorting(Arrays.stream(parts[0].split("\r?\n")).map(SortRule::fromString).toArray(SortRule[]::new)),
                Arrays.stream(parts[1].split("\r?\n")).map(PrintJob::fromString).toArray(PrintJob[]::new));
    }

    public int sumMiddlePages() {
        return Arrays.stream(jobs).filter(j -> j.isOrdered(sorting)).mapToInt(PrintJob::middlePage).sum();
    }

    public int sumResortedMiddlePages() {
        return Arrays.stream(jobs).filter(j -> !j.isOrdered(sorting)).map(j -> j.sorted(sorting)).mapToInt(PrintJob::middlePage).sum();
    }
}

record Sorting(SortRule[] rules) implements Comparator<Integer> {

    @Override
    public int compare(Integer o1, Integer o2) {
        for (var rule: rules) {
            if (rule.a() == o1 && rule.b() == o2) {
                return -1;
            }
            if (rule.a() == o2 && rule.b() == o1) {
                return 1;
            }
        }
        return 0;
    }
}

record SortRule(int a, int b) {
    public static SortRule fromString(String s) {
        String[] parts = s.trim().split("\\|");
        try {
            return new SortRule(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("For input: "+s, nfe);
        }
    }
}

record PrintJob(int[] pages) {
    public static PrintJob fromString(String s) {
        return new PrintJob(Arrays.stream(s.split(",")).mapToInt(Integer::parseInt).toArray());
    }

    public boolean isOrdered(Sorting sorting) {
        for (int i = 0; i < pages.length; i++) {
            for (int j = i + 1; j < pages.length; j++) {
                if (sorting.compare(pages[i], pages[j]) > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public PrintJob sorted(Sorting sorting) {
        int[] sorted = IntStream.of(pages).boxed().sorted(sorting).mapToInt(i -> i).toArray();
        return new PrintJob(sorted);
    }

    public int middlePage() {
        return pages()[pages().length / 2];
    }
}
