///usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.System.*;

/**
 * While The Historians do their thing, you take a look at the familiar huge antenna. Much to your surprise, it seems to have been reconfigured to emit a signal that makes people 0.1% more likely to buy Easter Bunny brand Imitation Mediocre Chocolate as a Christmas gift! Unthinkable!
 *
 * Scanning across the city, you find that there are actually many such antennas. Each antenna is tuned to a specific frequency indicated by a single lowercase letter, uppercase letter, or digit. You create a map (your puzzle input) of these antennas. For example:
 *
 * ............
 * ........0...
 * .....0......
 * .......0....
 * ....0.......
 * ......A.....
 * ............
 * ............
 * ........A...
 * .........A..
 * ............
 * ............
 *
 * The signal only applies its nefarious effect at specific antinodes based on the resonant frequencies of the antennas. In particular, an antinode occurs at any point that is perfectly in line with two antennas of the same frequency - but only when one of the antennas is twice as far away as the other. This means that for any pair of antennas with the same frequency, there are two antinodes, one on either side of them.
 *
 * So, for these two antennas with frequency a, they create the two antinodes marked with #:
 *
 * ..........
 * ...#......
 * ..........
 * ....a.....
 * ..........
 * .....a....
 * ..........
 * ......#...
 * ..........
 * ..........
 *
 * Adding a third antenna with the same frequency creates several more antinodes. It would ideally add four antinodes, but two are off the right side of the map, so instead it adds only two:
 *
 * ..........
 * ...#......
 * #.........
 * ....a.....
 * ........a.
 * .....a....
 * ..#.......
 * ......#...
 * ..........
 * ..........
 *
 * Antennas with different frequencies don't create antinodes; A and a count as different frequencies. However, antinodes can occur at locations that contain antennas. In this diagram, the lone antenna with frequency capital A creates no antinodes but has a lowercase-a-frequency antinode at its location:
 *
 * ..........
 * ...#......
 * #.........
 * ....a.....
 * ........a.
 * .....a....
 * ..#.......
 * ......A...
 * ..........
 * ..........
 *
 * The first example has antennas with two different frequencies, so the antinodes they create look like this, plus an antinode overlapping the topmost A-frequency antenna:
 *
 * ......#....#
 * ...#....0...
 * ....#0....#.
 * ..#....0....
 * ....0....#..
 * .#....A.....
 * ...#........
 * #......#....
 * ........A...
 * .........A..
 * ..........#.
 * ..........#.
 *
 * Because the topmost A-frequency antenna overlaps with a 0-frequency antinode, there are 14 total unique locations that contain an antinode within the bounds of the map.
 *
 * Calculate the impact of the signal. How many unique locations within the bounds of the map contain an antinode?
 */
public class day8 {

    private static final String SAMPLE = """
            ............
            ........0...
            .....0......
            .......0....
            ....0.......
            ......A.....
            ............
            ............
            ........A...
            .........A..
            ............
            ............
            """;

    public static void main(String... args) throws IOException {
        //var input = Stage.parse(SAMPLE.lines());
        var input = Stage.parse(Files.lines(Path.of("input.txt")));
        out.println(input.antinodeLocations());
        out.println(input.antinodeLocationsWithHarmonics());
    }


}

record Position(int x, int y) {}
record Antenna(char frequency, Position position) {}
record Stage(int width, int height, List<Antenna> antennas) {
    static Stage parse(Stream<String> lines) {
        class Collector {
            int width = 0;
            int height = 0;
            List<Antenna> antennas = new ArrayList<>();

            void add(String line) {
                line = line.trim();
                for (int x = 0; x < line.length(); x++) {
                    char c = line.charAt(x);
                    if (c == '.') {
                        continue;
                    }
                    if (Character.isDigit(c) || Character.isLowerCase(c) || Character.isUpperCase(c)) {
                        antennas.add(new Antenna(c, new Position(x, height)));
                    }
                }
                width = Math.max(width, line.length());
                height++;
            }
        }
        var collector = new Collector();
        lines.forEach(collector::add);
        return new Stage(collector.width, collector.height, List.copyOf(collector.antennas));
    }

    /**
     * For each frequency, we collect all the antennas, and then calculate antinode locations
     * for every pair of that type.
     * @return
     */
    int antinodeLocations() {
        var processedFrequencies = new HashSet<Character>();
        var antinodeLocations = new HashSet<Position>();
        for (var antenna : antennas) {
            if (processedFrequencies.contains(antenna.frequency())) {
                continue;
            }
            processedFrequencies.add(antenna.frequency());
            var sameFrequency = antennas.stream()
                    .filter(a -> a.frequency() == antenna.frequency())
                    .toList();
            for (int i = 0; i < sameFrequency.size(); i++) {
                for (int j = i + 1; j < sameFrequency.size(); j++) {
                    var a = sameFrequency.get(i).position();
                    var b = sameFrequency.get(j).position();
                    var dx = b.x() - a.x();
                    var dy = b.y() - a.y();
                    var antinode = new Position(b.x() + dx, b.y() + dy);
                    if (insideStage(antinode)) {
                        antinodeLocations.add(antinode);
                    }
                    antinode = new Position(a.x() - dx, a.y() - dy);
                    if (insideStage(antinode)) {
                        antinodeLocations.add(antinode);
                    }
                }
            }
        }
        return antinodeLocations.size();
    }

    /*
    Watching over your shoulder as you work, one of The Historians asks if you took the effects of resonant harmonics into your calculations.

Whoops!

After updating your model, it turns out that an antinode occurs at any grid position exactly in line with at least two antennas of the same frequency, regardless of distance. This means that some of the new antinodes will occur at the position of each antenna (unless that antenna is the only one of its frequency).

So, these three T-frequency antennas now create many antinodes:

T....#....
...T......
.T....#...
.........#
..#.......
..........
...#......
..........
....#.....
..........

In fact, the three T-frequency antennas are all exactly in line with two antennas, so they are all also antinodes! This brings the total number of antinodes in the above example to 9.

The original example now has 34 antinodes, including the antinodes that appear on every antenna:

##....#....#
.#.#....0...
..#.#0....#.
..##...0....
....0....#..
.#...#A....#
...#..#.....
#....#.#....
..#.....A...
....#....A..
.#........#.
...#......##

Calculate the impact of the signal using this updated model. How many unique locations within the bounds of the map contain an antinode?
     */
    int antinodeLocationsWithHarmonics() {
        var processedFrequencies = new HashSet<Character>();
        var antinodeLocations = new HashSet<Position>();
        for (var antenna : antennas) {
            if (processedFrequencies.contains(antenna.frequency())) {
                continue;
            }
            processedFrequencies.add(antenna.frequency());
            var sameFrequency = antennas.stream()
                    .filter(a -> a.frequency() == antenna.frequency())
                    .toList();
            for (int i = 0; i < sameFrequency.size(); i++) {
                for (int j = i + 1; j < sameFrequency.size(); j++) {
                    var a = sameFrequency.get(i).position();
                    var b = sameFrequency.get(j).position();
                    var dx = b.x() - a.x();
                    var dy = b.y() - a.y();
                    var inRange = true;
                    for (int n=0; inRange; n++ ) {
                        var antinode = new Position(b.x() + dx * n, b.y() + dy * n);
                        if (insideStage(antinode)) {
                            antinodeLocations.add(antinode);
                        } else {
                            inRange = false;
                        }
                        antinode = new Position(a.x() - dx * n, a.y() - dy * n);
                        if (insideStage(antinode)) {
                            antinodeLocations.add(antinode);
                            inRange = true;
                        }
                    }
                }
            }
        }
        return antinodeLocations.size();
    }

    private boolean insideStage(Position antinode) {
        return antinode.x() >= 0 && antinode.x() < width && antinode.y() >= 0 && antinode.y() < height;
    }
}
