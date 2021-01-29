public class Here {
    public static String where () {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
        String where = ste.getClassName() + " " + ste.getMethodName() + " " + ste.getLineNumber() + " ";
        return where;
    }
    public static int at () {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
        int ln = ste.getLineNumber();
        return ln;
    }
}