///usr/bin/env jbang "$0" "$@" ; exit $?


import javafx.geometry.Pos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import static java.lang.System.*;

/**
 * The reindeer is holding a book titled "Lava Island Hiking Guide". However, when you open the book, you discover that most of it seems to have been scorched by lava! As you're about to ask how you can help, the reindeer brings you a blank topographic map of the surrounding area (your puzzle input) and looks up at you excitedly.
 *
 * Perhaps you can help fill in the missing hiking trails?
 *
 * The topographic map indicates the height at each position using a scale from 0 (lowest) to 9 (highest). For example:
 *
 * 0123
 * 1234
 * 8765
 * 9876
 *
 * Based on un-scorched scraps of the book, you determine that a good hiking trail is as long as possible and has an even, gradual, uphill slope. For all practical purposes, this means that a hiking trail is any path that starts at height 0, ends at height 9, and always increases by a height of exactly 1 at each step. Hiking trails never include diagonal steps - only up, down, left, or right (from the perspective of the map).
 *
 * You look up from the map and notice that the reindeer has helpfully begun to construct a small pile of pencils, markers, rulers, compasses, stickers, and other equipment you might need to update the map with hiking trails.
 *
 * A trailhead is any position that starts one or more hiking trails - here, these positions will always have height 0. Assembling more fragments of pages, you establish that a trailhead's score is the number of 9-height positions reachable from that trailhead via a hiking trail. In the above example, the single trailhead in the top left corner has a score of 1 because it can reach a single 9 (the one in the bottom left).
 *
 * This trailhead has a score of 2:
 *
 * ...0...
 * ...1...
 * ...2...
 * 6543456
 * 7.....7
 * 8.....8
 * 9.....9
 *
 * (The positions marked . are impassable tiles to simplify these examples; they do not appear on your actual topographic map.)
 *
 * This trailhead has a score of 4 because every 9 is reachable via a hiking trail except the one immediately to the left of the trailhead:
 *
 * ..90..9
 * ...1.98
 * ...2..7
 * 6543456
 * 765.987
 * 876....
 * 987....
 *
 * This topographic map contains two trailheads; the trailhead at the top has a score of 1, while the trailhead at the bottom has a score of 2:
 *
 * 10..9..
 * 2...8..
 * 3...7..
 * 4567654
 * ...8..3
 * ...9..2
 * .....01
 *
 * Here's a larger example:
 *
 * 89010123
 * 78121874
 * 87430965
 * 96549874
 * 45678903
 * 32019012
 * 01329801
 * 10456732
 *
 * This larger example has 9 trailheads. Considering the trailheads in reading order, they have scores of 5, 6, 5, 3, 1, 3, 5, 3, and 5. Adding these scores together, the sum of the scores of all trailheads is 36.
 *
 * The reindeer gleefully carries over a protractor and adds it to the pile. What is the sum of the scores of all trailheads on your topographic map?
 */
public class day10 {

    private static final String SAMPLE = """
            89010123
            78121874
            87430965
            96549874
            45678903
            32019012
            01329801
            10456732
            """;

    public static void main(String... args) throws IOException {
        var input = Map.fromInput(Files.readString(Path.of("input.txt")));
        out.println(input.scoreAllPositions());
        out.println(input.rateAllPositions());
    }
}

record Map(int[][] map) {
    public static Map fromInput(String input) {
        return new Map(input.lines().map(line -> line.chars().map(c -> c - '0').toArray()).toArray(int[][]::new));
    }

    public int height() {
        return map.length;
    }

    public int width() {
        return map[0].length;
    }

    public int get(int x, int y) {
        return map[y][x];
    }

    public int height(Position position) {
        return get(position.x(), position.y());
    }

    boolean valid(Position position) {
        return position.x() >= 0 && position.x() < width() && position.y() >= 0 && position.y() < height();
    }

    int scoreAllPositions() {
        return sumOverMap(this::score);
    }

    int rateAllPositions() {
        return sumOverMap(this::rating);
    }

    private int sumOverMap(ToIntFunction<Position> func) {
        return Stream.iterate(new Position(0, 0), p -> p.x() < width() - 1
                        ? new Position(p.x() + 1, p.y())
                        : new Position(0, p.y() + 1))
                .limit(width() * height())
                .mapToInt(func)
                .sum();
    }

    int score(Position position) {
        var trails = trails(position, 0, new HashSet<>());
        return trails.size();
    }

    int rating(Position position) {
        return rating(position, 0);
    }

    private int rating(Position p, int height) {
        if (!valid(p) || height(p) != height) {
            return 0;
        }
        if (height == 9) {
            return 1;
        }
        return p.neighbors().mapToInt(n -> rating(n, height + 1)).sum();
    }

    private Set<Position> trails(Position p, int height, Set<Position> acc) {
        if (!valid(p) || height(p) != height) {
            return acc;
        }
        if (height == 9) {
            acc.add(p);
            return acc;
        }
        p.neighbors().forEach(n -> trails(n, height + 1, acc));
        return acc;
    }
 }

record Position(int x, int y) {
    public Stream<Position> neighbors() {
        return Stream.of(new Position(x + 1, y), new Position(x - 1, y), new Position(x, y + 1), new Position(x, y - 1));
    }
}