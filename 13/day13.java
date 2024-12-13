///usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Math.multiplyExact;
import static java.lang.System.*;

/**
 * The claw machines here are a little unusual. Instead of a joystick or directional buttons to control the claw, these machines have two buttons labeled A and B. Worse, you can't just put in a token and play; it costs 3 tokens to push the A button and 1 token to push the B button.
 *
 * With a little experimentation, you figure out that each machine's buttons are configured to move the claw a specific amount to the right (along the X axis) and a specific amount forward (along the Y axis) each time that button is pressed.
 *
 * Each machine contains one prize; to win the prize, the claw must be positioned exactly above the prize on both the X and Y axes.
 *
 * You wonder: what is the smallest number of tokens you would have to spend to win as many prizes as possible? You assemble a list of every machine's button behavior and prize location (your puzzle input). For example:
 *
 * Button A: X+94, Y+34
 * Button B: X+22, Y+67
 * Prize: X=8400, Y=5400
 *
 * Button A: X+26, Y+66
 * Button B: X+67, Y+21
 * Prize: X=12748, Y=12176
 *
 * Button A: X+17, Y+86
 * Button B: X+84, Y+37
 * Prize: X=7870, Y=6450
 *
 * Button A: X+69, Y+23
 * Button B: X+27, Y+71
 * Prize: X=18641, Y=10279
 *
 * This list describes the button configuration and prize location of four different claw machines.
 *
 * For now, consider just the first claw machine in the list:
 *
 *     Pushing the machine's A button would move the claw 94 units along the X axis and 34 units along the Y axis.
 *     Pushing the B button would move the claw 22 units along the X axis and 67 units along the Y axis.
 *     The prize is located at X=8400, Y=5400; this means that from the claw's initial position, it would need to move exactly 8400 units along the X axis and exactly 5400 units along the Y axis to be perfectly aligned with the prize in this machine.
 *
 * The cheapest way to win the prize is by pushing the A button 80 times and the B button 40 times. This would line up the claw along the X axis (because 80*94 + 40*22 = 8400) and along the Y axis (because 80*34 + 40*67 = 5400). Doing this would cost 80*3 tokens for the A presses and 40*1 for the B presses, a total of 280 tokens.
 *
 * For the second and fourth claw machines, there is no combination of A and B presses that will ever win a prize.
 *
 * For the third claw machine, the cheapest way to win the prize is by pushing the A button 38 times and the B button 86 times. Doing this would cost a total of 200 tokens.
 *
 * So, the most prizes you could possibly win is two; the minimum tokens you would have to spend to win all (two) prizes is 480.
 *
 * You estimate that each button would need to be pressed no more than 100 times to win a prize. How else would someone be expected to play?
 *
 * Figure out how to win as many prizes as possible. What is the fewest tokens you would have to spend to win all possible prizes?
 *
 */
public class day13 {

    public static void main(String... args) throws IOException {
        var problems = Problem.parse(Files.readAllLines(Path.of("input.txt")));
        out.println(problems.stream().mapToLong(Problem::minTokens).sum());
        out.println(problems.stream().flatMap(p -> p.solveHarder().stream().findFirst().stream()).mapToLong(Problem::cost).sum());
    }
}

record Vector(long x, long y) {}
record ExtendedGcd(long gcd, long s, long t) {
    /**
     * https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm
     * @param a
     * @param b
     * @return
     */
    static ExtendedGcd of(long a, long b) {
        long old_r = a, r = b, old_s = 1, s = 0, old_t = 0, t = 1;
        while (r != 0) {
            long q = old_r / r;
            long tr = r;
            r = old_r - q * r;
            old_r = tr;

            long ts = s;
            s = old_s - q * s;
            old_s = ts;

            long tt = t;
            t = old_t - q * t;
            old_t = tt;
        }
        return new ExtendedGcd(old_r, old_s, old_t);
    }
}
record DiofantineSolution(long x, long y, long dx, long dy) {
    List<DiofantineSolution> around100() {
        // find solutions with roots between 0 and 100
        List<DiofantineSolution> solutions = new ArrayList<>();
        for (long k = -x/dx, x1=0; Math.abs(x1) < 120; k+=Math.signum(dx)) {
            x1 = x + k * dx;
            var y1 = y + k * dy;
            if (x1 >= 0 && y1 >= 0 && x1 <= 100 && y1 <= 100) {
                solutions.add(new DiofantineSolution(x1, y1, dx, dy));
            }
        }
        return solutions;
    }
}

final class Diofantine {
    private final long a;
    private final long b;
    private final long c;
    private final ExtendedGcd gcd;

    Diofantine(long a, long b, long c) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.gcd = ExtendedGcd.of(a, b);
    }

    boolean hasSolution() {
        return c % gcd.gcd() == 0;
    }

    public long a() {
        return a;
    }

    public long b() {
        return b;
    }

    public long c() {
        return c;
    }

    Optional<DiofantineSolution> solve() {
        if (!hasSolution()) {
            return Optional.empty();
        }
        long x = multiplyExact(gcd.s(), (c / gcd.gcd()));
        long y = multiplyExact(gcd.t(), (c / gcd.gcd()));

        long step_x = -b / gcd.gcd();
        long step_y = a / gcd.gcd();

        return Optional.of(new DiofantineSolution(x, y, step_x, step_y));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Diofantine) obj;
        return this.a == that.a &&
                this.b == that.b &&
                this.c == that.c;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c);
    }

    @Override
    public String toString() {
        return "Diofantine[" +
                "a=" + a + ", " +
                "b=" + b + ", " +
                "c=" + c + ']';
    }


}
record Problem(Vector a, Vector b, Vector prize) {

    void something() {
        out.println(this);
        var a1 = x().solve();
        var a2 = y().solve();
        out.println(a1);
        out.println(a2);
    }

    List<Vector> solve() {
        var x = x().solve();
        var y = y().solve();
        if (x.isEmpty() || y.isEmpty()) {
            return List.of();
        }
        var x1 = x.get().around100();
        var y1 = y.get().around100();
        return x1.stream()
                .flatMap(x2 -> y1.stream()
                        .filter(y2 -> x2.x() == y2.x() && x2.y() == y2.y())
                )
                .map(x2 -> new Vector(x2.x(), x2.y()))
                .sorted(Comparator.comparingLong(Problem::cost))
                .toList();
    }

    /**
     * 
     * s you go to win the first prize, you discover that the claw is nowhere near where you expected it would be. Due to a unit conversion error in your measurements, the position of every prize is actually 10000000000000 higher on both the X and Y axis!
     *
     * Add 10000000000000 to the X and Y position of every prize. After making this change, the example above would now look like this:
     *
     * Button A: X+94, Y+34
     * Button B: X+22, Y+67
     * Prize: X=10000000008400, Y=10000000005400
     *
     * Button A: X+26, Y+66
     * Button B: X+67, Y+21
     * Prize: X=10000000012748, Y=10000000012176
     *
     * Button A: X+17, Y+86
     * Button B: X+84, Y+37
     * Prize: X=10000000007870, Y=10000000006450
     *
     * Button A: X+69, Y+23
     * Button B: X+27, Y+71
     * Prize: X=10000000018641, Y=10000000010279
     *
     * Now, it is only possible to win a prize on the second and fourth claw machines. Unfortunately, it will take many more than 100 presses to do so.
     *
     * Using the corrected prize coordinates, figure out how to win as many prizes as possible. What is the fewest tokens you would have to spend to win all possible prizes?
     * 35189540698579 too low, because I haven't solved colinear case yet
     * 145928630567610 too high
     * @return
     */
    List<Vector> solveHarder() {
        var x = harderX().solve();
        var y = harderY().solve();
        if (x.isEmpty() || y.isEmpty()) {
            return List.of();
        }
        var s1 = x.get();
        var s2 = y.get();

        // Find k1 and k2 such that s1.x + k1 * s1.dx = s2.x + k2 * s2.dx
        // and s1.y + k1 * s1.dy = s2.y + k2 * s2.dy
        long dx1 = s1.dx();
        long dx2 = s2.dx();
        long dy1 = s1.dy();
        long dy2 = s2.dy();
        long x1 = s1.x();
        long x2 = s2.x();
        long y1 = s1.y();
        long y2 = s2.y();

        // Solve for k1 and k2
        long a = dx1;
        long b = -dx2;
        long c = x2 - x1;
        long d = dy1;
        long e = -dy2;
        long f = y2 - y1;

        // Use Cramer's rule to solve the system of linear equations
        long det = a * e - b * d;
        if (det == 0) {
            return List.of(); // No solution or infinite solutions (collinear)
        }

        long k1_num = c * e - b * f;
        long k2_num = a * f - c * d;

        if (k1_num % det != 0 || k2_num % det != 0) {
            return List.of(); // No integer solution
        }

        long k1 = k1_num / det;
        long k2 = k2_num / det;

        // Calculate the intersection point
        long xIntersection = x1 + k1 * dx1;
        long yIntersection = y1 + k1 * dy1;
        long xIntersection2 = x2 + k2 * dx2;
        long yIntersection2 = y2 + k2 * dy2;

        if (xIntersection != xIntersection2 || yIntersection != yIntersection2) {
            throw new IllegalArgumentException("Invalid solution");
        }

        // Check if the solutions are positive integers
        if (xIntersection < 0 || yIntersection < 0) {
            return List.of();
        }

        // Return the solution with minimal cost
        return List.of(new Vector(xIntersection, yIntersection));
    }

    long minTokens() {
        var result = solve().stream()
                .mapToLong(Problem::cost)
                .findFirst()
                .orElse(0);
        out.println(this + " : " + result);
        return result;
    }

    static long cost(Vector solution) {
        return 3 * solution.x() + solution.y();
    }

    /**
     * Input is in format, finished with empty line or EOF
     *  Button A: X+94, Y+34
     *  Button B: X+22, Y+67
     *  Prize: X=8400, Y=5400
     *
     * @param lines
     * @return
     */
    static List<Problem> parse(List<String> lines) {
        List<Problem> problems = new ArrayList<>();
        for (int i = 0; i < lines.size(); i += 4) {
            var a = Pattern.compile("Button A: X\\+(\\d+), Y\\+(\\d+)").matcher(lines.get(i));
            var b = Pattern.compile("Button B: X\\+(\\d+), Y\\+(\\d+)").matcher(lines.get(i + 1));
            var prize = Pattern.compile("Prize: X=(\\d+), Y=(\\d+)").matcher(lines.get(i + 2));
            if (!a.matches() || !b.matches() || !prize.matches()) {
                throw new IllegalArgumentException("Invalid input");
            }
            problems.add(new Problem(
                new Vector(Integer.parseInt(a.group(1)), Integer.parseInt(a.group(2))),
                new Vector(Integer.parseInt(b.group(1)), Integer.parseInt(b.group(2))),
                new Vector(Integer.parseInt(prize.group(1)), Integer.parseInt(prize.group(2)))
            ));
        }
        return problems;
    }

    public Diofantine x() {
        return new Diofantine(a.x(), b.x(), prize.x());
    }

    Vector harderPrize() {
        return new Vector(prize.x() + 10000000000000L, prize.y() + 10000000000000L);
    }

    Diofantine harderX() {
        return new Diofantine(a.x(), b.x(), harderPrize().x());
    }

    public Diofantine y() {
        return new Diofantine(a.y(), b.y(), prize.y());
    }
    Diofantine harderY() {
        return new Diofantine(a.y(), b.y(), harderPrize().y());
    }
}