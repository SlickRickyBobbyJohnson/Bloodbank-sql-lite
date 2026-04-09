import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;


public class BloodBankTestApp {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        run("Add Objects", BloodBankTestApp::testAddObject);
        run("Remove Objects", BloodBankTestApp::testRemoveObject);
        run("Update Objects", BloodBankTestApp::testUpdateObject);
        run("Status Transition Rules", BloodBankTestApp::testStatusTransitionRules);
        run("Validator Bad Input", BloodBankTestApp::testValidatorBadInput);
        run("Custom Action", BloodBankTestApp::testCustomAction);
        run("Opening a File", BloodBankTestApp::testOpenFile);
        run("Load File With Corrupted Rows", BloodBankTestApp::testLoadFileWithCorruptedRows);

        System.out.println();
        System.out.println("=== Test Summary ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);

        if (failed > 0) {
            System.exit(1);
        }
    }
    //makes new blood object with 5 data points then tests if everything is valid
    private static void testAddObject() {
        ArrayList<BloodUnit> records = new ArrayList<>();

        String unitId = "U100";
        String bloodType = "O+";
        LocalDate donationDate = LocalDate.of(2026, 3, 1);
        String donorId = "D22";
        String status = "Available";

        assertTrue(Validator.isValidUnitId(unitId), "UnitID should be valid");
        assertTrue(Validator.isValidBloodType(bloodType), "BloodType should be valid");
        assertTrue(Validator.isValidDonorId(donorId), "DonorID should be valid");
        assertTrue(Validator.isValidStatus(status), "Status should be valid");

        BloodUnit unit = new BloodUnit(unitId, bloodType, donationDate, donorId, status);
        records.add(unit);

        assertEquals(1, records.size(), "One object should be added to the system");
        assertEquals("U100", records.get(0).getUnitId(), "Added object should keep UnitID");
    }
    //makes a new blood object with 5 data points then tests if the object can be removed and if the system is updated correctly after removal
    private static void testRemoveObject() {
        ArrayList<BloodUnit> records = new ArrayList<>();
        records.add(new BloodUnit("R001", "A+", LocalDate.of(2026, 1, 1), "D1", "Available"));
        records.add(new BloodUnit("R002", "A-", LocalDate.of(2026, 1, 2), "D2", "Available"));

        int idx = findIndexByUnitId(records, "R001");
        assertTrue(idx >= 0, "Object to remove should exist");

        BloodUnit removed = records.remove(idx);

        assertEquals("R001", removed.getUnitId(), "Removed object matches unitid");
        assertEquals(1, records.size(), "make sure only 1 blood unit left");
        assertEquals(-1, findIndexByUnitId(records, "R001"), "Removed object should no longer exist");
    }
//makes bloodunit object then tests if the status can be updated and if the donation date can be updated and if the expiry date is recalculated correctly after updating the donation date
    private static void testUpdateObject() {
        BloodUnit unit = new BloodUnit("U200", "B+", LocalDate.of(2026, 2, 1), "D8", "Available");

        unit.setStatus("Dispatched");
        assertEquals("Dispatched", unit.getStatus(), "Status should be updated");

        LocalDate newDonationDate = LocalDate.of(2026, 3, 10);
        unit.setDonationDate(newDonationDate);
        assertEquals(newDonationDate, unit.getDonationDate(), "Donation date should be updated");
        assertEquals(newDonationDate.plusDays(42), unit.getExpiryDate(), "Expiry date should recalculate");
    }

    private static void testStatusTransitionRules() {
        assertTrue(Validator.canTransitionStatus("Available", "Quarantined"), "Available -> Quarantined should be allowed");
        assertTrue(Validator.canTransitionStatus("Available", "Dispatched"), "Available -> Dispatched should be allowed");
        assertTrue(Validator.canTransitionStatus("Quarantined", "Expired"), "Quarantined -> Expired should be allowed");

        assertTrue(!Validator.canTransitionStatus("Expired", "Available"), "Expired -> Available should be blocked");
        assertTrue(!Validator.canTransitionStatus("Dispatched", "Quarantined"), "Dispatched -> Quarantined should be blocked");
        assertTrue(!Validator.canTransitionStatus("Quarantined", "Available"), "Quarantined -> Available should be blocked");
    }

    // Confirms validator methods reject bad values without throwing exceptions.
    private static void testValidatorBadInput() {
        assertTrue(!Validator.isValidUnitId(null), "Null UnitID should be rejected");
        assertTrue(!Validator.isValidUnitId("  "), "Blank UnitID should be rejected");
        assertTrue(!Validator.isValidDonorId("X"), "Too-short DonorID should be rejected");
        assertTrue(!Validator.isValidBloodType("ZZ"), "Unknown blood type should be rejected");
        assertTrue(!Validator.isValidStatus("Unknown"), "Unknown status should be rejected");
        assertEquals(null, Validator.tryParseDate("2026/03/01"), "Bad date format should return null");
        assertEquals(null, Validator.tryParseDate(""), "Blank date should return null");
        assertTrue(!Validator.canTransitionStatus(null, "Available"), "Null source status should be rejected");
        assertTrue(!Validator.canTransitionStatus("Available", null), "Null target status should be rejected");
    }
//makes a list of bloodunit objects with different blood types and statuses then tests if the custom action correctly counts only the available units of a specific blood type
    private static void testCustomAction() {
        ArrayList<BloodUnit> records = new ArrayList<>();
        records.add(new BloodUnit("C001", "O+", LocalDate.of(2026, 1, 1), "D1", "Available"));
        records.add(new BloodUnit("C002", "O+", LocalDate.of(2026, 1, 2), "D2", "Quarantined"));
        records.add(new BloodUnit("C003", "O+", LocalDate.of(2026, 1, 3), "D3", "Available"));
        records.add(new BloodUnit("C004", "A+", LocalDate.of(2026, 1, 4), "D4", "Available"));

        int availableOPlus = countAvailableByType(records, "O+");
        assertEquals(2, availableOPlus, "check that only available O+ units are counted");
    }
//this temp file method allows me to create a temp file that I can then load bloodunit data onto (including bad data) then test if said data gets loaded
    private static void testOpenFile() throws Exception {
        Path tempFile = Files.createTempFile("bloodunit-sample-", ".txt");

        String content = String.join("\n",
                "T001,O+,2026-03-01,2026-04-12,D10,Available",
                "T002,A-,2026-03-05,2026-04-16,D11,Quarantined",
                "BADROW",
                "T001,B+,2026-03-06,2026-04-17,D12,Available"
        );

        Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));

        ArrayList<BloodUnit> loaded = FileLoader.load(tempFile.toString());

        assertEquals(2, loaded.size(), "File should load only good records");
        assertEquals("T001", loaded.get(0).getUnitId(), "record should match file content");

        Files.deleteIfExists(tempFile);
    }

    // Ensures loader skips malformed rows instead of crashing the app.
    private static void testLoadFileWithCorruptedRows() throws Exception {
        Path tempFile = Files.createTempFile("bloodunit-corrupt-", ".txt");

        String content = String.join("\n",
                "", // empty row
                "ONLY,THREE,COLS", // too few columns
                "X001,O+,bad-date,2026-04-12,D10,Available", // invalid date
                "X002,O+,2026-03-01,2026-04-12,D10,NotAStatus", // invalid status
                "X003,B-,2026-03-01,2026-04-12,D11,Available" // valid row
        );

        Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));
        ArrayList<BloodUnit> loaded = FileLoader.load(tempFile.toString());

        assertEquals(1, loaded.size(), "Only one valid row should be loaded");
        assertEquals("X003", loaded.get(0).getUnitId(), "Valid row should be preserved");

        Files.deleteIfExists(tempFile);
    }
//makes sure only matching data was counted by the by iterating through the list of records and counting only those that match the specified blood type and have a status of "Available"
    private static int countAvailableByType(ArrayList<BloodUnit> records, String bloodType) {
        int count = 0;
        for (BloodUnit r : records) {
            if (r.getBloodType().equalsIgnoreCase(bloodType) && r.getStatus().equals("Available")) {
                count++;
            }
        }
        return count;
    }

    private static int findIndexByUnitId(ArrayList<BloodUnit> records, String unitId) {
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getUnitId().equalsIgnoreCase(unitId)) {
                return i;
            }
        }
        return -1;
    }

    private static void run(String testName, ThrowingRunnable test) {
        try {
            test.run();
            passed++;
            System.out.println("[PASS] " + testName);
        } catch (Throwable t) {
            failed++;
            System.out.println("[FAIL] " + testName + " -> " + t.getMessage());
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
//if the answers aren't equal shows what the expected answer was and what we got
    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}

