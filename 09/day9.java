///usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.*;

/**
 * While The Historians quickly figure out how to pilot these things, you notice an amphipod in the corner struggling with his computer. He's trying to make more contiguous free space by compacting all of the files, but his program isn't working; you offer to help.
 *
 * He shows you the disk map (your puzzle input) he's already generated. For example:
 *
 * 2333133121414131402
 *
 * The disk map uses a dense format to represent the layout of files and free space on the disk. The digits alternate between indicating the length of a file and the length of free space.
 *
 * So, a disk map like 12345 would represent a one-block file, two blocks of free space, a three-block file, four blocks of free space, and then a five-block file. A disk map like 90909 would represent three nine-block files in a row (with no free space between them).
 *
 * Each file on disk also has an ID number based on the order of the files as they appear before they are rearranged, starting with ID 0. So, the disk map 12345 has three files: a one-block file with ID 0, a three-block file with ID 1, and a five-block file with ID 2. Using one character for each block where digits are the file ID and . is free space, the disk map 12345 represents these individual blocks:
 *
 * 0..111....22222
 *
 * The first example above, 2333133121414131402, represents these individual blocks:
 *
 * 00...111...2...333.44.5555.6666.777.888899
 *
 * The amphipod would like to move file blocks one at a time from the end of the disk to the leftmost free space block (until there are no gaps remaining between file blocks). For the disk map 12345, the process looks like this:
 *
 * 0..111....22222
 * 02.111....2222.
 * 022111....222..
 * 0221112...22...
 * 02211122..2....
 * 022111222......
 *
 * The first example requires a few more steps:
 *
 * 00...111...2...333.44.5555.6666.777.888899
 * 009..111...2...333.44.5555.6666.777.88889.
 * 0099.111...2...333.44.5555.6666.777.8888..
 * 00998111...2...333.44.5555.6666.777.888...
 * 009981118..2...333.44.5555.6666.777.88....
 * 0099811188.2...333.44.5555.6666.777.8.....
 * 009981118882...333.44.5555.6666.777.......
 * 0099811188827..333.44.5555.6666.77........
 * 00998111888277.333.44.5555.6666.7.........
 * 009981118882777333.44.5555.6666...........
 * 009981118882777333644.5555.666............
 * 00998111888277733364465555.66.............
 * 0099811188827773336446555566..............
 *
 * The final step of this file-compacting process is to update the filesystem checksum. To calculate the checksum, add up the result of multiplying each of these blocks' position with the file ID number it contains. The leftmost block is in position 0. If a block contains free space, skip it instead.
 *
 * Continuing the first example, the first few blocks' position multiplied by its file ID number are 0 * 0 = 0, 1 * 0 = 0, 2 * 9 = 18, 3 * 9 = 27, 4 * 8 = 32, and so on. In this example, the checksum is the sum of these, 1928.
 *
 * Compact the amphipod's hard drive using the process he requested. What is the resulting filesystem checksum?
 */
public class day9 {
    private static final String SAMPLE="2333133121414131402";
    public static void main(String... args) throws IOException {
        //stage2(SAMPLE);
        stage2(Files.readString(Path.of("input.txt")));
    }

    static void stage1(String input) {
        var map = DiskMap.parse(input);
        map.compact();
        out.println(map.checksum());
    }

    static void stage2(String input) {
        var map = DiskMap.parse(input);
        map.compactContiguous();
        out.println(map.checksum());
    }
}

class DiskMap {
    int[] diskMap = new int[200000]; // that's enough for the input
    int size;
    int fileId;

    DiskMap() {
        Arrays.fill(diskMap, -1);
        size = 0;
    }

    void add(int allocated, int free) {
        Arrays.fill(diskMap, size, size + allocated, fileId++);
        size += allocated + free;
    }

    static DiskMap parse(String input) {
        DiskMap diskMap = new DiskMap();
        for (int i = 0; i < input.length(); i += 2) {
            int free = i + 1 < input.length() ? input.charAt(i + 1) - '0' : 0;
            diskMap.add(input.charAt(i) - '0', free);
        }
        return diskMap;
    }

    void compact() {
        int left = 0;
        for(int right = size; right > 0; right--) {
            if (diskMap[right] != -1) {
                for(; diskMap[left] != -1; left++);
                if (left > right) {
                    return;
                }
                diskMap[left] = diskMap[right];
                diskMap[right] = -1;
                size = right;
            }
        }
    }

    /**
     * Upon completion, two things immediately become clear. First, the disk definitely has a lot more contiguous free space, just like the amphipod hoped. Second, the computer is running much more slowly! Maybe introducing all of that file system fragmentation was a bad idea?
     *
     * The eager amphipod already has a new plan: rather than move individual blocks, he'd like to try compacting the files on his disk by moving whole files instead.
     *
     * This time, attempt to move whole files to the leftmost span of free space blocks that could fit the file. Attempt to move each file exactly once in order of decreasing file ID number starting with the file with the highest file ID number. If there is no span of free space to the left of a file that is large enough to fit the file, the file does not move.
     *
     * The first example from above now proceeds differently:
     *
     * 00...111...2...333.44.5555.6666.777.888899
     * 0099.111...2...333.44.5555.6666.777.8888..
     * 0099.1117772...333.44.5555.6666.....8888..
     * 0099.111777244.333....5555.6666.....8888..
     * 00992111777.44.333....5555.6666.....8888..
     */
    void compactContiguous() {
        record FreeBlock(int index, int length) {}
        var blocks = new ArrayList<FreeBlock>();
        // construct freeblock list
        for (int i = 0; i < size; i++) {
            if (diskMap[i] == -1) {
                int length = 1;
                for (int j = i + 1; j < size && diskMap[j] == -1; j++) {
                    length++;
                }
                blocks.add(new FreeBlock(i, length));
                i += length;
            }
        }
        // compact from the end
        for (int i = size - 1; i >= 0; i--) {
            if (diskMap[i] == -1) {
                continue;
            }
            int file = diskMap[i];
            int end = i;
            for(; i > 1 && diskMap[i - 1] == file; i--);
            int fileSize = end - i + 1;
            for (var it = blocks.listIterator(); it.hasNext();) {
                var block = it.next();
                if (block.index < i && block.length >= fileSize) {
                    System.arraycopy(diskMap, i, diskMap, block.index, fileSize);
                    Arrays.fill(diskMap, i, i + fileSize, -1);
                    if (block.length == fileSize) {
                        it.remove();
                    } else {
                        it.set(new FreeBlock(block.index + fileSize, block.length - fileSize));
                    }
                    break;
                }
            }
        }
    }

    BigInteger checksum() {
        BigInteger checksum = BigInteger.ZERO;
        for (int i = 0; i < size; i++) {
            if (diskMap[i] == -1) {
                continue;
            }
            checksum = checksum.add(BigInteger.valueOf(i)
                    .multiply(BigInteger.valueOf(diskMap[i])));
        }
        return checksum;
    }
}