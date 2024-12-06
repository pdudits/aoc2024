/// usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.lang.System.out;

/**
 * Unfortunately, a single guard is patrolling this part of the lab.
 *
 * Maybe you can work out where the guard will go ahead of time so that The Historians can search safely?
 *
 * You start by making a map (your puzzle input) of the situation. For example:
 *
 * ....#.....
 * .........#
 * ..........
 * ..#.......
 * .......#..
 * ..........
 * .#..^.....
 * ........#.
 * #.........
 * ......#...
 *
 * The map shows the current position of the guard with ^ (to indicate the guard is currently facing up from the perspective of the map). Any obstructions - crates, desks, alchemical reactors, etc. - are shown as #.
 *
 * Lab guards in 1518 follow a very strict patrol protocol which involves repeatedly following these steps:
 *
 *     If there is something directly in front of you, turn right 90 degrees.
 *     Otherwise, take a step forward.
 *
 * Following the above protocol, the guard moves up several times until she reaches an obstacle (in this case, a pile of failed suit prototypes):
 *
 * ....#.....
 * ....^....#
 * ..........
 * ..#.......
 * .......#..
 * ..........
 * .#........
 * ........#.
 * #.........
 * ......#...
 *
 * Because there is now an obstacle in front of the guard, she turns right before continuing straight in her new facing direction:
 *
 * ....#.....
 * ........>#
 * ..........
 * ..#.......
 * .......#..
 * ..........
 * .#........
 * ........#.
 * #.........
 * ......#...
 *
 * Reaching another obstacle (a spool of several very long polymers), she turns right again and continues downward:
 *
 * ....#.....
 * .........#
 * ..........
 * ..#.......
 * .......#..
 * ..........
 * .#......v.
 * ........#.
 * #.........
 * ......#...
 *
 * This process continues for a while, but the guard eventually leaves the mapped area (after walking past a tank of universal solvent):
 *
 * ....#.....
 * .........#
 * ..........
 * ..#.......
 * .......#..
 * ..........
 * .#........
 * ........#.
 * #.........
 * ......#v..
 *
 * By predicting the guard's route, you can determine which specific positions in the lab will be in the patrol path. Including the guard's starting position, the positions visited by the guard before leaving the area are marked with an X:
 *
 * ....#.....
 * ....XXXXX#
 * ....X...X.
 * ..#.X...X.
 * ..XXXXX#X.
 * ..X.X.X.X.
 * .#XXXXXXX.
 * .XXXXXXX#.
 * #XXXXXXX..
 * ......#X..
 *
 * In this example, the guard will visit 41 distinct positions on your map.
 *
 * Predict the path of the guard. How many distinct positions will the guard visit before leaving the mapped area?
 */
public class guard {
    private static final String SAMPLE = """
            ....#.....
            .........#
            ..........
            ..#.......
            .......#..
            ..........
            .#..^.....
            ........#.
            #.........
            ......#...
            """;

    public static void main(String... args) throws IOException {
        //var input = Map.fromLines(SAMPLE.lines());
        var input = Map.fromLines(Files.lines(Path.of("input.txt")));
        var start = input.findStart();
        out.println(input.walkAway());
        // 1747 is too high
        out.println(input.singleObstacleLoops(start, Direction.UP));
    }
}

record Position(int x, int y) {
    public Position move(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }
}

enum Direction {
    UP(0, -1), RIGHT(1, 0), DOWN(0, 1), LEFT(-1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Position move(Position p) {
        return p.move(dx, dy);
    }

    public Direction turnRight() {
        return switch (this) {
            case UP -> RIGHT;
            case RIGHT -> DOWN;
            case DOWN -> LEFT;
            case LEFT -> UP;
        };
    }
}

record Map(int[][] map) {
    public static Map fromLines(Stream<String> lines) {
        return new Map(lines.map(l -> l.chars().map(c -> switch (c) {
            case '^' -> 1;
            case '#' -> -1;
            default -> 0;
        }).toArray()).toArray(int[][]::new));
    }

    Position findStart() {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                if (map[y][x] == 1) {
                    return new Position(x, y);
                }
            }
        }
        throw new IllegalArgumentException("No start position found");
    }

    /**
     * just copy obstacles not directions
     * @return
     */
    Map copy() {
        return new Map(Arrays.stream(map)
                .map(a -> Arrays.stream(a).map(x -> x < 0 ? x : 0).toArray())
                .toArray(int[][]::new));
    }

    boolean wasHeading(Position p, Direction d) {
        return (map[p.y()][p.x()] & (1 << d.ordinal())) != 0;
    }

    boolean tryObstacle(Position start, Position p, Direction d) {
        var copy = copy();
        copy.mark(p, d);
        Position nextPos = d.move(p);
        if (nextPos.equals(start)) {
            return false;
        }
        if (!isInside(nextPos)) {
            return false;
        }
        if (isObstacle(nextPos)) {
                // cannot place new obstacle there
                return false;
        } else {
             copy.map[nextPos.y()][nextPos.x()] = -2;
             var result = copy.walksIntoLoop(p, d.turnRight());
             if (result) {
                 //out.println();
                 //copy.print();
                 return true;
             } else {
                 return false;
             }
        }
    }

    int singleObstacleLoops(Position start, Direction d) {
        Position p = start;
        int loops = 0;
        while (true) {
            if (tryObstacle(start, p, d)) {
                loops++;
            }
            boolean finished = false;
            for (int i = 0; i < 4; i++) {
                Position nextPos = d.move(p);
                if (!isInside(nextPos)) {
                    return loops;
                }
                if (!isObstacle(nextPos)) {
                    p = nextPos;
                    finished = true;
                    break;
                }
                d = d.turnRight();
            }
            if (!finished) {
                print();
                throw new AssertionError("Blocked at " + p);
            }
        }
    }

    boolean walksIntoLoop(Position p, Direction d) {
        while (true) {
            boolean finished = false;
            for (int i = 0; i < 4; i++) {
                Position nextPos = d.move(p);
                if (!isInside(nextPos)) {
                    return false;
                }
                if (!isObstacle(nextPos)) {
                    if (wasHeading(nextPos, d)) {
                        return true;
                    }
                    mark(nextPos, d);
                    p = nextPos;
                    finished = true;
                    break;
                }
                d = d.turnRight();
            }
            if (!finished) {
                print();
                throw new AssertionError("Blocked at " + p);
            }
        }
    }

    boolean isObstacle(Position p) {
        return map[p.y()][p.x()] == -1;
    }

    boolean isInside(Position p) {
        return p.y() >= 0 && p.y() < map.length && p.x() >= 0 && p.x() < map[p.y()].length;
    }

    void mark(Position p, Direction d) {
        map[p.y()][p.x()] |= 1 << d.ordinal();
    }

    int countVisited() {
        return (int) Stream.of(map).flatMapToInt(Arrays::stream).filter(i -> i > 0).count();
    }

    int walkAway() {
        Position p = findStart();
        Direction d = Direction.UP;
        while (true) {
            boolean finished = false;
            for (int i = 0; i < 4; i++) {
                Position nextPos = d.move(p);
                if (!isInside(nextPos)) {
                    print();
                    return countVisited();
                }
                if (!isObstacle(nextPos)) {
                    mark(nextPos, d);
                    p = nextPos;
                    finished = true;
                    break;
                }
                d = d.turnRight();
            }
            if (!finished) {
                print();
                throw new AssertionError("Blocked at " + p);
            }
        }
    }

    void print() {
        for (int[] row : map) {
            for (int cell : row) {
                out.print(
                        switch (cell) {
                            // I spent too much time here
                            case -2 -> 'O';
                            case -1 -> '#';
                            case 0 -> '.';
                            case 1 -> '↑';
                            case 2 -> '→';
                            case 3 -> '↗';
                            case 4 -> '↓';
                            case 5 -> '↕';
                            case 6 -> '↘';
                            case 7 -> '⇲';
                            case 8 -> '←';
                            case 9 -> '↖';
                            case 10 -> '↔';
                            case 11 -> '⟰';
                            case 12 -> '↙';
                            case 13 -> '⇚';
                            case 14 -> '⟱';
                            case 15 -> '⟴';
                            default -> throw new AssertionError("Unknown cell: " + cell);
                        });
            }
            out.println();
        }
    }
}