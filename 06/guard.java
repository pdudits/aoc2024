/// usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
        // step 1:
        var start = input.findStart();
        out.println(input.walkAway());
        // 1742 too high
        // 1591 as well
        // 1546 neither
        // nor 1402
        // maybe 1485....? no.

        out.println(input.copy().singleObstacles(start));
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

    public Position inverse(Position nextPos) {
        return nextPos.move(-dx, -dy);
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

    boolean isObstacle(Position p) {
        return map[p.y()][p.x()] < 0;
    }

    boolean isInside(Position p) {
        return p.y() >= 0 && p.y() < map.length && p.x() >= 0 && p.x() < map[p.y()].length;
    }

    void mark(Position p, Direction d) {
        map[p.y()][p.x()] = d.ordinal() + 1;
    }

    int countVisited() {
        return (int) Stream.of(map).flatMapToInt(Arrays::stream).filter(i -> i > 0).count();
    }

    int singleObstacles(Position start) {
        class LoopVerifier implements Marker {
            Set<Position> matches = new HashSet<>();

            @Override
            public void mark(Position nextPos, Direction d) {
                var p = d.inverse(nextPos);
                if (matches.contains(nextPos) /* || visited(nextPos) */) {
                    return;
                }
                var mapWithObstacle = copy();
                mapWithObstacle.map[nextPos.y()][nextPos.x()] = -2;
                mapWithObstacle.mark(p, d);
                mapWithObstacle.walk(p, d, (pos, dir) -> {
                    if (!isInside(pos)) {
                        return true;
                    }
                    if (mapWithObstacle.visited(pos, d)) {
                        out.println(pos);
                        mapWithObstacle.map[pos.y()][pos.x()] = 5;
                        //mapWithObstacle.print();
                        try {
                            mapWithObstacle.copy().revalidateLoop(start, Direction.UP);
                            matches.add(pos);
                            return true;
                        } catch (IllegalArgumentException e) {
                            System.err.println("Bad loop involving"+nextPos);
                            return true;
                        }
                    }
                    return false;
                }, mapWithObstacle::mark);
            }
        }
        var verifier = new LoopVerifier();
        verifier.matches.add(start);
        mark(start, Direction.UP);
        walk(start, Direction.UP, (p, d) -> !isInside(p), verifier);
        return verifier.matches.size();
    }

    void revalidateLoop(Position start, Direction d) {
        walk(start, d, (pos, dir) -> {
            if (!isInside(pos)) {
                throw new IllegalArgumentException("Not a loop");
            }
            if (visited(pos, dir)) {
                return true;
            }
            return false;
        }, this::mark);
    }

    boolean visited(Position p, Direction d) {
        return map[p.y()][p.x()] == d.ordinal() + 1;
    }

    boolean visited(Position p) {
        return map[p.y()][p.x()] > 0;
    }

    Map copy() {
        return new Map(Arrays.stream(map).map(a -> Arrays.stream(a).map(c -> c < 0 ? c : 0).toArray()).toArray(int[][]::new));
    }

    int walkAway() {
        walk(findStart(), Direction.UP, (p, d) -> !isInside(p), null);
        print();
        return countVisited();
    }

    void walk(Position start, Direction d, TerminalCondition condition, Marker marker) {
        var p = start;
        while (true) {
            boolean finished = false;
            for (int i = 0; i < 4; i++) {
                Position nextPos = d.move(p);
                if (condition.shouldStop(nextPos, d)) {
                    return;
                }
                if (!isObstacle(nextPos)) {
                    if (marker != null) {
                        marker.mark(nextPos, d);
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

    interface TerminalCondition {
        boolean shouldStop(Position p, Direction d);
    }

    interface Marker {
        void mark(Position p, Direction d);
    }

    void print() {
        for (int[] row : map) {
            for (int cell : row) {
                out.print(switch (cell) {
                    case -2 -> 'O';
                    case -1 -> '#';
                    case 0 -> '.';
                    case 1 -> '^';
                    case 2 -> '>';
                    case 3 -> 'v';
                    case 4 -> '<';
                    case 5 -> 'L';
                    default -> throw new AssertionError("Unknown cell: " + cell);
                });
            }
            out.println();
        }
    }
}