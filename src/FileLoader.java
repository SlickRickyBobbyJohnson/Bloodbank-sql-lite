import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;

public class FileLoader {

    //* Loads records from a file path
     // Returns a list of valid BloodUnit records
     // Invalid lines are skipped

    public static ArrayList<BloodUnit> load(String path) throws IOException {
        ArrayList<BloodUnit> loaded = new ArrayList<>();
        HashSet<String> seenIds = new HashSet<>(); //hash set to track seen UnitIDs for duplicate prevention

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

                //no duplicates by UnitID
                String key = unit.getUnitId().toLowerCase();
                if (seenIds.contains(key)) {
                    continue;
                }
//stores the key in the hash set to mark it as seen and adds the valid unit to the loaded list
                seenIds.add(key);
                loaded.add(unit);
            }
        }

        return loaded;
    }

    // Converts one text line into a BloodUnit (or null if invalid)
    private static BloodUnit parseLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 6) return null;
//.trim() is used to remove leading and trailing whitespace from each part
        String unitId = parts[0].trim();
        String bloodType = parts[1].trim();
        String donationDateS = parts[2].trim();
        // parts 3 is expiry date in file so it is ignored
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