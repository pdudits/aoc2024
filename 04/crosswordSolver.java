///usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.stream.Stream;

import static java.lang.System.*;

/**
 * As the search for the Chief continues, a small Elf who lives on the station tugs on your shirt; she'd like to know if you could help her with her word search (your puzzle input). She only has to find one word: XMAS.
 *
 * This word search allows words to be horizontal, vertical, diagonal, written backwards, or even overlapping other words. It's a little unusual, though, as you don't merely need to find one instance of XMAS - you need to find all of them. Here are a few ways XMAS might appear, where irrelevant characters have been replaced with .:
 *
 * ..X...
 * .SAMX.
 * .A..A.
 * XMAS.S
 * .X....
 *
 * The actual word search will be full of letters instead. For example:
 *
 * MMMSXXMASM
 * MSAMXMSMSA
 * AMXSXMAAMM
 * MSAMASMSMX
 * XMASAMXAMM
 * XXAMMXXAMA
 * SMSMSASXSS
 * SAXAMASAAA
 * MAMMMXMMMM
 * MXMXAXMASX
 *
 * In this word search, XMAS occurs a total of 18 times; here's the same word search again, but where letters not involved in any XMAS have been replaced with .:
 *
 * ....XXMAS.
 * .SAMXMS...
 * ...S..A...
 * ..A.A.MS.X
 * XMASAMX.MM
 * X.....XA.A
 * S.S.S.S.SS
 * .A.A.A.A.A
 * ..M.M.M.MM
 * .X.X.XMASX
 *
 * Take a look at the little Elf's word search. How many times does XMAS appear?
 */
public class crosswordSolver {
    private static final String SAMPLE = """
            MMMSXXMASM
            MSAMXMSMSA
            AMXSXMAAMM
            MSAMASMSMX
            XMASAMXAMM
            XXAMMXXAMA
            SMSMSASXSS
            SAXAMASAAA
            MAMMMXMMMM
            MXMXAXMASX
            """;

    public static void main(String... args) throws IOException {
        var crossword = new Crossword(Files.lines(Path.of("input.txt")));
        //var crossword = new Crossword(SAMPLE.lines());
        var result = crossword.findAll("XMAS");
        out.println(result.count());
        //result.mask().print(crossword);
        result = crossword.findCrosses();
        out.println(result.count());
        result.mask().print(crossword);
    }
}

record Position(int x, int y) {
    public Position move(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }
}

enum Direction {
    E(1, 0),
    S(0, 1),
    SE(1, 1),
    SW(-1, 1),
    W(-1, 0),
    N(0, -1),
    NW(-1, -1),
    NE(1, -1);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Position move(Position position) {
        return position.move(dx, dy);
    }

    public Position moveToEnd(Position position, String word) {
        return position.move(dx*(word.length()-1), dy*(word.length()-1));
    }
}

record Crossword(String... lines) {
    Crossword(Stream<String> lines) {
        this(lines.toArray(String[]::new));
    }

    boolean fits(Position start, Direction d, String word) {
        return !isOutOfBounds(d.moveToEnd(start, word));
    }
    boolean matches(Position start, Direction direction, String word) {
        if (!fits(start, direction, word)) {
            return false;
        }
        Position position = start;
        for (char c : word.toCharArray()) {
            if (isOutOfBounds(position)) {
                return false;
            }
            if (charAt(position) != c) {
                return false;
            }
            position = direction.move(position);
        }
        return true;
    }

    private boolean isOutOfBounds(Position position) {
        return position.x() < 0 || position.x() >= width() || position.y() < 0 || position.y() >= height();
    }

    int width() {
        return lines[0].length();
    }

    int height() {
        return lines.length;
    }

    char charAt(Position position) {
        return lines[position.y()].charAt(position.x());
    }

    Output findAll(String word) {
        int count = 0;
        var mask = new Mask(width());
        for (int y = 0; y < height(); y++) {
            for (int x = 0; x < width(); x++) {
                Position start = new Position(x, y);
                for (Direction direction : Direction.values()) {
                    if (matches(start, direction, word)) {
                        count++;
                        mask.add(start, direction, word);
                    }
                }
            }
        }
        return new Output(mask, count);
    }

    Output findCrosses() {
        int count = 0;
        var mask = new Mask(width());
        for (int y = 0; y < height(); y++) {
            for (int x = 0; x < width(); x++) {
                Position start = new Position(x, y);
                if (isXmas(start)) {
                    count++;
                    mask.add(start, Direction.SE, "MAS");
                    mask.add(start.move(0,2), Direction.NE, "MAS");
                }
            }
        }
        return new Output(mask, count);
    }

    /**
     * Looking for the instructions, you flip over the word search to find that this isn't actually an XMAS puzzle; it's an X-MAS puzzle in which you're supposed to find two MAS in the shape of an X. One way to achieve that is like this:
     *
     * M.S
     * .A.
     * M.S
     */
    boolean isXmas(Position start) {
        var downwardMatch = matches(start, Direction.SE, "MAS") || matches(start, Direction.SE, "SAM");
        var upward = Direction.S.moveToEnd(start, "MAS");
        var upwardMatch = matches(upward, Direction.NE, "MAS") || matches(upward, Direction.NE, "SAM");
        return downwardMatch && upwardMatch;
    }
}

class Mask {
    private final int width;
    private final BitSet set;

    Mask(int width) {
        this.width = width;
        this.set = new BitSet();
    }

    void add(Position p) {
        set.set(p.y() * width + p.x());
    }
    public void add(Position start, Direction direction, String word) {
        var pos = start;
        for(int i = 0; i < word.length(); i++) {
            add(pos);
            pos = direction.move(pos);
        }
    }

    void print(Crossword input) {
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                out.print(set.get(y * input.width() + x) ? input.charAt(new Position(x, y)) : '.');
            }
            out.println();
        }
    }
}

record Output(Mask mask, int count) {
}
