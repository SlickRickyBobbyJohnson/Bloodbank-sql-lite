import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Main GUI window for the Blood Bank Management System.
 *
 * This class provides a Swing-based graphical interface for managing blood unit inventory.
 * It includes a form for entering blood unit details, a table for viewing all records,
 * and buttons for performing CRUD (Create, Read, Update, Delete) operations. The window
 * connects to an SQLite database for persistent data storage.
 *
 * @author Blood Bank Management System
 * @version 1.0
 */
public class BBMSMainWindow extends JFrame {

    // In-memory working set of records loaded from file and manipulated by the UI.
    private final ArrayList<BloodUnit> records = new ArrayList<>();
    private DbConfig dbConfig;

    private JButton loadBtn;
    private JButton displayBtn;
    private JButton createBtn;
    private JButton updateBtn;
    private JButton removeBtn;
    private JButton customBtn;
    private JButton exitBtn;

    private JTextField unitIdField;
    private JComboBox<String> bloodTypeBox;
    private JTextField donationDateField;
    private JTextField donorIdField;
    private JComboBox<String> statusBox;

    private JTable table;
    private DefaultTableModel model;

    private JLabel statusLabel;

    private static final String[] BLOOD_TYPES = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
    private static final String[] STATUSES = {"Available", "Quarantined", "Dispatched", "Expired"};

    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(220, 225, 230);

    private static final Color HEADER = new Color(202, 0, 0);
    private static final Color HEADER_TEXT = Color.WHITE;

    private static final Color BTN_BLUE = new Color(33, 150, 243);
    private static final Color BTN_GREEN = new Color(76, 175, 80);
    private static final Color BTN_ORANGE = new Color(255, 152, 0);
    private static final Color BTN_RED = new Color(255, 105, 94);
    private static final Color BTN_GRAY = new Color(96, 125, 139);

    /**
     * Main entry point for the Blood Bank Management System GUI.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BBMSMainWindow().setVisible(true));
    }

    /**
     * Constructs and initializes the main Blood Bank Management window.
     *
     * Sets up the window layout with header, center content area (form and table),
     * and status bar. Initializes all buttons and table selection handlers.
     */
    public BBMSMainWindow() {
        setTitle("Blood Bank Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 600);
        setLocationRelativeTo(null);

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        // Top: title/actions, center: form and table, bottom: status feedback.
        add(makeHeader(), BorderLayout.NORTH);
        add(makeCenterArea(), BorderLayout.CENTER);
        add(makeStatusBar(), BorderLayout.SOUTH);

        // Connect buttons and table selection to their handlers.
        hookUpButtons();
        hookTableSelection();
    }

    /**
     * Creates the header component containing the title and action buttons.
     *
     * @return a JComponent containing the header section
     */
    private JComponent makeHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel("Blood Bank Management");
        title.setForeground(HEADER_TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel subtitle = new JLabel("Manage your blood inventory with ease");
        subtitle.setForeground(new Color(255, 230, 230));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createVerticalStrut(2));
        text.add(subtitle);

        header.add(text, BorderLayout.WEST);
        header.add(makeButtonBar(), BorderLayout.SOUTH);

        return header;
    }

    /**
     * Creates the button bar containing all action buttons.
     *
     * @return a JComponent containing the button bar
     */
    private JComponent makeButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        bar.setOpaque(false);

        loadBtn = new JButton("Load");
        displayBtn = new JButton("Display");
        createBtn = new JButton("Create");
        updateBtn = new JButton("Update");
        removeBtn = new JButton("Remove");
        customBtn = new JButton("Count Available");
        exitBtn = new JButton("Exit");

        styleButton(loadBtn, BTN_BLUE);
        styleButton(displayBtn, BTN_GRAY);
        styleButton(createBtn, BTN_GREEN);
        styleButton(updateBtn, BTN_ORANGE);
        styleButton(removeBtn, BTN_RED);
        styleButton(customBtn, new Color(156, 39, 176));
        styleButton(exitBtn, new Color(60, 60, 60));

        bar.add(loadBtn);
        bar.add(displayBtn);
        bar.add(createBtn);
        bar.add(updateBtn);
        bar.add(removeBtn);
        bar.add(customBtn);
        bar.add(exitBtn);

        return bar;
    }

    /**
     * Applies consistent styling to action buttons.
     *
     * @param b the button to style
     * @param bg the background color for the button
     */
    private void styleButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Creates the center area containing the input form and data table.
     *
     * @return a JComponent containing the split pane with form and table
     */
    private JComponent makeCenterArea() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel leftForm = makeFormCard();
        JPanel rightTable = makeTableCard();

        // Split view: editable form on the left, read-only grid on the right.
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftForm, rightTable);
        split.setDividerLocation(340);
        split.setResizeWeight(0.35);
        split.setBorder(null);

        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Creates the form card with input fields for blood unit data.
     *
     * @return a JPanel containing the input form
     */
    private JPanel makeFormCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel header = new JLabel("Blood Unit Form");
        header.setFont(new Font("SansSerif", Font.BOLD, 16));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 6, 8, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        card.add(header, c);

        c.gridwidth = 1;

        c.gridy++;
        c.gridx = 0;
        card.add(new JLabel("Unit ID:"), c);
        c.gridx = 1;
        unitIdField = new JTextField();
        styleInput(unitIdField);
        card.add(unitIdField, c);

        c.gridy++;
        c.gridx = 0;
        card.add(new JLabel("Blood Type:"), c);
        c.gridx = 1;
        bloodTypeBox = new JComboBox<>(BLOOD_TYPES);
        styleCombo(bloodTypeBox);
        card.add(bloodTypeBox, c);

        c.gridy++;
        c.gridx = 0;
        card.add(new JLabel("Donation Date:"), c);
        c.gridx = 1;
        donationDateField = new JTextField("YYYY-MM-DD");
        styleInput(donationDateField);
        card.add(donationDateField, c);

        c.gridy++;
        c.gridx = 0;
        card.add(new JLabel("Donor ID:"), c);
        c.gridx = 1;
        donorIdField = new JTextField();
        styleInput(donorIdField);
        card.add(donorIdField, c);

        c.gridy++;
        c.gridx = 0;
        card.add(new JLabel("Status:"), c);
        c.gridx = 1;
        statusBox = new JComboBox<>(STATUSES);
        styleCombo(statusBox);
        card.add(statusBox, c);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        JLabel note = new JLabel("Expiry date is calculated as Donation date + 42 days.");
        note.setFont(new Font("SansSerif", Font.ITALIC, 12));
        note.setForeground(new Color(90, 90, 90));
        card.add(note, c);

        c.gridy++;
        c.weighty = 1;
        card.add(Box.createVerticalGlue(), c);

        return card;
    }

    private void styleInput(JTextField f) {
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    private void styleCombo(JComboBox<String> box) {
        box.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    private JPanel makeTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel header = new JLabel("Inventory Table");
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        card.add(header, BorderLayout.NORTH);

        String[] cols = {"UnitID", "BloodType", "DonationDate", "ExpiryDate", "DonorID", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setGridColor(new Color(230, 230, 230));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER, 1, true));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JComponent makeStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(235, 238, 242));
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(statusLabel, BorderLayout.WEST);

        return panel;
    }

    private void hookUpButtons() {
        loadBtn.addActionListener(e -> onLoad());
        displayBtn.addActionListener(e -> refreshTable());
        createBtn.addActionListener(e -> onCreate());
        updateBtn.addActionListener(e -> onUpdate());
        removeBtn.addActionListener(e -> onRemove());
        customBtn.addActionListener(e -> onCountAvailable());
        exitBtn.addActionListener(e -> dispose());
    }

    private void hookTableSelection() {
        table.getSelectionModel().addListSelectionListener(e -> {
            // Ignore intermediate events while the selection is still changing.
            if (e.getValueIsAdjusting()) {
                return;
            }
            int row = table.getSelectedRow();
            if (row >= 0 && row < records.size()) {
                // Keep the form fields synced with the selected table row.
                fillFormFromRecord(records.get(row));
            }
        });
    }

    private void onLoad() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose DB config file (.properties)");

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            setStatus("Load cancelled.");
            return;
        }

        String path = chooser.getSelectedFile().getAbsolutePath();
        try {
            dbConfig = DbConfigLoader.load(path);
            SQLiteBloodUnitStore.ensureSchema(dbConfig);
            records.clear();
            records.addAll(SQLiteBloodUnitStore.loadAll(dbConfig));
            refreshTable();
            setStatus("Connected. Loaded " + records.size() + " records.");
        } catch (IOException ex) {
            showError("Could not read config file. Check path and permissions.");
        } catch (IllegalArgumentException ex) {
            showError("Invalid config: " + ex.getMessage());
        } catch (SQLException ex) {
            showError("Could not open SQLite database. Check db.path and JDBC driver.");
        }
    }

    private void onCreate() {
        String unitId = unitIdField.getText().trim();
        String donorId = donorIdField.getText().trim();
        String bloodType = (String) bloodTypeBox.getSelectedItem();
        String status = (String) statusBox.getSelectedItem();
        LocalDate donationDate = Validator.tryParseDate(donationDateField.getText().trim());

        // Validate in user-input order so error messages map to the active form fields.
        if (!Validator.isValidUnitId(unitId)) {
            showError("UnitID must be letters/numbers only and 3-20 chars.");
            return;
        }
        if (findIndexByUnitId(unitId) != -1) {
            showError("UnitID already exists.");
            return;
        }
        if (!Validator.isValidBloodType(bloodType)) {
            showError("Invalid blood type.");
            return;
        }
        if (donationDate == null) {
            showError("Donation date must be YYYY-MM-DD.");
            return;
        }
        if (!Validator.isValidDonorId(donorId)) {
            showError("DonorID must be letters/numbers only and 2-30 chars.");
            return;
        }
        if (!Validator.isValidStatus(status)) {
            showError("Invalid status.");
            return;
        }

        BloodUnit created = new BloodUnit(unitId, Validator.normalizeBloodType(bloodType), donationDate, donorId, status);

        if (dbConfig != null) {
            try {
                SQLiteBloodUnitStore.insert(dbConfig, created);
            } catch (SQLException ex) {
                showError("Could not save record to SQLite.");
                return;
            }
        }

        records.add(created);
        refreshTable();
        selectByUnitId(created.getUnitId());
        setStatus("Created " + created.getUnitId() + ".");
    }

    private void onUpdate() {
        if (records.isEmpty()) {
            showError("No records to update.");
            return;
        }

        int selected = table.getSelectedRow();
        if (selected < 0) {
            showError("Select a row to update.");
            return;
        }

        BloodUnit r = records.get(selected);
        String oldUnitId = r.getUnitId();
        String prevBloodType = r.getBloodType();
        LocalDate prevDonationDate = r.getDonationDate();
        String prevDonorId = r.getDonorId();
        String prevStatus = r.getStatus();

        String newUnitId = unitIdField.getText().trim();
        String newDonorId = donorIdField.getText().trim();
        String newBloodType = (String) bloodTypeBox.getSelectedItem();
        String newStatus = (String) statusBox.getSelectedItem();
        LocalDate newDonationDate = Validator.tryParseDate(donationDateField.getText().trim());

        if (!Validator.isValidUnitId(newUnitId)) {
            showError("UnitID must be letters/numbers only and 3-20 chars.");
            return;
        }

        int existingIdx = findIndexByUnitId(newUnitId);
        // Allow keeping the same UnitID on the currently selected record.
        if (existingIdx != -1 && existingIdx != selected) {
            showError("UnitID already exists.");
            return;
        }

        if (!Validator.isValidBloodType(newBloodType)) {
            showError("Invalid blood type.");
            return;
        }
        if (newDonationDate == null) {
            showError("Donation date must be YYYY-MM-DD.");
            return;
        }
        if (!Validator.isValidDonorId(newDonorId)) {
            showError("DonorID must be letters/numbers only and 2-30 chars.");
            return;
        }
        if (!Validator.isValidStatus(newStatus)) {
            showError("Invalid status.");
            return;
        }
        if (!Validator.canTransitionStatus(r.getStatus(), newStatus)) {
            showError("Invalid status transition: " + r.getStatus() + " -> " + newStatus);
            return;
        }

        // Apply all updates after passing validation to avoid partial writes.
        r.setUnitId(newUnitId);
        r.setBloodType(Validator.normalizeBloodType(newBloodType));
        r.setDonationDate(newDonationDate);
        r.setDonorId(newDonorId);
        r.setStatus(newStatus);

        if (dbConfig != null) {
            try {
                SQLiteBloodUnitStore.update(dbConfig, oldUnitId, r);
            } catch (SQLException ex) {
                r.setUnitId(oldUnitId);
                r.setBloodType(prevBloodType);
                r.setDonationDate(prevDonationDate);
                r.setDonorId(prevDonorId);
                r.setStatus(prevStatus);
                showError("Could not update record in SQLite.");
                return;
            }
        }

        refreshTable();
        selectByUnitId(r.getUnitId());
        setStatus("Updated " + r.getUnitId() + ".");
    }

    private void onRemove() {
        if (records.isEmpty()) {
            showError("No records to remove.");
            return;
        }

        int selected = table.getSelectedRow();
        if (selected < 0) {
            showError("Select a row to remove.");
            return;
        }

        BloodUnit removed = records.get(selected);

        if (dbConfig != null) {
            try {
                SQLiteBloodUnitStore.deleteByUnitId(dbConfig, removed.getUnitId());
            } catch (SQLException ex) {
                showError("Could not remove record from SQLite.");
                return;
            }
        }

        records.remove(selected);
        refreshTable();
        setStatus("Removed " + removed.getUnitId() + ".");
    }

    private void onCountAvailable() {
        if (records.isEmpty()) {
            showError("No records loaded.");
            return;
        }

        String bt = (String) JOptionPane.showInputDialog(
                this,
                "Choose blood type",
                "Count Available Units",
                JOptionPane.QUESTION_MESSAGE,
                null,
                BLOOD_TYPES,
                BLOOD_TYPES[0]
        );

        if (bt == null) {
            setStatus("Count cancelled.");
            return;
        }

        int count;
        if (dbConfig != null) {
            try {
                count = SQLiteBloodUnitStore.countAvailableByType(dbConfig, bt);
            } catch (SQLException ex) {
                showError("Could not count records from SQLite.");
                return;
            }
        } else {
            count = 0;
            for (BloodUnit r : records) {
                // "Available" count is constrained by both type and current status.
                if (r.getBloodType().equals(bt) && r.getStatus().equals("Available")) {
                    count++;
                }
            }
        }

        String message = "Available units for " + bt + ": " + count;
        JOptionPane.showMessageDialog(this, message, "Count Result", JOptionPane.INFORMATION_MESSAGE);
        setStatus(message);
    }

    private void refreshTable() {
        // Rebuild the model from records so table state always reflects source data.
        model.setRowCount(0);
        for (BloodUnit r : records) {
            model.addRow(new Object[]{
                    r.getUnitId(),
                    r.getBloodType(),
                    r.getDonationDate(),
                    r.getExpiryDate(),
                    r.getDonorId(),
                    r.getStatus()
            });
        }
        setStatus("Showing " + records.size() + " records.");
    }

    private void fillFormFromRecord(BloodUnit r) {
        unitIdField.setText(r.getUnitId());
        bloodTypeBox.setSelectedItem(r.getBloodType());
        donationDateField.setText(String.valueOf(r.getDonationDate()));
        donorIdField.setText(r.getDonorId());
        statusBox.setSelectedItem(r.getStatus());
    }

    private int findIndexByUnitId(String unitId) {
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getUnitId().equalsIgnoreCase(unitId)) {
                return i;
            }
        }
        return -1;
    }

    private void selectByUnitId(String unitId) {
        int idx = findIndexByUnitId(unitId);
        if (idx >= 0) {
            // Restore focus to the affected record after create/update actions.
            table.setRowSelectionInterval(idx, idx);
            table.scrollRectToVisible(table.getCellRect(idx, 0, true));
        }
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}
