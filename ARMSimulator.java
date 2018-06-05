import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * An application which simulates how the ARM instructions
 * are handled in a pipelined ARM processor.
 *
 * @author Aditya Singh
 * @author Shivin Dass
 * @author Taejas Gupta
 */

public class ARMSimulator {
    private static int[] R = new int[16];
    private static ArrayList<String> instructionMemory = new ArrayList<String>();
    private static int[] dataMemory = new int[100000];
    private static int opcode;
    private static int imm;
    private static int condition;
    private static int I;
    private static int F;
    private static int N;
    private static int Z;
    private static int op1;
    private static int op2;
    private static int dest;
    private static int off;
    private static int Rd;
    private static int address;
    private static boolean isLS;
    private static int shamt;
    private static String instruction;


    /**
     * Reads the instruction memory from the instruction.mem file and stores it in the ArrayList instructionMemory.
     * @throws IOException
     */
    public static void loadInstructionMemory() throws IOException {
        File file = new File("instructionMemory.mem");
        Scanner reader = new Scanner(new BufferedReader(new FileReader(file)));

        while (reader.hasNext()) {
            instructionMemory.add(reader.nextLine());
        }
        reader.close();
    }

    /**
     * Reads the data memory from the dataMemory.mem file and stores it in the ArrayList dataMemory.
     * @throws FileNotFoundException
     */
    public static void loadDataMemory() throws FileNotFoundException {
        File file = new File("dataMemory.mem");
        Scanner reader = new Scanner(new BufferedReader(new FileReader(file)));

        int cnt = 0;
        while (reader.hasNext()) {
            String data = reader.nextLine();
            dataMemory[cnt++] = Integer.parseInt(data);
        }
        reader.close();
    }

    /**
     * Writes the values in the ArrayList dataMemory to the dataMemory.mem file.
     * @throws IOException
     */
    public static void writeDataMemory() throws IOException {
        File file = new File("dataMemory.mem");

        FileWriter writer = new FileWriter(file);

        for (int data: dataMemory) {
            writer.write(Integer.toString(data)+"\n");
        }
        writer.close();
    }

    /**
     * Converts a hexadecimal number to a binary number.
     * @param hex The hexadecimal number in String format.
     * @return
     */
    public static String hexToBin(String hex)
    {
        String bin="";
        char[] h=hex.toCharArray();
        for(int i=0; i<hex.length(); i++)
        {
            if(h[i]=='0') bin=bin+"0000";
            else if(h[i]=='1') bin=bin+"0001";
            else if(h[i]=='2') bin=bin+"0010";
            else if(h[i]=='3') bin=bin+"0011";
            else if(h[i]=='4') bin=bin+"0100";
            else if(h[i]=='5') bin=bin+"0101";
            else if(h[i]=='6') bin=bin+"0110";
            else if(h[i]=='7') bin=bin+"0111";
            else if(h[i]=='8') bin=bin+"1000";
            else if(h[i]=='9') bin=bin+"1001";
            else if(h[i]=='A' || h[i]=='a') bin=bin+"1010";
            else if(h[i]=='B' || h[i]=='b') bin=bin+"1011";
            else if(h[i]=='C' || h[i]=='c') bin=bin+"1100";
            else if(h[i]=='D' || h[i]=='d') bin=bin+"1101";
            else if(h[i]=='E' || h[i]=='e') bin=bin+"1110";
            else if(h[i]=='F' || h[i]=='f') bin=bin+"1111";
        }
        return bin;
    }

    /**
     * Converts a binary number to a hexadecimal number.
     * @param bin The binary number in String format.
     * @return
     */
    public static int binToDec(String bin)
    {
        char[] b=bin.toCharArray();
        int pot=1;
        int dec=0;
        for(int i=bin.length()-1; i>=0; i--)
        {
            //System.out.println("x");
            if(b[i]=='1')  dec=dec+pot;
            pot=pot*2;
        }
        return dec;
    }

    /**
     * Simulates the fetch stage of the pipeline.
     */
    public static void fetch() {
        String instruction = instructionMemory.get(R[15]);
        R[15]++;

        String address = instruction.substring(0, instruction.length() - 11);
        instruction = instruction.substring(instruction.length() - 8);

        System.out.println("FETCH: Fetch instruction 0x" + instruction + " from address " + address + ".");
    }

    /**
     * Simulates the decode stage of the pipeline.
     */
    public static void decode() {
        instruction = instructionMemory.get(R[15] - 1);
        instruction = instruction.substring(instruction.length() - 8);
        instruction=hexToBin(instruction);
        I=binToDec(instruction.substring(4, 5));
        F=binToDec(instruction.substring(5, 6));

        if(binToDec(instruction.substring(27, 28)) == 0)
            shamt = binToDec(instruction.substring(20, 25));
        else
            shamt = R[binToDec(instruction.substring(20, 24))];

        if(binToDec(instruction.substring(26, 27)) == 0)
            isLS = true;
        else
            isLS = false;

        if(binToDec(instruction.substring(4, 11)) == 0 && binToDec(instruction.substring(16, 20)) == 0 && binToDec(instruction.substring(24, 28)) == 9)
        {
            // Custom opcode to support MUL
            opcode = 100;
            imm = 0;
            dest = binToDec(instruction.substring(12, 16));
            op1 = binToDec(instruction.substring(28, 32));
            op2 = binToDec(instruction.substring(20, 24));

            System.out.println("DECODE: Operation is MUL, First operand is R"+op1+", Second operand is R"+op2+", Destination register is R"+dest+".");
        }

        else if(I==0 && F==0)
        {
            opcode=binToDec(instruction.substring(7, 11));
            imm=binToDec(instruction.substring(6, 7));
            op1=binToDec(instruction.substring(12, 16));
            dest=binToDec(instruction.substring(16, 20));

            String im="";
            String reg="";
            String operation="";
            if(imm==0)
            {
                op2 = binToDec(instruction.substring(28, 32));
                im=", Second operand is R"+op2;
                reg="Read Registers R"+op1+" = "+R[op1]+", R"+op2+" = "+R[op2];

            }
            else if(imm==1)
            {
                op2=binToDec(instruction.substring(24, 32));
                im=", Immediate Second operand is "+op2;
                reg="Read Registers R"+op1+" = "+R[op1];
            }

            switch(opcode)
            {
                case 0: operation="AND"; break;
                case 1: operation="XOE"; break;
                case 2: operation="SUB"; break;
                case 3: operation="RSB"; break;
                case 4: operation="ADD"; break;
                case 5: operation="ADC"; break;
                case 6: operation="SBC"; break;
                case 7: operation="RSC"; break;
                case 10: operation="CMP"; break;
                case 12: operation="ORR"; break;
                case 13: operation="MOV"; break;
            }
            System.out.println("DECODE: Operation is "+operation+", First operand is R"+op1+im+", Destination register is R"+dest+".");
            System.out.println(reg + ".");
        }
        else if(I==0 && F==1)
        {
            opcode=binToDec(instruction.substring(6, 12));
            op1=binToDec(instruction.substring(12, 16));
            op2=binToDec(instruction.substring(20, 32));
            dest=binToDec(instruction.substring(16, 20));
            if(opcode==25)
            {
                System.out.println("DECODE: Operation is LDR, base register is R"+op1+", offset is "+op2+", Destination register is R"+dest+".");
            }
            else if(opcode==24)
            {
                System.out.println("DECODE: Operation is STR, base register is R"+op1+", offset is "+op2+", register to be stored is R"+dest+".");
            }
        }
        else if(I==1 && F==0)
        {
            opcode=binToDec(instruction.substring(5, 8));
            off=binToDec(instruction.substring(7, 32));
            condition=binToDec(instruction.substring(0, 4));
            String operation="";
            if(opcode==2)
            {
                switch(condition)
                {
                    case 0: operation="BEQ"; break;
                    case 1: operation="BNE"; break;
                    case 10: operation="BGE"; break;
                    case 11: operation="BLT"; break;
                    case 12: operation="BGT"; break;
                    case 13: operation="BLE"; break;
                    case 14: operation="BAL"; break;
                }
                System.out.println("DECODE: Operation is "+operation+".");
            }
        }
        else {
            System.out.println("DECODE: No decode operation.");
        }
    }

    /**
     * Simulates the execute stage of the pipeline.
     */
    public static void execute() {
        if(!(I==1 && F==1)) {
            System.out.print("EXECUTE: ");
        }
        else {
            System.out.println("EXECUTE: No execute operation.");
        }

        // MUL
        if(I == 0 && F == 0 && opcode == 100)
        {
            Rd = R[op1] * R[op2];
            System.out.println("MUL " + R[op1] + " and " + R[op2] + ".");
        }

        else if(I == 0 && F == 0)
        {
            int a = R[op1];
            int b;

            if(imm == 1)
                b = op2;
            else
            {
                b = R[op2];
                if(shamt > 0)
                {
                    if(isLS)
                    {
                        String op2str = Integer.toBinaryString(b);
                        while(op2str.length() < 32)
                            op2str = "0" + op2str;

                        op2str = op2str.substring(shamt, 32);
                        for(int i = 0; i < shamt; i++)
                            op2str += "0";
                        b = binToDec(op2str);
                    }

                    else
                    {
                        b = b >>> shamt;
                    }
                }
            }

            switch(opcode)
            {
                // 0000: AND
                case 0:
                    Rd = a & b;
                    System.out.println("AND " + a + " and " + b + ".");
                    break;

                // 0001: Exclusive OR
                case 1:
                    Rd = a ^ b;
                    System.out.println("EOR " + a + " and " + b + ".");
                    break;

                // 0010: Subtract
                case 2:
                    Rd = a - b;
                    System.out.println("SUB " + a + " and " + b + ".");
                    break;

                // 0011: Reverse Subtract
                case 3:
                    Rd = b - a;
                    System.out.println("RSB " + a + " and " + b + ".");
                    break;

                // 0100: Add
                case 4:
                    Rd = a + b;
                    System.out.println("ADD " + a + " and " + b + ".");
                    break;

                /*
                // 0101: Add with Carry
                case 5:
                    Rd = a + b + C;
                    System.out.println("ADC " + a + " and " + b + " with carry " + C + ".");
                    break;

                // 0110: Subtract with Carry
                case 6:
                    Rd = a - b + C - 1;
                    System.out.println("SBC " + a + " and " + b + " with carry " + C + ".");
                    break;

                // 0111: Reverse Subtract with Carry
                case 7:
                    Rd = b - a + C - 1;
                    System.out.println("RSC " + a + " and " + b + " with carry " + C + ".");
                    break;
                */

                // 1010: Compare
                case 10:
                    if(a < b)
                        N = 1;
                    else if(a == b)
                    {
                        Rd = 0;
                        Z = 1;
                    }
                    System.out.println("CMP " + a + " and " + b + ".");
                    break;

                // 1100: OR
                case 12:
                    Rd = a | b;
                    System.out.println("ORR: " + a + " and " + b + ".");
                    break;

                // 1101: Move Register or Constant
                case 13:
                    Rd = b;
                    System.out.println("MOV " + b + " in R" + dest + ".");
                    break;
            }
        }

        else if(I == 0 && F == 1)
        {
            switch(opcode)
            {
                // 011000: Store Register to Memory
                case 24:
                    //dataMem[op1 + op2 / 4] = Rd;
                    address=R[op1] + op2 / 4;
                    System.out.println("STR R" + dest + " in data memory location " + (R[op1] + op2) + ".");
                    break;

                // 011001: Load Register from Memory
                case 25:
                    //Rd = dataMem[op1 + op2 / 4];
                    address=R[op1] + op2 / 4;
                    System.out.println("LDR " + dataMemory[R[op1] + op2 / 4] + " in R" + dest + ".");
                    break;
            }
        }

        else if(I == 1 && F == 0 && opcode == 2)
        {
            int distance;
            if((off >> 23 & 1) == 0)
                distance = off;
            else
                distance = 0xFF000000 | off;

            switch(condition)
            {
                // 0000: BEQ
                case 0:
                    if(Z == 1)
                    {
                        R[15] += distance + 1;
                        System.out.println("BEQ with offset " + distance + ".");
                    }
                    else
                        System.out.println("BEQ branch not taken" + ".");
                    break;

                // 0001: BNE
                case 1:
                    if(Z == 0)
                    {
                        R[15] += distance + 1;
                        System.out.println("BNE with offset " + distance + ".");
                    }
                    else
                        System.out.println("BNE branch not taken.");
                    break;

                // 1010: BGE
                case 10:
                    if(N == 0 || Z == 1)
                    {
                        R[15] += distance + 1;
                        System.out.println("BGE with offset " + distance + ".");
                    }
                    else
                        System.out.println("BGE branch not taken.");
                    break;

                // 1011: BLT
                case 11:
                    if(N == 1 && Z == 0)
                    {
                        R[15] += distance + 1;
                        System.out.println("BLT with offset " + distance + ".");
                    }
                    else
                        System.out.println("BLT branch not taken.");
                    break;

                // 1100: BGT
                case 12:
                    if(N == 0 && Z == 0)
                    {
                        R[15] += distance + 1;
                        System.out.println("BGT with offset " + distance + ".");
                    }
                    else
                        System.out.println("BGT branch not taken.");
                    break;

                // 1101: BLE
                case 13:
                    if(N == 1 || Z == 1)
                    {
                        R[15] += distance + 1;
                        System.out.println("BLE with offset " + distance + ".");
                    }
                    else
                        System.out.println("BLE branch not taken.");
                    break;

                // 1110: BAL
                case 14:
                    R[15] += distance + 1;
                    System.out.println("BAL with offset " + distance + ".");
            }
        }
    }

    /**
     * Simulates the memory stage of the pipeline.
     */
    public static void memory() {
        if (I == 0 && F == 1) {
            if (opcode == 25) {
                Rd = dataMemory[address];
                System.out.println("MEMORY: Load " + Rd + " from memory at address " + (R[op1] + op2) + " into R" + dest + ".");
            }
            else if (opcode == 24) {
                dataMemory[address] = R[dest];
                System.out.println("MEMORY: Store " + R[dest] + " to memory at address " + (R[op1] + op2) + " from R" + dest + ".");
            }
        }
        else {
            System.out.println("MEMORY: No memory operation.");
        }
    }

    /**
     * Simulates the write back stage of the pipeline.
     * @throws IOException
     */
    public static void writeBack() throws IOException {
        if (I == 1 && F == 1) {
            System.out.println("WRITEBACK: No decode operation.");
        }

        if (I == 0 && F == 0) {
            if (opcode == 10) {
                System.out.println("WRITEBACK: No writeback operation.");
            }
            else {
                R[dest] = Rd;
                System.out.println("WRITEBACK: Write " + Rd + " to R" + dest + ".");
            }
        }
        else if (I == 0 && F == 1) {
            if (opcode == 25) {
                R[dest] = Rd;
                System.out.println("WRITEBACK: Write " + Rd + " to R" + dest + ".");
            }
            else if (opcode == 24) {System.out.println("WRITEBACK: No writeback operation.");
            }
        }
        else if (I == 1 && F == 0) {
            System.out.println("WRITEBACK: No writeback operation.");
        }
        else if (I == 1 && F == 1) {
            String instruction = instructionMemory.get(R[15] - 1);
            instruction = instruction.substring(instruction.length() - 8);
            instruction=hexToBin(instruction);

            int swiCode = binToDec(instruction.substring(instruction.length() - 8));

            switch(swiCode) {
                case 17: // 0x11
                    System.out.println("\nExit.");
                    writeDataMemory();
                    System.exit(0);
                    break;

                case 107: // 0x6b
                    System.out.println("\nSWI OUTPUT: " + R[1]+"\n");
                    break;

                case 108: // 0x6c
                    Scanner sc = new Scanner(System.in);
                    System.out.print("\nEnter Integer value: ");
                    R[0] = sc.nextInt();
                    System.out.println();
                    break;
            }
            System.out.println("\n");
        }
        System.out.println();
    }

    /**
     * Runs the methods to simulate the pipeline.
     * @throws IOException
     */
    public static void run() throws IOException {
        while(true) {
            fetch();
            decode();
            execute();
            memory();
            writeBack();
        }
    }

    /**
     * Calls the methods to load the instruction memory and run the pipeline simulation.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        loadInstructionMemory();
        loadDataMemory();
        run();
    }
}