///usr/bin/env jbang "$0" "$@" ; exit $?
//
//DEPS org.openjfx:javafx-graphics:21.0.5:${os.detected.jfxname}


import javafx.application.Application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.lang.System.*;

/**
 * One of The Historians needs to use the bathroom; fortunately, you know there's a bathroom near an unvisited location on their list, and so you're all quickly teleported directly to the lobby of Easter Bunny Headquarters.
 *
 * Unfortunately, EBHQ seems to have "improved" bathroom security again after your last visit. The area outside the bathroom is swarming with robots!
 *
 * To get The Historian safely to the bathroom, you'll need a way to predict where the robots will be in the future. Fortunately, they all seem to be moving on the tile floor in predictable straight lines.
 *
 * You make a list (your puzzle input) of all of the robots' current positions (p) and velocities (v), one robot per line. For example:
 *
 * p=0,4 v=3,-3
 * p=6,3 v=-1,-3
 * p=10,3 v=-1,2
 * p=2,0 v=2,-1
 * p=0,0 v=1,3
 * p=3,0 v=-2,-2
 * p=7,6 v=-1,-3
 * p=3,0 v=-1,-2
 * p=9,3 v=2,3
 * p=7,3 v=-1,2
 * p=2,4 v=2,-3
 * p=9,5 v=-3,-3
 *
 * Each robot's position is given as p=x,y where x represents the number of tiles the robot is from the left wall and y represents the number of tiles from the top wall (when viewed from above). So, a position of p=0,0 means the robot is all the way in the top-left corner.
 *
 * Each robot's velocity is given as v=x,y where x and y are given in tiles per second. Positive x means the robot is moving to the right, and positive y means the robot is moving down. So, a velocity of v=1,-2 means that each second, the robot moves 1 tile to the right and 2 tiles up.
 *
 * The robots outside the actual bathroom are in a space which is 101 tiles wide and 103 tiles tall (when viewed from above). However, in this example, the robots are in a space which is only 11 tiles wide and 7 tiles tall.
 *
 * The robots are good at navigating over/under each other (due to a combination of springs, extendable legs, and quadcopters), so they can share the same tile and don't interact with each other. Visually, the number of robots on each tile in this example looks like this:
 *
 * 1.12.......
 * ...........
 * ...........
 * ......11.11
 * 1.1........
 * .........1.
 * .......1...
 *
 * These robots have a unique feature for maximum bathroom security: they can teleport. When a robot would run into an edge of the space they're in, they instead teleport to the other side, effectively wrapping around the edges. Here is what robot p=2,4 v=2,-3 does for the first few seconds:
 *
 * Initial state:
 * ...........
 * ...........
 * ...........
 * ...........
 * ..1........
 * ...........
 * ...........
 *
 * After 1 second:
 * ...........
 * ....1......
 * ...........
 * ...........
 * ...........
 * ...........
 * ...........
 *
 * After 2 seconds:
 * ...........
 * ...........
 * ...........
 * ...........
 * ...........
 * ......1....
 * ...........
 *
 * After 3 seconds:
 * ...........
 * ...........
 * ........1..
 * ...........
 * ...........
 * ...........
 * ...........
 *
 * After 4 seconds:
 * ...........
 * ...........
 * ...........
 * ...........
 * ...........
 * ...........
 * ..........1
 *
 * After 5 seconds:
 * ...........
 * ...........
 * ...........
 * .1.........
 * ...........
 * ...........
 * ...........
 *
 * The Historian can't wait much longer, so you don't have to simulate the robots for very long. Where will the robots be after 100 seconds?
 *
 * In the above example, the number of robots on each tile after 100 seconds has elapsed looks like this:
 *
 * ......2..1.
 * ...........
 * 1..........
 * .11........
 * .....1.....
 * ...12......
 * .1....1....
 *
 * To determine the safest area, count the number of robots in each quadrant after 100 seconds. Robots that are exactly in the middle (horizontally or vertically) don't count as being in any quadrant, so the only relevant robots are:
 *
 * ..... 2..1.
 * ..... .....
 * 1.... .....
 *
 * ..... .....
 * ...12 .....
 * .1... 1....
 *
 * In this example, the quadrants contain 1, 3, 4, and 1 robot. Multiplying these together gives a total safety factor of 12.
 *
 * Predict the motion of the robots in your list within a space which is 101 tiles wide and 103 tiles tall. What will the safety factor be after exactly 100 seconds have elapsed?
 */
public class day14 {
    private static final String SAMPLE = """
            p=0,4 v=3,-3
            p=6,3 v=-1,-3
            p=10,3 v=-1,2
            p=2,0 v=2,-1
            p=0,0 v=1,3
            p=3,0 v=-2,-2
            p=7,6 v=-1,-3
            p=3,0 v=-1,-2
            p=9,3 v=2,3
            p=7,3 v=-1,2
            p=2,4 v=2,-3
            p=9,5 v=-3,-3
            """;

    public static void main(String... args) throws IOException {
        safetyFactor(SAMPLE.lines(), new Room(11, 7));
        safetyFactor(Files.lines(Path.of("input.txt")), new Room(101, 103));
        var robots = Robot.parse(Files.lines(Path.of("input.txt"))).toList();
        RobotView.robots = robots;
        RobotView.room = new Room(101, 103);
        Application.launch(RobotView.class, args);
    }

    static int safetyFactor(Stream<String> input, Room room) {
        var robots = Robot.parse(input).toList();
        for (int i = 0; i < 100; i++) {
            robots = robots.stream().map(r -> r.move(room)).toList();
        }
        robots.forEach(r -> out.println(r));
        var factor = robots.stream()
                .collect(Collectors.groupingBy(r -> quadrant(r, room), Collectors.counting()))
                .entrySet().stream()
                .peek(out::println)
                .filter(e -> e.getKey() != 0)
                .mapToLong(e -> e.getValue()).reduce(1, (a, b) -> a * b);
        out.println(factor);
        return (int)factor;
    }

    static int quadrant(Robot r, Room room) {
        if (r.x() == room.width() /2 || r.y() == room.height() / 2) {
            return 0;
        }
        return (r.x() < room.width() / 2 ? 1 : 2) + (r.y() < room.height() / 2 ? 0 : 2);
    }
}



record Room(int width, int height) {
    public boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}
record Robot(int x, int y, int vx, int vy) {
    public Robot move(Room r) {
        return new Robot((x + vx + r.width()) % r.width(), (y + vy + r.height()) % r.height(), vx, vy);
    }

    public Robot moveBack(Room r) {
        return new Robot((x - vx + r.width()) % r.width(), (y - vy + r.height()) % r.height(), vx, vy);
    }

    /**
     * Each line is in form p=0,4 v=3,-3
     * @param input
     * @return
     */
    static Stream<Robot> parse(Stream<String> input) {
        return input.map(line -> {
            var match = Pattern.compile("p=(\\d+),(\\d+) v=(-?\\d+),(-?\\d+)").matcher(line);
            if (!match.matches()) {
                throw new IllegalArgumentException("Invalid input: " + line);
            }
            return new Robot(parseInt(match.group(1)), parseInt(match.group(2)), parseInt(match.group(3)), parseInt(match.group(4)));
        });
    }
}

