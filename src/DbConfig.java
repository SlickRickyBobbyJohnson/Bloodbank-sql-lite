/**
 * Configuration object for database connection and table/column mappings.
 *
 * This class stores all database configuration parameters, including the database path,
 * table name, and column names for each blood unit attribute. It provides getters for each parameter and a method to construct the JDBC URL for SQLite.
 *
 *
 * @author Blood Bank Management System
 * @version 1.0
 */
public class DbConfig {
    private final String dbPath;
    private final String tableName;
    private final String unitIdCol;
    private final String bloodTypeCol;
    private final String donationDateCol;
    private final String expiryDateCol;
    private final String donorIdCol;
    private final String statusCol;

    /**
     * Constructs a DbConfig with the specified database and table configuration.
     *
     * All parameters are required and define how the application connects to the database
     * and maps blood unit attributes to database columns.
     *
     * @param dbPath the file path or connection string to the SQLite database
     * @param tableName the name of the table containing blood unit records
     * @param unitIdCol the column name for unit IDs
     * @param bloodTypeCol the column name for blood types
     * @param donationDateCol the column name for donation dates
     * @param expiryDateCol the column name for expiry dates
     * @param donorIdCol the column name for donor IDs
     * @param statusCol the column name for unit statuses
     */
    public DbConfig(
            String dbPath,
            String tableName,
            String unitIdCol,
            String bloodTypeCol,
            String donationDateCol,
            String expiryDateCol,
            String donorIdCol,
            String statusCol
    ) {
        this.dbPath = dbPath;
        this.tableName = tableName;
        this.unitIdCol = unitIdCol;
        this.bloodTypeCol = bloodTypeCol;
        this.donationDateCol = donationDateCol;
        this.expiryDateCol = expiryDateCol;
        this.donorIdCol = donorIdCol;
        this.statusCol = statusCol;
    }

    /**
     * Gets the database file path or connection string.
     *
     * @return the database path
     */
    public String getDbPath() {
        return dbPath;
    }

    /**
     * Gets the table name for blood unit records.
     *
     * @return the table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Gets the column name for unit IDs.
     *
     * @return the unit ID column name
     */
    public String getUnitIdCol() {
        return unitIdCol;
    }

    /**
     * Gets the column name for blood types.
     *
     * @return the blood type column name
     */
    public String getBloodTypeCol() {
        return bloodTypeCol;
    }

    /**
     * Gets the column name for donation dates.
     *
     * @return the donation date column name
     */
    public String getDonationDateCol() {
        return donationDateCol;
    }

    /**
     * Gets the column name for expiry dates.
     *
     * @return the expiry date column name
     */
    public String getExpiryDateCol() {
        return expiryDateCol;
    }

    /**
     * Gets the column name for donor IDs.
     *
     * @return the donor ID column name
     */
    public String getDonorIdCol() {
        return donorIdCol;
    }

    /**
     * Gets the column name for unit statuses.
     *
     * @return the status column name
     */
    public String getStatusCol() {
        return statusCol;
    }

    /**
     * Builds and returns the JDBC connection URL for the SQLite database.
     *
     * @return the JDBC URL in the format jdbc:sqlite:[dbPath]
     */
    public String getJdbcUrl() {
        return "jdbc:sqlite:" + dbPath;
    }
}
