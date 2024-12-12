/// usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.lang.System.out;

/**
 * Why not search for the Chief Historian near the gardener and his massive farm? There's plenty of food, so The Historians grab something to eat while they search.
 * <p>
 * You're about to settle near a complex arrangement of garden plots when some Elves ask if you can lend a hand. They'd like to set up fences around each region of garden plots, but they can't figure out how much fence they need to order or how much it will cost. They hand you a map (your puzzle input) of the garden plots.
 * <p>
 * Each garden plot grows only a single type of plant and is indicated by a single letter on your map. When multiple garden plots are growing the same type of plant and are touching (horizontally or vertically), they form a region. For example:
 * <p>
 * AAAA
 * BBCD
 * BBCC
 * EEEC
 * <p>
 * This 4x4 arrangement includes garden plots growing five different types of plants (labeled A, B, C, D, and E), each grouped into their own region.
 * <p>
 * In order to accurately calculate the cost of the fence around a single region, you need to know that region's area and perimeter.
 * <p>
 * The area of a region is simply the number of garden plots the region contains. The above map's type A, B, and C plants are each in a region of area 4. The type E plants are in a region of area 3; the type D plants are in a region of area 1.
 * <p>
 * Each garden plot is a square and so has four sides. The perimeter of a region is the number of sides of garden plots in the region that do not touch another garden plot in the same region. The type A and C plants are each in a region with perimeter 10. The type B and E plants are each in a region with perimeter 8. The lone D plot forms its own region with perimeter 4.
 * <p>
 * Visually indicating the sides of plots in each region that contribute to the perimeter using - and |, the above map's regions' perimeters are measured as follows:
 * <p>
 * +-+-+-+-+
 * |A A A A|
 * +-+-+-+-+     +-+
 * |D|
 * +-+-+   +-+   +-+
 * |B B|   |C|
 * +   +   + +-+
 * |B B|   |C C|
 * +-+-+   +-+ +
 * |C|
 * +-+-+-+   +-+
 * |E E E|
 * +-+-+-+
 * <p>
 * Plants of the same type can appear in multiple separate regions, and regions can even appear within other regions. For example:
 * <p>
 * OOOOO
 * OXOXO
 * OOOOO
 * OXOXO
 * OOOOO
 * <p>
 * The above map contains five regions, one containing all of the O garden plots, and the other four each containing a single X plot.
 * <p>
 * The four X regions each have area 1 and perimeter 4. The region containing 21 type O plants is more complicated; in addition to its outer edge contributing a perimeter of 20, its boundary with each X region contributes an additional 4 to its perimeter, for a total perimeter of 36.
 * <p>
 * Due to "modern" business practices, the price of fence required for a region is found by multiplying that region's area by its perimeter. The total price of fencing all regions on a map is found by adding together the price of fence for every region on the map.
 * <p>
 * In the first example, region A has price 4 * 10 = 40, region B has price 4 * 8 = 32, region C has price 4 * 10 = 40, region D has price 1 * 4 = 4, and region E has price 3 * 8 = 24. So, the total price for the first example is 140.
 * <p>
 * In the second example, the region with all of the O plants has price 21 * 36 = 756, and each of the four smaller X regions has price 1 * 4 = 4, for a total price of 772 (756 + 4 + 4 + 4 + 4).
 * <p>
 * Here's a larger example:
 * <p>
 * RRRRIICCFF
 * RRRRIICCCF
 * VVRRRCCFFF
 * VVRCCCJFFF
 * VVVVCJJCFE
 * VVIVCCJJEE
 * VVIIICJJEE
 * MIIIIIJJEE
 * MIIISIJEEE
 * MMMISSJEEE
 * <p>
 * It contains:
 * <p>
 * A region of R plants with price 12 * 18 = 216.
 * A region of I plants with price 4 * 8 = 32.
 * A region of C plants with price 14 * 28 = 392.
 * A region of F plants with price 10 * 18 = 180.
 * A region of V plants with price 13 * 20 = 260.
 * A region of J plants with price 11 * 20 = 220.
 * A region of C plants with price 1 * 4 = 4.
 * A region of E plants with price 13 * 18 = 234.
 * A region of I plants with price 14 * 22 = 308.
 * A region of M plants with price 5 * 12 = 60.
 * A region of S plants with price 3 * 8 = 24.
 * <p>
 * So, it has a total price of 1930.
 */
public class day12 {
    private static final String SAMPLE = """
            RRRRIICCFF
            RRRRIICCCF
            VVRRRCCFFF
            VVRCCCJFFF
            VVVVCJJCFE
            VVIVCCJJEE
            VVIIICJJEE
            MIIIIIJJEE
            MIIISIJEEE
            MMMISSJEEE
            """;

    private static final String TRIVIAL = """
            AABAA
            AAABA
            AAAAA
            """;

    public static void main(String... args) throws IOException {
        var input = Input.from(Files.lines(Path.of("input.txt")));
        //var input = Input.from(SAMPLE);
        out.println(price(input));
    }

    static int price(Input input) {
        var prices = new RegionPrices(input);
        return prices.discountedPrice();
    }

    static class RegionPrices {
        private final Input input;
        List<Region> regions = new ArrayList<>();
        Region[][] regionMap;

        RegionPrices(Input input) {
            this.input = input;
            this.regionMap = new Region[input.height()][input.width()];
            input.iterate().forEach(p -> {
                if (regionMap[p.y()][p.x()] == null) {
                    var region = new Region(input.at(p), p, input);
                    regions.add(region);
                    regionMap[p.y()][p.x()] = region;
                    floodFill(region);
                }
            });

        }

        int compute() {
            return regions.stream().mapToInt(Region::price).sum();
        }

        /**
         * Fortunately, the Elves are trying to order so much fence that they qualify for a bulk discount!
         * <p>
         * Under the bulk discount, instead of using the perimeter to calculate the price, you need to use the number of sides each region has. Each straight section of fence counts as a side, regardless of how long it is.
         * <p>
         * Consider this example again:
         * <p>
         * AAAA
         * BBCD
         * BBCC
         * EEEC
         * <p>
         * The region containing type A plants has 4 sides, as does each of the regions containing plants of type B, D, and E. However, the more complex region containing the plants of type C has 8 sides!
         * <p>
         * Using the new method of calculating the per-region price by multiplying the region's area by its number of sides, regions A through E have prices 16, 16, 32, 4, and 12, respectively, for a total price of 80.
         * <p>
         * The second example above (full of type X and O plants) would have a total price of 436.
         * <p>
         * Here's a map that includes an E-shaped region full of type E plants:
         * <p>
         * EEEEE
         * EXXXX
         * EEEEE
         * EXXXX
         * EEEEE
         * <p>
         * The E-shaped region has an area of 17 and 12 sides for a price of 204. Including the two regions full of type X plants, this map has a total price of 236.
         * <p>
         * This map has a total price of 368:
         * <p>
         * AAAAAA
         * AAABBA
         * AAABBA
         * ABBAAA
         * ABBAAA
         * AAAAAA
         * <p>
         * It includes two regions full of type B plants (each with 4 sides) and a single region full of type A plants (with 4 sides on the outside and 8 more sides on the inside, a total of 12 sides). Be especially careful when counting the fence around regions like the one full of type A plants; in particular, each section of fence has an in-side and an out-side, so the fence does not connect across the middle of the region (where the two B regions touch diagonally). (The Elves would have used the Möbius Fencing Company instead, but their contract terms were too one-sided.)
         * <p>
         * The larger example from before now has the following updated prices:
         * <p>
         * A region of R plants with price 12 * 10 = 120.
         * A region of I plants with price 4 * 4 = 16.
         * A region of C plants with price 14 * 22 = 308.
         * A region of F plants with price 10 * 12 = 120.
         * A region of V plants with price 13 * 10 = 130.
         * A region of J plants with price 11 * 12 = 132.
         * A region of C plants with price 1 * 4 = 4.
         * A region of E plants with price 13 * 8 = 104.
         * A region of I plants with price 14 * 16 = 224.
         * A region of M plants with price 5 * 6 = 30.
         * A region of S plants with price 3 * 6 = 18.
         * <p>
         * Adding these together produces its new total price of 1206.
         * <p>
         * 933579 too low
         * 941238 too high
         *
         * @return
         */
        int discountedPrice() {
            return regions.stream().mapToInt(Region::discountedPrice).sum();
        }

        void floodFill(Region region) {
            var toCheck = new ArrayDeque<Position>();
            toCheck.add(region.seed);
            Position p;
            while ((p = toCheck.poll()) != null) {
                input.sameNeighbours(p).forEach(np -> {
                            if (regionMap[np.y()][np.x()] == null) {
                                toCheck.add(np);
                                region.add(np);
                                regionMap[np.y()][np.x()] = region;
                            }
                        }
                );
            }
        }

        boolean isInside(Position p) {
            return p.x() >= 0 && p.x() < input.width() && p.y() >= 0 && p.y() < input.height();
        }

        class Region {
            private final char type;
            private final Input input;
            private int area;
            private int perimeter;
            private final Position seed;
            private final Set<Position> visited = new HashSet<>();
            private final Set<Edge> edges = new HashSet<>();

            public Region(char type, Position seed, Input input) {
                this.type = type;
                this.seed = seed;
                this.input = input;
                add(seed);
            }

            void add(Position p) {
                if (!visited.add(p)) {
                    return;
                }
                area++;
                perimeter += 4 - (int) input.sameNeighbours(p).count();
                // Check all four edges of the current cell
                checkEdge(p, Direction.UP);
                checkEdge(p, Direction.RIGHT);
                checkEdge(p, Direction.DOWN);
                checkEdge(p, Direction.LEFT);
            }

            int price() {
                return area * perimeter;
            }

            private void checkEdge(Position p, Direction dir) {
                Position neighbor = dir.move(p);
                // If neighbor is outside bounds or different type, this is a boundary edge
                if (!isInBounds(neighbor) || input.at(neighbor) != type) {
                    edges.add(new Edge(p, dir));
                }
            }

            private boolean isInBounds(Position p) {
                return p.x() >= 0 && p.x() < input.width() &&
                        p.y() >= 0 && p.y() < input.height();
            }

            public int discountedPrice() {
                int sides = countDistinctSides();
                out.println("A region of %s plants with price %d * %d = %d.".formatted(type, area, sides, area * sides));
                return area * sides;
            }

            private int countDistinctSides() {
                int sides = 0;
                // Group edges by their orientation and position
                Map<Integer, Set<Edge>> horizontalEdges = new HashMap<>();
                Map<Integer, Set<Edge>> verticalEdges = new HashMap<>();

                for (Edge edge : edges) {
                    if (edge.direction.isHorizontal()) {
                        horizontalEdges
                                .computeIfAbsent(edge.position.x(), k -> new TreeSet<>(
                                        Comparator.comparing(Edge::direction).thenComparingInt(e -> e.position().y())
                                ))
                                .add(edge);
                    } else {
                        verticalEdges
                                .computeIfAbsent(edge.position.y(), k -> new TreeSet<>(Comparator.comparing(Edge::direction).thenComparingInt(e -> e.position().x())))
                                .add(edge);
                    }
                }

                // Count continuous horizontal segments
                for (Set<Edge> row : horizontalEdges.values()) {
                    sides += countContinuousSegments(row);
                }

                // Count continuous vertical segments
                for (Set<Edge> column : verticalEdges.values()) {
                    sides += countContinuousSegments(column);
                }
                return sides;
            }

            private int countContinuousSegments(Set<Edge> edges) {
                if (edges.isEmpty()) {
                    return 0;
                }

                // Sort edges by position
                var it = edges.iterator();
                int segments = 1;
                Edge prev = it.next();

                while (it.hasNext()) {
                    Edge curr = it.next();
                    if (!isAdjacent(prev, curr)) {
                        segments++;
                    }
                    prev = curr;
                }

                return segments;
            }

            private boolean isAdjacent(Edge e1, Edge e2) {
                if (e1.direction != e2.direction) {
                    return false;
                }
                return Math.abs(e1.position.x() - e2.position.x()) + Math.abs(e1.position.y() - e2.position.y()) == 1;
            }

            private record Edge(Position position, Direction direction) { }

            private enum Direction {
                UP(0, -1), RIGHT(1, 0), DOWN(0, 1), LEFT(-1, 0);

                final int dx, dy;

                Direction(int dx, int dy) {
                    this.dx = dx;
                    this.dy = dy;
                }

                Position move(Position p) {
                    return new Position(p.x() + dx, p.y() + dy);
                }

                boolean isHorizontal() {
                    return this == LEFT || this == RIGHT;
                }
            }
        }
    }


}

record Position(int x, int y) {
}

record Input(char[][] garden) {
    public static Input from(String input) {
        return from(input.lines());
    }

    public static Input from(Stream<String> lines) {
        return new Input(lines.map(String::toCharArray).toArray(char[][]::new));
    }

    int width() {
        return garden[0].length;
    }

    int height() {
        return garden.length;
    }

    char at(Position p) {
        return garden[p.y()][p.x()];
    }

    Stream<Position> sameNeighbours(Position p) {
        return Stream.of(
                        new Position(p.x() - 1, p.y()),
                        new Position(p.x() + 1, p.y()),
                        new Position(p.x(), p.y() - 1),
                        new Position(p.x(), p.y() + 1)
                )
                .filter(p2 -> p2.x() >= 0 && p2.x() < width() && p2.y() >= 0 && p2.y() < height()
                        && garden[p.y()][p.x()] == garden[p2.y()][p2.x()]);
    }

    Stream<Position> iterate() {
        return Stream.iterate(new Position(0, 0), p -> p.x() < width() - 1 || p.y() < height() - 1, p -> {
            if (p.x() < width() - 1) {
                return new Position(p.x() + 1, p.y());
            } else {
                return new Position(0, p.y() + 1);
            }
        });
    }

}

