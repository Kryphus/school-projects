/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUIs;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;
import Project.Book;
import Project.DatabaseUtils;
import Project.Reservation;
import Project.User;
import java.awt.Color;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 *
 * @author User
 */
public class AdminFrame extends javax.swing.JFrame {

    /**
     * Creates new form AdminFrame
     */
    private DefaultTableModel userModel;

    public AdminFrame(User user) {
        initComponents();
        setResizable(false);
        setTitle("Readaroo | Admin");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        updateDashboardCounts();
        viewAllBooksButton.setOpaque(true);
        searchBooksButton.setOpaque(true);
        reservedBooksButton.setOpaque(true);
        addNewUserButton.setOpaque(true);
        manageUsersButton.setOpaque(true);
        logoutButton.setOpaque(true);
    }

    //1 Dashboard
    private void updateDashboardCounts() {
        int totalBooks = DatabaseUtils.getTotalBooksCount();
        int totalUsers = DatabaseUtils.getTotalUsersCount();
        int totalReservations = DatabaseUtils.getTotalReservationsCount();

        totalBooksLabel.setText(String.valueOf(totalBooks));
        totalUsersLabel.setText(String.valueOf(totalUsers));
        totalReservationsLabel.setText(String.valueOf(totalReservations));
    }

    //2 Adding Book Function
    //3 Search Function
    private void searchBook() {
        String searchText = searchBookField.getText();
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a book title to search.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Book book = DatabaseUtils.getBookByTitle(searchText);
        if (book != null) {
            titleFieldS.setText(book.getTitle());
            authorFieldS.setText(book.getAuthor());
            availabilityCheckBoxS.setSelected(book.isAvailable());
        } else {
            JOptionPane.showMessageDialog(this, "Book not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBook() {
        String title = titleFieldS.getText();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please search for a book first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String author = authorFieldS.getText();
        boolean availability = availabilityCheckBoxS.isSelected();

        boolean success = DatabaseUtils.updateBookByTitle(title, author, availability);
        if (success) {
            JOptionPane.showMessageDialog(this, "Book updated successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update book.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void issueBook() {
        String title = titleFieldS.getText();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please search for a book first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = DatabaseUtils.issueBookByTitle(title);
        if (success) {
            availabilityCheckBoxS.setSelected(false); // Update the UI to reflect the change
            JOptionPane.showMessageDialog(this, "Book issued successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to issue book.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reserveBook() {
        String title = titleFieldS.getText();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please search for a book first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Input user name, reservation date, and expiry date using JOptionPane
        String userName = JOptionPane.showInputDialog(this, "Enter User Name:");
        String reservationDateStr = JOptionPane.showInputDialog(this, "Enter Reservation Date (yyyy-MM-dd):");
        String expiryDateStr = JOptionPane.showInputDialog(this, "Enter Expiry Date (yyyy-MM-dd):");

        try {
            // Get user ID based on user name
            int userId = DatabaseUtils.getUserIdByName(userName);
            if (userId == -1) {
                JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date reservationDateUtil = dateFormat.parse(reservationDateStr);
            java.util.Date expiryDateUtil = dateFormat.parse(expiryDateStr);

            java.sql.Date reservationDateSql = new java.sql.Date(reservationDateUtil.getTime());
            java.sql.Date expiryDateSql = new java.sql.Date(expiryDateUtil.getTime());

            int bookId = DatabaseUtils.getBookIdByTitle(title);

            if (bookId == -1) {
                JOptionPane.showMessageDialog(this, "Book not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = DatabaseUtils.reserveBook(userId, bookId, reservationDateSql, expiryDateSql);
            if (success) {
                JOptionPane.showMessageDialog(this, "Book reserved successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reserve book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid user ID. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please enter the date in yyyy-MM-dd format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearSearchFields() {
        searchBookField.setText("");
        titleFieldS.setText("");
        authorFieldS.setText("");
        availabilityCheckBoxS.setSelected(false);
    }

    //4 Reserved Books Function
    private void loadReservedBooks() {
        DefaultTableModel model = (DefaultTableModel) reservedBooksTable.getModel();
        model.setRowCount(0); // Clear the table

        List<Reservation> reservations = DatabaseUtils.getAllReservations();
        for (Reservation reservation : reservations) {
            model.addRow(new Object[]{
                reservation.getReservationId(),
                reservation.getUserId(),
                reservation.getBookId(),
                reservation.getReservationDate(),
                reservation.getExpiryDate()
            });
        }
    }

    //Add new Users Function
    private void loadUsers() {
        if (userModel == null) {
            System.err.println("UserModel is null. Ensure it is properly initialized.");
            return;
        }
        userModel.setRowCount(0); // Clear existing rows
        List<User> users = DatabaseUtils.getAllUsers(); // Fetch all users from the database
        for (User user : users) {
            userModel.addRow(new Object[]{user.getUserId(), user.getUsername(), user.getPassword(), user.getEmail(), user.isAdmin()});
        }
    }

    //Manage Users Function
    private void initManageUsersTab() {
        userModel = new DefaultTableModel(new String[]{"User ID", "Username", "Password", "Email", "Is Admin"}, 0);
        userTable.setModel(userModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        loadUsers();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        dashboardPanel = new javax.swing.JPanel();
        dashboardButton = new javax.swing.JLabel();
        addNewBookPanel = new javax.swing.JPanel();
        addNewBookButton = new javax.swing.JLabel();
        searchBooksPanel = new javax.swing.JPanel();
        searchBooksButton = new javax.swing.JLabel();
        addNewUserPanel = new javax.swing.JPanel();
        addNewUserButton = new javax.swing.JLabel();
        manageUsersPanel = new javax.swing.JPanel();
        manageUsersButton = new javax.swing.JLabel();
        logoutPanel = new javax.swing.JPanel();
        logoutButton = new javax.swing.JLabel();
        reservedBooksPanel = new javax.swing.JPanel();
        reservedBooksButton = new javax.swing.JLabel();
        viewAllBooksPanel = new javax.swing.JPanel();
        viewAllBooksButton = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        totalBooksLabel = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        totalUsersLabel = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        totalReservationsLabel = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        dashboardRefreshPanelButton = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        inputAuthorField = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        inputTitleField = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        checkBoxAvailability = new javax.swing.JCheckBox();
        addBookPanelButton = new javax.swing.JPanel();
        jLabel52 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableBooks = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        searchBookField = new javax.swing.JTextField();
        titleFieldS = new javax.swing.JTextField();
        searchBookButton = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        authorFieldS = new javax.swing.JTextField();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        availabilityCheckBoxS = new javax.swing.JCheckBox();
        updateBookButton = new javax.swing.JPanel();
        jLabel40 = new javax.swing.JLabel();
        reserveBookButton = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        issueBookButton = new javax.swing.JPanel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        deleteBookButton = new javax.swing.JPanel();
        jLabel53 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        reservedBooksTable = new javax.swing.JTable();
        deleteReservationButton = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        addUsernameField = new javax.swing.JTextField();
        jLabel46 = new javax.swing.JLabel();
        addPasswordField = new javax.swing.JPasswordField();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        addIsAdminCheckBox = new javax.swing.JCheckBox();
        addUserButton = new javax.swing.JPanel();
        jLabel49 = new javax.swing.JLabel();
        addEmailField = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        userTable = new javax.swing.JTable();
        updateUserButton = new javax.swing.JPanel();
        jLabel50 = new javax.swing.JLabel();
        deleteUserButton = new javax.swing.JPanel();
        jLabel51 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(899, 656));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(212, 212, 212));

        jLabel2.setBackground(new java.awt.Color(0, 0, 0));
        jLabel2.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 24)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-user-male-48.png"))); // NOI18N
        jLabel2.setText("Hello Admin!");

        dashboardPanel.setBackground(new java.awt.Color(212, 212, 212));
        dashboardPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dashboardPanelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                dashboardPanelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                dashboardPanelMouseExited(evt);
            }
        });

        dashboardButton.setBackground(new java.awt.Color(0, 0, 0));
        dashboardButton.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 14)); // NOI18N
        dashboardButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dashboardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-dashboard-35.png"))); // NOI18N
        dashboardButton.setText(" Dashboard");
        dashboardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dashboardButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                dashboardButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                dashboardButtonMouseExited(evt);
            }
        });

        javax.swing.GroupLayout dashboardPanelLayout = new javax.swing.GroupLayout(dashboardPanel);
        dashboardPanel.setLayout(dashboardPanelLayout);
        dashboardPanelLayout.setHorizontalGroup(
            dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dashboardButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        dashboardPanelLayout.setVerticalGroup(
            dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dashboardButton, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
        );

        addNewBookPanel.setBackground(new java.awt.Color(212, 212, 212));
        addNewBookPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addNewBookPanelMouseClicked(evt);
            }
        });

        addNewBookButton.setBackground(new java.awt.Color(0, 0, 0));
        addNewBookButton.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 14)); // NOI18N
        addNewBookButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        addNewBookButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-add-new-36.png"))); // NOI18N
        addNewBookButton.setText(" Add New book");
        addNewBookButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addNewBookButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addNewBookButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addNewBookButtonMouseExited(evt);
            }
        });

        javax.swing.GroupLayout addNewBookPanelLayout = new javax.swing.GroupLayout(addNewBookPanel);
        addNewBookPanel.setLayout(addNewBookPanelLayout);
        addNewBookPanelLayout.setHorizontalGroup(
            addNewBookPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addNewBookButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        addNewBookPanelLayout.setVerticalGroup(
            addNewBookPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addNewBookButton, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
        );

        searchBooksPanel.setBackground(new java.awt.Color(212, 212, 212));
        searchBooksPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchBooksPanelMouseClicked(evt);
            }
        });

        searchBooksButton.setBackground(new java.awt.Color(212, 212, 212));
        searchBooksButton.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 14)); // NOI18N
        searchBooksButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        searchBooksButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-search-35.png"))); // NOI18N
        searchBooksButton.setText(" Search Books");
        searchBooksButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchBooksButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchBooksButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchBooksButtonMouseExited(evt);
            }
        });

        javax.swing.GroupLayout searchBooksPanelLayout = new javax.swing.GroupLayout(searchBooksPanel);
        searchBooksPanel.setLayout(searchBooksPanelLayout);
        searchBooksPanelLayout.setHorizontalGroup(
            searchBooksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchBooksButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        searchBooksPanelLayout.setVerticalGroup(
            searchBooksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchBooksButton, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
        );

        addNewUserPanel.setBackground(new java.awt.Color(212, 212, 212));
        addNewUserPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addNewUserPanelMouseClicked(evt);
            }
        });

        addNewUserButton.setBackground(new java.awt.Color(212, 212, 212));
        addNewUserButton.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 14)); // NOI18N
        addNewUserButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        addNewUserButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-add-user-male-35.png"))); // NOI18N
        addNewUserButton.setText(" Add New User");
        addNewUserButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addNewUserButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addNewUserButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addNewUserButtonMouseExited(evt);
            }
        });

        javax.swing.GroupLayout addNewUserPanelLayout = new javax.swing.GroupLayout(addNewUserPanel);
        addNewUserPanel.setLayout(addNewUserPanelLayout);
        addNewUserPanelLayout.setHorizontalGroup(
            addNewUserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addNewUserButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        addNewUserPanelLayout.setVerticalGroup(
            addNewUserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addNewUserButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
        );

        manageUsersPanel.setBackground(new java.awt.Color(212, 212, 212));
        manageUsersPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                manageUsersPanelMouseClicked(evt);
            }
        });

        manageUsersButton.setBackground(new java.awt.Color(212, 212, 212));
        manageUsersButton.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 14)); // NOI18N
        manageUsersButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        manageUsersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-select-users-35.png"))); // NOI18N
        manageUsersButton.setText(" Manage Users");
        manageUsersButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                manageUsersButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                manageUsersButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                manageUsersButtonMouseExited(evt);
            }
        });

        javax.swing.GroupLayout manageUsersPanelLayout = new javax.swing.GroupLayout(manageUsersPanel);
        manageUsersPanel.setLayout(manageUsersPanelLayout);
        manageUsersPanelLayout.setHorizontalGroup(
            manageUsersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(manageUsersButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        manageUsersPanelLayout.setVerticalGroup(
            manageUsersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(manageUsersButton, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
        );

        logoutPanel.setBackground(new java.awt.Color(212, 212, 212));
        logoutPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logoutPanelMouseClicked(evt);
            }
        });

        logoutButton.setBackground(new java.awt.Color(212, 212, 212));
        logoutButton.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 12)); // NOI18N
        logoutButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logoutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-log-out-20.png"))); // NOI18N
        logoutButton.setText("Log Out");
        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logoutButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButtonMouseExited(evt);
            }
        });

        javax.swing.GroupLayout logoutPanelLayout = new javax.swing.GroupLayout(logoutPanel);
        logoutPanel.setLayout(logoutPanelLayout);
        logoutPanelLayout.setHorizontalGroup(
            logoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(logoutButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        logoutPanelLayout.setVerticalGroup(
            logoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(logoutButton, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
        );

        reservedBooksPanel.setBackground(new java.awt.Color(212, 212, 212));
        reservedBooksPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                reservedBooksPanelMouseClicked(evt);
            }
        });

        reservedBooksButton.setBackground(new java.awt.Color(212, 212, 212));
        reservedBooksButton.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 14)); // NOI18N
        reservedBooksButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        reservedBooksButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-reservedbooks-35 (2).png"))); // NOI18N
        reservedBooksButton.setText(" Reserved Books");
        reservedBooksButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                reservedBooksButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                reservedBooksButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                reservedBooksButtonMouseExited(evt);
            }
        });

        javax.swing.GroupLayout reservedBooksPanelLayout = new javax.swing.GroupLayout(reservedBooksPanel);
        reservedBooksPanel.setLayout(reservedBooksPanelLayout);
        reservedBooksPanelLayout.setHorizontalGroup(
            reservedBooksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(reservedBooksButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        reservedBooksPanelLayout.setVerticalGroup(
            reservedBooksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(reservedBooksButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
        );

        viewAllBooksPanel.setBackground(new java.awt.Color(212, 212, 212));
        viewAllBooksPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                viewAllBooksPanelMouseClicked(evt);
            }
        });

        viewAllBooksButton.setBackground(new java.awt.Color(212, 212, 212));
        viewAllBooksButton.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 14)); // NOI18N
        viewAllBooksButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        viewAllBooksButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-view-all-35.png"))); // NOI18N
        viewAllBooksButton.setText("View All Books");
        viewAllBooksButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                viewAllBooksButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                viewAllBooksButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                viewAllBooksButtonMouseExited(evt);
            }
        });

        javax.swing.GroupLayout viewAllBooksPanelLayout = new javax.swing.GroupLayout(viewAllBooksPanel);
        viewAllBooksPanel.setLayout(viewAllBooksPanelLayout);
        viewAllBooksPanelLayout.setHorizontalGroup(
            viewAllBooksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewAllBooksButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        viewAllBooksPanelLayout.setVerticalGroup(
            viewAllBooksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewAllBooksButton, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dashboardPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(addNewBookPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(searchBooksPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(logoutPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(reservedBooksPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(addNewUserPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(manageUsersPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel2)
                .addContainerGap(16, Short.MAX_VALUE))
            .addComponent(viewAllBooksPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel2)
                .addGap(30, 30, 30)
                .addComponent(dashboardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addNewBookPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewAllBooksPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchBooksPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reservedBooksPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addNewUserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(manageUsersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(logoutPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
        );

        jPanel2.setBackground(new java.awt.Color(242, 242, 242));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(242, 242, 242));

        jPanel12.setBackground(new java.awt.Color(242, 242, 242));
        jPanel12.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-books-100.png"))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 14)); // NOI18N
        jLabel1.setText("Total Books");

        totalBooksLabel.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 36)); // NOI18N
        totalBooksLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        totalBooksLabel.setText("69");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel18))
                .addContainerGap(51, Short.MAX_VALUE))
            .addComponent(totalBooksLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(totalBooksLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel13.setBackground(new java.awt.Color(242, 242, 242));
        jPanel13.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        totalUsersLabel.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 36)); // NOI18N
        totalUsersLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        totalUsersLabel.setText("15");

        jLabel21.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 14)); // NOI18N
        jLabel21.setText("Total Users");

        jLabel22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-users-100.png"))); // NOI18N

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                .addContainerGap(45, Short.MAX_VALUE)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel22)
                    .addComponent(jLabel21))
                .addGap(53, 53, 53))
            .addComponent(totalUsersLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalUsersLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel21)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel14.setBackground(new java.awt.Color(242, 242, 242));
        jPanel14.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel27.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 36)); // NOI18N
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel27.setText("--");

        jLabel28.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 14)); // NOI18N
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setText("TBA");

        jLabel26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-overhead-crane-100.png"))); // NOI18N

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addComponent(jLabel26)
                .addContainerGap(52, Short.MAX_VALUE))
            .addComponent(jLabel28, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel27, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel28)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jPanel15.setBackground(new java.awt.Color(242, 242, 242));
        jPanel15.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        totalReservationsLabel.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 36)); // NOI18N
        totalReservationsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        totalReservationsLabel.setText("69");

        jLabel24.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 14)); // NOI18N
        jLabel24.setText("Reserved Books");

        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-books-90.png"))); // NOI18N

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap(46, Short.MAX_VALUE)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jLabel25)))
                .addGap(40, 40, 40))
            .addComponent(totalReservationsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalReservationsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel24)
                .addGap(14, 14, 14))
        );

        jLabel17.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 30)); // NOI18N
        jLabel17.setText("DASHBOARD/Admin");

        dashboardRefreshPanelButton.setBackground(new java.awt.Color(242, 242, 242));
        dashboardRefreshPanelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dashboardRefreshPanelButtonMouseClicked(evt);
            }
        });

        jLabel29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-repeat-43.png"))); // NOI18N

        javax.swing.GroupLayout dashboardRefreshPanelButtonLayout = new javax.swing.GroupLayout(dashboardRefreshPanelButton);
        dashboardRefreshPanelButton.setLayout(dashboardRefreshPanelButtonLayout);
        dashboardRefreshPanelButtonLayout.setHorizontalGroup(
            dashboardRefreshPanelButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dashboardRefreshPanelButtonLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel29)
                .addGap(47, 47, 47))
        );
        dashboardRefreshPanelButtonLayout.setVerticalGroup(
            dashboardRefreshPanelButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dashboardRefreshPanelButtonLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel29)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(84, 84, 84)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addGap(118, 118, 118)
                        .addComponent(dashboardRefreshPanelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(43, 43, 43)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(565, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(74, 74, 74)
                        .addComponent(jLabel17)
                        .addGap(29, 29, 29))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(dashboardRefreshPanelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(55, 55, 55)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(127, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab1", jPanel3);

        jPanel4.setBackground(new java.awt.Color(242, 242, 242));

        jLabel9.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 30)); // NOI18N
        jLabel9.setText("Add New Book?");

        inputAuthorField.setBackground(new java.awt.Color(224, 223, 223));

        jLabel30.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel30.setText("Enter Book Title:");

        jLabel31.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel31.setText("Enter Book Author:");

        inputTitleField.setBackground(new java.awt.Color(224, 223, 223));

        jLabel32.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel32.setText("Available to borrow?");

        checkBoxAvailability.setMaximumSize(new java.awt.Dimension(50, 50));
        checkBoxAvailability.setMinimumSize(new java.awt.Dimension(50, 50));

        addBookPanelButton.setBackground(new java.awt.Color(78, 175, 253));
        addBookPanelButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addBookPanelButton.setPreferredSize(new java.awt.Dimension(97, 53));
        addBookPanelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addBookPanelButtonMouseClicked(evt);
            }
        });

        jLabel52.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(51, 51, 51));
        jLabel52.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel52.setText("Add Book");

        javax.swing.GroupLayout addBookPanelButtonLayout = new javax.swing.GroupLayout(addBookPanelButton);
        addBookPanelButton.setLayout(addBookPanelButtonLayout);
        addBookPanelButtonLayout.setHorizontalGroup(
            addBookPanelButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel52, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
        );
        addBookPanelButtonLayout.setVerticalGroup(
            addBookPanelButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel52, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(121, 121, 121)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addBookPanelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(inputAuthorField, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                        .addComponent(jLabel9)
                        .addComponent(jLabel30)
                        .addComponent(jLabel31)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel32)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(checkBoxAvailability, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(inputTitleField)))
                .addContainerGap(560, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(111, 111, 111)
                .addComponent(jLabel9)
                .addGap(36, 36, 36)
                .addComponent(jLabel30)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inputTitleField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inputAuthorField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel32)
                    .addComponent(checkBoxAvailability, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addComponent(addBookPanelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(208, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab2", jPanel4);

        jPanel5.setBackground(new java.awt.Color(242, 242, 242));

        jLabel12.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 24)); // NOI18N
        jLabel12.setText("View All Books");

        jTableBooks.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 12)); // NOI18N
        jTableBooks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Book ID", "Title", "Author", "Availability"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTableBooks);
        if (jTableBooks.getColumnModel().getColumnCount() > 0) {
            jTableBooks.getColumnModel().getColumn(0).setResizable(false);
            jTableBooks.getColumnModel().getColumn(0).setHeaderValue("Book ID");
            jTableBooks.getColumnModel().getColumn(1).setResizable(false);
            jTableBooks.getColumnModel().getColumn(1).setHeaderValue("Title");
            jTableBooks.getColumnModel().getColumn(2).setResizable(false);
            jTableBooks.getColumnModel().getColumn(2).setHeaderValue("Author");
            jTableBooks.getColumnModel().getColumn(3).setResizable(false);
            jTableBooks.getColumnModel().getColumn(3).setHeaderValue("Availability");
        }

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 571, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(85, 85, 85)
                        .addComponent(jLabel12)))
                .addContainerGap(459, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 556, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab3", jPanel5);

        jPanel6.setBackground(new java.awt.Color(242, 242, 242));

        searchBookField.setBackground(new java.awt.Color(212, 212, 212));

        titleFieldS.setBackground(new java.awt.Color(212, 212, 212));

        searchBookButton.setBackground(new java.awt.Color(212, 212, 212));
        searchBookButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchBookButtonMouseClicked(evt);
            }
        });

        jLabel33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons8-search-35 (1).png"))); // NOI18N

        javax.swing.GroupLayout searchBookButtonLayout = new javax.swing.GroupLayout(searchBookButton);
        searchBookButton.setLayout(searchBookButtonLayout);
        searchBookButtonLayout.setHorizontalGroup(
            searchBookButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 47, Short.MAX_VALUE)
            .addGroup(searchBookButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(searchBookButtonLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel33)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        searchBookButtonLayout.setVerticalGroup(
            searchBookButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(searchBookButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(searchBookButtonLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        authorFieldS.setBackground(new java.awt.Color(212, 212, 212));

        jLabel35.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel35.setText("Search Here:");

        jLabel36.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel36.setText("Author:");

        jLabel37.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel37.setText("Availability:");

        updateBookButton.setBackground(new java.awt.Color(78, 175, 253));
        updateBookButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        updateBookButton.setPreferredSize(new java.awt.Dimension(197, 53));
        updateBookButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                updateBookButtonMouseClicked(evt);
            }
        });

        jLabel40.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 14)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(51, 51, 51));
        jLabel40.setText("Update Book Details");

        javax.swing.GroupLayout updateBookButtonLayout = new javax.swing.GroupLayout(updateBookButton);
        updateBookButton.setLayout(updateBookButtonLayout);
        updateBookButtonLayout.setHorizontalGroup(
            updateBookButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, updateBookButtonLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel40)
                .addGap(21, 21, 21))
        );
        updateBookButtonLayout.setVerticalGroup(
            updateBookButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updateBookButtonLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel40)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        reserveBookButton.setBackground(new java.awt.Color(78, 175, 253));
        reserveBookButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        reserveBookButton.setPreferredSize(new java.awt.Dimension(97, 53));
        reserveBookButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                reserveBookButtonMouseClicked(evt);
            }
        });

        jLabel41.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 14)); // NOI18N
        jLabel41.setForeground(new java.awt.Color(51, 51, 51));
        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel41.setText("Reserve this Book");

        javax.swing.GroupLayout reserveBookButtonLayout = new javax.swing.GroupLayout(reserveBookButton);
        reserveBookButton.setLayout(reserveBookButtonLayout);
        reserveBookButtonLayout.setHorizontalGroup(
            reserveBookButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel41, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
        );
        reserveBookButtonLayout.setVerticalGroup(
            reserveBookButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reserveBookButtonLayout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addComponent(jLabel41)
                .addGap(14, 14, 14))
        );

        issueBookButton.setBackground(new java.awt.Color(78, 175, 253));
        issueBookButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        issueBookButton.setPreferredSize(new java.awt.Dimension(197, 53));
        issueBookButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                issueBookButtonMouseClicked(evt);
            }
        });

        jLabel42.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 14)); // NOI18N
        jLabel42.setForeground(new java.awt.Color(51, 51, 51));
        jLabel42.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel42.setText("Issue this Book");

        javax.swing.GroupLayout issueBookButtonLayout = new javax.swing.GroupLayout(issueBookButton);
        issueBookButton.setLayout(issueBookButtonLayout);
        issueBookButtonLayout.setHorizontalGroup(
            issueBookButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel42, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
        );
        issueBookButtonLayout.setVerticalGroup(
            issueBookButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(issueBookButtonLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel42)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jLabel43.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel43.setText("Title:");

        deleteBookButton.setBackground(new java.awt.Color(207, 48, 12));
        deleteBookButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        deleteBookButton.setPreferredSize(new java.awt.Dimension(97, 53));
        deleteBookButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteBookButtonMouseClicked(evt);
            }
        });

        jLabel53.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 14)); // NOI18N
        jLabel53.setForeground(new java.awt.Color(204, 204, 204));
        jLabel53.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel53.setText("Delete this Book");

        javax.swing.GroupLayout deleteBookButtonLayout = new javax.swing.GroupLayout(deleteBookButton);
        deleteBookButton.setLayout(deleteBookButtonLayout);
        deleteBookButtonLayout.setHorizontalGroup(
            deleteBookButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel53, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
        );
        deleteBookButtonLayout.setVerticalGroup(
            deleteBookButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, deleteBookButtonLayout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addComponent(jLabel53)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(101, 101, 101)
                        .addComponent(searchBookField, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(searchBookButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(122, 122, 122)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(titleFieldS, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel36)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel37)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(availabilityCheckBoxS))
                            .addComponent(jLabel43)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(jLabel35))
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                    .addComponent(reserveBookButton, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(deleteBookButton, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addComponent(updateBookButton, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                    .addGap(18, 18, 18)
                                    .addComponent(issueBookButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(authorFieldS, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(533, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addComponent(jLabel35)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(searchBookButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(searchBookField, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
                .addGap(65, 65, 65)
                .addComponent(jLabel43)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(titleFieldS, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel36)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(authorFieldS, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(37, 37, 37)
                        .addComponent(jLabel37))
                    .addComponent(availabilityCheckBoxS))
                .addGap(36, 36, 36)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(updateBookButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(issueBookButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reserveBookButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteBookButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(113, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab4", jPanel6);

        jPanel7.setBackground(new java.awt.Color(242, 242, 242));

        jLabel14.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 24)); // NOI18N
        jLabel14.setText("Reserved Books");

        jScrollPane2.setBackground(new java.awt.Color(212, 212, 212));

        reservedBooksTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Reservation ID", "User ID", "Book ID", "Reservation Date", "Expiry Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(reservedBooksTable);
        if (reservedBooksTable.getColumnModel().getColumnCount() > 0) {
            reservedBooksTable.getColumnModel().getColumn(0).setResizable(false);
            reservedBooksTable.getColumnModel().getColumn(1).setResizable(false);
            reservedBooksTable.getColumnModel().getColumn(2).setResizable(false);
            reservedBooksTable.getColumnModel().getColumn(3).setResizable(false);
        }

        deleteReservationButton.setBackground(new java.awt.Color(207, 48, 12));
        deleteReservationButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteReservationButtonMouseClicked(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 14)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Remove Reservation");

        javax.swing.GroupLayout deleteReservationButtonLayout = new javax.swing.GroupLayout(deleteReservationButton);
        deleteReservationButton.setLayout(deleteReservationButtonLayout);
        deleteReservationButtonLayout.setHorizontalGroup(
            deleteReservationButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
        );
        deleteReservationButtonLayout.setVerticalGroup(
            deleteReservationButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                            .addGap(413, 413, 413)
                            .addComponent(deleteReservationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel7Layout.createSequentialGroup()
                            .addGap(72, 72, 72)
                            .addComponent(jLabel14))))
                .addContainerGap(449, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 472, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(deleteReservationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(82, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab5", jPanel7);

        jPanel8.setBackground(new java.awt.Color(242, 242, 242));

        jLabel44.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 1, 30)); // NOI18N
        jLabel44.setText("ADD NEW USER?");

        jLabel45.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel45.setText("Enter username:");

        addUsernameField.setBackground(new java.awt.Color(224, 223, 223));

        jLabel46.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel46.setText("Enter password:");

        addPasswordField.setBackground(new java.awt.Color(224, 223, 223));

        jLabel47.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel47.setText("Enter email:");

        jLabel48.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel48.setText("Admin privileges?");

        addUserButton.setBackground(new java.awt.Color(78, 175, 253));
        addUserButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addUserButton.setPreferredSize(new java.awt.Dimension(97, 53));
        addUserButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addUserButtonMouseClicked(evt);
            }
        });

        jLabel49.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(51, 51, 51));
        jLabel49.setText("Add user");

        javax.swing.GroupLayout addUserButtonLayout = new javax.swing.GroupLayout(addUserButton);
        addUserButton.setLayout(addUserButtonLayout);
        addUserButtonLayout.setHorizontalGroup(
            addUserButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addUserButtonLayout.createSequentialGroup()
                .addContainerGap(56, Short.MAX_VALUE)
                .addComponent(jLabel49)
                .addGap(49, 49, 49))
        );
        addUserButtonLayout.setVerticalGroup(
            addUserButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addUserButtonLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel49, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE))
        );

        addEmailField.setBackground(new java.awt.Color(224, 223, 223));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(138, 138, 138)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addUserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel44)
                        .addComponent(jLabel45)
                        .addComponent(addUsernameField, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
                        .addComponent(jLabel46)
                        .addComponent(addPasswordField)
                        .addComponent(jLabel47)
                        .addComponent(addEmailField, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(jLabel48)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(addIsAdminCheckBox))))
                .addContainerGap(525, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(87, 87, 87)
                .addComponent(jLabel44)
                .addGap(24, 24, 24)
                .addComponent(jLabel45)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(jLabel46)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39)
                .addComponent(jLabel47)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(addEmailField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel48))
                    .addComponent(addIsAdminCheckBox))
                .addGap(33, 33, 33)
                .addComponent(addUserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(137, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab6", jPanel8);

        jPanel9.setBackground(new java.awt.Color(242, 242, 242));

        jLabel16.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 24)); // NOI18N
        jLabel16.setText("Manage Users");

        userTable.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 12)); // NOI18N
        userTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "User ID", "Username", "Password", "Email", "Admin Priv"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(userTable);
        if (userTable.getColumnModel().getColumnCount() > 0) {
            userTable.getColumnModel().getColumn(0).setResizable(false);
            userTable.getColumnModel().getColumn(4).setResizable(false);
        }

        updateUserButton.setBackground(new java.awt.Color(78, 175, 253));
        updateUserButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        updateUserButton.setPreferredSize(new java.awt.Dimension(97, 53));
        updateUserButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                updateUserButtonMouseClicked(evt);
            }
        });

        jLabel50.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel50.setForeground(new java.awt.Color(51, 51, 51));
        jLabel50.setText("Update Details");

        javax.swing.GroupLayout updateUserButtonLayout = new javax.swing.GroupLayout(updateUserButton);
        updateUserButton.setLayout(updateUserButtonLayout);
        updateUserButtonLayout.setHorizontalGroup(
            updateUserButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updateUserButtonLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel50)
                .addContainerGap(17, Short.MAX_VALUE))
        );
        updateUserButtonLayout.setVerticalGroup(
            updateUserButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, updateUserButtonLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel50, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE))
        );

        deleteUserButton.setBackground(new java.awt.Color(212, 59, 26));
        deleteUserButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        deleteUserButton.setPreferredSize(new java.awt.Dimension(97, 53));
        deleteUserButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteUserButtonMouseClicked(evt);
            }
        });

        jLabel51.setFont(new java.awt.Font("Bitstream Vera Sans Mono", 0, 18)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(204, 204, 204));
        jLabel51.setText("Delete User");

        javax.swing.GroupLayout deleteUserButtonLayout = new javax.swing.GroupLayout(deleteUserButton);
        deleteUserButton.setLayout(deleteUserButtonLayout);
        deleteUserButtonLayout.setHorizontalGroup(
            deleteUserButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(deleteUserButtonLayout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(jLabel51)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        deleteUserButtonLayout.setVerticalGroup(
            deleteUserButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, deleteUserButtonLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel51, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 581, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(122, 122, 122)
                        .addComponent(deleteUserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(55, 55, 55)
                        .addComponent(updateUserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(jLabel16)))
                .addContainerGap(468, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(updateUserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteUserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49))
        );

        jTabbedPane1.addTab("tab7", jPanel9);

        jPanel2.add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -47, -1, 740));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void dashboardPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardPanelMouseClicked
        // TODO add your handling code here:
//        jTabbedPane1.setSelectedIndex(0);
//        updateDashboardCounts();
    }//GEN-LAST:event_dashboardPanelMouseClicked

    private void addNewBookPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addNewBookPanelMouseClicked
        // TODO add your handling code here:
//        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_addNewBookPanelMouseClicked

    private void searchBooksPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchBooksPanelMouseClicked
        // TODO add your handling code here:
        //  jTabbedPane1.setSelectedIndex(3);
    }//GEN-LAST:event_searchBooksPanelMouseClicked

    private void addNewUserPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addNewUserPanelMouseClicked
        // TODO add your handling code here:
        // jTabbedPane1.setSelectedIndex(5);
    }//GEN-LAST:event_addNewUserPanelMouseClicked

    private void manageUsersPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_manageUsersPanelMouseClicked
        // TODO add your handling code here:
//        jTabbedPane1.setSelectedIndex(6);
//        initManageUsersTab();
    }//GEN-LAST:event_manageUsersPanelMouseClicked

    private void logoutPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutPanelMouseClicked
        // TODO add your handling code here:
//        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
//
//        if (response == JOptionPane.YES_OPTION) {
//            LoginFrame loginFrame = new LoginFrame();
//            loginFrame.setVisible(true);
//            this.setVisible(false);
//        }

    }//GEN-LAST:event_logoutPanelMouseClicked

    private void reservedBooksPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reservedBooksPanelMouseClicked
        // TODO add your handling code here:
//        jTabbedPane1.setSelectedIndex(4);
//        loadReservedBooks();
    }//GEN-LAST:event_reservedBooksPanelMouseClicked

    private void dashboardRefreshPanelButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardRefreshPanelButtonMouseClicked
        // TODO add your handling code here:
        updateDashboardCounts();
    }//GEN-LAST:event_dashboardRefreshPanelButtonMouseClicked

    private void addBookPanelButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addBookPanelButtonMouseClicked
        // TODO add your handling code here:
        String title = inputTitleField.getText();
        String author = inputAuthorField.getText();
        boolean availability = checkBoxAvailability.isSelected();

        boolean result = DatabaseUtils.addBook(title, author, availability);
        if (result) {
            JOptionPane.showMessageDialog(this, "Book added successfully!");
            inputTitleField.setText("");
            inputAuthorField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Error adding book", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_addBookPanelButtonMouseClicked

    private void searchBookButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchBookButtonMouseClicked
        // TODO add your handling code here:
        searchBook();
    }//GEN-LAST:event_searchBookButtonMouseClicked

    private void updateBookButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateBookButtonMouseClicked
        // TODO add your handling code here:
        updateBook();
    }//GEN-LAST:event_updateBookButtonMouseClicked

    private void issueBookButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_issueBookButtonMouseClicked
        // TODO add your handling code here:
        issueBook();
    }//GEN-LAST:event_issueBookButtonMouseClicked

    private void reserveBookButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reserveBookButtonMouseClicked
        // TODO add your handling code here:
        reserveBook();
    }//GEN-LAST:event_reserveBookButtonMouseClicked

    private void deleteReservationButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteReservationButtonMouseClicked
        // TODO add your handling code here:
        int selectedRow = reservedBooksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reservation to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int reservationId = (int) reservedBooksTable.getValueAt(selectedRow, 0); // Assuming the reservation ID is in the first column
        boolean success = DatabaseUtils.deleteReservation(reservationId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Reservation deleted successfully.");
            loadReservedBooks();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete reservation.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_deleteReservationButtonMouseClicked

    private void addUserButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addUserButtonMouseClicked
        // TODO add your handling code here:
        // Retrieve user details from the form
        String username = addUsernameField.getText();
        String password = new String(addPasswordField.getPassword());
        String email = addEmailField.getText();
        boolean isAdmin = addIsAdminCheckBox.isSelected();

        // Call the DatabaseUtils method to add the user
        boolean success = DatabaseUtils.addUser(username, password, email, isAdmin);
        if (success) {
            JOptionPane.showMessageDialog(this, "User added successfully!");
            loadUsers(); // Refresh the user list
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_addUserButtonMouseClicked

    private void deleteUserButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteUserButtonMouseClicked
        // TODO add your handling code here:
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow != -1) {
            // Retrieve the user_id of the selected user
            int userId = (int) userTable.getValueAt(selectedRow, 0);

            // Call the deleteUser method
            boolean success = DatabaseUtils.deleteUser(userId);
            if (success) {
                // Remove the row from the table model
                DefaultTableModel model = (DefaultTableModel) userTable.getModel();
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "User deleted successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
        }
    }//GEN-LAST:event_deleteUserButtonMouseClicked

    private void updateUserButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateUserButtonMouseClicked
        // TODO add your handling code here:
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow != -1) {
            // Retrieve updated user details from the table
            int userId = (int) userTable.getValueAt(selectedRow, 0);
            String username = (String) userTable.getValueAt(selectedRow, 1);
            String password = (String) userTable.getValueAt(selectedRow, 2);
            String email = (String) userTable.getValueAt(selectedRow, 3);
            boolean isAdmin = (boolean) userTable.getValueAt(selectedRow, 4);

            // Create a User object with the updated details
            User user = new User(userId, username, password, email, isAdmin);

            // Call the updateUser method
            boolean success = DatabaseUtils.updateUser(user);
            if (success) {
                JOptionPane.showMessageDialog(this, "User updated successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to update.");
        }
    }//GEN-LAST:event_updateUserButtonMouseClicked

    private void dashboardButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardButtonMouseEntered
        // TODO add your handling code here:
        dashboardPanel.setBackground(new Color(242, 242, 242));
    }//GEN-LAST:event_dashboardButtonMouseEntered

    private void dashboardPanelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardPanelMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_dashboardPanelMouseExited

    private void dashboardPanelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardPanelMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_dashboardPanelMouseEntered

    private void dashboardButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardButtonMouseExited
        // TODO add your handling code here:
        dashboardPanel.setBackground(new Color(212, 212, 212));
    }//GEN-LAST:event_dashboardButtonMouseExited

    private void dashboardButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardButtonMouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(0);
        updateDashboardCounts();
    }//GEN-LAST:event_dashboardButtonMouseClicked

    private void addNewBookButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addNewBookButtonMouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_addNewBookButtonMouseClicked

    private void addNewBookButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addNewBookButtonMouseEntered
        // TODO add your handling code here:
        addNewBookPanel.setBackground(new Color(242, 242, 242));
    }//GEN-LAST:event_addNewBookButtonMouseEntered

    private void addNewBookButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addNewBookButtonMouseExited
        // TODO add your handling code here:
        addNewBookPanel.setBackground(new Color(212, 212, 212));
    }//GEN-LAST:event_addNewBookButtonMouseExited

    private void viewAllBooksButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewAllBooksButtonMouseClicked
        // TODO add your handling code here:

        jTabbedPane1.setSelectedIndex(2);

        List<Book> books = DatabaseUtils.getAllBooks();
        DefaultTableModel model = (DefaultTableModel) jTableBooks.getModel();
        model.setRowCount(0); // Clear existing rows

        for (Book book : books) {
            model.addRow(new Object[]{book.getBookId(), book.getTitle(), book.getAuthor(), book.isAvailable()});
        }

    }//GEN-LAST:event_viewAllBooksButtonMouseClicked

    private void viewAllBooksButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewAllBooksButtonMouseEntered
        // TODO add your handling code here:

        viewAllBooksButton.setBackground(new Color(242, 242, 242));

    }//GEN-LAST:event_viewAllBooksButtonMouseEntered

    private void viewAllBooksButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewAllBooksButtonMouseExited
        // TODO add your handling code here:
        viewAllBooksButton.setBackground(new Color(212, 212, 212));

    }//GEN-LAST:event_viewAllBooksButtonMouseExited

    private void viewAllBooksPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewAllBooksPanelMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_viewAllBooksPanelMouseClicked

    private void searchBooksButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchBooksButtonMouseEntered
        // TODO add your handling code here:
        searchBooksButton.setBackground(new Color(242, 242, 242));
    }//GEN-LAST:event_searchBooksButtonMouseEntered

    private void searchBooksButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchBooksButtonMouseExited
        // TODO add your handling code here:
        searchBooksButton.setBackground(new Color(212, 212, 212));
    }//GEN-LAST:event_searchBooksButtonMouseExited

    private void searchBooksButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchBooksButtonMouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(3);
    }//GEN-LAST:event_searchBooksButtonMouseClicked

    private void reservedBooksButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reservedBooksButtonMouseEntered
        // TODO add your handling code here:
        reservedBooksButton.setBackground(new Color(242, 242, 242));
    }//GEN-LAST:event_reservedBooksButtonMouseEntered

    private void reservedBooksButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reservedBooksButtonMouseExited
        // TODO add your handling code here:
        reservedBooksButton.setBackground(new Color(212, 212, 212));
    }//GEN-LAST:event_reservedBooksButtonMouseExited

    private void reservedBooksButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reservedBooksButtonMouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(4);
        loadReservedBooks();
    }//GEN-LAST:event_reservedBooksButtonMouseClicked

    private void addNewUserButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addNewUserButtonMouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(5);
    }//GEN-LAST:event_addNewUserButtonMouseClicked

    private void addNewUserButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addNewUserButtonMouseEntered
        // TODO add your handling code here:
        addNewUserButton.setBackground(new Color(242, 242, 242));
    }//GEN-LAST:event_addNewUserButtonMouseEntered

    private void addNewUserButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addNewUserButtonMouseExited
        // TODO add your handling code here:
        addNewUserButton.setBackground(new Color(212, 212, 212));
    }//GEN-LAST:event_addNewUserButtonMouseExited

    private void manageUsersButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_manageUsersButtonMouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(6);
        initManageUsersTab();
    }//GEN-LAST:event_manageUsersButtonMouseClicked

    private void manageUsersButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_manageUsersButtonMouseEntered
        // TODO add your handling code here:
        manageUsersButton.setBackground(new Color(242, 242, 242));
    }//GEN-LAST:event_manageUsersButtonMouseEntered

    private void manageUsersButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_manageUsersButtonMouseExited
        // TODO add your handling code here:
        manageUsersButton.setBackground(new Color(212, 212, 212));
    }//GEN-LAST:event_manageUsersButtonMouseExited

    private void logoutButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutButtonMouseClicked
        // TODO add your handling code here:
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            this.setVisible(false);
        }
    }//GEN-LAST:event_logoutButtonMouseClicked

    private void logoutButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutButtonMouseEntered
        // TODO add your handling code here:
        logoutButton.setBackground(new Color(242, 242, 242));
    }//GEN-LAST:event_logoutButtonMouseEntered

    private void logoutButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutButtonMouseExited
        // TODO add your handling code here:
        logoutButton.setBackground(new Color(212, 212, 212));

    }//GEN-LAST:event_logoutButtonMouseExited

    private void deleteBookButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteBookButtonMouseClicked
        // TODO add your handling code here:
        String title = titleFieldS.getText();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please search for a book first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this book?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = DatabaseUtils.deleteBookByTitle(title);
            if (success) {
                JOptionPane.showMessageDialog(this, "Book deleted successfully.");
                clearSearchFields(); // Optional: Clear the form fields after deletion
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_deleteBookButtonMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AdminFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        java.awt.EventQueue.invokeLater(new Runnable() {

            User testUser = new User(1, "admin", "password123", "admin@gmail.com", true);

            public void run() {
                new AdminFrame(testUser).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel addBookPanelButton;
    private javax.swing.JTextField addEmailField;
    private javax.swing.JCheckBox addIsAdminCheckBox;
    private javax.swing.JLabel addNewBookButton;
    private javax.swing.JPanel addNewBookPanel;
    private javax.swing.JLabel addNewUserButton;
    private javax.swing.JPanel addNewUserPanel;
    private javax.swing.JPasswordField addPasswordField;
    private javax.swing.JPanel addUserButton;
    private javax.swing.JTextField addUsernameField;
    private javax.swing.JTextField authorFieldS;
    private javax.swing.JCheckBox availabilityCheckBoxS;
    private javax.swing.JCheckBox checkBoxAvailability;
    private javax.swing.JLabel dashboardButton;
    private javax.swing.JPanel dashboardPanel;
    private javax.swing.JPanel dashboardRefreshPanelButton;
    private javax.swing.JPanel deleteBookButton;
    private javax.swing.JPanel deleteReservationButton;
    private javax.swing.JPanel deleteUserButton;
    private javax.swing.JTextField inputAuthorField;
    private javax.swing.JTextField inputTitleField;
    private javax.swing.JPanel issueBookButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableBooks;
    private javax.swing.JLabel logoutButton;
    private javax.swing.JPanel logoutPanel;
    private javax.swing.JLabel manageUsersButton;
    private javax.swing.JPanel manageUsersPanel;
    private javax.swing.JPanel reserveBookButton;
    private javax.swing.JLabel reservedBooksButton;
    private javax.swing.JPanel reservedBooksPanel;
    private javax.swing.JTable reservedBooksTable;
    private javax.swing.JPanel searchBookButton;
    private javax.swing.JTextField searchBookField;
    private javax.swing.JLabel searchBooksButton;
    private javax.swing.JPanel searchBooksPanel;
    private javax.swing.JTextField titleFieldS;
    private javax.swing.JLabel totalBooksLabel;
    private javax.swing.JLabel totalReservationsLabel;
    private javax.swing.JLabel totalUsersLabel;
    private javax.swing.JPanel updateBookButton;
    private javax.swing.JPanel updateUserButton;
    private javax.swing.JTable userTable;
    private javax.swing.JLabel viewAllBooksButton;
    private javax.swing.JPanel viewAllBooksPanel;
    // End of variables declaration//GEN-END:variables
}
