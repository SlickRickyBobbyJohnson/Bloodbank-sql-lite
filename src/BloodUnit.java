import java.time.LocalDate;

/**
 * Represents a blood unit in the blood bank management system.
 *
 * This class encapsulates all information related to a blood donation unit,
 * including its unique identifier, blood type, donation and expiry dates,
 * donor information, and current status. The expiry date is automatically
 * calculated as 42 days from the donation date upon creation.
 *
 * @author Blood Bank Management System
 * @version 1.0
 */
public class BloodUnit {
    private String unitId;
    private String bloodType;
    private LocalDate donationDate;
    private LocalDate expiryDate;   // donationDate + 42 days
    private String donorId;
    private String status;

    /**
     * Constructs a BloodUnit with the specified parameters.
     *
     * This constructor initializes a new blood unit with the provided information.
     * The expiry date is automatically calculated as 42 days from the donation date,
     * as per blood bank regulations. This ensures that expiry dates are always
     * consistent and cannot be set incorrectly.
     *
     * @param unitId the unique identifier for this blood unit (3-20 alphanumeric characters)
     * @param bloodType the blood type of this unit (e.g., "O+", "AB-")
     * @param donationDate the date this blood was donated
     * @param donorId the unique identifier of the donor (2-30 alphanumeric characters)
     * @param status the current status of the unit (e.g., "Available", "Quarantined", "Expired")
     */
    public BloodUnit(String unitId, String bloodType, LocalDate donationDate, String donorId, String status) {
        this.unitId = unitId;
        this.bloodType = bloodType;
        this.donationDate = donationDate;

        // Expiry date is always calculated on creation, not read from file
        this.expiryDate = donationDate.plusDays(42);

        this.donorId = donorId;
        this.status = status;
    }

    // Getters

    /**
     * Returns the unique identifier of this blood unit.
     * 
     * @return the unit ID (3-20 alphanumeric characters)
     */
    public String getUnitId() {
        return unitId;
    }

    /**
     * Returns the blood type of this unit.
     * 
     * @return the blood type (e.g., "O+", "AB-")
     */
    public String getBloodType() {
        return bloodType;
    }

    /**
     * Returns the date this blood was donated.
     * 
     * @return the donation date
     */
    public LocalDate getDonationDate() {
        return donationDate;
    }

    /**
     * Returns the expiration date of this blood unit.
     * 
     * The expiry date is always 42 days after the donation date.
     * 
     * @return the expiry date
     */
    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    /**
     * Returns the unique identifier of the donor who provided this unit.
     * 
     * @return the donor ID (2-30 alphanumeric characters)
     */
    public String getDonorId() {
        return donorId;
    }

    /**
     * Returns the current status of this blood unit.
     * 
     * @return the status (e.g., "Available", "Quarantined", "Expired")
     */
    public String getStatus() {
        return status;
    }

    // Setters

    /**
     * Sets the unique identifier for this blood unit.
     * 
     * @param unitId the new unit ID (3-20 alphanumeric characters)
     */
    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    /**
     * Sets the blood type for this unit.
     * 
     * @param bloodType the new blood type (e.g., "O+", "AB-")
     */
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    /**
     * Sets the donation date for this unit and recalculates the expiry date.
     * 
     * When the donation date is updated, the expiry date is automatically
     * recalculated as 42 days from the new donation date to maintain consistency.
     * 
     * @param donationDate the new donation date
     */
    public void setDonationDate(LocalDate donationDate) {
        this.donationDate = donationDate;
        // Recalculate expiry date when donation date changes
        this.expiryDate = donationDate.plusDays(42);
    }

    /**
     * Sets the donor ID for this blood unit.
     * 
     * @param donorId the new donor ID (2-30 alphanumeric characters)
     */
    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    /**
     * Sets the current status of this blood unit.
     * 
     * @param status the new status (e.g., "Available", "Quarantined", "Expired")
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns a formatted string representation of this blood unit.
     * 
     * The string includes all relevant information about the unit in a
     * human-readable format suitable for display.
     * 
     * @return a string containing all blood unit information
     */
    public String toDisplayString() {
        return "UnitID=" + unitId
                + " | Type=" + bloodType
                + " | Donation=" + donationDate
                + " | Expiry=" + expiryDate
                + " | DonorID=" + donorId
                + " | Status=" + status;
    }
}