package tud.bp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class to do some general conversions needed by the program
 */
public class GeneralConversion {

    /**
     * Converts any inputStream to a String by converting it to a reader first and then using the readerToString method
     * from this same class
     * @param in Any inputStream
     * @return A String containing the data of the inputStream
     * @throws IOException if the creation of the InputStreamReader has failed due to a faulty InputStream
     */
    public static String inputStreamToString(InputStream in) throws IOException {
        InputStreamReader isr = new InputStreamReader(in);
        return readerToString(isr);
    }

    /**
     * Converts any InputStreamReader to a String using a BufferedReader and a StringBuilder.
     * @param reader Any InputStreamReader
     * @return A String containing the data of the reader
     * @throws IOException if the creation of the BufferedReader has failed due to a faulty InputStreamReader
     */
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
