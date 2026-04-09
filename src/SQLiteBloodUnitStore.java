import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

public class SQLiteBloodUnitStore {

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
            // 1-6 are new values, 7 is the origial.
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

    public static void deleteByUnitId(DbConfig cfg, String unitId) throws SQLException {
        String sql = "DELETE FROM " + cfg.getTableName() + " WHERE " + cfg.getUnitIdCol() + " = ?";

        try (Connection conn = DriverManager.getConnection(cfg.getJdbcUrl());
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, unitId);
            ps.executeUpdate();
        }
    }

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
