/// usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
        var guardsPath = WalkedPath.walk(input);
        out.println(guardsPath.visitedCells());

        out.println(loopingObstacles(input, guardsPath));
    }

    static int loopingObstacles(Map input, WalkedPath path) {
        var candidates = new HashSet<Position>();
        // not the first step
        for (var step : path.steps()) {
            var cell = step.move().position();
            if (!input.isValid(cell)) {
                continue;
            }
            if (!candidates.contains(cell)) {
                var walk = WalkedPath.walk(input.withObstacle(cell));
                if (walk.result() == PathResult.LOOP) {
                    candidates.add(cell);
                }
            }
        }
        return candidates.size();
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

record Step(Position position, Direction direction) {
    public Step move() {
        return new Step(direction.move(position), direction);
    }
}

enum PathResult {
    EXIT, LOOP;
}

record WalkedPath(PathResult result, List<Step> steps) {
    static WalkedPath walk(Map map) {
        var position = map.start();
        var direction = Direction.UP;
        var path = new LinkedHashSet<Step>();
        while (true) {
            var newPosition = direction.move(position);
            // turn until we can move
            for (int i=0; map.isValid(newPosition) && map.isObstacle(newPosition); i++) {
                direction = direction.turnRight();
                newPosition = direction.move(position);
                if (i > 4) {
                    throw new AssertionError("Stuck at " + position);
                }
            }
            if (!map.isValid(newPosition)) {
                path.add(new Step(position, direction));
                return new WalkedPath(PathResult.EXIT, List.copyOf(path));
            }
            if (!path.add(new Step(position, direction))) {
                return new WalkedPath(PathResult.LOOP, List.copyOf(path));
            }
            position = newPosition;
        }
    }

    int visitedCells() {
        return steps.stream().map(Step::position).collect(HashSet::new, Set::add, Set::addAll).size();
    }
}

record Map(Position start, int[][] map) {
    public static Map fromLines(Stream<String> lines) {
        var grid = lines.map(l -> l.chars().map(c -> switch (c) {
            case '^' -> 6;
            case '#' -> -1;
            default -> 0;
        }).toArray()).toArray(int[][]::new);
        Position start = null;
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x] == 6) {
                    start = new Position(x, y);
                    break;
                }
            }
        }
        if (start == null) {
            throw new AssertionError("No start found");
        }
        return new Map(start, grid);
    }

    boolean isValid(Position p) {
        return p.y() >= 0 && p.y() < map.length && p.x() >= 0 && p.x() < map[p.y()].length;
    }

    boolean isObstacle(Position p) {
        return map[p.y()][p.x()] < 0;
    }

    private Map copy() {
        return new Map(start, Arrays.stream(map).map(int[]::clone).toArray(int[][]::new));
    }

    Map withObstacle(Position p) {
        var copy = copy();
        copy.map[p.y()][p.x()] = -2;
        return copy;
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
                    case 6 -> 'S';
                    default -> throw new AssertionError("Unknown cell: " + cell);
                });
            }
            out.println();
        }
    }
}