package tud.bp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ApiAccess {
    public static void runCurl() {
        String cookieCode = "df5835fe734b74e9a2d49462fbee1bc5";
        String curlComm = "curl --cookie \"_gitlab_session=" + cookieCode + "\" https://dev8.booknpark.de/api/v4/issues?milestone=Bachelor%20Praktikum%20Admin";

        System.out.println(curlComm);
        try {
            Process curlProc = Runtime.getRuntime().exec(curlComm);
            int exitVal = curlProc.waitFor();

            if (exitVal == 0) {
                System.out.println(
                        GeneralConversion.inputStreamToString(curlProc.getInputStream())
                );
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
