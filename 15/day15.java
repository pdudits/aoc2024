///usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.lang.System.*;
import static java.util.stream.Collectors.joining;

/**
 * You appear back inside your own mini submarine! Each Historian drives their mini submarine in a different direction; maybe the Chief has his own submarine down here somewhere as well?
 *
 * You look up to see a vast school of lanternfish swimming past you. On closer inspection, they seem quite anxious, so you drive your mini submarine over to see if you can help.
 *
 * Because lanternfish populations grow rapidly, they need a lot of food, and that food needs to be stored somewhere. That's why these lanternfish have built elaborate warehouse complexes operated by robots!
 *
 * These lanternfish seem so anxious because they have lost control of the robot that operates one of their most important warehouses! It is currently running amok, pushing around boxes in the warehouse with no regard for lanternfish logistics or lanternfish inventory management strategies.
 *
 * Right now, none of the lanternfish are brave enough to swim up to an unpredictable robot so they could shut it off. However, if you could anticipate the robot's movements, maybe they could find a safe option.
 *
 * The lanternfish already have a map of the warehouse and a list of movements the robot will attempt to make (your puzzle input). The problem is that the movements will sometimes fail as boxes are shifted around, making the actual movements of the robot difficult to predict.
 *
 * For example:
 *
 * ##########
 * #..O..O.O#
 * #......O.#
 * #.OO..O.O#
 * #..O@..O.#
 * #O#..O...#
 * #O..O..O.#
 * #.OO.O.OO#
 * #....O...#
 * ##########
 *
 * <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
 * vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
 * ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
 * <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
 * ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
 * ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
 * >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
 * <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
 * ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
 * v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
 *
 * As the robot (@) attempts to move, if there are any boxes (O) in the way, the robot will also attempt to push those boxes. However, if this action would cause the robot or a box to move into a wall (#), nothing moves instead, including the robot. The initial positions of these are shown on the map at the top of the document the lanternfish gave you.
 *
 * The rest of the document describes the moves (^ for up, v for down, < for left, > for right) that the robot will attempt to make, in order. (The moves form a single giant sequence; they are broken into multiple lines just to make copy-pasting easier. Newlines within the move sequence should be ignored.)
 */
public class day15 {
    private static String SAMPLE = """
            ##########
            #..O..O.O#
            #......O.#
            #.OO..O.O#
            #..O@..O.#
            #O#..O...#
            #O..O..O.#
            #.OO.O.OO#
            #....O...#
            ##########
            
            <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
            vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
            ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
            <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
            ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
            ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
            >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
            <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
            ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
            v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
            """;

    public static void main(String... args) throws IOException {
        var input = Input.parse(Files.readString(Path.of("day15.txt")).replaceAll("\r", ""));
        //var input = Input.parse(SAMPLE);
        input = input.widen();
        input.applyMoves();
        out.println(input.gridToString());
        out.println(input.checksum());
    }
}

record Input(char[][] grid, char[] moves) {
    public static Input parse(String input) {
        String[] parts = input.split("\n\n");
        char[][] grid = parts[0].lines().map(String::toCharArray).toArray(char[][]::new);
        char[] moves = parts[1].replaceAll("\n", "").toCharArray();
        return new Input(grid, moves);
    }

    String gridToString() {
        return Arrays.stream(grid).map(String::new).collect(joining("\n"));
    }

    /**
     * To get the wider warehouse's map, start with your original map and, for each tile, make the following changes:
     *
     *     If the tile is #, the new map contains ## instead.
     *     If the tile is O, the new map contains [] instead.
     *     If the tile is ., the new map contains .. instead.
     *     If the tile is @, the new map contains @. instead.
     *
     * This will produce a new warehouse map which is twice as wide and with wide boxes that are represented by []. (The robot does not change size.)
     * @return
     */
    Input widen() {
        char[][] newGrid = new char[grid.length][grid[0].length * 2];
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                char c = grid[y][x];
                newGrid[y][x * 2] = switch (c) {
                    case '#' -> '#';
                    case 'O' -> '[';
                    case '.' -> '.';
                    case '@' -> '@';
                    default -> throw new IllegalArgumentException("Invalid character: " + c);
                };
                newGrid[y][x * 2 + 1] = switch (c) {
                    case '#' -> '#';
                    case 'O' -> ']';
                    case '.' -> '.';
                    case '@' -> '.';
                    default -> throw new IllegalArgumentException("Invalid character: " + c);
                };
            }
        }
        return new Input(newGrid, moves);
    }

    Position robot() {
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x] == '@') {
                    return new Position(x, y);
                }
            }
        }
        throw new IllegalArgumentException("Robot not found");
    }

    static int depth = 0;

    boolean move(Position position, Direction direction) {
        depth++;
        char c = grid[position.y()][position.x()];
        Position next = position.move(direction);
        char n = grid[next.y()][next.x()];
        if (n == '#') {
            return false;
        }
        if (n == 'O') {
            if (!move(next, direction)) {
                return false;
            }
        }
        if (n == '[') {
            var b = checkpoint();
            if (!move(next.move(Direction.RIGHT), direction)
               || !move(next, direction)) {
                restore(b);
                return false;
            }
        }
        if (n == ']') {
            if (grid[next.y()][next.x()-1] != '[') {
                throw new IllegalStateException("Invalid box position");
            }
            var b = checkpoint();
            if (!move(next.move(Direction.LEFT), direction)
               || !move(next, direction)) {
                restore(b);
                return false;
            }
        }
        grid[position.y()][position.x()] = '.';
        grid[next.y()][next.x()] = c;
        depth--;
        return true;
    }

    char[][] checkpoint() {
        char[][] result = new char[grid.length][];
        for (int y = 0; y < grid.length; y++) {
            result[y] = grid[y].clone();
        }
        return result;
    }

    void restore(char[][] c) {
        for (int y = 0; y < grid.length; y++) {
            grid[y] = c[y];
        }
    }

    void applyMoves() {
        Position position = robot();
        for (char move : moves) {
            depth = 0;
            if (move(position, Direction.of(move))) {
                position = position.move(Direction.of(move));
            }
        }
    }

    /**
     * The lanternfish use their own custom Goods Positioning System (GPS for short) to track the locations of the boxes. The GPS coordinate of a box is equal to 100 times its distance from the top edge of the map plus its distance from the left edge of the map. (This process does not stop at wall tiles; measure all the way to the edges of the map.)
     *
     * So, the box shown below has a distance of 1 from the top edge of the map and 4 from the left edge of the map, resulting in a GPS coordinate of 100 * 1 + 4 = 104.
     *
     * #######
     * #...O..
     * #......
     *
     * The lanternfish would like to know the sum of all boxes' GPS coordinates after the robot finishes moving.
     * @return
     */
    int checksum() {
        var result = 0;
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x] == 'O' || grid[y][x] == '[') {
                    result += 100 * y + x;
                }
            }
        }
        return result;
    }
}

enum Direction {
    UP, DOWN, LEFT, RIGHT;

    static Direction of(char c) {
        return switch (c) {
            case '^' -> UP;
            case 'v' -> DOWN;
            case '<' -> LEFT;
            case '>' -> RIGHT;
            default -> throw new IllegalArgumentException("Invalid direction: " + c);
        };
    }
}

record Position(int x, int y) {
    public Position move(Direction direction) {
        return switch (direction) {
            case UP -> new Position(x, y - 1);
            case DOWN -> new Position(x, y + 1);
            case LEFT -> new Position(x - 1, y);
            case RIGHT -> new Position(x + 1, y);
        };
    }
}


