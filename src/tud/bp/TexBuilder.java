package tud.bp;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * <-- L a T e X   S K E L E T O N -->
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

/**
 * Class to build a .tex file for every single issue, as long as the issues description follows the predetermined format
 * of our "Bachelor Praktikum" group
 */
public class TexBuilder {
    private JSONObject issue;

    private int iid;
    private String title;
    private String assigneeName;
    private String milestoneName;
    private String description;
    private String[] acceptanceCriteria = new String[] {};
    private int estimate;
    private String timeSpent;
    private String velocity;
    private String iteration;
    private String[] notes = new String[] {};

    /**
     * Call the functions automatically on creation
     * @param issue The issue as a JSONObject
     */
    public TexBuilder(JSONObject issue) {
        this.issue = issue;
        this.extractIID();

        // If the iid matches any of the dontIncludes then don't create the file, due to unexpected behaviour and wrong
        // formats
        for (Integer n : Main.dontInclude) {
            if (this.iid == n) return;
        }

        this.extractTitle();
        this.extractAssigneeName();
        this.extractMilestoneName();

        this.formatDescription();
    }

    /**
     * Builds the complete issue file using a BufferedWriter
     * @throws IOException if the creation of the BufferedWriter or FileWriter fails
     */
    public void buildIssueFile() throws IOException {
        File out = new File(Main.pathToBPRepo + "qs/issues/" + this.iid + ".tex");
        BufferedWriter writer = new BufferedWriter(new FileWriter(out));

        // <-- G E N E R A L   I N F O R M A T I O N -->
        writer.write("\\subsection{Issue \\#" + this.iid + "}");
        writer.newLine();
        writer.write("\\label{sec:" + this.iid + "}");
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
                    // \n- * [x] Name must not be empty
                    String[] splitted = s.split("\\n-\\s\\*\\s\\[[x|\\s]\\]\\s*");
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
                        .replace("</del>", "}")
                        .replaceAll("\\*\\*(.*)\\*\\*", "\\\\textbf{$1}");
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
        writer.write("    Iteration & " + this.iteration + "\\\\");
        writer.newLine();
        writer.write("    \\hline");
        writer.newLine();
        writer.write("\\end{tabular}");

        writer.newLine();
        writer.newLine();

        // <-- N O T E S -->
        writer.write("\\paragraph{Anmerkungen:}");

        // Saves whether a note has already been written or not. If the issue does not contain any notes then no itemize
        // is needed and instead later "Keine Anmerkungen" is printed instead
        boolean begun = false;

        for (String s : this.notes) {
            // skip /estimate, /spend or empty strings in every form ( with \ or / )
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
                // detect any mentions of any issue and refer to them inside the LaTeX document
                s = s.replaceAll("#([0-9]*)", "\\\\hyperref[sec:$1]{\\\\textcolor{linkred}{\\\\#$1}}");

                // Replace _ with \_ due to the LaTeX compiler recognizing _ as the index character in math mode
                s = s.replace("_", "\\_");

                // finally write the note inside itemize
                writer.write("    \\item " + s);
                writer.newLine();
            }
        }

        // If the issue contained notes (beside spend and estimate) close the itemize.
        // Otherwise print "Keine Anmerkungen"
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
        this.extractIID();
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

        // Correct Julians name because he was created in GitLab as the User Julian H
        if (name.contains("Julian")) name = "Julian Hochgürtel";

        this.assigneeName = name;
    }

    private void extractMilestoneName() {
        this.milestoneName = issue.getJSONObject("milestone").getString("title");
    }

    /**
     * Extracts all parts of the description (description itself, tasks, acceptance criteria, time and notes) and
     * formats every single one of them. (Tasks are not formatted because they are not needed for the final issue file)
     */
    private void formatDescription() {
        System.out.println("Current Issue: #" + this.iid);
        String desc = this.issue.getString("description");
        String[] parts = desc.split("####\\s.*");
        // 0 = empty,
        // 1 = description,
        // 2 = tasks,
        // 3 = acceptance criteria,
        // 4 = time,
        // 5 = notes

        // First correct some things of the description
        // Refer to mentioned issues inside the LaTeX document
        this.description = parts[1].replaceAll("#([0-9]*)", "\\\\hyperref[sec:$1]{\\\\textcolor{linkred}{\\\\#$1}}")

                // Replace the html deleted tag with the \st{...} command of the LaTeX soul package
                .replace("<del>", "\\st{")
                .replace("</del>", "}")

                // Detect double new line and replace it with latex double new line and the \noindent tag
                .replaceAll("\\n\\s*\\n\\s*", "\\\\\\\\\n\n\\\\noindent\n")

                // Make sure no whitespaces follow the end of the description
                .stripTrailing()

                // Delete the first newLine in case the description starts with it
                .replaceFirst("\\n", "");

        // Remove the trailing newLines
        int index = this.description.lastIndexOf("\\\\\n\n\\noindent");
        if (index >= 0) {
            this.description = this.description.substring(0, index) + "\n\n";
        }

        // If the enumerator symbol is detected then format the enumeration as itemize
        if (this.description.contains("* ")) {
            // replace all * enumerators with \item
            this.description = this.description.replaceAll("\\*\\s([A-Za-z0-9(),\\s_\\-]*)\\n*(\\\\)?[^*]",
                    "    \\\\item $1$2\n");

            // place \begin{itemize} at the top
            this.description = this.description.replaceAll("\\\\noindent\\n*(\\s*\\\\item\\s[A-Za-z0-9(),_\\-\\s]*)[^\\n]",
                    "\\\\noindent\n\\\\begin{itemize}\n$1");

            // correct error where the backslash of the second item is removed (this is only a workaround)
            this.description = this.description.replaceFirst("[^\\\\]item\\s",
                    " \\\\item ");

            // place \end{itemize} at the end
            this.description = this.description.replaceAll("\\s*\\\\item\\s([A-Za-z0-9_(),\\-\\s]*)\\\\\\n*[^\\\\\\w]",
                    "\n    \\\\item $1\n\\\\end{itemize}\n\n");
        }

        // Finally replace the LaTeX math index sign with the escaped underscore ( _ to \_ )
        this.description = this.description.replace("_", "\\_");

        // The acceptance Criteria are processed in the buildIssueFile() method and therefore only split by the
        // checkbox signs ( * [ ] or * [x] ).
        // Here just the links to the issues in LaTeX document are created
        this.acceptanceCriteria = parts[3].replaceAll("#([0-9]*)", "\\\\hyperref[sec:$1]{\\\\textcolor{linkred}{\\\\#$1}}")
                .split("(\\n\\* \\[[x|\\s]\\]\\s*)|(\\n\\*\\s*)");

        // Save the time data by splitting the time table by "|"
        String[] timeData = parts[4].split("\\s*\\|\\s*");
        // 8 = Estimate,
        // 11 = Spent,
        // 17 = Sprint

        // Pre format the estimate to save it to the estimate field
        if (timeData[8].contains("x")) timeData[8] = "0";
        if (timeData[8].contains("SP")) timeData[8] = timeData[8].replace("SP", "");
        this.estimate = Integer.parseInt(timeData[8]);

        this.formatTimeSpent(timeData[11]);

        this.calculateVelocity();

        this.iteration = formatIteration(timeData[17]);

        // The notes are also processed in the buildIssueFile() so just split them by the enumeration sign *
        this.notes = parts[5].split("\\s*\\*\\s*");
    }

    /**
     * Calculates the velocity of the issue using the class fields. It extracts the time from the issues description
     * and the estimated story points (which in this project are equal to 1 SP = 1 hour).
     */
    private void calculateVelocity() {
        // Convert time spent string to a float that represents the hours spent
        int[] hoursMinutes = this.splitTimeStringToIntArray(this.timeSpent);
        double time = (double)hoursMinutes[0] + (double)hoursMinutes[1] / 60;

        // If no time was spent return with a velocity of zero to avoid division by zero
        if (time == 0) {
            this.velocity = "0";
            return;
        }

        double vel = (double)this.estimate / time;

        // round to 3 decimals
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.HALF_UP); // Half_up is the standard rounding mode where .5 is rounded up to 1
        this.velocity = df.format(vel);
    }

    /**
     * Formats the spent time with every possible combination (Only hours, hours and minutes, only minutes)
     * @param time The time string representing the spent time from the time table of the issue
     */
    private void formatTimeSpent(String time) {
        String hoursString = "";
        String minutesString = "";

        // Get the pre formatted time string as integer array
        int[] hoursMinutes = this.splitTimeStringToIntArray(time);

        if (hoursMinutes[0] != 0) {
            hoursString = hoursMinutes[0] + "h";
            if (hoursMinutes[1] != 0) minutesString = " " + hoursMinutes[1] + "m";
        } else {
            minutesString = hoursMinutes[1] + "m";
        }

        this.timeSpent = hoursString + minutesString;
    }

    /**
     * Splits the time string to an integer array, containing the hours at index 0 and the minutes at index 1,
     * so it can be worked with easily
     * @param time The time string from the issues time table
     * @return An integer array with index 0 = hours and index 1 = minutes
     */
    private int[] splitTimeStringToIntArray(String time) {
        // If no time was spent return 0, 0
        if (time.contains("x")) return new int[] {0, 0};

        // First remove all whitespaces
        time = time.replaceAll("\\s", "");

        int hours = 0;
        int minutes = 0;

        // Cover all possible formats (only hours, hours and minutes, only minutes)
        if (time.contains("h")) {
            int index = time.indexOf("h");
            String hourSubstring = time.substring(0, index);

            // React to the format where only hours are given but containing decimal points (e.g. 1.5 h)
            if (hourSubstring.contains(".")) {
                // split by the dot to get the hour part and the decimal part separated
                String[] splitHour = hourSubstring.split("\\.");
                hours = Integer.parseInt(splitHour[0]);

                // process the decimal part. 10 ^ <the length of the decimal part> * 60 gives us the number to divide by
                // If the decimal part contains hundredth (length 2) we need to divide by 600 to get the correct minutes
                double min = Double.parseDouble(splitHour[1]) / Math.pow(10.0, (double)splitHour[1].length())  * 60;
                DecimalFormat df = new DecimalFormat("#");
                df.setRoundingMode(RoundingMode.HALF_UP); // Again half_up is the normal rounding (.5 to 1)
                minutes = Integer.parseInt(df.format(min));
            } else {
                hours = Integer.parseInt(hourSubstring);
            }

            // if also minutes are given
            if (time.contains("m")) {
                minutes = Integer.parseInt(time.substring(index + 1, time.indexOf("m")));
            }
        } else { // if only minutes are given
            minutes = Integer.parseInt(time.substring(0, time.indexOf("m")));
        }

        return new int[] {hours, minutes};
    }

    /**
     * Format the list or the single iteration in the issues time table
     * @param it The string containing the list of iterations split by "," or the single iteration
     * @return The correctly formatted iteration
     */
    private String formatIteration(String it) {
        if (it.contains(",")) {
            String[] split = it.split(",");
            it = split[split.length-1];
        }

        it = it.strip();

        if (!it.contains("x")) {
            return Integer.toString(Integer.parseInt(it));
        }

        return "-";
    }
}
