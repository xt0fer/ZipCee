/*
    Symbol holds the concept of a symbol found in the code.
    Either a variable name or a function name.
    This is the entries in the ZipCee symbol table during compilation.
 */
public class Symbol {
    // String ctx, String name, char type, int addr
    private String type;
    private int  addr;
    private final String name;
    private final String sname;
    private final String context;
    private int  nParams;

    // ctx:  context
// name: symbol name
// type: symbol type
//       L - local symbol
    public static String LocalSymbol = "L";
//       F - function
    public static String FunctionSymbol = "F";
//       G - global
    public static String GlobalSymbol = "G";
//       U - undefined
    public static String UndefSymbol = "U";

    public Symbol(String context, String name, String type, int addr) {
        this.type = type;
        this.context = context;
        this.addr = addr;
        this.name = name;
        this.nParams = 0;
        this.sname = context + "_" + name;
        //System.err.println(this.toString());
    }

    public String getType() {
        return type;
    }

    public int getAddr() {
        return addr;
    }

    public void setAddr(int addr) {
        this.addr = addr;
    }

    public String getName() {
        return name;
    }
    public String getSymbol() { return sname; }
    public int getnParams() {
        return nParams;
    }
    public void setnParams(int n) {
        this.nParams = n;
    }

    public void setType(String g) {
        this.type = g;
    }

    @Override
    public String toString() {
        return "SYMBOL: " + context + " " + name + " " + type;
    }
}
