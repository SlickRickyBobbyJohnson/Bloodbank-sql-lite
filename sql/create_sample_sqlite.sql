-- Sample schema + data for BloodUnit app
-- You can rename table/columns if you update your db-config.properties accordingly.

CREATE TABLE IF NOT EXISTS blood_units (
    unit_id TEXT PRIMARY KEY,
    blood_type TEXT NOT NULL,
    donation_date TEXT NOT NULL,
    expiry_date TEXT NOT NULL,
    donor_id TEXT NOT NULL,
    status TEXT NOT NULL
);

INSERT OR REPLACE INTO blood_units(unit_id, blood_type, donation_date, expiry_date, donor_id, status) VALUES
('U001', 'O+', '2026-03-01', '2026-04-12', 'D10', 'Available'),
('U002', 'A-', '2026-03-05', '2026-04-16', 'D11', 'Quarantined'),
('U003', 'B+', '2026-03-07', '2026-04-18', 'D12', 'Available'),
('U004', 'AB-', '2026-03-08', '2026-04-19', 'D13', 'Dispatched'),
('U005', 'A+', '2026-03-10', '2026-04-21', 'D14', 'Available'),
('U006', 'O-', '2026-03-11', '2026-04-22', 'D15', 'Expired'),
('U007', 'B-', '2026-03-12', '2026-04-23', 'D16', 'Available'),
('U008', 'AB+', '2026-03-13', '2026-04-24', 'D17', 'Quarantined'),
('U009', 'A-', '2026-03-14', '2026-04-25', 'D18', 'Available'),
('U010', 'O+', '2026-03-15', '2026-04-26', 'D19', 'Dispatched'),
('U011', 'B+', '2026-03-16', '2026-04-27', 'D20', 'Available'),
('U012', 'AB-', '2026-03-17', '2026-04-28', 'D21', 'Expired'),
('U013', 'A+', '2026-03-18', '2026-04-29', 'D22', 'Available'),
('U014', 'O-', '2026-03-19', '2026-04-30', 'D23', 'Quarantined'),
('U015', 'B-', '2026-03-20', '2026-05-01', 'D24', 'Available'),
('U016', 'AB+', '2026-03-21', '2026-05-02', 'D25', 'Dispatched'),
('U017', 'A-', '2026-03-22', '2026-05-03', 'D26', 'Available'),
('U018', 'O+', '2026-03-23', '2026-05-04', 'D27', 'Expired'),
('U019', 'B+', '2026-03-24', '2026-05-05', 'D28', 'Available'),
('U020', 'AB-', '2026-03-25', '2026-05-06', 'D29', 'Quarantined');


