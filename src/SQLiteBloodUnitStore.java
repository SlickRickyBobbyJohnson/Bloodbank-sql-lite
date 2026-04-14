import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Utility class for SQLite database operations on blood unit records.
 *
 * This class provides methods to create and maintain the database schema,
 * and perform CRUD (Create, Read, Update, Delete) operations on blood unit records
 * stored in an SQLite database. All queries are built using configuration parameters
 * for flexibility in column and table naming.
 *
 * @author Blood Bank Management System
 * @version 1.0
 */
public class SQLiteBloodUnitStore {

    /**
     * Ensures the blood unit table exists in the database, creating it if necessary.
     *
     * This method uses CREATE TABLE IF NOT EXISTS to safely ensure the schema exists
     * without failing if the table is already present.
     *
     * @param cfg the database configuration
     * @throws SQLException if a database error occurs
     */
    public static void ensureSchema(DbConfig cfg) throws SQLException {
        // Build CREATE data using names from config.
        String sql = "CREATE TABLE IF NOT EXISTS " + cfg.getTableName()
                + " ("
                + cfg.getUnitIdCol() + " TEXT PRIMARY KEY, "
                + cfg.getBloodTypeCol() + " TEXT NOT NULL, "
                + cfg.getDonationDateCol() + " TEXT NOT NULL, "
                + cfg.getExpiryDateCol() + " TEXT NOT NULL, "
                + cfg.getDonorIdCol() + " TEXT NOT NULL, "
                + cfg.getStatusCol() + " TEXT NOT NULL"
                + ")";

        try (Connection conn = DriverManager.getConnection(cfg.getJdbcUrl());
             Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    /**
     * Loads all blood unit records from the database.
     *
     * Reads all records from the database table and validates each row. Invalid records
     * (those that fail validation checks) are silently skipped to prevent a single bad
     * record from stopping the entire load operation.
     *
     * @param cfg the database configuration
     * @return an ArrayList of all valid BloodUnit records from the database
     * @throws SQLException if a database error occurs
     */
    public static ArrayList<BloodUnit> loadAll(DbConfig cfg) throws SQLException {
        ArrayList<BloodUnit> loaded = new ArrayList<>();

        String sql = "SELECT "
                + cfg.getUnitIdCol() + ", "
                + cfg.getBloodTypeCol() + ", "
                + cfg.getDonationDateCol() + ", "
                + cfg.getDonorIdCol() + ", "
                + cfg.getStatusCol()
                + " FROM " + cfg.getTableName();

        try (Connection conn = DriverManager.getConnection(cfg.getJdbcUrl());
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                // Column order matches the SELECT list above.
                String unitId = rs.getString(1);
                String bloodType = rs.getString(2);
                String donationDateS = rs.getString(3);
                String donorId = rs.getString(4);
                String status = rs.getString(5);

                // Ignore invalid data rows so one bad row does not stop loading.
                if (!Validator.isValidUnitId(unitId)) continue;
                if (!Validator.isValidBloodType(bloodType)) continue;
                if (!Validator.isValidDonorId(donorId)) continue;
                if (!Validator.isValidStatus(status)) continue;

                LocalDate donationDate = Validator.tryParseDate(donationDateS);
                if (donationDate == null) continue;

                BloodUnit unit = new BloodUnit(
                        unitId,
                        Validator.normalizeBloodType(bloodType),
                        donationDate,
                        donorId,
                        status
                );
                loaded.add(unit);
            }
        }

        return loaded;
    }

    /**
     * Inserts a new blood unit record into the database.
     *
     * @param cfg the database configuration
     * @param unit the BloodUnit record to insert
     * @throws SQLException if a database error occurs (e.g., duplicate unit ID)
     */
    public static void insert(DbConfig cfg, BloodUnit unit) throws SQLException {
        String sql = "INSERT INTO " + cfg.getTableName() + " ("
                + cfg.getUnitIdCol() + ", "
                + cfg.getBloodTypeCol() + ", "
                + cfg.getDonationDateCol() + ", "
                + cfg.getExpiryDateCol() + ", "
                + cfg.getDonorIdCol() + ", "
                + cfg.getStatusCol() + ") VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(cfg.getJdbcUrl());
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, unit.getUnitId());
            ps.setString(2, unit.getBloodType());
            ps.setString(3, unit.getDonationDate().toString());
            ps.setString(4, unit.getExpiryDate().toString());
            ps.setString(5, unit.getDonorId());
            ps.setString(6, unit.getStatus());
            ps.executeUpdate();
        }
    }

    /**
     * Updates an existing blood unit record in the database.
     *
     * This method updates a record by its old unit ID and stores all new values.
     * This approach allows the unit ID itself to be changed during an update.
     *
     * @param cfg the database configuration
     * @param oldUnitId the current unit ID of the record to update (used to find the record)
     * @param unit the BloodUnit object with new values to store
     * @throws SQLException if a database error occurs
     */
    public static void update(DbConfig cfg, String oldUnitId, BloodUnit unit) throws SQLException {
        String sql = "UPDATE " + cfg.getTableName() + " SET "
                + cfg.getUnitIdCol() + " = ?, "
                + cfg.getBloodTypeCol() + " = ?, "
                + cfg.getDonationDateCol() + " = ?, "
                + cfg.getExpiryDateCol() + " = ?, "
                + cfg.getDonorIdCol() + " = ?, "
                + cfg.getStatusCol() + " = ? "
                + "WHERE " + cfg.getUnitIdCol() + " = ?";

         try (Connection conn = DriverManager.getConnection(cfg.getJdbcUrl());
              PreparedStatement ps = conn.prepareStatement(sql)) {
             // Parameters 1-6 are new values, parameter 7 is the original unit ID.
             ps.setString(1, unit.getUnitId());
             ps.setString(2, unit.getBloodType());
             ps.setString(3, unit.getDonationDate().toString());
             ps.setString(4, unit.getExpiryDate().toString());
             ps.setString(5, unit.getDonorId());
             ps.setString(6, unit.getStatus());
             ps.setString(7, oldUnitId);
             ps.executeUpdate();
         }
     }

     /**
      * Deletes a blood unit record from the database by its unit ID.
      * 
      * @param cfg the database configuration
      * @param unitId the unit ID of the record to delete
      * @throws SQLException if a database error occurs
      */
    public static void deleteByUnitId(DbConfig cfg, String unitId) throws SQLException {
        String sql = "DELETE FROM " + cfg.getTableName() + " WHERE " + cfg.getUnitIdCol() + " = ?";

        try (Connection conn = DriverManager.getConnection(cfg.getJdbcUrl());
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, unitId);
            ps.executeUpdate();
        }
    }

    /**
     * Counts the number of available blood units of a specific type in the database.
     * 
     * @param cfg the database configuration
     * @param bloodType the blood type to count (must match exactly)
     * @return the number of units with the specified blood type and "Available" status
     * @throws SQLException if a database error occurs
     */
    public static int countAvailableByType(DbConfig cfg, String bloodType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + cfg.getTableName()
                + " WHERE " + cfg.getBloodTypeCol() + " = ? AND " + cfg.getStatusCol() + " = ?";

        try (Connection conn = DriverManager.getConnection(cfg.getJdbcUrl());
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bloodType);
            ps.setString(2, "Available");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }
}
