public class DbConfig {
    private final String dbPath;
    private final String tableName;
    private final String unitIdCol;
    private final String bloodTypeCol;
    private final String donationDateCol;
    private final String expiryDateCol;
    private final String donorIdCol;
    private final String statusCol;

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

    public String getDbPath() {
        return dbPath;
    }

    public String getTableName() {
        return tableName;
    }

    public String getUnitIdCol() {
        return unitIdCol;
    }

    public String getBloodTypeCol() {
        return bloodTypeCol;
    }

    public String getDonationDateCol() {
        return donationDateCol;
    }

    public String getExpiryDateCol() {
        return expiryDateCol;
    }

    public String getDonorIdCol() {
        return donorIdCol;
    }

    public String getStatusCol() {
        return statusCol;
    }

    public String getJdbcUrl() {
        return "jdbc:sqlite:" + dbPath;
    }
}
