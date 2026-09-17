/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.hotelmanaagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * HOTEL RESERVATION SYSTEM - single file version
 * Run this file directly (it contains the main method at the bottom).
 *
 * Contains 4 classes in one file:
 *   1. Room               - a hotel room
 *   2. Reservation        - a booking
 *   3. HotelDataManager   - booking logic + File I/O (rooms.txt, reservations.txt)
 *   4. HotelReservationGUI - the Swing GUI (Search / Book / Cancel / Booking Details tabs)
 */
public class HotelReservationGUI extends JFrame {

    private HotelDataManager manager;

    // ---- Search tab ----
    private JComboBox<String> searchCategoryBox;
    private DefaultTableModel searchTableModel;
    private JTable searchTable;

    // ---- Book tab ----
    private JTextField nameField;
    private JTextField roomNumberField;
    private JTextField nightsField;
    private JCheckBox payNowCheckBox;
    private JTextArea bookResultArea;

    // ---- Cancel tab ----
    private JTextField cancelIdField;
    private JTextArea cancelResultArea;

    // ---- Booking details tab ----
    private DefaultTableModel bookingsTableModel;
    private JTable bookingsTable;

    public HotelReservationGUI() {
        manager = new HotelDataManager();

        setTitle("Hotel Reservation System");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Search Rooms", buildSearchTab());
        tabs.addTab("Book a Room", buildBookTab());
        tabs.addTab("Cancel Reservation", buildCancelTab());
        tabs.addTab("Booking Details", buildBookingDetailsTab());

        add(tabs);
    }

    // ================= SEARCH TAB =================

    private JPanel buildSearchTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Category:"));
        searchCategoryBox = new JComboBox<>(new String[]{"All", "Standard", "Deluxe", "Suite"});
        topPanel.add(searchCategoryBox);

        JButton searchButton = new JButton("Search Available Rooms");
        searchButton.addActionListener(e -> performSearch());
        topPanel.add(searchButton);

        searchTableModel = new DefaultTableModel(
                new String[]{"Room No.", "Category", "Price/Night", "Status"}, 0);
        searchTable = new JTable(searchTableModel);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(searchTable), BorderLayout.CENTER);

        performSearch(); // show all rooms once at startup

        return panel;
    }

    private void performSearch() {
        String category = (String) searchCategoryBox.getSelectedItem();
        searchTableModel.setRowCount(0);
        List<Room> results = manager.searchAvailableRooms(category);
        for (Room r : results) {
            searchTableModel.addRow(new Object[]{
                    r.getRoomNumber(), r.getCategory(), r.getPricePerNight(), "Available"
            });
        }
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No available rooms found in that category.");
        }
    }

    // ================= BOOK TAB =================

    private JPanel buildBookTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Guest Name:"));
        nameField = new JTextField();
        form.add(nameField);

        form.add(new JLabel("Room Number (see Search tab):"));
        roomNumberField = new JTextField();
        form.add(roomNumberField);

        form.add(new JLabel("Number of Nights:"));
        nightsField = new JTextField();
        form.add(nightsField);

        form.add(new JLabel("Pay Now (simulate payment)?"));
        payNowCheckBox = new JCheckBox();
        form.add(payNowCheckBox);

        JButton bookButton = new JButton("Book Room");
        bookButton.addActionListener(e -> performBooking());

        bookResultArea = new JTextArea(6, 40);
        bookResultArea.setEditable(false);
        bookResultArea.setBorder(BorderFactory.createTitledBorder("Booking Result / Receipt"));

        panel.add(form);
        panel.add(Box.createVerticalStrut(10));
        panel.add(bookButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JScrollPane(bookResultArea));

        return panel;
    }

    private void performBooking() {
        String name = nameField.getText().trim();
        String roomText = roomNumberField.getText().trim();
        String nightsText = nightsField.getText().trim();

        if (name.isEmpty() || roomText.isEmpty() || nightsText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        int roomNumber;
        int nights;
        try {
            roomNumber = Integer.parseInt(roomText);
            nights = Integer.parseInt(nightsText);
            if (nights <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Room number and nights must be valid positive numbers.");
            return;
        }

        boolean payNow = payNowCheckBox.isSelected();
        Reservation reservation = manager.bookRoom(name, roomNumber, nights, payNow);

        if (reservation == null) {
            bookResultArea.setText("Booking failed: Room " + roomNumber + " is not available.\n"
                    + "Check the Search Rooms tab for available room numbers.");
        } else {
            bookResultArea.setText(
                    "Booking confirmed!\n" +
                    "-----------------------------\n" +
                    "Reservation ID : " + reservation.getReservationId() + "\n" +
                    "Guest Name     : " + reservation.getGuestName() + "\n" +
                    "Room Number    : " + reservation.getRoomNumber() + "\n" +
                    "Category       : " + reservation.getCategory() + "\n" +
                    "Nights         : " + reservation.getNights() + "\n" +
                    "Total Amount   : Rs." + reservation.getTotalAmount() + "\n" +
                    "Payment Status : " + reservation.getPaymentStatus()
            );
            nameField.setText("");
            roomNumberField.setText("");
            nightsField.setText("");
            payNowCheckBox.setSelected(false);
            performSearch();
        }
    }

    // ================= CANCEL TAB =================

    private JPanel buildCancelTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Reservation ID:"));
        cancelIdField = new JTextField(10);
        form.add(cancelIdField);

        JButton cancelButton = new JButton("Cancel Reservation");
        cancelButton.addActionListener(e -> performCancel());
        form.add(cancelButton);

        cancelResultArea = new JTextArea(6, 40);
        cancelResultArea.setEditable(false);
        cancelResultArea.setBorder(BorderFactory.createTitledBorder("Result"));

        panel.add(form);
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JScrollPane(cancelResultArea));

        return panel;
    }

    private void performCancel() {
        String idText = cancelIdField.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a reservation ID.");
            return;
        }
        try {
            int id = Integer.parseInt(idText);
            boolean success = manager.cancelReservation(id);
            if (success) {
                cancelResultArea.setText("Reservation #" + id + " cancelled. The room is now available again.");
                cancelIdField.setText("");
            } else {
                cancelResultArea.setText("No reservation found with ID " + id + ".");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Reservation ID must be a number.");
        }
    }

    // ================= BOOKING DETAILS TAB =================

    private JPanel buildBookingDetailsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshBookingsTable());

        bookingsTableModel = new DefaultTableModel(
                new String[]{"ID", "Guest", "Room No.", "Category", "Nights", "Total", "Payment"}, 0);
        bookingsTable = new JTable(bookingsTableModel);

        panel.add(refreshButton, BorderLayout.NORTH);
        panel.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);

        refreshBookingsTable();

        return panel;
    }

    private void refreshBookingsTable() {
        bookingsTableModel.setRowCount(0);
        for (Reservation r : manager.getAllReservations()) {
            bookingsTableModel.addRow(new Object[]{
                    r.getReservationId(), r.getGuestName(), r.getRoomNumber(),
                    r.getCategory(), r.getNights(), r.getTotalAmount(), r.getPaymentStatus()
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HotelReservationGUI().setVisible(true));
    }
}

// ======================================================================
// Below: the supporting classes. They live in the same file and package,
// so this is the ONLY file you need to add to your project.
// ======================================================================

/** Represents a single hotel room. */
class Room implements Serializable {

    private int roomNumber;
    private String category;   // Standard, Deluxe, Suite
    private double pricePerNight;
    private boolean available;

    public Room(int roomNumber, String category, double pricePerNight, boolean available) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.available = available;
    }

    public int getRoomNumber() { return roomNumber; }
    public String getCategory() { return category; }
    public double getPricePerNight() { return pricePerNight; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String toCsv() {
        return roomNumber + "," + category + "," + pricePerNight + "," + available;
    }

    public static Room fromCsv(String line) {
        String[] parts = line.split(",");
        return new Room(Integer.parseInt(parts[0]), parts[1],
                Double.parseDouble(parts[2]), Boolean.parseBoolean(parts[3]));
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + category + ") - Rs." + pricePerNight + "/night - "
                + (available ? "Available" : "Booked");
    }
}

/** Represents one reservation made by a guest for a room. */
class Reservation implements Serializable {

    private int reservationId;
    private String guestName;
    private int roomNumber;
    private String category;
    private int nights;
    private double totalAmount;
    private String paymentStatus; // "Paid" or "Pending"

    public Reservation(int reservationId, String guestName, int roomNumber, String category,
                        int nights, double totalAmount, String paymentStatus) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.category = category;
        this.nights = nights;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
    }

    public int getReservationId() { return reservationId; }
    public String getGuestName() { return guestName; }
    public int getRoomNumber() { return roomNumber; }
    public String getCategory() { return category; }
    public int getNights() { return nights; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentStatus() { return paymentStatus; }

    public String toCsv() {
        return reservationId + "," + guestName + "," + roomNumber + "," + category + ","
                + nights + "," + totalAmount + "," + paymentStatus;
    }

    public static Reservation fromCsv(String line) {
        String[] parts = line.split(",");
        return new Reservation(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2]),
                parts[3], Integer.parseInt(parts[4]), Double.parseDouble(parts[5]), parts[6]);
    }

    @Override
    public String toString() {
        return "Booking #" + reservationId + " | " + guestName + " | Room " + roomNumber
                + " (" + category + ") | " + nights + " night(s) | Rs." + totalAmount
                + " | " + paymentStatus;
    }
}

/** Handles booking/cancellation logic and all File I/O (rooms.txt, reservations.txt). */
class HotelDataManager {

    private static final String ROOMS_FILE = "rooms.txt";
    private static final String RESERVATIONS_FILE = "reservations.txt";

    private List<Room> rooms;
    private List<Reservation> reservations;
    private int nextReservationId;

    public HotelDataManager() {
        rooms = new ArrayList<>();
        reservations = new ArrayList<>();
        loadRooms();
        loadReservations();
        if (rooms.isEmpty()) {
            createDefaultRooms();
            saveRooms();
        }
        nextReservationId = computeNextReservationId();
    }

    private void createDefaultRooms() {
        rooms.add(new Room(101, "Standard", 5000, true));
        rooms.add(new Room(102, "Standard", 5000, true));
        rooms.add(new Room(103, "Standard", 5000, true));
        rooms.add(new Room(201, "Deluxe", 9000, true));
        rooms.add(new Room(202, "Deluxe", 9000, true));
        rooms.add(new Room(301, "Suite", 15000, true));
        rooms.add(new Room(302, "Suite", 15000, true));
    }

    private int computeNextReservationId() {
        int max = 0;
        for (Reservation r : reservations) {
            if (r.getReservationId() > max) max = r.getReservationId();
        }
        return max + 1;
    }

    private void loadRooms() {
        File file = new File(ROOMS_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) rooms.add(Room.fromCsv(line));
            }
        } catch (IOException e) {
            System.out.println("Error loading rooms: " + e.getMessage());
        }
    }

    private void saveRooms() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ROOMS_FILE))) {
            for (Room r : rooms) writer.println(r.toCsv());
        } catch (IOException e) {
            System.out.println("Error saving rooms: " + e.getMessage());
        }
    }

    private void loadReservations() {
        File file = new File(RESERVATIONS_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) reservations.add(Reservation.fromCsv(line));
            }
        } catch (IOException e) {
            System.out.println("Error loading reservations: " + e.getMessage());
        }
    }

    private void saveReservations() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESERVATIONS_FILE))) {
            for (Reservation r : reservations) writer.println(r.toCsv());
        } catch (IOException e) {
            System.out.println("Error saving reservations: " + e.getMessage());
        }
    }

    public List<Room> getAllRooms() { return rooms; }

    public List<Room> searchAvailableRooms(String category) {
        List<Room> results = new ArrayList<>();
        for (Room r : rooms) {
            boolean categoryMatches = category.equals("All") || r.getCategory().equals(category);
            if (r.isAvailable() && categoryMatches) results.add(r);
        }
        return results;
    }

    private Room findRoomByNumber(int roomNumber) {
        for (Room r : rooms) {
            if (r.getRoomNumber() == roomNumber) return r;
        }
        return null;
    }

    public Reservation bookRoom(String guestName, int roomNumber, int nights, boolean payNow) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null || !room.isAvailable()) return null;

        double total = room.getPricePerNight() * nights;
        String status = payNow ? "Paid" : "Pending";

        Reservation reservation = new Reservation(
                nextReservationId++, guestName, roomNumber, room.getCategory(), nights, total, status);

        room.setAvailable(false);
        reservations.add(reservation);

        saveRooms();
        saveReservations();

        return reservation;
    }

    public boolean cancelReservation(int reservationId) {
        Reservation toRemove = null;
        for (Reservation r : reservations) {
            if (r.getReservationId() == reservationId) {
                toRemove = r;
                break;
            }
        }
        if (toRemove == null) return false;

        reservations.remove(toRemove);

        Room room = findRoomByNumber(toRemove.getRoomNumber());
        if (room != null) room.setAvailable(true);

        saveRooms();
        saveReservations();
        return true;
    }

    public List<Reservation> getAllReservations() {return reservations;}
}
