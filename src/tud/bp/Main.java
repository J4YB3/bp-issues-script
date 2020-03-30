package tud.bp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static final String pathToBPRepo = "/home/jan/Dokumente/git/bp-doc/";

    // 505 is a bug report created by our client and does therefore not follow our layout. It must be written manually.
    public static final int[] dontInclude = new int[] {/*505*/};

    public static void main(String[] args) {
        // Initialize
        JSONData jsonArrays = new JSONData();

        int[] issueIDs = new int[jsonArrays.getTotalLength()];

        TexBuilder tb;

        // Iterate through the admin issues and build every single issue file
        for (int i = 0; i < jsonArrays.getAdminLength(); i++) {
            tb = new TexBuilder( jsonArrays.getAdmin().getJSONObject(i) );
            try {
                tb.buildIssueFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // add the id of the issue to the id list to create the LaTeX include file later
            issueIDs[i] = tb.getIID();
        }

        // Iterate through the support issues and build every single issue file
        for (int i = 0; i < jsonArrays.getSupportLength(); i++) {
            tb = new TexBuilder( jsonArrays.getSupport().getJSONObject(i) );
            try {
                tb.buildIssueFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // add the id of the issue with an offset to the id list to not overwrite the admin issues.
            issueIDs[i + jsonArrays.getAdminLength()] = tb.getIID();
        }

        // Sort the issues to have a nice sorted list later in LaTeX
        Arrays.sort(issueIDs);

        // Now write the actual input file for LaTeX
        File out = new File(Main.pathToBPRepo + "qs/issues.tex");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(out));

            writer.write("\\section{Issues}\n");

            // Iterate through all ids and add an input for every single one. Don't include the ids that are contained
            // in the dontInclude array.
            for (Integer i : issueIDs) {
                boolean match = false;
                for (Integer d : dontInclude) {
                    if (d.equals(i)) {
                        match = true;
                        break;
                    }
                }

                // If the current issue id does not match any id in the dontInclude array, print the input line to the
                // output file.
                if (!match) {
                    writer.write("\n\\input{issues/" + i + ".tex}");
                }
            }

            // End of input file.
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
