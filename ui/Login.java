package ui;

import db.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Login Screen
 *
 * HOW YOUR LOGIN WORKS WITH YOUR EXACT DATABASE:
 * ─────────────────────────────────────────────────────────
 *  users table has: student/123 (Student) | warden/123 (Warden)
 *  students table:  id=1 Amit (amit@gmail.com)
 *                   id=2 Neha (neha@gmail.com)
 *                   id=3 Rahul (rahul@gmail.com)
 *
 *  Login flow:
 *  1. Authenticate: users WHERE username=? AND password=?
 *  2. If role=Student → show dropdown to pick WHICH student you are
 *  3. If role=Warden  → go straight to WardenDashboard
 * ─────────────────────────────────────────────────────────
 *  Credentials to use RIGHT NOW (no DB changes needed):
 *  Student: username=student  password=123
 *  Warden:  username=warden   password=123
 */
public class Login extends JFrame {

    // ── colours ──────────────────────────────────────────────────────────────
    private static final Color BG1       = new Color(10,  20,  50);
    private static final Color BG2       = new Color(20,  50, 105);
    private static final Color CARD_BG   = new Color(248, 251, 255);
    private static final Color CARD_BDR  = new Color(210, 224, 248);
    private static final Color BTN_BG    = new Color(37,  99, 235);
    private static final Color BTN_HOV   = new Color(29,  78, 216);
    private static final Color FLD_BG    = new Color(238, 244, 255);
    private static final Color FLD_BDR   = new Color(196, 215, 248);
    private static final Color LBL_DARK  = new Color(30,  55, 110);
    private static final Color LBL_DIM   = new Color(100, 130, 180);
    private static final Color WHITE     = Color.WHITE;

    private JTextField    usernameField;
    private JPasswordField passwordField;
    private JButton        loginButton;

    public Login() {
        setTitle("Smart Hostel CMS — Login");
        setSize(440, 540);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // gradient background
        JPanel bg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,BG1,0,getHeight(),BG2));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        bg.setLayout(new GridBagLayout());
        bg.setBorder(new EmptyBorder(24,36,24,36));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx=0; g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1;

        // ── brand ─────────────────────────────────────────────────────────────
        JLabel ico = new JLabel("🏠", SwingConstants.CENTER);
        ico.setFont(new Font("Segoe UI Emoji",Font.PLAIN,52));
        g.gridy=0; g.insets=new Insets(0,0,2,0);
        bg.add(ico,g);

        JLabel h1 = new JLabel("Smart Hostel CMS", SwingConstants.CENTER);
        h1.setFont(new Font("Segoe UI",Font.BOLD,21));
        h1.setForeground(WHITE);
        g.gridy=1; g.insets=new Insets(0,0,3,0);
        bg.add(h1,g);

        JLabel h2 = new JLabel("Complaint Management System", SwingConstants.CENTER);
        h2.setFont(new Font("Segoe UI",Font.PLAIN,12));
        h2.setForeground(new Color(180,205,240));
        g.gridy=2; g.insets=new Insets(0,0,20,0);
        bg.add(h2,g);

        // ── card ──────────────────────────────────────────────────────────────
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BDR,1,true),
            new EmptyBorder(28,28,28,28)));

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx=0; fc.fill=GridBagConstraints.HORIZONTAL; fc.weightx=1;

        // hint
        JLabel hint = new JLabel("<html><center>Student: <b>student / 123</b> &nbsp;|&nbsp; Warden: <b>warden / 123</b></center></html>");
        hint.setFont(new Font("Segoe UI",Font.PLAIN,11));
        hint.setForeground(LBL_DIM);
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        fc.gridy=0; fc.insets=new Insets(0,0,16,0);
        card.add(hint,fc);

        // username
        fc.gridy=1; fc.insets=new Insets(0,0,4,0);
        card.add(mkLabel("Username"),fc);

        usernameField = new JTextField();
        styleComp(usernameField);
        fc.gridy=2; fc.insets=new Insets(0,0,14,0);
        card.add(usernameField,fc);

        // password
        fc.gridy=3; fc.insets=new Insets(0,0,4,0);
        card.add(mkLabel("Password"),fc);

        passwordField = new JPasswordField();
        styleComp(passwordField);
        fc.gridy=4; fc.insets=new Insets(0,0,22,0);
        card.add(passwordField,fc);

        // button
        loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("Segoe UI",Font.BOLD,14));
        loginButton.setForeground(WHITE);
        loginButton.setBackground(BTN_BG);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setOpaque(true);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(360,44));
        loginButton.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){loginButton.setBackground(BTN_HOV);}
            public void mouseExited (MouseEvent e){loginButton.setBackground(BTN_BG); }
        });
        loginButton.addActionListener(e -> doLogin());
        fc.gridy=5; fc.insets=new Insets(0,0,0,0);
        card.add(loginButton,fc);

        g.gridy=3; g.insets=new Insets(0,0,0,0);
        bg.add(card,g);

        // footer
        JLabel foot = new JLabel("© 2026 Smart Hostel CMS",SwingConstants.CENTER);
        foot.setFont(new Font("Segoe UI",Font.PLAIN,10));
        foot.setForeground(new Color(130,155,200));
        g.gridy=4; g.insets=new Insets(14,0,0,0);
        bg.add(foot,g);

        add(bg);
        getRootPane().setDefaultButton(loginButton);
        setVisible(true);
    }

    // ── CORE LOGIN LOGIC ─────────────────────────────────────────────────────
    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            err("Please enter both username and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Connecting…");

        try (Connection conn = DBConnection.getConnection()) {

            // Step 1: authenticate
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, role FROM users WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                err("Invalid username or password.\n\nUse:\n  Student → username: student | password: 123\n  Warden  → username: warden  | password: 123");
                resetBtn(); return;
            }

            String role = rs.getString("role");
            dispose();

            if ("Warden".equalsIgnoreCase(role)) {
                new WardenDashboard("Warden");

            } else if ("Student".equalsIgnoreCase(role)) {
                // Step 2: since there is ONE generic "student" account,
                // let the user choose which student they are.
                selectStudentAndOpen(conn);
            }

        } catch (SQLException ex) {
            err("Database Error:\n" + ex.getMessage()
                + "\n\nCheck: DBConnection.java → PASSWORD field.");
            resetBtn();
        }
    }

    /**
     * Shows a student-picker dialog.
     * This is needed because your users table has a single "student" account
     * shared across Amit, Neha, Rahul. Each student picks themselves after login.
     */
    private void selectStudentAndOpen(Connection conn) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name, room_no FROM students ORDER BY id");
            ResultSet rs = ps.executeQuery();

            // build display items
            java.util.List<int[]>    ids   = new java.util.ArrayList<>();
            java.util.List<String>   names = new java.util.ArrayList<>();
            while (rs.next()) {
                ids.add(new int[]{rs.getInt("id")});
                names.add(rs.getString("name") + "  (Room: " + rs.getString("room_no") + ")");
            }

            if (ids.isEmpty()) {
                err("No students found in database.");
                new Login(); return;
            }

            String[] options = names.toArray(new String[0]);
            String choice = (String) JOptionPane.showInputDialog(
                null,
                "Select your student profile:",
                "Who are you?",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

            if (choice == null) { new Login(); return; } // cancelled

            int idx        = names.indexOf(choice);
            int studentId  = ids.get(idx)[0];
            String sName   = names.get(idx).split("  \\(")[0];

            new StudentDashboard(sName, studentId);

        } catch (SQLException ex) {
            err("Error loading students: " + ex.getMessage());
            new Login();
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private JLabel mkLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI",Font.BOLD,12));
        l.setForeground(LBL_DARK);
        return l;
    }

    private void styleComp(JComponent c) {
        c.setFont(new Font("Segoe UI",Font.PLAIN,13));
        c.setForeground(new Color(30,45,90));
        c.setBackground(FLD_BG);
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FLD_BDR,1),
            new EmptyBorder(8,12,8,12)));
        c.setPreferredSize(new Dimension(360,40));
    }

    private void err(String m){
        JOptionPane.showMessageDialog(this,m,"Login Error",JOptionPane.ERROR_MESSAGE);
    }

    private void resetBtn(){
        loginButton.setEnabled(true);
        loginButton.setText("LOGIN");
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(Login::new);
    }
}
