import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntSupplier;

public class ZipCee {
//    #include <stdlib.h>
//#include <stdint.h>
//#include <stdarg.h>
//#include <stdio.h>
//#include <ctype.h>
//#include <string.h>

    public static int MAXTOKSZ = 256;

    /* print fatal error message and exit */
    public void error(String fmt, Object... args) {
        System.err.printf(fmt, args);
        System.exit(1);
    }

//
// SYMBOLS
//
    private HashMap<String, Symbol> sym = new HashMap<>();


//
// LEXER
//
    //public FILE *f;            /* input source file */

    private InputFile f = new InputFile();
    // public char tok[MAXTOKSZ]; /* current token */
    private char[] tok = new char[MAXTOKSZ];
    private int tokpos = 0;         /* offset inside the current token */
    private char nextc;          /* next char to be pushed into token */
    private int linenum = 1;
    private boolean _debug = false;
    //public char context[MAXTOKSZ];
    private String context = "";
    private boolean genPreamble = false;
    private int numPreambleVars = 0;
    private int numGlobalVars = 0;
    private boolean lastIsReturn = false;
    private boolean flagScanGlobalVars = true;
    //public struct sym *currFunction = NULL;
    Symbol currFunction;

    /* read next char */
    void readchr() {
        if (tokpos == MAXTOKSZ - 1) {
            tok[tokpos] = '\0';
            error("[line %d] Token too long: %s\n", linenum, tok);
        }
        tok[tokpos++] = nextc;
        nextc = f.fgetc();
        if ('\n'==nextc) {linenum++;}
    }

    /* read single token */
    void readtok() {
        for (;;) {
            /* skip spaces */
            while (Character.isWhitespace(nextc)) {
                nextc = f.fgetc();
                if ('\n'==nextc) {linenum++;}
            }
            /* try to read a literal token */
            tokpos = 0;
            while (Character.isLetterOrDigit(nextc) || nextc == '_') {
                readchr();
            }
            /* if it's not a literal token */
            if (tokpos == 0) {
                while (nextc == '<' || nextc == '=' || nextc == '>' || nextc == '!' || nextc == '&' || nextc == '|') {
                    readchr();
                }
            }
            /* if it's not special chars that looks like an operator */
            if (tokpos == 0) {
                /* try strings and chars inside quotes */
                if (nextc == '\'' || nextc == '"') {
                    char c = nextc;
                    readchr();
                    while (nextc != c) {
                        readchr();
                    }
                    readchr();
                } else if (nextc == '/') { // skip comments
                    readchr();
                    if (nextc == '*') {      // support comments of the form '/**/'
                        nextc = f.fgetc();
                        if ('\n'==nextc) {linenum++;}
                        while (nextc != '/') {
                            while (nextc != '*') {
                                nextc = f.fgetc();
                                if ('\n'==nextc) {linenum++;}
                            }
                            nextc = f.fgetc();
                            if ('\n'==nextc) {linenum++;}
                        }
                        nextc = f.fgetc();
                        if ('\n'==nextc) {linenum++;}
                        continue;
                    } else if (nextc == '/') { // support comments of the form '//'
                        while (nextc != '\n') {
                            nextc = f.fgetc();
                            if ('\n'==nextc) {linenum++;}
                        }
                        nextc = f.fgetc();
                        if ('\n'==nextc) {linenum++;}
                        continue;
                    }
                } else if (nextc != '\0') { /* EOF */
                    /* otherwise it looks like a single-char symbol, like '+', '-' etc */
                    readchr();
                }
            }
            break;
        }
        tok[tokpos] = '\0';
        if (_debug)  {
            System.err.printf("TOKEN: %s\n",fromToken(tok));
        }
    }

    /* check if the current token machtes the string */
    boolean peek(String s) {
        return (fromToken(tok).equals(s));
    }

    /* read the next token if the current token machtes the string */
    boolean accept(String s) {
        if (peek(s)) {
            readtok();
            return true;
        }
        return false;
    }

    /* throw fatal error if the current token doesn't match the string */
    void expect(int srclinenum, String s) {
        if (!accept(s)) {
            if (_debug) {
                error("[line %d ; srcline %d] Error: expected '%s', but found: %s\n", linenum, srclinenum, s, fromToken(tok));
            } else {
                error("[line %d] Error: expected '%s', but found: %s\n", linenum, s, fromToken(tok));
            }
        }
    }

//    public struct sym *sym_find(String s) {
//        int i;
//        struct sym *symbol = NULL;
//
//        for (i = 0; i < sympos; i++) {
//            if (strcmp(sym[i].name, s) == 0) {
//                symbol = &sym[i];
//            }
//        }
//        return symbol;
//    }

    // ctx:  context
// name: symbol name
// type: symbol type
//       L - local symbol
//       F - function
//       G - global
//       U - undefined
// addr: symbol address
    // public Symbol sym_declare()
//    public struct sym *sym_declare(String ctx, String name, char type, int addr) {
//        char sName[MAXTOKSZ];
//        int  ii;
//
//        strcpy(sName,ctx);
//        strcat(sName,"_");
//        strcat(sName,name);
//
//        for (ii=0; ii<sympos; ii++) {
//            if (0==strcmp(sym[ii].name,sName)) {
//                error("[line %d] variable redefined '%s'\n",linenum,name);
//            }
//        }
//
//        strncpy(sym[sympos].name, sName, MAXTOKSZ);
//        sym[sympos].addr = addr;
//        sym[sympos].type = type;
//        sympos++;
//        if (sympos > MAXSYMBOLS) {
//            error("[line %d] Too many symbols\n",linenum);
//        }
//        return &sym[sympos-1];
//    }

    /*
     * BACKEND
     */
//    public static int MAXCODESZ 4096
//    public char code[MAXCODESZ];
    public int codepos = 0;

    ArrayList<String> code = new ArrayList<>();

    public void emit(String line) {
        code.add(line);
    }

public static int TYPE_NUM = 0;
public static int TYPE_CHARVAR = 1;
public static int TYPE_INTVAR = 2;

//            #ifndef GEN
//#error "A code generator (backend) must be provided (use -DGEN=...)"
//            #else
//            #include GEN
//#endif

    private CodeGen gen = new CodeGen(this.code, this.sym);

    /*
     * PARSER AND COMPILER
     */

    //public int expr();

    // read type name:
//   int, char and pointers (int* char*) are supported
//   void is skipped (as if nothing was there)
//   NOTE: void * is not supported
    public boolean typename() {
        if (peek("int") || peek("char") ) {
            readtok();
            while (accept("*"));
            return true;
        }
        if (peek("void") ) {  // skip 'void' token
            readtok();
        }
        return false;
    }

    public String fromToken(char [] token) {
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        while (token[idx] != '\0') {
            sb.append(token[idx]);
            idx += 1;
        }
        return sb.toString();
    }
    public int parse_immediate_value() {
        if ( tok[0] =='0' ) {
            if (tok[1]==0 ) return 0;
            String token = fromToken(tok);
            if (token.length()<3) {
                error("[line %d] Invalid symbol: %s\n", linenum, token);
            }
            if ( (token.charAt(1) =='x') || (token.charAt(1)=='X') ) {
                return Integer.parseInt(token.substring(2), 16);
            } else
            if ( (token.charAt(1)=='o') || (token.charAt(1)=='O') ) {
                return Integer.parseInt(token.substring(2),  8);
            } else
            if ( (token.charAt(1)=='b') || (token.charAt(1)=='B') ) {
                return Integer.parseInt(token.substring(2), 2);
            } else {
                error("[line %d] Invalid symbol: %s\n", linenum, tok);
                return 0;
            }
        } else {
            return Integer.parseInt(fromToken(tok), 10);
        }
    }

    public int prim_expr() {
        int type = TYPE_NUM;
        if (Character.isDigit(tok[0])) {
            int n = parse_immediate_value();
            gen.gen_const(n);
        } else if (Character.isAlphabetic(tok[0])) {
            String token = fromToken(tok);
            String symName = context + "_" + token;
            Symbol s;  // find symbol in local context..
            if (sym.containsKey(symName)) {
                s = sym.get(symName);
            } else {
                // look in global context
                symName = "_" + token;
                if (!sym.containsKey(symName)) {
                    // symbol not found in either context... this is an error...
                    error("[line %d] Undeclared symbol: %s\n", linenum, fromToken(tok));
                }
                s = sym.get(symName);
            }

            if (_debug) System.err.printf("SYM: %s\n",symName);
            if (s.getType().equals("L")) {
                // Local Symbol
                //gen_stack_addr(stack_pos - s.getAddr() - 1);
                gen.gen_stack_addr(gen.stackPeek() - s.getAddr() - 1);
            } else {
                // Other Symbols (Global)
                gen.gen_sym_addr(s);
            }
            type = TYPE_INTVAR;
        } else if (accept("(")) {
            type = expr();
            expect(Here.at(),")");
        } else if (tok[0] == '"') {
            int i, j;
            i = 0; j = 1;
            while (tok[j] != '"') {
                if (tok[j] == '\\' && tok[j+1] == 'x') {
                    String s = ""+tok[j+2]+tok[j+3];
                    int n = Integer.parseUnsignedInt(s,16);
                    tok[i++] = Character.forDigit(n, 16);
                    j += 4;
                } else {
                    tok[i++] = tok[j++];
                }
            }
            tok[i] = 0;
            if (i % 2 == 0) {
                i++;
                tok[i] = 0;
            }
            gen.gen_array(tok, i);
            type = TYPE_NUM;
        } else {
            error("[line %d] Unexpected primary expression: %s\n", linenum,fromToken(tok));
        }
        readtok();
        return type;
    }

    public int binary(int type, IntSupplier aMethod, String buf) {
        if (type != TYPE_NUM) {
            gen.gen_unref(type);
        }
        gen.gen_push();
        type = aMethod.getAsInt();
        if (type != TYPE_NUM) {
            gen.gen_unref(type);
        }
        emit(buf);
        //stack_pos = stack_pos - 1; /* assume that buffer contains a "pop" */
        gen.stackPop(1);
        return TYPE_NUM;
    }

    public int postfix_expr() {
        int type = prim_expr();

        if (type == TYPE_INTVAR && accept("[")) {
            binary(type, () -> expr(), gen.GEN_ADD);
            expect(Here.at(),"]");
            type = TYPE_CHARVAR;
        } else if (accept("(")) {
            int prev_stack_pos = gen.stackPeek(); //stack_pos;
            gen.gen_push(); /* store function address */
            int call_addr = gen.stackPeek() - 1; //stack_pos - 1;
            if (accept(")") == false) {
                expr();
                gen.gen_push();
                while (accept(",")) {
                    expr();
                    gen.gen_push();
                }
                expect(Here.at(),")");
            }
            type = TYPE_NUM;
            gen.gen_stack_addr(gen.stackPeek() - call_addr - 1);
            gen.gen_unref(TYPE_INTVAR);
            gen.gen_call();
            if (currFunction != null) {
                gen.gen_call_cleanup(currFunction.getnParams());
            } else {
                error("[line %d] Error: unexpected function exit\n",linenum);
            }
            /* remove function address and args */
            gen.gen_pop(gen.stackPeek() - prev_stack_pos);
            gen.stackSet(prev_stack_pos);
        }
        return type;
    }

    public int add_expr() {
        int type = postfix_expr();
        while (peek("+") || peek("-")) {
            if (accept("+")) {
                type = binary(type, this::postfix_expr, gen.GEN_ADD);
            } else if (accept("-")) {
                type = binary(type, this::postfix_expr, gen.GEN_SUB);
            }
        }
        return type;
    }

    public int shift_expr() {
        int type = add_expr();
        while (peek("<<") || peek(">>")) {
            if (accept("<<")) {
                type = binary(type, this::add_expr, gen.GEN_SHL);
            } else if (accept(">>")) {
                type = binary(type, this::add_expr, gen.GEN_SHR);
            }
        }
        return type;
    }

    public int rel_expr() {
        int type = shift_expr();
        while (peek("<")) {
            if (accept("<")) {
                type = binary(type, this::shift_expr, gen.GEN_LESS);
            }
        }
        return type;
    }

    public int eq_expr() {
        int type = rel_expr();
        while (peek("==") || peek("!=")) {
            if (accept("==")) {
                type = binary(type, this::rel_expr, gen.GEN_EQ);
            } else if (accept("!=")) {
                type = binary(type, this::rel_expr, gen.GEN_NEQ);
            }
        }
        return type;
    }

    public int bitwise_expr() {
        int type = eq_expr();

        while (peek("|") || peek("&") || peek("^") || peek("/") || peek("*") || peek("%") ) {
            if (accept("|")) {        // expression '|'
                type = binary(type, this::eq_expr, gen.GEN_OR);
            } else if (accept("&")) { // expression '&'
                type = binary(type, this::eq_expr, gen.GEN_AND);
            } else if (accept("^")) { // expression '^'
                type = binary(type, this::eq_expr, gen.GEN_XOR);
            } else if (accept("/")) { // expression '/'
                type = binary(type, this::eq_expr, gen.GEN_DIV);
            } else if (accept("*")) { // expression '*'
                type = binary(type, this::eq_expr, gen.GEN_MUL);
            } else if (accept("%")) { // expression '%'
                type = binary(type, this::eq_expr, gen.GEN_MOD);
            }
        }
        return type;
    }

    public int expr() {
        int type = bitwise_expr();
        if (type != TYPE_NUM) {
            if (accept("=")) {
                //System.err.printf("HERE 1=\n");
                gen.gen_push();
                expr();
                if (type == TYPE_INTVAR) {
                    emit(gen.GEN_ASSIGN);
                } else {
                    emit(gen.GEN_ASSIGN8);
                }
                //stack_pos = stack_pos - 1; // assume ASSIGN contains pop
                gen.stackPop(1);
                type = TYPE_NUM;
            } else {
                gen.gen_unref(type);
            }
        }
        return type;
    }

    public void statement() {
        lastIsReturn = false;
        if (accept("{")) {
            int prev_stack_pos = gen.stackPeek();
            while (accept("}") == false) {
                statement();
            }
            gen.gen_pop(gen.stackPeek()-prev_stack_pos);
            gen.stackSet(prev_stack_pos);
            //strcpy(context,"");
            context = "";
            genPreamble = false;
            numPreambleVars = 0;
            return;
        }
        if (typename()) {
            //struct sym *var = sym_declare(context,tok, 'L', gen.stackPeek());
            Symbol var = new Symbol(context, fromToken(tok), "L", gen.stackPeek());
            sym.put(var.getSymbol(), var);
            System.err.printf("GENERATE_VAR %s_%s\n",context, fromToken(tok));
            readtok();
            if (accept("=")) {
                System.err.printf("HERE 2=\n");
                expr();
            }
            numPreambleVars++;
            // gen_push(); // make room for new local variable
            var.setAddr(gen.stackPeek()-1);
            expect(Here.at(),";");
            return;
        }
        // if we arrive here, we can generate the preamble
        if (genPreamble) {
            genPreamble = false;
            System.err.printf("Generate Preamble (nvars = %d)\n",numPreambleVars);
            gen.gen_preamble(numPreambleVars);
        }

        if (accept("if")) {
            expect(Here.at(),"(");
            expr();
            emit(gen.GEN_JZ);
            int p1 = codepos;
            expect(Here.at(),")");
            int prev_stack_pos = gen.stackPeek();
            statement();
            emit(gen.GEN_JMP);
            int p2 = codepos;
            gen.gen_patch(code.get(p1), codepos);
            if (accept("else")) {
                gen.stackSet(prev_stack_pos);
                statement();
            }
            gen.stackSet(prev_stack_pos);
            gen.gen_patch(code.get(p2), codepos);
            return;
        }
        if (accept("while")) {
            expect(Here.at(),"(");
            int p1 = codepos;
            gen.gen_loop_start();
            expr();
            emit(gen.GEN_JZ);
            int p2 = codepos;
            expect(Here.at(),")");
            statement();
            emit(gen.GEN_JMP);
            gen.gen_patch(code.get(codepos), p1);
            gen.gen_patch(code.get(p2), codepos);
            return;
        }
        if (accept("return")) {
            if (peek(";") == false) {
                expr();
            }
            expect(Here.at(),";");
            gen.gen_pop(gen.stackPeek()); // remove all locals from stack (except return address)
            lastIsReturn = true;
            gen.gen_ret(numPreambleVars);
            return;
        }
        // we should process an expression...
        expr();
        expect(Here.at(),";");
    }

    public void compile() {
        while (tok[0] != 0) { // until EOF
            if (typename() == false) {
                error("[line %d] Error: type name expected %s \n",linenum, fromToken(tok));
            }
            //struct sym *var = sym_declare(context,tok, 'U', 0);
            Symbol var = new Symbol(context, fromToken(tok), "U", 0);
            sym.put(var.getSymbol(), var);
            readtok();
            if (accept(";")) {
                if (flagScanGlobalVars) {
                    var.setType("G");
                    numGlobalVars++;
                    gen.gen_sym(var);
                    continue;
                } else {
                    error("[line %d] Error: unexpected global variable declaration\n",linenum);
                }
            }
            if (flagScanGlobalVars) {
                gen.gen_start(numGlobalVars);
                flagScanGlobalVars = false;
            }
            //System.err.println("?? "+fromToken(tok));
            expect(Here.at(),"(");
            int argc = 0;
            for (;;) {
                argc++;
                if (typename() == false) {
                    break;
                }
                System.out.printf("GEN_PARM_VAR %s_%s\n",var.getName(),fromToken(tok));
                //sym_declare(var->name,tok, 'L', -argc-1);
                Symbol varL = new Symbol(var.getName(), fromToken(tok), "L", argc);
                sym.put(varL.getSymbol(), var);
                readtok();
                if (peek(")")) {
                    break;
                }
                expect(Here.at(),",");
            }
            expect(Here.at(),")");
            if (accept(";") == false) {
                if (context.equals("") == false) {
                    error("");
                }
                //stack_pos = 0;
                gen.stackSet(0);

                var.setAddr(codepos);
                var.setType("F");
                var.setnParams(argc);
                gen.gen_sym(var);
                System.out.printf("FUNCTION: %s with %d params\n",var.getName(), argc);
                //strcpy(context,var->name);
                context = var.getName();
                genPreamble = true;
                numPreambleVars = 0;
                currFunction = var;
                statement(); // function body
                if (!lastIsReturn) {
                    gen.gen_ret(numPreambleVars);   // issue a ret if user forgets to put 'return'
                }
            }
        }
    }

    public void print() {

    }
    public int runCompiler(String[] argv) {
        int ii;

        context = "";

//        if (argv.length >1) {
//            _debug = true;
//        } else {
//            _debug = false;
//        }

        //f = stdin;
        f.readFile();

        // prefetch first char and first token
        nextc = f.fgetc();
        if ('\n'==nextc) {linenum++;}
        readtok();
        compile();
        //_load_immediate(0xffaaba94); System.out.printf("\n");
        //_load_immediate(0x000aba94); System.out.printf("\n");
        //_load_immediate(0xcd0);      System.out.printf("\n");

        if (_debug) {
            System.err.printf("\n");
            System.err.printf("****************\n");
            System.err.printf("* Symbol Table *\n");
            System.err.printf("****************\n");
            System.err.printf("NAME\tADDR\tTYPE\n");
            for(Map.Entry<String, Symbol> entry : sym.entrySet()) {
                Symbol sym = entry.getValue();
                System.err.printf("%s\t0x%08x\t%s\n",sym.getName(), sym.getAddr(), sym.getType());
            }
            System.err.printf("\n");
        }
        System.out.printf("**********\n");
        System.out.printf("* Output *\n");
        System.out.printf("**********\n");
        System.out.printf("\n");
        gen.gen_finish();
        return 0;

    }
    
    public static void main(String[] argv) {
        ZipCee zc = new ZipCee();
        int status = zc.runCompiler(argv);
        System.exit(status);
    }


}
