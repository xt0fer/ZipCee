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
//       F - function
//       G - global
//       U - undefined
// addr: symbol address

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
