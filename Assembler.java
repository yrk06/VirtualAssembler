import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import parser.*;

// Assembler for RISC-16
public class Assembler {

    static int instruction_counter = 0;
    static int starting_addr = 0;

    static ArrayList<Short> instructions;
    static HashMap<String, Short> labelList;
    static boolean pre_processed = false;

    public static void main(String argv[]) throws Exception {

        InputStream source = new FileInputStream(argv[0]);
        RiSCAssembler parser = new RiSCAssembler(source);

        instructions = new ArrayList<>();
        labelList = new HashMap<>();

        try {
            // The parser is built using javacc and jjtree
            // The grammar is in parser/grammar.jjt
            SimpleNode root = parser.Input();

            // Pre process the tree to replace labels with their value
            if (!preprocess(root, false)) {
                instruction_counter = 0;
                preprocess(root, false);
            }
            // Print label for debugging purposes
            for (String label : labelList.keySet()) {
                System.out.printf("Label %s has value %d\n", label, labelList.get(label));
            }
            // process the tree and generate the instructions
            processTree(root);

            // Write the instructions
            FileOutputStream fos = new FileOutputStream(argv[1]);
            int instruction_acc = 0;
            boolean lower = false;
            System.out.printf("Final Size: %d\n", instructions.size());
            for (int inst : instructions) {
                // Java works with 32bit integers, so we pack instructions in ints and save to
                // file
                if (lower) {
                    instruction_acc |= inst & 0xFFFF;
                    lower = false;
                    byte[] bytes = ByteBuffer.allocate(4).putInt(instruction_acc).array();
                    fos.write(bytes);
                } else {
                    instruction_acc = 0;
                    instruction_acc |= (inst & 0xFFFF) << 16;
                    lower = true;
                }
            }
            if (lower) {
                byte[] bytes = ByteBuffer.allocate(4).putInt(instruction_acc).array();
                fos.write(bytes);
            }
            fos.close();

        } catch (Exception e) {
            System.out.println("Oops.");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Top level function for processing
     * 
     * @param root
     */
    public static void processTree(SimpleNode root) {
        for (int i = 0; i < root.jjtGetNumChildren(); i++) {
            Node node = root.jjtGetChild(i);
            SimpleNode p = (SimpleNode) node;
            switch (p.toString()) {
                case "Directive":
                    parseDirective(p);
                    break;
                case "Command":
                    parseCommand(p);
                    break;
            }
        }
    }

    /**
     * Transform the directives into data added to the binary generated
     * 
     * @param directive
     */
    public static void parseDirective(SimpleNode directive) {
        switch ((String) directive.jjtGetValue()) {
            case ".fill":
                try {
                    instructions.add((Short) ((SimpleNode) directive.jjtGetChild(0)).jjtGetValue());
                } catch (Exception e) {
                    System.out.printf("Invalid directive \".fill %s\"\n",
                            (String) ((SimpleNode) directive.jjtGetChild(0)).jjtGetValue());
                }
                break;
            case ".space":
                try {
                    int value = (Short) ((SimpleNode) directive.jjtGetChild(0)).jjtGetValue();
                    for (int i = 0; i < value; i++) {
                        instructions.add((short) 0);
                    }
                } catch (Exception e) {
                    System.out.printf("Invalid directive \".space %s\"\n",
                            (String) ((SimpleNode) directive.jjtGetChild(0)).jjtGetValue());
                }
                break;
        }
    }

    /**
     * Parses a command branch
     * 
     * @param command
     */
    public static void parseCommand(SimpleNode command) {
        for (int i = 0; i < command.jjtGetNumChildren(); i++) {
            Node node = command.jjtGetChild(i);
            SimpleNode p = (SimpleNode) node;
            if (p.toString() == "Instruction") {
                System.out.println(p.jjtGetValue());
                switch ((String) p.jjtGetValue()) {
                    // Builds the command value based on the tree parameters
                    case "add": {
                        SimpleNode params = (SimpleNode) p.jjtGetChild(0);
                        if (params.jjtGetNumChildren() != 3)
                            System.out.printf("add requires 3 parameters but got %d\n", params.jjtGetNumChildren());
                        short regA = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(0)).jjtGetChild(0))
                                .jjtGetValue();
                        short regB = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(1)).jjtGetChild(0))
                                .jjtGetValue();
                        short regC = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(2)).jjtGetChild(0))
                                .jjtGetValue();
                        instructions.add(
                                (short) (0b000 << 13 |
                                        regA << 10 |
                                        regB << 7 |
                                        regC));
                        break;
                    }
                    case "addi": {
                        SimpleNode params = (SimpleNode) p.jjtGetChild(0);
                        if (params.jjtGetNumChildren() != 3)
                            System.out.printf("addi requires 3 parameters but got %d\n", params.jjtGetNumChildren());
                        short regA = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(0)).jjtGetChild(0))
                                .jjtGetValue();
                        short regB = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(1)).jjtGetChild(0))
                                .jjtGetValue();
                        short imm7 = imm7b((short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(2)).jjtGetChild(0))
                                .jjtGetValue());
                        instructions.add(
                                (short) (0b001 << 13 |
                                        regA << 10 |
                                        regB << 7 |
                                        (imm7 & 0x7F)));
                        break;
                    }
                    case "nand": {
                        SimpleNode params = (SimpleNode) p.jjtGetChild(0);
                        if (params.jjtGetNumChildren() != 3)
                            System.out.printf("nand requires 3 parameters but got %d\n", params.jjtGetNumChildren());
                        short regA = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(0)).jjtGetChild(0))
                                .jjtGetValue();
                        short regB = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(1)).jjtGetChild(0))
                                .jjtGetValue();
                        short regC = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(2)).jjtGetChild(0))
                                .jjtGetValue();
                        instructions.add(
                                (short) (0b010 << 13 |
                                        regA << 10 |
                                        regB << 7 |
                                        regC));
                        break;
                    }
                    case "lui": {
                        SimpleNode params = (SimpleNode) p.jjtGetChild(0);
                        if (params.jjtGetNumChildren() != 2)
                            System.out.printf("lui requires 2 parameters but got %d\n", params.jjtGetNumChildren());
                        short regA = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(0)).jjtGetChild(0))
                                .jjtGetValue();
                        short imm10 = (short) ((short) (((SimpleNode) ((SimpleNode) params.jjtGetChild(1))
                                .jjtGetChild(0)).jjtGetValue()) >> 6);

                        instructions.add(
                                (short) (0b011 << 13 |
                                        regA << 10 |
                                        (imm10 & 0x3FF)));
                        break;
                    }
                    case "sw": {
                        SimpleNode params = (SimpleNode) p.jjtGetChild(0);
                        if (params.jjtGetNumChildren() != 3)
                            System.out.printf("sw requires 3 parameters but got %d\n", params.jjtGetNumChildren());
                        short regA = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(0)).jjtGetChild(0))
                                .jjtGetValue();
                        short regB = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(1)).jjtGetChild(0))
                                .jjtGetValue();
                        short imm7 = imm7b((short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(2)).jjtGetChild(0))
                                .jjtGetValue());
                        instructions.add(
                                (short) (0b101 << 13 |
                                        regA << 10 |
                                        regB << 7 |
                                        (imm7 & 0x7F)));
                        break;
                    }
                    case "lw": {
                        SimpleNode params = (SimpleNode) p.jjtGetChild(0);
                        if (params.jjtGetNumChildren() != 3)
                            System.out.printf("lw requires 3 parameters but got %d\n", params.jjtGetNumChildren());
                        short regA = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(0)).jjtGetChild(0))
                                .jjtGetValue();
                        short regB = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(1)).jjtGetChild(0))
                                .jjtGetValue();
                        short imm7 = imm7b((short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(2)).jjtGetChild(0))
                                .jjtGetValue());
                        instructions.add(
                                (short) (0b100 << 13 |
                                        regA << 10 |
                                        regB << 7 |
                                        (imm7 & 0x7F)));
                        break;
                    }
                    case "beq": {
                        SimpleNode params = (SimpleNode) p.jjtGetChild(0);
                        if (params.jjtGetNumChildren() != 3)
                            System.out.printf("beq requires 3 parameters but got %d\n", params.jjtGetNumChildren());
                        short regA = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(0)).jjtGetChild(0))
                                .jjtGetValue();
                        short regB = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(1)).jjtGetChild(0))
                                .jjtGetValue();
                        short imm7 = imm7b((short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(2)).jjtGetChild(0))
                                .jjtGetValue());
                        System.out.println(imm7);
                        instructions.add(
                                (short) (0b110 << 13 |
                                        regA << 10 |
                                        regB << 7 |
                                        (imm7 & 0x7F)));
                        break;
                    }
                    case "jalr": {
                        SimpleNode params = (SimpleNode) p.jjtGetChild(0);
                        if (params.jjtGetNumChildren() < 2)
                            System.out.printf("jalr requires at least 2 parameters but got %d\n",
                                    params.jjtGetNumChildren());
                        short regA = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(0)).jjtGetChild(0))
                                .jjtGetValue();
                        short regB = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(1)).jjtGetChild(0))
                                .jjtGetValue();
                        short imm7 = 0;
                        if (params.jjtGetNumChildren() > 2) {
                            imm7 = imm7b((short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(2)).jjtGetChild(0))
                                    .jjtGetValue());
                        }
                        instructions.add(
                                (short) (0b111 << 13 |
                                        regA << 10 |
                                        regB << 7 |
                                        (imm7 & 0x3F)));
                        break;
                    }

                    // Custom compound instructions
                    // These instructions are expanded to more simple instructions (macro-like)

                    // mov expands: (mov)->(lui, add)
                    case "mov": {
                        SimpleNode params = (SimpleNode) p.jjtGetChild(0);
                        if (params.jjtGetNumChildren() != 2)
                            System.out.printf("lui requires 2 parameters but got %d\n", params.jjtGetNumChildren());
                        short regA = (short) ((SimpleNode) ((SimpleNode) params.jjtGetChild(0)).jjtGetChild(0))
                                .jjtGetValue();
                        short imm10 = (short) ((short) (((SimpleNode) ((SimpleNode) params.jjtGetChild(1))
                                .jjtGetChild(0)).jjtGetValue()) >> 6);
                        short imm7 = (short) ((short) (((SimpleNode) ((SimpleNode) params.jjtGetChild(1))
                                .jjtGetChild(0)).jjtGetValue())
                                & 0x3F);

                        // lui
                        instructions.add(
                                (short) (0b011 << 13 |
                                        regA << 10 |
                                        (imm10 & 0x3FF)));
                        // add
                        instructions.add(
                                (short) (0b001 << 13 |
                                        regA << 10 |
                                        regA << 7 |
                                        (imm7 & 0x3F)));
                        break;
                    }

                    // call expands into (call) -> (mov, jalr) -> (lui,add,jalr)
                    case "call": {
                        SimpleNode params = (SimpleNode) p.jjtGetChild(0);
                        if (params.jjtGetNumChildren() != 1)
                            System.out.printf("call requires 1 parameters but got %d\n", params.jjtGetNumChildren());
                        short regA = 1;
                        short imm10 = (short) ((short) (((SimpleNode) ((SimpleNode) params.jjtGetChild(0))
                                .jjtGetChild(0)).jjtGetValue()) >> 6);
                        short imm7 = (short) ((short) (((SimpleNode) ((SimpleNode) params.jjtGetChild(0))
                                .jjtGetChild(0)).jjtGetValue())
                                & 0x3F);

                        // lui
                        instructions.add(
                                (short) (0b011 << 13 |
                                        regA << 10 |
                                        (imm10 & 0x3FF)));
                        // add
                        instructions.add(
                                (short) (0b001 << 13 |
                                        regA << 10 |
                                        regA << 7 |
                                        (imm7 & 0x3F)));

                        // jalr
                        instructions.add(
                                (short) (0b111 << 13 |
                                        regA << 10 |
                                        regA << 7 |
                                        (imm7 & 0x3F)));
                        break;
                    }

                    // ret expands into (ret) -> (jalr)
                    case "ret": {
                        short regA = 1;
                        short imm7 = 0;
                        // jalr
                        instructions.add(
                                (short) (0b111 << 13 |
                                        regA << 10 |
                                        regA << 7 |
                                        (imm7 & 0x3F)));
                        break;
                    }

                    // halt expands into (halt) -> (jalr)
                    case "halt": {
                        short regA = 0;
                        short imm7 = 1;
                        // jalr
                        instructions.add(
                                (short) (0b111 << 13 |
                                        regA << 10 |
                                        regA << 7 |
                                        (imm7 & 0x3F)));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Returns the size of the instructions (used for label preprocessing)
     * 
     * @param instruction
     * @return instruction size in words
     */
    public static int getInstructionSize(SimpleNode instruction) {
        switch ((String) instruction.jjtGetValue()) {
            case "call":
                return 3;
            case "mov":
                return 2;
            default:
                return 1;
        }
    }

    /**
     * Return the size of a directive (used for label preprocessing)
     * 
     * @param directive
     * @return directive size in words
     */
    public static int getDirectiveSize(SimpleNode directive) {
        switch ((String) directive.jjtGetValue()) {
            case ".fill":
                return 1;
            case ".space":
                try {
                    return (short) ((SimpleNode) directive.jjtGetChild(0)).jjtGetValue();
                } catch (Exception e) {
                    System.out.printf("Invalid directive \".space %s\"\n",
                            (String) ((SimpleNode) directive.jjtGetChild(0)).jjtGetValue());
                }

            default:
                return 0;
        }
    }

    /**
     * Recursive functions, goes through the tree in order to remove labels
     * 
     * @param root the Tree root
     * @param beq  use relative addressing mode (only in beq instructions)
     * @return if the tree was sucessfully pre processed
     */
    public static boolean preprocess(SimpleNode root, boolean beq) {
        boolean processed = true;
        if (root.jjtGetNumChildren() == 0) {
            return true;
        }
        for (int k = 0; k < root.jjtGetNumChildren(); k++) {
            Node node = root.jjtGetChild(k);
            SimpleNode command = (SimpleNode) node;

            // If this is a beq instruction, use relative addresses
            if (command.toString() == "Instruction"
                    && ((String) command.jjtGetValue()).compareTo("beq") == 0) {
                beq = true;
            }
            if (!preprocess(command, beq))
                processed = false;

            switch (command.toString()) {
                case "Label":
                    if (labelList.containsKey(command.jjtGetValue())) {
                        for (int i = 0; i < RiSCAssembler.jjtNodeName.length; i++) {
                            if (RiSCAssembler.jjtNodeName[i] == "Immediate") {

                                int index = -1;
                                for (int j = 0; j < command.jjtGetParent().jjtGetNumChildren(); j++) {
                                    if (command.jjtGetParent().jjtGetChild(j) == command) {
                                        index = j;
                                        break;
                                    }
                                }
                                SimpleNode immValue = new SimpleNode(i);
                                command.jjtGetParent().jjtAddChild(immValue, index);

                                if (beq) {
                                    Integer rel = labelList.get(command.jjtGetValue()) - instruction_counter;
                                    immValue.jjtSetValue(rel.shortValue());
                                } else {
                                    immValue.jjtSetValue(labelList.get(command.jjtGetValue()).shortValue());
                                }

                                break;
                            }
                        }

                    } else {
                        processed = false;
                    }
                    break;
                case "LabelDec":
                    labelList.put((String) command.jjtGetValue(), (short) instruction_counter);

                    break;
                case "Instruction":
                    instruction_counter += getInstructionSize(command);
                    break;
                case "Directive":
                    instruction_counter += getDirectiveSize(command);
                    switch ((String) command.jjtGetValue()) {
                        case ".set":
                            SimpleNode label = (SimpleNode) command.jjtGetChild(0);
                            SimpleNode value = (SimpleNode) command.jjtGetChild(1);
                            labelList.put((String) label.jjtGetValue(), (short) value.jjtGetValue());

                            for (int i = 0; i < RiSCAssembler.jjtNodeName.length; i++) {
                                if (RiSCAssembler.jjtNodeName[i] == "NotProcess") {
                                    int index = -1;
                                    for (int j = 0; j < command.jjtGetParent().jjtGetNumChildren(); j++) {
                                        if (command.jjtGetParent().jjtGetChild(j) == command) {
                                            index = j;
                                            break;
                                        }

                                    }
                                    SimpleNode np = new SimpleNode(i);
                                    command.jjtGetParent().jjtAddChild(np, index);
                                    break;
                                }

                            }
                            break;
                    }
                    break;
                case "Register":
                case "Immediate":
                    try {
                        command.jjtSetValue(Short.parseShort((String) command.jjtGetValue()));
                    } catch (ClassCastException e) {
                        // ignore
                    } catch (Exception e) {

                        e.printStackTrace();
                    }

            }
        }
        return processed;
    }

    /**
     * Converts a short 16 bit signed number into a 7 bit signed number
     * 
     * @param num 16 bit signed number
     * @return 7 bit signed number
     */
    public static short imm7b(short num) {
        boolean negative = false;
        if (num < 0) {
            negative = true;
            num = (short) Math.abs(num);
        }

        num = (short) (num & 0x3F);
        if (negative) {
            num = (short) (((~num) + 1) & 0x7F);
        }

        return num;
    }
}
