import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.SwingUtilities;

/**
 * Main application entry point for the Blood Bank Management System.
 *
 * This class supports two modes:
 * <ul>
 *   <li>a graphical user interface (default), and</li>
 *   <li>a command-line interface when started with the argument {@code cli}.</li>
 * </ul>
 * The CLI provides menu-based access to loading, viewing, creating, updating,
 * removing, and counting blood unit records.
 */
public class BloodBankApp {

    // In-memory list to hold data
    private static ArrayList<BloodUnit> records = new ArrayList<>();
    private static DbConfig dbConfig;

    /**
     * Launches the application.
     *
     * If the first command-line argument is {@code cli}, the console menu is started.
     * Otherwise, the Swing GUI is launched.
     *
     * @param args command-line arguments used to choose between GUI and CLI mode
     */
    public static void main(String[] args) {
        // Default startup is GUI; pass "cli" to run the old console menu.
        if (args.length == 0 || !args[0].equalsIgnoreCase("cli")) {
            SwingUtilities.invokeLater(() -> new BBMSMainWindow().setVisible(true));
            return;
        }

        runCli();
    }

    private static void runCli() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            printMenu();
            int choice = promptMenuChoice(sc, 1, 7);

            if (choice == 1) {
                doLoad(sc);
            } else if (choice == 2) {
                doDisplay();
            } else if (choice == 3) {
                doCreate(sc);
            } else if (choice == 4) {
                doUpdate(sc);
            } else if (choice == 5) {
                doRemove(sc);
            } else if (choice == 6) {
                doCustomFeature(sc);
            } else if (choice == 7) {
                System.out.println("Goodbye.");
                break;
            }
        }

        sc.close();
    }

    // -----------------------------
    // Menu
    // -----------------------------
    private static void printMenu() {
        System.out.println();
        System.out.println("=== Blood Bank CLI ===");
        System.out.println("1) Connect to SQLite and load records");
        System.out.println("2) Display all records");
        System.out.println("3) Create new record");
        System.out.println("4) Update a record");
        System.out.println("5) Remove a record");
        System.out.println("6) count available units by type");
        System.out.println("7) Exit");
        System.out.println("======================");
    }

    // Menu input validation (user can never enter an invalid menu option)
    private static int promptMenuChoice(Scanner sc, int min, int max) {
        while (true) {
            System.out.print("Choose an option (" + min + "-" + max + "): ");
            String raw = sc.nextLine().trim();

            if (raw.isEmpty()) {
                System.out.println("Error please enter a number.");
                continue;
            }
            if (!raw.matches("\\d+")) {
                System.out.println("Error digits only.");
                continue;
            }

            int n;
            try {
                n = Integer.parseInt(raw);
            } catch (NumberFormatException ex) {
                System.out.println("Error invalid number.");
                continue;
            }

            if (n < min || n > max) {
                System.out.println("Error choose a number from " + min + " to " + max + ".");
                continue;
            }

            return n;
        }
    }

    //loads data from a text file
    private static void doLoad(Scanner sc) {
        System.out.println();
        System.out.println("Connect To SQLite + Load Data");

        System.out.print("Enter DB config file path (.properties): ");
        String path = sc.nextLine().trim();

        if (path.isEmpty()) {
            System.out.println("Error file path cannot be blank");
            return;
        }

        try {
            dbConfig = DbConfigLoader.load(path);
            SQLiteBloodUnitStore.ensureSchema(dbConfig);
            records = SQLiteBloodUnitStore.loadAll(dbConfig);
            System.out.println("Connected. Loaded " + records.size() + " records from database.");
        } catch (IOException ex) {
            System.out.println("Error could not read config file (check path).");
        } catch (IllegalArgumentException ex) {
            System.out.println("Error invalid config: " + ex.getMessage());
        } catch (SQLException ex) {
            System.out.println("Error could not open SQLite database (check db.path and JDBC driver).");
        }
    }

    //menu option to display all data
    private static void doDisplay() {
        if (records.isEmpty()) {
            System.out.println("No records loaded.");
            return;
        }

        System.out.println();
        System.out.println("Blood Units");
        for (int i = 0; i < records.size(); i++) {
            System.out.println((i + 1) + ". " + records.get(i).toDisplayString());
        }
        System.out.println("-------------------");
    }

    //create new record
    private static void doCreate(Scanner sc) {
        System.out.println();
        System.out.println("Create New Blood Unit");

        String unitId;
        while (true) {
            unitId = promptNonEmpty(sc, "Unique UnitID ");
            if (!Validator.isValidUnitId(unitId)) {
                System.out.println("Error: UnitID letters & numbas only and 3-20 characters.");
                continue;
            }
            if (findIndexByUnitId(unitId) != -1) {
                System.out.println("Error: UnitID already exists.");
                continue;
            }
            break;
        }

        String bloodType = promptBloodType(sc);
        LocalDate donationDate = promptDate(sc, "Donation Date (YYYY-MM-DD): ");

        String donorId;
        while (true) {
            donorId = promptNonEmpty(sc, "DonorID (letters & numbas only) ");
            if (!Validator.isValidDonorId(donorId)) {
                System.out.println("Error DonorID must be letters, numbers only and 2-30 characters.");
                continue;
            }
            break;
        }

        String status = promptStatus(sc);

        BloodUnit created = new BloodUnit(unitId, bloodType, donationDate, donorId, status);
        records.add(created);

        if (dbConfig != null) {
            try {
                SQLiteBloodUnitStore.insert(dbConfig, created);
            } catch (SQLException ex) {
                records.remove(records.size() - 1);
                System.out.println("Error could not save record to SQLite.");
                return;
            }
        }

        System.out.println("Created record:");
        System.out.println(created.toDisplayString());
    }


    //Remove data
    private static void doRemove(Scanner sc) {
        System.out.println();
        System.out.println("Remove a Blood Unit Record");

        if (records.isEmpty()) {
            System.out.println("No records to remove.");
            return;
        }

        String unitId = promptNonEmpty(sc, "Enter UnitID to delete ");
        int index = findIndexByUnitId(unitId);

        if (index == -1) {
            System.out.println("Error unitID not found. Nothing deleted.");
            return;
        }

        BloodUnit deleted = records.get(index);

        if (dbConfig != null) {
            try {
                SQLiteBloodUnitStore.deleteByUnitId(dbConfig, deleted.getUnitId());
            } catch (SQLException ex) {
                System.out.println("Error could not delete record from SQLite.");
                return;
            }
        }

        records.remove(index);
        System.out.println("Deleted record:");
        System.out.println(deleted.toDisplayString());
    }

    //update record
    private static void doUpdate(Scanner sc) {
        System.out.println();
        System.out.println("update a Blood Unit Record");

        if (records.isEmpty()) {
            System.out.println("no records to update.");
            return;
        }

        String unitId = promptNonEmpty(sc, "Enter unitID to update ");
        int index = findIndexByUnitId(unitId);

        if (index == -1) {
            System.out.println("eror UnitID not found.");
            return;
        }

        BloodUnit r = records.get(index);

        System.out.println("Current record");
        System.out.println(r.toDisplayString());

        System.out.println();
        System.out.println("Which field do you want to update?");
        System.out.println("1) UnitID");
        System.out.println("2) BloodType");
        System.out.println("3) DonationDate (recalculates ExpiryDate)");
        System.out.println("4) DonorID");
        System.out.println("5) Status");

        int choice = promptMenuChoice(sc, 1, 5);

        String oldUnitId = r.getUnitId();
        String prevBloodType = r.getBloodType();
        LocalDate prevDonationDate = r.getDonationDate();
        String prevDonorId = r.getDonorId();
        String prevStatus = r.getStatus();

        if (choice == 1) {
            while (true) {
                String newId = promptNonEmpty(sc, "New UnitID (letters & numbers): ");
                if (!Validator.isValidUnitId(newId)) {
                    System.out.println("Error: UnitID must be letter, number omly and 3-20 characters.");
                    continue;
                }
                int existing = findIndexByUnitId(newId);
                if (existing != -1 && existing != index) {
                    System.out.println("Error that UnitID already exists.");
                    continue;
                }
                r.setUnitId(newId);
                break;
            }
        } else if (choice == 2) {
            r.setBloodType(promptBloodType(sc));
        } else if (choice == 3) {
            LocalDate newDonation = promptDate(sc, "New Donation Date (YYYY-MM-DD): ");
            r.setDonationDate(newDonation);
        } else if (choice == 4) {
            while (true) {
                String newDonor = promptNonEmpty(sc, "New DonorID (alphanumeric): ");
                if (!Validator.isValidDonorId(newDonor)) {
                    System.out.println("Error: DonorID must be alphanumeric and 2-30 characters.");
                    continue;
                }
                r.setDonorId(newDonor);
                break;
            }
        } else if (choice == 5) {
            String newStatus = promptStatus(sc);
            if (!Validator.canTransitionStatus(r.getStatus(), newStatus)) {
                System.out.println("Error invalid status transition: " + r.getStatus() + " -> " + newStatus + ".");
                return;
            }
            r.setStatus(newStatus);
        }

        if (dbConfig != null) {
            try {
                SQLiteBloodUnitStore.update(dbConfig, oldUnitId, r);
            } catch (SQLException ex) {
                // Roll back in-memory mutation if DB update fails.
                r.setUnitId(oldUnitId);
                r.setBloodType(prevBloodType);
                r.setDonationDate(prevDonationDate);
                r.setDonorId(prevDonorId);
                r.setStatus(prevStatus);
                System.out.println("Error could not update record in SQLite.");
                return;
            }
        }

        System.out.println("Updated record");
        System.out.println(r.toDisplayString());
    }

   //count available units by blood type
    private static void doCustomFeature(Scanner sc) {
        System.out.println();
        System.out.println("Count Units by Blood Type");

        if (records.isEmpty()) {
            System.out.println("No records loaded.");
            return;
        }

        String bt = promptBloodType(sc);

        int count;
        if (dbConfig != null) {
            try {
                count = SQLiteBloodUnitStore.countAvailableByType(dbConfig, bt);
            } catch (SQLException ex) {
                System.out.println("Error could not count from SQLite.");
                return;
            }
        } else {
            count = 0;
            for (BloodUnit r : records) {
                if (r.getBloodType().equals(bt) && r.getStatus().equals("Available")) {
                    count++;
                }
            }
        }

        System.out.println("AVAILABLE units for " + bt + ": " + count);
    }

    // Helper methods for prompting input with validation
    private static String promptNonEmpty(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (s.isEmpty()) {
                System.out.println("Error input cannot be blank.");
                continue;
            }
            return s;
        }
    }

    private static String promptBloodType(Scanner sc) {
        while (true) {
            System.out.print("Blood Type (A+, A-, B+, B-, AB+, AB-, O+, O-): ");
            String s = sc.nextLine().trim();
            if (!Validator.isValidBloodType(s)) {
                System.out.println("Error: invalid blood type.");
                continue;
            }
            return Validator.normalizeBloodType(s);
        }
    }

    private static String promptStatus(Scanner sc) {
        while (true) {
            System.out.print("Status (Available, Quarantined, Dispatched, Expired): ");
            String s = sc.nextLine().trim();
            if (!Validator.isValidStatus(s)) {
                System.out.println("Error: invalid status.");
                continue;
            }
            return s;
        }
    }

    private static LocalDate promptDate(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            LocalDate d = Validator.tryParseDate(s);
            if (d == null) {
                System.out.println("Error invalid date format. Use YYYY-MM-DD  2026-03-09 FOR EAMPLE.");
                continue;
            }
            return d;
        }
    }

    // Finds a record by UnitID, returns index or -1
    private static int findIndexByUnitId(String unitId) {
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getUnitId().equalsIgnoreCase(unitId)) {
                return i;
            }
        }
        return -1;
    }
}