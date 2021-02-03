/*
One of the common Macros in a C compilation system is __LINE__ which
refers to the line number of the current line in the C file.
Here is just a class that reaches down into the thread context and can report
on where an error was found - not in the input file but in the compiler's source code(!)
This allows you to look and see where an error was thrown from.
Don't worry, you get it eventually.
 */
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