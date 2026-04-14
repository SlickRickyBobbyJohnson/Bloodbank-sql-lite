import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Utility class that loads database configuration from a properties file.
 *
 * This class reads configuration settings from a .properties file and validates them
 * before creating a DbConfig object. It handles relative paths, validates SQL identifiers,
 * and resolves database paths relative to the config file or working directory.
 *
 * @author Blood Bank Management System
 * @version 1.0
 */
public class DbConfigLoader {

    /**
     * Loads database configuration from a properties file.
     *
     * This method reads all required configuration keys from the properties file,
     * validates their format, and resolves relative database paths. It ensures that
     * all SQL identifiers are safe before creating a DbConfig object.
     *
     * @param propertiesPath the file path to the .properties configuration file
     * @return a DbConfig object with the loaded configuration
     * @throws IOException if the properties file cannot be read
     * @throws IllegalArgumentException if required keys are missing or invalid
     */
    public static DbConfig load(String propertiesPath) throws IOException {
        // Reads settings from the properties file.
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(propertiesPath)) {
            p.load(in);
        }

        String dbPath = p.getProperty("db.path");
        if (dbPath == null || dbPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required config key: db.path");
        }
        // Handle values like "data/bloodbank.db" copied with quotes.
        dbPath = stripWrappingQuotes(dbPath.trim());
        // Convert relative paths to a real path the app can open.
        dbPath = resolveDbPath(dbPath, propertiesPath);

        String tableName = p.getProperty("db.table");
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required config key: db.table");
        }
        tableName = tableName.trim();
        // only safe SQL identifier names.
        if (!tableName.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid SQL identifier for key db.table: " + tableName);
        }

        String unitIdCol = p.getProperty("col.unit_id");
        if (unitIdCol == null || unitIdCol.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required config key: col.unit_id");
        }
        unitIdCol = unitIdCol.trim();
        if (!unitIdCol.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid SQL identifier for key col.unit_id: " + unitIdCol);
        }

        String bloodTypeCol = p.getProperty("col.blood_type");
        if (bloodTypeCol == null || bloodTypeCol.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required config key: col.blood_type");
        }
        bloodTypeCol = bloodTypeCol.trim();
        if (!bloodTypeCol.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid SQL identifier for key col.blood_type: " + bloodTypeCol);
        }

        String donationDateCol = p.getProperty("col.donation_date");
        if (donationDateCol == null || donationDateCol.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required config key: col.donation_date");
        }
        donationDateCol = donationDateCol.trim();
        if (!donationDateCol.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid SQL identifier for key col.donation_date: " + donationDateCol);
        }

        String expiryDateCol = p.getProperty("col.expiry_date");
        if (expiryDateCol == null || expiryDateCol.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required config key: col.expiry_date");
        }
        expiryDateCol = expiryDateCol.trim();
        if (!expiryDateCol.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid SQL identifier for key col.expiry_date: " + expiryDateCol);
        }

        String donorIdCol = p.getProperty("col.donor_id");
        if (donorIdCol == null || donorIdCol.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required config key: col.donor_id");
        }
        donorIdCol = donorIdCol.trim();
        if (!donorIdCol.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid SQL identifier for key col.donor_id: " + donorIdCol);
        }

        String statusCol = p.getProperty("col.status");
        if (statusCol == null || statusCol.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required config key: col.status");
        }
        statusCol = statusCol.trim();
        if (!statusCol.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid SQL identifier for key col.status: " + statusCol);
        }

        return new DbConfig(
                dbPath,
                tableName,
                unitIdCol,
                bloodTypeCol,
                donationDateCol,
                expiryDateCol,
                donorIdCol,
                statusCol
        );
    }

    /**
     * Removes surrounding double quotes from a string if present.
     *
     * This handles the case where configuration values are copied with quotes
     * from other sources and need to be stripped for actual use.
     *
     * @param raw the string to process
     * @return the string without surrounding quotes, or the original string if no quotes
     */
    private static String stripWrappingQuotes(String raw) {
        if (raw.length() >= 2 && raw.startsWith("\"") && raw.endsWith("\"")) {
            return raw.substring(1, raw.length() - 1).trim();
        }
        return raw;
    }

    /**
     * Resolves a database path from the configuration, handling both absolute and relative paths.
     *
     * For relative paths, this method attempts to find the database file in this order:
     * 1. Relative to the current working directory
     * 2. Relative to the configuration file's directory
     * 3. Relative to the parent directory of the config file's directory
     * If the file is not found, it returns the working-directory-relative path to allow SQLite
     * to create a new database file if needed.
     *
     * @param configuredPath the path specified in the configuration file
     * @param propertiesPath the path to the configuration file itself
     * @return the resolved absolute path to the database file
     */
    private static String resolveDbPath(String configuredPath, String propertiesPath) {
        Path configured = Paths.get(configuredPath);
        if (configured.isAbsolute()) {
            return configured.normalize().toString();
        }

        Path propsFile = Paths.get(propertiesPath).toAbsolutePath().normalize();
        Path propsDir = propsFile.getParent();

        // First try: resolve from the current working directory.
        Path fromWorkingDir = Paths.get("").toAbsolutePath().resolve(configured).normalize();
        if (Files.exists(fromWorkingDir)) {
            return fromWorkingDir.toString();
        }

        if (propsDir != null) {
            // Second try: resolve relative to the config file folder.
            Path fromConfigDir = propsDir.resolve(configured).normalize();
            if (Files.exists(fromConfigDir)) {
                return fromConfigDir.toString();
            }

            Path parent = propsDir.getParent();
            if (parent != null) {
                // Third try: resolve relative to the parent of config/.
                Path fromConfigParent = parent.resolve(configured).normalize();
                if (Files.exists(fromConfigParent)) {
                    return fromConfigParent.toString();
                }
            }
        }

        // Fall back to working-dir resolution so SQLite can still create a new DB file if intended.
        return fromWorkingDir.toString();
    }
}
