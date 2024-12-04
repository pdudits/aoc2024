///usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.System.*;

/**
 * "Our computers are having issues, so I have no idea if we have any Chief Historians in stock! You're welcome to check the warehouse, though," says the mildly flustered shopkeeper at the North Pole Toboggan Rental Shop. The Historians head out to take a look.
 *
 * The shopkeeper turns to you. "Any chance you can see why our computers are having issues again?"
 *
 * The computer appears to be trying to run a program, but its memory (your puzzle input) is corrupted. All of the instructions have been jumbled up!
 *
 * It seems like the goal of the program is just to multiply some numbers. It does that with instructions like mul(X,Y), where X and Y are each 1-3 digit numbers. For instance, mul(44,46) multiplies 44 by 46 to get a result of 2024. Similarly, mul(123,4) would multiply 123 by 4.
 *
 * However, because the program's memory has been corrupted, there are also many invalid characters that should be ignored, even if they look like part of a mul instruction. Sequences like mul(4*, mul(6,9!, ?(12,34), or mul ( 2 , 4 ) do nothing.
 *
 * For example, consider the following section of corrupted memory:
 *
 * xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))
 *
 * Only the four highlighted sections are real mul instructions. Adding up the result of each instruction produces 161 (2*4 + 5*5 + 11*8 + 8*5).
 *
 * Scan the corrupted memory for uncorrupted mul instructions. What do you get if you add up all of the results of the multiplications?
 *
 *
 *
 */
public class computer1 {
    private static final String SAMPLE = "xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))";
    private static final String SAMPLE_2 ="xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))";
    public static void main(String... args) throws IOException {
        out.println(Multiplication.fromString(Files.readString(Path.of("input.txt"))).
            mapToInt(Multiplication::result).sum());
        out.println(Multiplication.withEnable(Files.readString(Path.of("input.txt"))).
                mapToInt(Multiplication::result).sum());
    }


}

record Multiplication(int x, int y) {
    static final Pattern MUL = Pattern.compile("mul\\((\\d+),(\\d+)\\)");
    /**
     * As you scan through the corrupted memory, you notice that some of the conditional statements are also still intact. If you handle some of the uncorrupted conditional statements in the program, you might be able to get an even more accurate result.
     *
     * There are two new instructions you'll need to handle:
     *
     *     The do() instruction enables future mul instructions.
     *     The don't() instruction disables future mul instructions.
     *
     * Only the most recent do() or don't() instruction applies. At the beginning of the program, mul instructions are enabled.
     */
    static final Pattern ENABLED_MUL = Pattern.compile("(do)\\(\\)|mul\\((\\d+),(\\d+)\\)|(don't)\\(\\)");

    public static Stream<Multiplication> fromString(String input) {
        return MUL.matcher(input).results()
                .map(m -> new Multiplication(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))));
    }

    public static Stream<Multiplication> withEnable(String input) {
        class EnableFilter implements Predicate<MatchResult> {
            boolean enabled = true;
            @Override
            public boolean test(MatchResult matchResult) {
                if (matchResult.group(1) != null) {
                    enabled = true;
                    return false;
                } else if (matchResult.group(4) != null) {
                    enabled = false;
                    return false;
                }
                return enabled;
            }
        };
        return ENABLED_MUL.matcher(input).results()
                .filter(new EnableFilter())
                .map(m -> new Multiplication(Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3))));
    }

    public int result() {
        return x * y;
    }
}
