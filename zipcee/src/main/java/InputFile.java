import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
