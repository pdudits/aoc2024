///usr/bin/env jbang "$0" "$@" ; exit $?


import static java.lang.System.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
/**
 * ou and The Historians look a lot more pixelated than you remember. You're inside a computer at the North Pole!

Just as you're about to check out your surroundings, a program runs up to you. "This region of memory isn't safe! The User misunderstood what a pushdown automaton is and their algorithm is pushing whole bytes down on top of us! Run!"

The algorithm is fast - it's going to cause a byte to fall into your memory space once every nanosecond! Fortunately, you're faster, and by quickly scanning the algorithm, you create a list of which bytes will fall (your puzzle input) in the order they'll land in your memory space.

Your memory space is a two-dimensional grid with coordinates that range from 0 to 70 both horizontally and vertically. However, for the sake of example, suppose you're on a smaller grid with coordinates that range from 0 to 6 and the following list of incoming byte positions:

5,4
4,2
4,5
3,0
2,1
6,3
2,4
1,5
0,6
3,3
2,6
5,1
1,2
5,5
2,5
6,5
1,4
0,4
6,4
1,1
6,1
1,0
0,5
1,6
2,0

Each byte position is given as an X,Y coordinate, where X is the distance from the left edge of your memory space and Y is the distance from the top edge of your memory space.

You and The Historians are currently in the top left corner of the memory space (at 0,0) and need to reach the exit in the bottom right corner (at 70,70 in your memory space, but at 6,6 in this example). You'll need to simulate the falling bytes to plan out where it will be safe to run; for now, simulate just the first few bytes falling into your memory space.

As bytes fall into your memory space, they make that coordinate corrupted. Corrupted memory coordinates cannot be entered by you or The Historians, so you'll need to plan your route carefully. You also cannot leave the boundaries of the memory space; your only hope is to reach the exit.

In the above example, if you were to draw the memory space after the first 12 bytes have fallen (using . for safe and # for corrupted), it would look like this:

...#...
..#..#.
....#..
...#..#
..#..#.
.#..#..
#.#....

You can take steps up, down, left, or right. After just 12 bytes have corrupted locations in your memory space, the shortest path from the top left corner to the exit would take 22 steps. Here (marked with O) is one such path:

OO.#OOO
.O#OO#O
.OOO#OO
...#OO#
..#OO#.
.#.O#..
#.#OOOO

Simulate the first kilobyte (1024 bytes) falling onto your memory space. Afterward, what is the minimum number of steps needed to reach the exit?
 */
public class day18 {
    private static final String EXAMPLE = """
            5,4
            4,2
            4,5
            3,0
            2,1
            6,3
            2,4
            1,5
            0,6
            3,3
            2,6
            5,1
            1,2
            5,5
            2,5
            6,5
            1,4
            0,4
            6,4
            1,1
            6,1
            1,0
            0,5
            1,6
            2,0
            """;

    public static void main(String... args) throws IOException {
        //var input = Input.parse(7, 7, 12, EXAMPLE.lines());
        var input = Input.parse(71,71,1024,Files.lines(Path.of("input.txt")));
        //System.out.println(new Maze(input).shortestPathFixedTime());
        System.out.println(new Maze(input).cutoffPosition(input.startTime()));
    }
}

enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    public final int x;
    public final int y;

    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

record Position(int x, int y) {
    public Position move(Direction direction) {
        return new Position(x + direction.x, y + direction.y);
    }
}

record Input(int width, int height, int startTime, List<Position> corruptions) {
    static Input parse(int  width, int height, int startTime, Stream<String> input) {
        return new Input(width, height, startTime, input.map(line -> {
            String[] parts = line.split(",");
            return new Position(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }).toList());
    }
}

class Maze {
    private Input input;
    private List<BitSet> maze;
    private Position end;
            
    Maze(Input input) {
        this.input = input;
        this.maze = new ArrayList<>();
        var m = new BitSet(input.height()*input.width());
        for (var c : input.corruptions()) {
            m.set(c.x() + c.y() * input.width());
            maze.add((BitSet)m.clone());
        }
        this.end = new Position(input.width()-1, input.height()-1);
    }

    boolean isValid(Position position, int time) {
        return position.x() >= 0 && position.x() < input.width() 
          && position.y() >= 0 && position.y() < input.height() 
          && !maze.get(Math.min(time,maze.size()-1)).get(position.x() + position.y() * input.width());
    }

    int distance(Position position) {
        return Math.abs(position.x() - end.x()) + Math.abs(position.y() - end.y());
    }

    int shortestPathFixedTime() {
        var heads = new PriorityQueue<Head>(Comparator.comparingInt(Head::score));
        heads.add(new Head(new Position(0, 0), input.startTime()-1, distance(new Position(0, 0)), List.of()));
        Head h;
        var executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            System.out.println("Heads: " + heads.size());
            System.out.println("Leading head: " + heads.peek().shortString());
        }, 10, 3, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));
        var bestScore = new HashMap<Position, Integer>();
        try {
        while((h = heads.poll()) != null) {
            if (h.position().equals(end)) {
                return h.steps().size();
            }
            for (Direction direction : Direction.values()) {
                Step nextStep = new Step(h.position(), Direction.UP); // in fixed time the directions don't matter for cycle detection
                Position nextPosition = nextStep.position().move(direction);
                var nextScore = distance(nextPosition)+h.steps().size();
                if (bestScore.getOrDefault(nextPosition, Integer.MAX_VALUE) <= nextScore) {
                    continue;
                } else {
                    bestScore.put(nextPosition, nextScore);
                }
                // if (h.steps().contains(nextStep)) {
                //     continue;
                // }   
                if (isValid(nextPosition, h.time())) {
                    List<Step> newSteps = new ArrayList<>(h.steps());
                    newSteps.add(nextStep);
                    Head nextHead = new Head(nextPosition, h.time(), nextScore, newSteps);
                    heads.add(nextHead);
                }
            }
        }
    } finally {
        executor.shutdownNow();
    }
        throw new IllegalStateException("Got lost in the maze");
    }

    Position cutoffPosition(int startTime) {
        var target = new Position(0, 0);
        for(int time = startTime; time < maze.size(); time++) {
            var m = maze.get(time);
            var heads = new PriorityQueue<Head>(Comparator.comparingInt(Head::score));
            BitSet visited = new BitSet(input.width()*input.height());
            heads.add(new Head(end, time, distance(new Position(0,0)), List.of()));
            Head head;
            boolean found = false;
            while((head = heads.poll()) != null && !found) {
                if (head.position().equals(target)) {
                    found = true;
                    break;
                }
                for (Direction direction : Direction.values()) {
                    Position nextPosition = head.position().move(direction);
                    if (!isValid(nextPosition, head.time())) {
                        continue;
                    }
                    if (visited.get(nextPosition.x() + nextPosition.y() * input.width())) {
                        continue;
                    } else {
                        visited.set(nextPosition.x() + nextPosition.y() * input.width());
                    }
                    Head nextHead = new Head(nextPosition, head.time(), distance(nextPosition), List.of());
                    heads.add(nextHead);
                }
            }
            if (!found) {
                return input.corruptions().get(time);
            } else {
                System.out.println("Found target at time " + time);
            }
        }
        throw new IllegalStateException("No cutoff position found");
    }
}

record Step(Position position, Direction direction) {}

record Head(Position position, int time, int score, List<Step> steps) {
    Stream<Head> next(Maze maze) {
        return Stream.of(Direction.values())
            .map(direction -> new Step(position, direction))
            .filter(nextStep -> maze.isValid(nextStep.position().move(nextStep.direction()), time + 1))
            .filter(nextStep -> !steps.contains(nextStep))
            .map(step -> new Head(step.position().move(step.direction()), time+1, maze.distance(step.position()), concat(steps, step)));
    }

    String shortString() {
        return String.format("[%d, %d] @ %d steps %d", position.x(), position.y(), score, steps.size());
    }

    static List<Step> concat(List<Step> steps, Step step) {
        var newSteps = new ArrayList<>(steps);
        newSteps.add(step);
        return newSteps;
    }
}
