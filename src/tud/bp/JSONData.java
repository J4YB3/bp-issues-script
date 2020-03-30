package tud.bp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Fields that we may want to show in the .tex file:
 * iid
 * title
 * description
 * milestone.title
 * assignees[0].name
 */

public class JSONData {
    private JSONArray adminArray;
    private JSONArray supportArray;

    private final String pathToExportFiles = "nutzer_geschichten/exportFiles/";

    /**
     * Constructor. Reads the files automatically on creation
     */
    public JSONData() {
        this.readFiles();
    }

    /**
     * Reads the data of the json string files containing the issues and converts them to a JSON Array.
     */
    private void readFiles() {
        File admin = new File(Main.pathToBPRepo + this.pathToExportFiles + "admin.json");
        File support = new File(Main.pathToBPRepo + this.pathToExportFiles + "support.json");
        try {
            FileReader adminReader = new FileReader(admin);
            FileReader supportReader = new FileReader(support);

            String adminFileContent = GeneralConversion.readerToString(adminReader);
            String supportFileContent = GeneralConversion.readerToString(supportReader);

            this.adminArray = new JSONArray(adminFileContent);
            this.supportArray = new JSONArray(supportFileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the Admin JSONArray
     * @return JSONArray the array of the admin issues
     */
    public JSONArray getAdmin() {
        return this.adminArray;
    }

    /**
     * Returns the Support JSONArray
     * @return JSONArray the array of the support issues
     */
    public JSONArray getSupport() {
        return this.supportArray;
    }

    public int getAdminLength() {
        return this.adminArray.length();
    }

    public int getSupportLength() {
        return this.supportArray.length();
    }

    public int getTotalLength() {
        return this.getAdminLength() + this.getSupportLength();
    }
}
