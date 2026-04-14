import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class that provides validation and normalization methods for blood unit data.
 *
 * This class contains static validation methods to ensure that all blood unit information
 * (unit IDs, donor IDs, blood types, dates, and statuses) meets the required format and
 * business rules. It also enforces status transition policies to ensure data integrity.
 *
 * @author Blood Bank Management System
 * @version 1.0
 */
public class Validator {

    /**
     * Array of valid blood types that can be stored in the system.
     */
    public static final String[] ALLOWED_BLOOD_TYPES =
            {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    /**
     * Array of valid status values for blood units.
     */
    public static final String[] ALLOWED_STATUSES =
            {"Available", "Quarantined", "Dispatched", "Expired"};

    /**
     * Date format pattern used throughout the system: yyyy-MM-dd.
     */
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Validates that a unit ID contains only letters and numbers and is between 3-20 characters.
     *
     * @param s the unit ID to validate
     * @return true if the unit ID is valid, false otherwise
     */
    public static boolean isValidUnitId(String s) {
        if (s == null) return false;
        String v = s.trim();
        if (v.isEmpty()) return false;
        if (v.length() < 3 || v.length() > 20) return false;
        return v.matches("[A-Za-z0-9]+");
    }

    /**
     * Validates that a donor ID contains only letters and numbers and is between 2-30 characters.
     *
     * @param s the donor ID to validate
     * @return true if the donor ID is valid, false otherwise
     */
    public static boolean isValidDonorId(String s) {
        if (s == null) return false;
        String v = s.trim();
        if (v.isEmpty()) return false;
        if (v.length() < 2 || v.length() > 30) return false;
        return v.matches("[A-Za-z0-9]+");

    }

    /**
     * Validates that a blood type matches one of the allowed blood types in the system.
     *
     * @param s the blood type to validate
     * @return true if the blood type is valid, false otherwise
     */
    public static boolean isValidBloodType(String s) {
        if (s == null) return false;
        String v = normalizeBloodType(s);
        for (String bt : ALLOWED_BLOOD_TYPES) {
            if (bt.equals(v)) return true;
        }
        return false;
    }

    /**
     * Validates that a status matches one of the allowed statuses in the system.
     *
     * @param s the status to validate
     * @return true if the status is valid, false otherwise
     */
    public static boolean isValidStatus(String s) {
        if (s == null) return false;
        String v = s.trim();
        for (String st : ALLOWED_STATUSES) {
            if (st.equals(v)) return true;
        }
        return false;
    }

    /**
     * Determines if a status transition from one status to another is allowed by business rules.
     *
     * Status transition rules:
     * - Expired and Dispatched are terminal states (no transitions allowed).
     * - Quarantined cannot transition back to Available.
     * - Available can transition to Quarantined, Dispatched, or Expired.
     * - Quarantined can transition to Dispatched or Expired.
     *
     * @param from the current status
     * @param to the target status
     * @return true if the transition is allowed, false otherwise
     */
    public static boolean canTransitionStatus(String from, String to) {
        if (!isValidStatus(from) || !isValidStatus(to)) return false;
        if (from.equals(to)) return true;

        if (from.equals("Expired") || from.equals("Dispatched")) return false;

        if (from.equals("Available")) {
            return to.equals("Quarantined") || to.equals("Dispatched") || to.equals("Expired");
        }

        if (from.equals("Quarantined")) {
            return to.equals("Dispatched") || to.equals("Expired");
        }

        return false;
    }

    /**
     * Attempts to parse a date string in the required format (yyyy-MM-dd).
     *
     * Returns null if the date string is invalid or cannot be parsed, allowing
     * the caller to handle invalid dates gracefully without exceptions.
     *
     * @param s the date string to parse
     * @return the parsed LocalDate, or null if parsing fails
     */
    public static LocalDate tryParseDate(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;
        // LocalDate.parse() will throw an exception if the format is wrong, so we catch it and return null
        try {
            return LocalDate.parse(v, DATE_FMT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /**
     * Normalizes a blood type string by trimming whitespace and converting to uppercase.
     *
     * @param s the blood type string to normalize
     * @return the normalized blood type
     */
    public static String normalizeBloodType(String s) {
        return s.trim().toUpperCase();
    }
}
