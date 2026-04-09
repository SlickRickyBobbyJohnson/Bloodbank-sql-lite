import java.time.LocalDate;

public class BloodUnit {
    private String unitId;
    private String bloodType;
    private LocalDate donationDate;
    private LocalDate expiryDate;   // donationDate + 42 days
    private String donorId;
    private String status;

    public BloodUnit(String unitId, String bloodType, LocalDate donationDate, String donorId, String status) {
        this.unitId = unitId;
        this.bloodType = bloodType;
        this.donationDate = donationDate;

        //  (always calculated on creation, not read from file)
        this.expiryDate = donationDate.plusDays(42);

        this.donorId = donorId;
        this.status = status;
    }

    // Getters
    public String getUnitId() {
        return unitId;
    }

    public String getBloodType() {
        return bloodType;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public String getDonorId() {
        return donorId;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public void setDonationDate(LocalDate donationDate) {
        this.donationDate = donationDate;
        // Recalculate expiry date when donation date changes
        this.expiryDate = donationDate.plusDays(42);
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toDisplayString() {
        return "UnitID=" + unitId
                + " | Type=" + bloodType
                + " | Donation=" + donationDate
                + " | Expiry=" + expiryDate
                + " | DonorID=" + donorId
                + " | Status=" + status;
    }
}