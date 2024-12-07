///usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.lang.System.*;

/**
 * When you go to cross the bridge, you notice a group of engineers trying to repair it. (Apparently, it breaks pretty frequently.) You won't be able to cross until it's fixed.
 *
 * You ask how long it'll take; the engineers tell you that it only needs final calibrations, but some young elephants were playing nearby and stole all the operators from their calibration equations! They could finish the calibrations if only someone could determine which test values could possibly be produced by placing any combination of operators into their calibration equations (your puzzle input).
 *
 * For example:
 *
 * 190: 10 19
 * 3267: 81 40 27
 * 83: 17 5
 * 156: 15 6
 * 7290: 6 8 6 15
 * 161011: 16 10 13
 * 192: 17 8 14
 * 21037: 9 7 18 13
 * 292: 11 6 16 20
 *
 * Each line represents a single equation. The test value appears before the colon on each line; it is your job to determine whether the remaining numbers can be combined with operators to produce the test value.
 *
 * Operators are always evaluated left-to-right, not according to precedence rules. Furthermore, numbers in the equations cannot be rearranged. Glancing into the jungle, you can see elephants holding two different types of operators: add (+) and multiply (*).
 *
 * Only three of the above equations can be made true by inserting operators:
 *
 *     190: 10 19 has only one position that accepts an operator: between 10 and 19. Choosing + would give 29, but choosing * would give the test value (10 * 19 = 190).
 *     3267: 81 40 27 has two positions for operators. Of the four possible configurations of the operators, two cause the right side to match the test value: 81 + 40 * 27 and 81 * 40 + 27 both equal 3267 (when evaluated left-to-right)!
 *     292: 11 6 16 20 can be solved in exactly one way: 11 + 6 * 16 + 20.
 *
 * The engineers just need the total calibration result, which is the sum of the test values from just the equations that could possibly be true. In the above example, the sum of the test values for the three equations listed above is 3749.
 *
 * Determine which equations could possibly be true. What is their total calibration result?
 */
public class day7 {
    private static final String SAMPLE = """
            190: 10 19
            3267: 81 40 27
            83: 17 5
            156: 15 6
            7290: 6 8 6 15
            161011: 16 10 13
            192: 17 8 14
            21037: 9 7 18 13
            292: 11 6 16 20
            """;

    public static void main(String... args) throws IOException {
        //var inputs = SAMPLE.lines().map(Input::fromString).toArray(Input[]::new);
        var inputs = Files.lines(Path.of("input.txt")).map(Input::fromString).toArray(Input[]::new);
        var validInputs = Arrays.stream(inputs).filter(day7::isSolvable).toArray(Input[]::new);
        out.println(Arrays.stream(validInputs).mapToLong(Input::testValue).sum());
    }

    static boolean isSolvable(Input input) {
        return isSolvable(input, input.operands()[0], 1);
    }

    static boolean isSolvable(Input input, long acc, int index) {
        if (acc > input.testValue()) {
            return false;
        }
        if (index == input.operands().length) {
            return acc == input.testValue();
        }
        var operand = input.operands()[index];
        return isSolvable(input, acc*operand, index + 1) ||
                isSolvable(input, acc+operand, index + 1) ||
                isSolvable(input, concat(acc, operand), index + 1);
    }

    /**
     * The engineers seem concerned; the total calibration result you gave them is nowhere close to being within safety tolerances. Just then, you spot your mistake: some well-hidden elephants are holding a third type of operator.
     *
     * The concatenation operator (||) combines the digits from its left and right inputs into a single number. For example, 12 || 345 would become 12345. All operators are still evaluated left-to-right.
     *
     * Now, apart from the three equations that could be made true using only addition and multiplication, the above example has three more equations that can be made true by inserting operators:
     *
     *     156: 15 6 can be made true through a single concatenation: 15 || 6 = 156.
     *     7290: 6 8 6 15 can be made true using 6 * 8 || 6 * 15.
     *     192: 17 8 14 can be made true using 17 || 8 + 14.
     * @param acc
     * @param operand
     * @return
     */
    static long concat(long acc, long operand) {
        return Long.parseLong(acc + "" + operand);
    }
}

record Input(long testValue, long[] operands) {
    public static Input fromString(String input) {
        String[] parts = input.split(": ");
        long testValue =Long.parseLong(parts[0]);
        long[] operands = Arrays.stream(parts[1].trim().split(" ")).mapToLong(Long::parseLong).toArray();
        return new Input(testValue, operands);
    }
}
