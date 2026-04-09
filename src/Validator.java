import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Validator {

    // Allowed values for blood type and status
    public static final String[] ALLOWED_BLOOD_TYPES =
            {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    public static final String[] ALLOWED_STATUSES =
            {"Available", "Quarantined", "Dispatched", "Expired"};

    // Date format for validation
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // UnitID: letters or numbas, 3-20 chars
    public static boolean isValidUnitId(String s) {
        if (s == null) return false;
        String v = s.trim();
        if (v.isEmpty()) return false;
        if (v.length() < 3 || v.length() > 20) return false;
        return v.matches("[A-Za-z0-9]+");
    }

    // DonorID: letters and numbers, 2-30 chars
    public static boolean isValidDonorId(String s) {
        if (s == null) return false;
        String v = s.trim();
        if (v.isEmpty()) return false;
        if (v.length() < 2 || v.length() > 30) return false;
        //[A-Za-z0-9] = one character that is either:
        //A-Z uppercase letter
        //a-z lowercase letter
        //0-9 digit
        return v.matches("[A-Za-z0-9]+");

    }

    // Blood type must be one of the allowed types
    public static boolean isValidBloodType(String s) {
        if (s == null) return false;
        String v = normalizeBloodType(s);
        for (String bt : ALLOWED_BLOOD_TYPES) {
            if (bt.equals(v)) return true;
        }
        return false;
    }

    // status must be one of the allowed statuses
    public static boolean isValidStatus(String s) {
        if (s == null) return false;
        String v = s.trim();
        for (String st : ALLOWED_STATUSES) {
            if (st.equals(v)) return true;
        }
        return false;
    }

    // Status transition policy used by both CLI and Swing update paths.
    // Expired/Dispatched are terminal states, and Quarantined cannot move back to Available.
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

    // Tries to parse a date, returns null if invalid (kept crashing without this)
    public static LocalDate tryParseDate(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;
// LocalDate.parse() will throw an exception if the format is wrong, so we catch it and return null (no crash pls)
        try {
            return LocalDate.parse(v, DATE_FMT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    // Normalizes blood type input
    public static String normalizeBloodType(String s) {
        return s.trim().toUpperCase();
    }
}
