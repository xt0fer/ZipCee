import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

/*
this is the compiler's Code Generation class.
From various points in the compiler, you can generate some kind of lower level
machine code.
This class could be re-written to produce x86 or ZipRISC1 "code" (which is the .zas
assembly language) Or for any other virtual machine you can dream up.
 */
public class CodeGen {
    private ArrayList<String> code; // pased in from the ZipCee
    private HashMap<String, Symbol> sym = new HashMap<>();

    public CodeGen(ArrayList<String> c, HashMap<String, Symbol> s) {
        this.code = c;
        this.sym = s;
    }

    //    public int sympos = 0;
    private int stack_pos = 0;
    public int stackPush() {
        stack_pos += 1;
        return stack_pos;
    }
    public int stackPop(int n) {
        stack_pos -= n;
        return stack_pos;
    }
    public int stackPeek() {
        return stack_pos;
    }
    public void stackSet(int n) {
        stack_pos = n;
    }

    public void emits(String line) {
        this.code.add(line);
    }
    //#define emits(s) emit(s, strlen(s))

    int TYPE_NUM_SIZE = 1;
    int mem_pos = 0;

    public static String GEN_ADD  = "pop B  \nA:=B+A \n";

    public static String GEN_SUB  = "pop B  \nA:=B-A \n";

    public static String GEN_SHL  = "pop B  \nA:=B<<A\n";

    public static String GEN_SHR  = "pop B  \nA:=B>>A\n";

    public static String GEN_LESS = "pop B  \nA:=B<A \n";

    public static String GEN_EQ = "pop B  \nA:=B==A\n";
    public static String GEN_NEQ = "pop B  \nA:=B!=A\n";

    public static String GEN_OR = "pop B  \nA:=B|A \n";
    public static String GEN_AND = "pop B  \nA:=B&A \n";
    public static String GEN_XOR = "pop B  \nA:=B^A \n";
    public static String GEN_DIV = "pop B  \nA:=B/A \n";
    public static String GEN_MUL = "pop B  \nA:=B*A \n";
    public static String GEN_MOD = "pop B  \nA:=B%A \n";

    public static String GEN_ASSIGN = "pop B  \nM[B]:=A\n";
    public static String GEN_ASSIGN8 = "pop B  \nm[B]:=A\n";

    public static String GEN_JMP = "jmp....\n";

    public static String GEN_JZ = "jmz....\n";

    int fixme_offset = 0;
    int addrCnt = 0;

    public void gen_start(int nGlobalVars) {
        String buf = String.format("GLOBALS %d\n", nGlobalVars);
        emits(buf);
        //sprintf(buf,"GLOBALS %d\n", nGlobalVars);
        emits("---\n");
        emits("JMP");
        fixme_offset = code.size()-1;
        emits("---\n");
        //emits(buf);
    }

    public void gen_finish() {
        //struct sym *funcmain = sym_find("main");
        //char s[32];
        //sprintf(s, "%04x", funcmain->addr);
        //memcpy(code+3, s, 4);
        // Patch the main address in first line of code
        if (!this.sym.containsKey("_main")) {
            System.err.println("In gen_finish: ERROR: could not find main function\n");
            System.exit(1);
        }
        Symbol funcmain = this.sym.get("_main");
        String firstline = code.get(fixme_offset);

        this.code.add(fixme_offset, String.format("%s %04X\n", firstline, funcmain.getAddr()));

        // would this print the ENTIRE code list? prob yes.
        //printf("%s", code);
        for (String line : this.code) {
            System.out.print(line);
        }
    }


    public void gen_call_cleanup(int nVars) {
        String buf = String.format("DO CLEAN %d\n",nVars);
        emits(buf);
    }

    public void gen_ret(int nVars) {
        gen_postamble(nVars);
        emits("ret    \n");
        stackPop(1);
    }

    public void gen_const(int n) {
        String s = String.format("A:=%04x\n", n);
        emits(s);
    }

    public void gen_sym(Symbol sym) {
        if (sym.getType() == "G") {
            //sym->addr = mem_pos;
            sym.setAddr(mem_pos);
            mem_pos +=  TYPE_NUM_SIZE;
        }
    }

    public void gen_loop_start() {}

    public void gen_sym_addr(Symbol sym) {
        gen_const(sym.getAddr());
    }

    public void gen_push() {
        emits("push A \n");
        //stack_pos = stack_pos + 1;
        stackPush();
    }

    public void gen_pop(int n) {
        //char s[32];
        if (n > 0) {
            //sprintf(s, "pop%04x\n", n);
            String s = String.format("pop%04X\n", n);
            emits(s);
            //stack_pos = stack_pos - n;
            stackPop(n);
        }
    }

    public void gen_stack_addr(int addr) {
        //char s[32];
        //sprintf(s, "sp@%04x\n", addr);
        String s = String.format("sp@%04x\n", addr);
        emits(s);
    }

    public void gen_unref(int type) {
        if (type == ZipCee.TYPE_INTVAR) {
            emits("A:=M[A]\n");
        } else if (type == ZipCee.TYPE_CHARVAR) {
            emits("A:=m[A]\n");
        }
    }

    public void gen_call() {
        emits("call A \n");
    }

    public void gen_array(char[] arrayId, int size) {
        // is this to allocate stack space for an array??
        int i = size;
        char[] tok = arrayId;
        /* put token on stack */
        for (; i >= 0; i-=2) {
            gen_const((tok[i] << 8 | tok[i-1]));
            gen_push();
        }
        /* put token address on stack */
        gen_stack_addr(0);
    }


    public void gen_patch(String line, int value) {
        System.err.println("gen_patch not implemented.");
        // patches an address with the code_position
//        char s[32];
//        sprintf(s, "%04x", value);
//        memcpy(op-5, s, 4);
        String s = String.format("", value);
    }

    // generate function pre-amble
// nVars: number of variables to save in the stack frame
    public void gen_preamble(int nVars) {
        emits(String.format("PREAMB %d\n",nVars));
    }

    public void gen_postamble(int nVars) {
        emits(String.format("POSTAMB %d\n",nVars));
        stackPush();
    }

}
