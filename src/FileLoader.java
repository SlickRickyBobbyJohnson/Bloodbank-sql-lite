import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Utility class for loading blood unit records from a text file.
 *
 * This class reads comma-separated blood unit records from a file, validates each line,
 * and prevents duplicate unit IDs. Invalid records are skipped silently.
 *
 * @author Blood Bank Management System
 * @version 1.0
 */
public class FileLoader {

    /**
     * Loads blood unit records from a file.
     *
     * Reads a CSV file where each line represents a blood unit record with 6 comma-separated fields.
     * Empty lines are skipped. Invalid records are silently ignored. Duplicate unit IDs are also skipped,
     * with only the first occurrence being retained.
     *
     * @param path the file path to the data file
     * @return an ArrayList of valid BloodUnit records
     * @throws IOException if the file cannot be read
     */
    public static ArrayList<BloodUnit> load(String path) throws IOException {
        ArrayList<BloodUnit> loaded = new ArrayList<>();
        HashSet<String> seenIds = new HashSet<>(); // Track seen UnitIDs to prevent duplicates

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int lineNo = 0;

            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();

                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }

                BloodUnit unit = parseLine(line);

                // Skip invalid lines
                if (unit == null) {
                    continue;
                }

                // Prevent duplicates by UnitID
                String key = unit.getUnitId().toLowerCase();
                if (seenIds.contains(key)) {
                    continue;
                }

                // Mark this ID as seen and add the valid unit to the result
                seenIds.add(key);
                loaded.add(unit);
            }
        }

        return loaded;
    }

    /**
     * Parses a single CSV line into a BloodUnit object.
     *
     * Expected format: unitId,bloodType,donationDate,expiryDate,donorId,status
     * The expiryDate field is ignored (it is calculated automatically).
     *
     * @param line the CSV line to parse
     * @return a BloodUnit if all fields are valid, or null if the line is invalid
     */
    private static BloodUnit parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 6) return null;

        // Trim whitespace from each field
        String unitId = parts[0].trim();
        String bloodType = parts[1].trim();
        String donationDateS = parts[2].trim();
        // parts[3] is expiry date in file, but we ignore it and calculate it instead
        String donorId = parts[4].trim();
        String status = parts[5].trim();

        if (!Validator.isValidUnitId(unitId)) return null;
        if (!Validator.isValidBloodType(bloodType)) return null;

        LocalDate donationDate = Validator.tryParseDate(donationDateS);
        if (donationDate == null) return null;

        if (!Validator.isValidDonorId(donorId)) return null;
        if (!Validator.isValidStatus(status)) return null;

        return new BloodUnit(unitId, Validator.normalizeBloodType(bloodType), donationDate, donorId, status);
    }
}