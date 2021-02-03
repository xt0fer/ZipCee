import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/*
    The InputFile class is built because Java doesn't do char by char input like C can.
    InputFile reads in the entire file and buffers it into a String.
    Real compilers won't do that. This isn't a real compiler, it will never compile
    really big programs.

    fgetc is modelled after the C function.
    Zero '\0' indicates end of buffer.
    I didn't bother to forward the EOF (end of file).

    This class is not parameterized, it only wraps Standard Input (System.in)
 */
public class InputFile {
    private String buffer;
    private int bufptr = 0;
    private int buflen = 0;
    public InputFile() {
    }

    public void readFile() {
        final int bufferSize = 8192;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(System.in, StandardCharsets.UTF_8);
        int charsRead = 0;
        while(true) {
            try {
                if (!((charsRead = in.read(buffer, 0, buffer.length)) > 0)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.append(buffer, 0, charsRead);
            buflen += charsRead;
        }
        this.buffer = out.toString();
    }

    public char fgetc() {
        if (bufptr >= buffer.length()){
            return '\0';
        }
        char ch = buffer.charAt(bufptr);
        bufptr += 1;
        return ch;
    }
}
