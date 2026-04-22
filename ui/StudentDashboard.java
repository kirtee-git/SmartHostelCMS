package ui;

import db.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Student Dashboard
 * ─────────────────────────────────────────────────────────
 *  ✅ Add complaint
 *  ✅ View OWN complaints only (WHERE student_id = ?)
 *  ✅ Update own complaint (category / description / status)
 *  ✅ Delete own complaint
 *  ✅ Auto-refresh after every operation
 *  ✅ Click a row → form auto-fills for easy edit/delete
 *  ✅ Status shown in colour (Pending=gold, Resolved=green)
 * ─────────────────────────────────────────────────────────
 */
public class StudentDashboard extends JFrame {

    private final String studentName;
    private final int    studentId;

    // form widgets
    private JTextField        tfCategory;
    private JTextArea         taDescription;
    private JComboBox<String> cbStatus;

    // table
    private JTable            table;
    private DefaultTableModel tableModel;

    // info
    private JLabel lbCount, lbRoomInfo;

    // ── palette (deep navy) ───────────────────────────────────────────────────
    private static final Color C_BG      = new Color(11,  23,  52);
    private static final Color C_PANEL   = new Color(18,  36,  80);
    private static final Color C_HDR     = new Color(21,  56, 130);
    private static final Color C_GREEN   = new Color(34, 197,  94);
    private static final Color C_BLUE    = new Color(59, 130, 246);
    private static final Color C_RED     = new Color(239, 68,  68);
    private static final Color C_AMBER   = new Color(251,191,  36);
    private static final Color C_SLATE   = new Color(100,120, 160);
    private static final Color C_TXT     = new Color(220,235, 255);
    private static final Color C_DIM     = new Color(140,165, 210);
    private static final Color C_IN_BG   = new Color( 28, 52, 105);
    private static final Color C_IN_BDR  = new Color( 55, 85, 155);
    private static final Color C_ROW_A   = new Color( 14, 27,  60);
    private static final Color C_ROW_B   = new Color( 18, 34,  72);
    private static final Color C_SEL     = new Color( 37, 80, 175);
    private static final Color C_TBL_FG  = new Color(195,218, 255);

    public StudentDashboard(String studentName, int studentId) {
        this.studentName = studentName;
        this.studentId   = studentId;

        setTitle("Student Dashboard  ·  " + studentName);
        setSize(1130, 710);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
        getContentPane().setBackground(C_BG);

        buildUI();
        loadComplaints();
        loadRoomInfo();
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI BUILD
    // ─────────────────────────────────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout(0,0));
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildCenter(),    BorderLayout.CENTER);
    }

    // ── TOP BAR ──────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_PANEL);
        p.setBorder(new EmptyBorder(12,22,12,22));

        JLabel lTitle = new JLabel("🏠  Smart Hostel CMS  —  Student Portal");
        lTitle.setFont(new Font("Segoe UI",Font.BOLD,16));
        lTitle.setForeground(C_TXT);
        p.add(lTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        right.setOpaque(false);

        lbRoomInfo = new JLabel("Loading…");
        lbRoomInfo.setFont(new Font("Segoe UI",Font.PLAIN,12));
        lbRoomInfo.setForeground(C_DIM);
        right.add(lbRoomInfo);

        right.add(mkBtn("Logout", C_RED, e -> { dispose(); new Login(); }));
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── LEFT PANEL ────────────────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_PANEL);
        p.setBorder(new EmptyBorder(20,16,20,16));
        p.setPreferredSize(new Dimension(252,0));

        heading(p, "📝  Complaint Form");
        gap(p, 12);

        // Category
        fieldLabel(p,"Category");
        gap(p,4);
        tfCategory = mkTextField();
        p.add(tfCategory);
        gap(p,12);

        // Description
        fieldLabel(p,"Description");
        gap(p,4);
        taDescription = new JTextArea(5,15);
        taDescription.setFont(new Font("Segoe UI",Font.PLAIN,13));
        taDescription.setForeground(C_TXT);
        taDescription.setBackground(C_IN_BG);
        taDescription.setCaretColor(Color.WHITE);
        taDescription.setLineWrap(true);
        taDescription.setWrapStyleWord(true);
        taDescription.setBorder(new EmptyBorder(7,10,7,10));
        JScrollPane dscScroll = new JScrollPane(taDescription);
        dscScroll.setBorder(BorderFactory.createLineBorder(C_IN_BDR));
        dscScroll.setAlignmentX(LEFT_ALIGNMENT);
        dscScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE,115));
        p.add(dscScroll);
        gap(p,12);

        // Status
        fieldLabel(p,"Status");
        gap(p,4);
        cbStatus = new JComboBox<>(new String[]{"Pending","Resolved"});
        cbStatus.setFont(new Font("Segoe UI",Font.PLAIN,13));
        cbStatus.setBackground(C_IN_BG);
        cbStatus.setForeground(C_TXT);
        cbStatus.setAlignmentX(LEFT_ALIGNMENT);
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
        p.add(cbStatus);
        gap(p,20);

        // Buttons
        JButton btnAdd = mkBtn("➕  Add Complaint", C_GREEN, e -> addComplaint());
        btnAdd.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        btnAdd.setAlignmentX(LEFT_ALIGNMENT);
        p.add(btnAdd); gap(p,8);

        JButton btnUpd = mkBtn("✏️  Update Selected", C_BLUE, e -> updateComplaint());
        btnUpd.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        btnUpd.setAlignmentX(LEFT_ALIGNMENT);
        p.add(btnUpd); gap(p,8);

        JButton btnDel = mkBtn("🗑️  Delete Selected", C_RED, e -> deleteComplaint());
        btnDel.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        btnDel.setAlignmentX(LEFT_ALIGNMENT);
        p.add(btnDel); gap(p,8);

        JButton btnClr = mkBtn("✖  Clear Form", C_SLATE, e -> clearForm());
        btnClr.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
        btnClr.setAlignmentX(LEFT_ALIGNMENT);
        p.add(btnClr);

        p.add(Box.createVerticalGlue());

        // complaint count badge
        lbCount = new JLabel("My complaints: 0");
        lbCount.setFont(new Font("Segoe UI",Font.BOLD,12));
        lbCount.setForeground(C_DIM);
        lbCount.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lbCount);

        return p;
    }

    // ── TABLE PANEL ───────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_BG);
        p.setBorder(new EmptyBorder(18,14,18,18));

        // title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0,0,10,0));

        JLabel lbl = new JLabel("My Complaints");
        lbl.setFont(new Font("Segoe UI",Font.BOLD,15));
        lbl.setForeground(C_TXT);
        titleRow.add(lbl, BorderLayout.WEST);

        JButton btnRefresh = mkBtn("🔄 Refresh", C_SLATE, e -> loadComplaints());
        btnRefresh.setFont(new Font("Segoe UI",Font.BOLD,11));
        titleRow.add(btnRefresh, BorderLayout.EAST);

        p.add(titleRow, BorderLayout.NORTH);

        // table
        String[] cols = {"ID","Category","Description","Status","Date Created"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r,int c){return false;}
        };

        table = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(C_SEL); c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row%2==0 ? C_ROW_A : C_ROW_B);
                    if (col==3) {
                        String v = (String)getValueAt(row,col);
                        c.setForeground("Resolved".equalsIgnoreCase(v)?C_GREEN:C_AMBER);
                    } else {
                        c.setForeground(C_TBL_FG);
                    }
                }
                return c;
            }
        };
        table.setFont(new Font("Segoe UI",Font.PLAIN,13));
        table.setRowHeight(32);
        table.setGridColor(new Color(32,52,110));
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(C_ROW_A);
        table.setSelectionBackground(C_SEL);
        table.setSelectionForeground(Color.WHITE);

        // column widths
        int[] widths = {44,115,330,95,170};
        for (int i=0;i<widths.length;i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JTableHeader hdr = table.getTableHeader();
        hdr.setBackground(C_HDR);
        hdr.setForeground(Color.WHITE);
        hdr.setFont(new Font("Segoe UI",Font.BOLD,13));
        hdr.setPreferredSize(new Dimension(0,36));

        // row click → fill form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromRow();
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(38,62,130)));
        sp.getViewport().setBackground(C_ROW_A);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DB OPERATIONS
    // ─────────────────────────────────────────────────────────────────────────

    private void addComplaint() {
        String cat  = tfCategory.getText().trim();
        String desc = taDescription.getText().trim();
        String stat = (String) cbStatus.getSelectedItem();

        if (cat.isEmpty())  { warn("Category cannot be empty.");   return; }
        if (desc.isEmpty()) { warn("Description cannot be empty."); return; }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO complaints(student_id,category,description,status,date_created,complaint_date) " +
                "VALUES(?,?,?,?,NOW(),NOW())");
            ps.setInt(1,studentId);
            ps.setString(2,cat);
            ps.setString(3,desc);
            ps.setString(4,stat);
            ps.executeUpdate();
            info("✅  Complaint added successfully!");
            clearForm();
            loadComplaints();
        } catch (SQLException ex) { dbErr(ex); }
    }

    private void updateComplaint() {
        int row = table.getSelectedRow();
        if (row==-1) { warn("Please click a complaint row first."); return; }

        int    id   = (int)    tableModel.getValueAt(row,0);
        String cat  = tfCategory.getText().trim();
        String desc = taDescription.getText().trim();
        String stat = (String) cbStatus.getSelectedItem();

        if (cat.isEmpty()||desc.isEmpty()) { warn("Category/Description cannot be empty."); return; }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE complaints SET category=?,description=?,status=? " +
                "WHERE complaint_id=? AND student_id=?");
            ps.setString(1,cat); ps.setString(2,desc); ps.setString(3,stat);
            ps.setInt(4,id);     ps.setInt(5,studentId);
            if (ps.executeUpdate()>0) info("✅  Complaint updated!");
            else warn("Update failed — you can only edit your own complaints.");
            loadComplaints();
        } catch (SQLException ex) { dbErr(ex); }
    }

    private void deleteComplaint() {
        int row = table.getSelectedRow();
        if (row==-1) { warn("Please click a complaint row first."); return; }

        int id = (int) tableModel.getValueAt(row,0);
        int ok = JOptionPane.showConfirmDialog(this,
            "Delete Complaint ID " + id + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok!=JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM complaints WHERE complaint_id=? AND student_id=?");
            ps.setInt(1,id); ps.setInt(2,studentId);
            if (ps.executeUpdate()>0) info("🗑️  Complaint deleted.");
            else warn("Delete failed — you can only delete your own complaints.");
            clearForm();
            loadComplaints();
        } catch (SQLException ex) { dbErr(ex); }
    }

    private void loadComplaints() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT complaint_id,category,description,status,date_created " +
                "FROM complaints WHERE student_id=? ORDER BY date_created DESC");
            ps.setInt(1,studentId);
            ResultSet rs = ps.executeQuery();
            int cnt=0;
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("complaint_id"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("date_created")
                });
                cnt++;
            }
            lbCount.setText("My complaints: " + cnt);
        } catch (SQLException ex) { dbErr(ex); }
    }

    private void loadRoomInfo() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT name,email,room_no FROM students WHERE id=?");
            ps.setInt(1,studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                lbRoomInfo.setText("👤  " + rs.getString("name")
                    + "   Room: " + rs.getString("room_no")
                    + "   (" + rs.getString("email") + ")");
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void fillFormFromRow() {
        int row = table.getSelectedRow();
        if (row==-1) return;
        tfCategory.setText   ((String)tableModel.getValueAt(row,1));
        taDescription.setText((String)tableModel.getValueAt(row,2));
        cbStatus.setSelectedItem(tableModel.getValueAt(row,3));
    }

    private void clearForm() {
        tfCategory.setText(""); taDescription.setText("");
        cbStatus.setSelectedIndex(0); table.clearSelection();
    }

    private void heading(JPanel p, String t) {
        JLabel l=new JLabel(t);
        l.setFont(new Font("Segoe UI",Font.BOLD,14));
        l.setForeground(C_TXT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l);
    }

    private void fieldLabel(JPanel p, String t) {
        JLabel l=new JLabel(t);
        l.setFont(new Font("Segoe UI",Font.BOLD,12));
        l.setForeground(C_DIM);
        l.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l);
    }

    private void gap(JPanel p, int h) {
        p.add(Box.createVerticalStrut(h));
    }

    private JTextField mkTextField() {
        JTextField f=new JTextField();
        f.setFont(new Font("Segoe UI",Font.PLAIN,13));
        f.setForeground(C_TXT);
        f.setBackground(C_IN_BG);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_IN_BDR),
            new EmptyBorder(6,10,6,10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
        f.setAlignmentX(LEFT_ALIGNMENT);
        return f;
    }

    private JButton mkBtn(String text, Color bg, ActionListener al) {
        JButton b=new JButton(text);
        b.setFont(new Font("Segoe UI",Font.BOLD,13));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8,14,8,14));
        Color dark=bg.darker();
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){b.setBackground(dark);}
            public void mouseExited (MouseEvent e){b.setBackground(bg);  }
        });
        if (al!=null) b.addActionListener(al);
        return b;
    }

    private void info (String m){JOptionPane.showMessageDialog(this,m,"Info",   JOptionPane.INFORMATION_MESSAGE);}
    private void warn (String m){JOptionPane.showMessageDialog(this,m,"Warning",JOptionPane.WARNING_MESSAGE);    }
    private void dbErr(SQLException ex){
        JOptionPane.showMessageDialog(this,"DB Error:\n"+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
