package tud.bp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static final String pathToBPRepo = "/home/jan/Dokumente/git/bp-doc/"; // TODO: maybe change to relative path (not possible?)

    public static final int[] dontInclude = new int[] {505};

    public static void main(String[] args) {
        JSONData jsonArrays = new JSONData();

        int[] issueIDs = new int[jsonArrays.getTotalLength()];

        TexBuilder tb;

        tb = new TexBuilder(jsonArrays.getAdmin().getJSONObject(2));

        for (int i = 0; i < jsonArrays.getAdminLength(); i++) {
            tb = new TexBuilder( jsonArrays.getAdmin().getJSONObject(i) );
            try {
                tb.buildIssueFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            issueIDs[i] = tb.getIID();
        }

        for (int i = 0; i < jsonArrays.getSupportLength(); i++) {
            tb = new TexBuilder( jsonArrays.getSupport().getJSONObject(i) );
            try {
                tb.buildIssueFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            issueIDs[i + jsonArrays.getAdminLength()] = tb.getIID();
        }

        Arrays.sort(issueIDs);

        File out = new File(Main.pathToBPRepo + "qs/issues.tex");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(out));

            writer.write("\\section{Issues}\n");

            for (Integer i : issueIDs) {
                boolean match = false;
                for (Integer d : dontInclude) {
                    if (d.equals(i)) {
                        match = true;
                        break;
                    }
                }

                if (!match) {
                    writer.write("\n\\input{issues/" + i + ".tex}");
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
