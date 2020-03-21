package tud.bp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GeneralConversion {
    public static String inputStreamToString(InputStream in) throws IOException {
        InputStreamReader isr = new InputStreamReader(in);
        return readerToString(isr);
    }

    public static String readerToString(InputStreamReader reader) throws IOException {
        BufferedReader breader = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String str;

        while ((str = breader.readLine()) != null) {
            sb.append(str);
        }

        return sb.toString();
    }
}
