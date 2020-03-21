package tud.bp;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * <-- S K E L E T O N -->
 *
 * \subsection{Issue \#<iid>}
 * \subsubsection*{Titel: <title>}
 *
 * \paragraph{Bearbeitende Person:}  <assignees[0].name>
 * \paragraph{Milestone:} <milestone.name>
 *
 * \paragraph{Beschreibung:} <description>
 *
 * \paragraph{Akzeptanzkriterien:} \begin{itemize}
 *      \item <description.acceptanceCriteria>
 * \end{itemize}
 *
 * \paragraph{Zeit:}
 * \begin{tabular}{|r|l|}
 *     \hline
 *     Story Points & </estimate>\\
 *     \hline
 *     Zeitaufwand & </spend> \\
 *     \hline
 *     Geschwindigkeit & </estimate / /spend>.roundTo3digits \\
 *     \hline
 * \end{tabular}
 *
 * \paragraph{Anmerkungen:} \begin{itemize}
 *     \item <notes>
 * \end{itemize}
 */

public class TexBuilder {
    private JSONObject issue;

    private int iid;
    private String title;
    private String assigneeName;
    private String milestoneName;
    private String description;
    private String[] acceptanceCriteria;
    private int estimate;
    private String timeSpent;
    private String velocity;
    private String[] notes;

    public TexBuilder(JSONObject issue) {
        this.issue = issue;

        this.extractIID();
        this.extractTitle();
        this.extractAssigneeName();
        this.extractMilestoneName();

        this.formatDescription();
    }

    public void buildIssueFile() throws IOException {
        File out = new File(Main.pathToBPRepo + "qs/issues/" + this.iid + ".tex");
        BufferedWriter writer = new BufferedWriter(new FileWriter(out));

        // <-- G E N E R A L   I N F O R M A T I O N -->
        writer.write("\\subsection{Issue \\#" + this.iid + "}");
        writer.newLine();
        writer.write("\\subsubsection*{Titel: " + this.title + "}");
        writer.newLine();
        writer.newLine();
        writer.write("\\paragraph{Bearbeitende Person:} " + this.assigneeName);
        writer.newLine();
        writer.write("\\paragraph{Milestone:} " + this.milestoneName);
        writer.newLine();
        writer.newLine();
        writer.write("\\paragraph{Beschreibung:} " + this.description);

        // <-- A C C E P T A N C E   C R I T E R I A -->
        writer.write("\\paragraph{Akzeptanzkriterien:} \\begin{itemize}");
        writer.newLine();

        for (String s : this.acceptanceCriteria) {
            // skip the first string, because it is empty
            if (!s.equals("")) {
                if (s.contains("- *")) {
                    // itemize inside itemize detected
                    String[] splitted = s.split("\\n-\\s\\* \\[[x|\\s]\\]\\s*");
                    StringBuilder sb = new StringBuilder();

                    sb.append(splitted[0] + "\n");
                    sb.append("    \\item[] \\begin{itemize}\n");

                    // build sub-items
                    for (int i = 1; i < splitted.length; i++) {
                        sb.append("        \\item " + splitted[i].replaceAll("\\n", "") + "\n");
                    }

                    sb.append("    \\end{itemize}\n");

                    s = sb.toString();
                } else {
                    s = s.stripTrailing();
                }
                s = s.replace("_", "\\_")
                        .replace("<del>", "\\st{")
                        .replace("</del>", "}");
                writer.write("    \\item " + s);
                writer.newLine();
            }
        }

        writer.write("\\end{itemize}");

        writer.newLine();
        writer.newLine();

        // <-- T I M E   T A B L E-->
        writer.write("\\paragraph{Zeit:}");
        writer.newLine();
        writer.write("\\begin{tabular}{|r|l|}");
        writer.newLine();
        writer.write("    \\hline");
        writer.newLine();
        writer.write("    Story Points & " + this.estimate + "\\\\");
        writer.newLine();
        writer.write("    \\hline");
        writer.newLine();
        writer.write("    Zeitaufwand & " + this.timeSpent + "\\\\");
        writer.newLine();
        writer.write("    \\hline");
        writer.newLine();
        writer.write("    Geschwindigkeit & " + this.velocity + "\\\\");
        writer.newLine();
        writer.write("    \\hline");
        writer.newLine();
        writer.write("\\end{tabular}");

        writer.newLine();
        writer.newLine();

        // <-- N O T E S -->
        writer.write("\\paragraph{Anmerkungen:}");

        boolean begun = false;

        for (String s : this.notes) {
            // skip /estimate, /spend or empty strings
            if (!(
                    s.contains("/estimate")
                    || s.contains("\\estimate")
                    || s.contains("/spend")
                    || s.contains("\\spend")
                    || s.equals("")
                )) {
                if (!begun) {
                    begun = true;
                    writer.write(" \\begin{itemize}");
                    writer.newLine();
                }
                s = s.replace("#", "\\#");
                s = s.replace("_", "\\_");
                writer.write("    \\item " + s);
                writer.newLine();
            }
        }

        if (begun) {
            writer.write("\\end{itemize}");
        } else {
            writer.write(" Keine Anmerkungen");
        }
        writer.close();
    }

    private void extractIID() {
        this.iid = issue.getInt("iid");
    }

    public int getIID() {
        return this.iid;
    }

    private void extractTitle() {
        this.title = issue.getString("title").replace("_", "\\_");
    }

    private void extractAssigneeName() {
        // NO ASSIGNEES
        if (issue.getJSONArray("assignees").length() == 0) {
            this.assigneeName = "Nicht zugewiesen";
            return;
        }

        String name = issue.getJSONArray("assignees")
                .getJSONObject(0)
                .getString("name");

        if (name.contains("Julian")) name = "Julian Hochgürtel";

        this.assigneeName = name;
    }

    private void extractMilestoneName() {
        this.milestoneName = issue.getJSONObject("milestone").getString("title");
    }

    private void formatDescription() {
        String desc = this.issue.getString("description");
        String[] parts = desc.split("####\\s.*");
        // 0 = empty,
        // 1 = description,
        // 2 = tasks,
        // 3 = acceptance criteria,
        // 4 = time,
        // 5 = notes

        this.description = parts[1].replace("#", "\\#")
                .replace("<del>", "\\st{")
                .replace("</del>", "}")
                .replaceAll("\\n\\s*\\n\\s*", "\\\\\\\\\n\n\\\\noindent\n")
                .stripTrailing()
                .replaceFirst("\\n", "");
        int index = this.description.lastIndexOf("\\\\\n\n\\noindent");
        if (index >= 0) {
            this.description = this.description.substring(0, index) + "\n\n";
        }

        if (this.description.contains("* ")) {
            // replace all * enumerators with \item
            this.description = this.description.replaceAll("\\*\\s([A-Za-z0-9\\s_\\-]*)\\n*(\\\\)?[^*]",
                    "    \\\\item $1$2\n");
            // place \begin{itemize} at the top
            this.description = this.description.replaceAll("\\\\noindent\\n*(\\s*\\\\item\\s[A-Za-z0-9_\\-\\s]*)[^\\n]",
                    "\\\\noindent\n\\\\begin{itemize}\n$1");
            // correct error where the backslash of the second item is removed
            this.description = this.description.replaceFirst("[^\\\\]item\\s",
                    " \\\\item ");
            // place \end{itemize} at the end
            this.description = this.description.replaceAll("\\s*\\\\item\\s([A-Za-z0-9_\\-\\s]*)\\\\\\n*[^\\\\\\w]",
                    "\n    \\\\item $1\n\\\\end{itemize}\n\n");
        }
        this.description = this.description.replace("_", "\\_");

        this.acceptanceCriteria = parts[3].split("(\\n*\\* \\[[x|\\s]\\]\\s*)|(\\n*\\*\\s*)");

        String[] timeData = parts[4].split("\\s*\\|\\s*");
        // 8 = Estimate,
        // 11 = Spent

        if (timeData[8].contains("x")) timeData[8] = "0";
        if (timeData[8].contains("SP")) timeData[8] = timeData[8].replace("SP", "");
        this.estimate = Integer.parseInt(timeData[8]);

        this.formatTimeSpent(timeData[11]);

        this.calculateVelocity();

        System.out.println(this.iid);
        this.notes = parts[5].split("\\s*\\*\\s*");
    }

    private void calculateVelocity() {
        // Convert time spent string to a float that represents the hours spent
        int[] hoursMinutes = this.splitTimeStringToIntArray(this.timeSpent);
        double time = (double)hoursMinutes[0] + (double)hoursMinutes[1] / 60;

        if (time == 0) {
            this.velocity = "0";
            return;
        }

        double vel = (double)this.estimate / time;

        // round to 3 decimals
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.HALF_UP);
        this.velocity = df.format(vel);
    }

    private void formatTimeSpent(String time) {
        String hoursString = "";
        String minutesString = "";

        int[] hoursMinutes = this.splitTimeStringToIntArray(time);

        if (hoursMinutes[0] != 0) {
            hoursString = hoursMinutes[0] + "h";
            if (hoursMinutes[1] != 0) minutesString = " " + hoursMinutes[1] + "m";
        } else {
            minutesString = hoursMinutes[1] + "m";
        }

        this.timeSpent = hoursString + minutesString;
    }

    private int[] splitTimeStringToIntArray(String time) {
        if (time.contains("x")) return new int[] {0, 0};
        time = time.replaceAll("\\s", "");

        int hours = 0;
        int minutes = 0;

        if (time.contains("h")) {
            int index = time.indexOf("h");
            String hourSubstring = time.substring(0, index);

            if (hourSubstring.contains(".")) {
                String[] splitHour = hourSubstring.split("\\.");
                hours = Integer.parseInt(splitHour[0]);
                double min = Double.parseDouble(splitHour[1]) / Math.pow(10.0, (double)splitHour[1].length())  * 60;
                DecimalFormat df = new DecimalFormat("#");
                df.setRoundingMode(RoundingMode.HALF_UP);
                minutes = Integer.parseInt(df.format(min));
            } else {
                hours = Integer.parseInt(hourSubstring);
            }

            if (time.contains("m")) {
                minutes = Integer.parseInt(time.substring(index + 1, time.indexOf("m")));
            }
        } else {
            minutes = Integer.parseInt(time.substring(0, time.indexOf("m")));
        }

        return new int[] {hours, minutes};
    }
}
