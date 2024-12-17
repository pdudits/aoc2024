import java.util.Arrays;
import java.util.function.IntConsumer;

class Computer {
    long A, B, C;
    int[] program;
    int ip;

    public Computer(long A, long B, long C, int[] program) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.program = program;
        this.ip = 0;
    }

    public void run(IntConsumer output) {
        while (ip < program.length) {
            int opcode = program[ip];
            int operand = program[ip + 1];
            Inst inst = Inst.fromOpcode(opcode);
            long value = inst.isLiteral() ? operand : comboValue(operand);
            switch (inst) {
                case ADV -> A = A / (1 << value);
                case BXL -> B = B ^ value;
                case BST -> B = value % 8;
                case JNZ -> {
                    if (A != 0) {
                        ip = (int)value;
                        continue;
                    }
                }
                case BXC -> B ^= C;
                case OUT -> {
                    output.accept((int)(value % 8));
                }
                case BDV -> B = A / (1 << value);
                case CDV -> C = A / (1 << value);
            }
            ip += 2;
        }
    }

    long comboValue(int operand) {
        if (operand > 6 || operand < 0) {
            throw new IllegalArgumentException("Invalid operand " + operand);
        }
        return switch (operand) {
            case 4 -> A;
            case 5 -> B;
            case 6 -> C;
            default -> operand;
        };
    }

    static Computer parse(String input) {
        String[] lines = input.split("\r?\n");
        int A = Integer.parseInt(lines[0].split(": ")[1]);
        int B = Integer.parseInt(lines[1].split(": ")[1]);
        int C = Integer.parseInt(lines[2].split(": ")[1]);
        int[] program = Arrays.stream(lines[4].substring("Program: ".length())
                        .split(","))
                .mapToInt(Integer::parseInt)
                .toArray();
        return new Computer(A, B, C, program);
    }

    enum Inst {
        ADV(),
        BXL(),
        BST(),
        JNZ(),
        BXC(),
        OUT(),
        BDV(),
        CDV();

        boolean isLiteral() {
            return this == JNZ || this == BXL || this == BXC;
        }

        public static Inst fromOpcode(int opcode) {
            return values()[opcode];
        }
    }
}
