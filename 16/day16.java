/// usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.lang.System.out;

/**
 * The Reindeer start on the Start Tile (marked S) facing East and need to reach the End Tile (marked E). They can move forward one tile at a time (increasing their score by 1 point), but never into a wall (#). They can also rotate clockwise or counterclockwise 90 degrees at a time (increasing their score by 1000 points).
 *
 * To figure out the best place to sit, you start by grabbing a map (your puzzle input) from a nearby kiosk. For example:
 *
 * ###############
 * #.......#....E#
 * #.#.###.#.###.#
 * #.....#.#...#.#
 * #.###.#####.#.#
 * #.#.#.......#.#
 * #.#.#####.###.#
 * #...........#.#
 * ###.#.#####.#.#
 * #...#.....#.#.#
 * #.#.#.###.#.#.#
 * #.....#...#.#.#
 * #.###.#.#.#.#.#
 * #S..#.....#...#
 * ###############
 *
 * There are many paths through this maze, but taking any of the best paths would incur a score of only 7036. This can be achieved by taking a total of 36 steps forward and turning 90 degrees a total of 7 times:
 *
 *
 * ###############
 * #.......#....E#
 * #.#.###.#.###^#
 * #.....#.#...#^#
 * #.###.#####.#^#
 * #.#.#.......#^#
 * #.#.#####.###^#
 * #..>>>>>>>>v#^#
 * ###^#.#####v#^#
 * #>>^#.....#v#^#
 * #^#.#.###.#v#^#
 * #^....#...#v#^#
 * #^###.#.#.#v#^#
 * #S..#.....#>>^#
 * ###############
 *
 * Here's a second example:
 *
 * #################
 * #...#...#...#..E#
 * #.#.#.#.#.#.#.#.#
 * #.#.#.#...#...#.#
 * #.#.#.#.###.#.#.#
 * #...#.#.#.....#.#
 * #.#.#.#.#.#####.#
 * #.#...#.#.#.....#
 * #.#.#####.#.###.#
 * #.#.#.......#...#
 * #.#.###.#####.###
 * #.#.#...#.....#.#
 * #.#.#.#####.###.#
 * #.#.#.........#.#
 * #.#.#.#########.#
 * #S#.............#
 * #################
 *
 * In this maze, the best paths cost 11048 points; following one such path would look like this:
 *
 * #################
 * #...#...#...#..E#
 * #.#.#.#.#.#.#.#^#
 * #.#.#.#...#...#^#
 * #.#.#.#.###.#.#^#
 * #>>v#.#.#.....#^#
 * #^#v#.#.#.#####^#
 * #^#v..#.#.#>>>>^#
 * #^#v#####.#^###.#
 * #^#v#..>>>>^#...#
 * #^#v###^#####.###
 * #^#v#>>^#.....#.#
 * #^#v#^#####.###.#
 * #^#v#^........#.#
 * #^#v#^#########.#
 * #S#>>^..........#
 * #################
 *
 * Note that the path shown above includes one 90 degree turn as the very first move, rotating the Reindeer from facing East to facing North.
 *
 * Analyze your map carefully. What is the lowest score a Reindeer could possibly get?
 */
public class day16 {
    private static final String SAMPLE1 = """
            ###############
            #.......#....E#
            #.#.###.#.###.#
            #.....#.#...#.#
            #.###.#####.#.#
            #.#.#.......#.#
            #.#.#####.###.#
            #...........#.#
            ###.#.#####.#.#
            #...#.....#.#.#
            #.#.#.###.#.#.#
            #.....#...#.#.#
            #.###.#.#.#.#.#
            #S..#.....#...#
            ###############
            """;
    private static String SAMPLE2 = """
            #################
            #...#...#...#..E#
            #.#.#.#.#.#.#.#.#
            #.#.#.#...#...#.#
            #.#.#.#.###.#.#.#
            #...#.#.#.....#.#
            #.#.#.#.#.#####.#
            #.#...#.#.#.....#
            #.#.#####.#.###.#
            #.#.#.......#...#
            #.#.###.#####.###
            #.#.#...#.....#.#
            #.#.#.#####.###.#
            #.#.#.........#.#
            #.#.#.#########.#
            #S#.............#
            #################
            """;

    public static void main(String... args) throws IOException {
        var maze = Maze.parse(Files.lines(Path.of("input.txt")));
        out.println(maze.findShortestPath());
    }
}

enum Direction {
    NORTH, EAST, SOUTH, WEST;

    public Direction turnRight() {
        return Direction.values()[(this.ordinal() + 1) % 4];
    }

    public Direction turnLeft() {
        return Direction.values()[(this.ordinal() + 3) % 4];
    }
}

record Point(int x, int y) {
    public Point move(int dx, int dy) {
        return new Point(x + dx, y + dy);
    }

    public int distance(Point p) {
        return Math.abs(x - p.x) + Math.abs(y - p.y);
    }
}

record Position(Point p,  Direction direction) {
    public Position moveForward() {
        return switch (direction) {
            case NORTH -> new Position(p.move(0, -1), direction);
            case EAST -> new Position(p.move(1,0), direction);
            case SOUTH -> new Position(p.move(0, 1), direction);
            case WEST -> new Position(p.move(-1, 0), direction);
        };
    }

    public Position turnRight() {
        return new Position(p, direction.turnRight());
    }

    public Position turnLeft() {
        return new Position(p, direction.turnLeft());
    }

    public int distance(Position end) {
        return p.distance(end.p());
    }
}

record Maze(char[][] map, Position start, Position end) {
    public int width() {
        return map[0].length;
    }

    public int height() {
        return map.length;
    }

    public boolean isWall(Position position) {
        return map[position.p().y()][position.p().x()] == '#';
    }

    public boolean isEnd(Position position) {
        return position.p().equals(end.p());
    }

    static Maze parse(Stream<String> lines) {
        char[][] map = lines.map(String::toCharArray).toArray(char[][]::new);
        Position start = null;
        Position end = null;
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                if (map[y][x] == 'S') {
                    start = new Position(new Point(x,y), Direction.EAST);
                } else if (map[y][x] == 'E') {
                    end = new Position(new Point(x, y), Direction.EAST);
                }
            }
        }
        return new Maze(map, start, end);
    }

    int findShortestPath() {
        record Head(Position p, int score, int distance, List<Position> path) {
            int sortKey() {
                return score + distance;
            }

            Head create(Direction dir, int addScore, Position end) {
                Position newPosition = dir == p.direction() ? p.moveForward() : new Position(p.p(), dir);
                var newPath = new ArrayList<Position>(path.size()+1);
                newPath.addAll(path);
                newPath.add(newPosition);
                return new Head(newPosition, score + addScore, newPosition.distance(end), newPath);
            }

            static Head create(Position p, int score, Position end) {
                return new Head(p, score, p.distance(end), List.of());
            }
        }


        TreeSet<Head> heads = new TreeSet<>(Comparator.comparingInt(Head::sortKey)
                .thenComparingInt(x -> x.p().p().x())
                .thenComparingInt(x -> x.p().p().y())
                .thenComparing(x -> x.p().direction()));
        var visited = new HashSet<Position>();
        heads.add(Head.create(start, 0, end));
        while (!heads.isEmpty()) {
            Head head = heads.pollFirst();
            if (!visited.add(head.p())) {
                continue;
            }
            if (isEnd(head.p())) {
                return head.score();
            }
            Position forward = head.p().moveForward();
            if (isValidPosition(forward)) {
                heads.add(head.create(head.p().direction(), 1, end));
            }
            if (isValidPosition(head.p().turnRight().moveForward())) {
                heads.add(head.create(head.p().direction().turnRight(), 1000, end));
            }
            if (isValidPosition(head.p().turnLeft().moveForward())) {
                heads.add(head.create(head.p().direction().turnLeft(), 1000, end));
            }
        }
        throw new IllegalStateException("No path found");
    }

    private boolean isValidPosition(Position forward) {
        return forward.p().x() >= 0 && forward.p().x() < width()
                && forward.p().y() >= 0 && forward.p().y() < height()
                && !isWall(forward);
    }
}