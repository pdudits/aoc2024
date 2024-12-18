import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class day172 {
    // futhrest I got was 64025057, around halfway. But I should have done the recursive search I intended before
    final static int LOOKAHEAD = 10;
    static int[] translation = new int[2<<LOOKAHEAD];
    final static int[] program = 
                            {2,4,1,1,7,5,0,3,1,4,4,4,5,5,3,0};

    public static void main(String... args) {
        for (int i=0; i< translation.length; i++) {
            translation[i] = TabStep.of(i).bXorC();
        }
        var result = search();
        var actualResult = run(result);
        System.out.printf("%d %1$o\n%s", result, actualResult);
    }

    static long search() {
        for (int i=0; i<translation.length;i++) {
            if (translation[i] == program[0]) {
                var result = search(i, 1, i % 8);
                if (result != -1) {
                    var actualResult = run(result);
                    System.out.printf("%d %1$o\n%s\n", result, actualResult);
                    if (Arrays.asList(program).equals(actualResult)) {
                        return result;
                    }
                }
            }
        }
        return -1;
    }

    static long search(int lookahead, int index, long a) {
        var b = lookahead / 8;
        for (int i=0; i<8; i++) {
            var nextLookahead = (i << LOOKAHEAD-3) | b;
            if (translation[nextLookahead] == program[index]) {
                long result = (long)nextLookahead % 8 << 3*index | a;
                if (index == program.length-1) {
                    result = (long)nextLookahead << 3*index | a;
                    return result;
                }
                result = search(nextLookahead, index+1, result);
                if (result != -1) {
                    return result;
                }
            }
        }
        return -1;
    }
        // var target = List.of(2,4,1,1,7,5,0,3,1);//,4,4,4,5,5,3,0);
        // var lengthHit = false;
        // //var sofar = 0464025052L;
        // var sofar = 025052L;
        // // 13642282                             64025052 [2, 4, 1, 1, 7, 5, 0, 3]
        // // 13642285                             64025055 [2, 4, 1, 1, 7, 5, 0, 3]
        // // 13642287                             64025057 [2, 4, 1, 1, 7, 5, 0, 3]
        // //var sofar = 064025052L;
        // var shift = 24;
        // System.out.printf("Checking shift %36o\n", 011 << shift | sofar);
        // for (long i=0; i<01000000000; i++) {
        //     long a = i << shift | sofar;
        //     //long a = i;
        //     var result = run(a);
        //     if (!lengthHit && result.size() == target.size()) {
        //         System.out.printf("Length hist at %o\n", a);
        //         lengthHit = true;
        //     }
        //     if (result.size() > target.size()) {
        //         System.out.printf("Result got too big at %o\n", a);
        //         break;
        //     }
        //     //System.out.printf("%-3d %9o %s\n", i, a, result);
        //     if (result.equals(target)) {
        //         System.out.printf("%-3d %36o %s\n", i, a, result);
        //     }
        


    //     var c = new Computer(0464025052L,0,0,program);

    //     var result = new ArrayList<Integer>();
    //     c.run(result::add);
    //     System.out.println(result);
    //     if (Arrays.asList(program).equals(result)) {
    //         System.out.println("Success");
    //     }
    //     //int a = compute(0, program, program.length-1);
    // }

    static ArrayList<Integer> run(long a) {
        var c = new Computer(a,0,0,program);
        var result = new ArrayList<Integer>();
        c.run(result::add);
        return result;
    }

}

record TabStep(int a, int b, int bXor1, int c, int bXor4, int bXorC) {
    public static TabStep of(int a) {
        var b = a%8;
        var bXor1 = b^1;
        var bXor4 = bXor1^4;
        var c = a/(1<<bXor1);
        var bXorC = bXor4^c;
        return new TabStep(a, b, bXor1, c, bXor4, bXorC % 8);
    }

    public String tabular() {
        // decimal A  octal A B bXor1 C bXor4 bXorC
        var next = of(a>>3);
        return "%-3d %3o %2d %2d %4o %2d  %2$3o %1d,%1d".formatted(a, a, b, bXor1, c, bXor4, bXorC, next.bXorC);
    }
}
