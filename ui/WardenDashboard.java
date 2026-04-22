package ui;

import db.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Warden Dashboard
 * ─────────────────────────────────────────────────────────
 *  ✅ View ALL complaints from every student
 *  ✅ Search by category keyword or status keyword
 *  ✅ Filter: All / Pending / Resolved
 *  ✅ Update complaint status (Pending ↔ Resolved)
 *  ✅ Live stats panel (Total / Pending / Resolved counts)
 *  ✅ No delete option for warden
 *  ✅ Auto-refresh after every update
 *  ✅ Shows student name alongside complaint
 * ─────────────────────────────────────────────────────────
 */
public class WardenDashboard extends JFrame {

    private final String wardenName;

    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextField        tfSearch;
    private JComboBox<String> cbFilter;
    private JComboBox<String> cbNewStatus;

    // live stats labels
    private JLabel lbTotal, lbPending, lbResolved;

    // ── palette (dark forest) ─────────────────────────────────────────────────
    private static final Color C_BG      = new Color( 8,  20,  16);
    private static final Color C_PANEL   = new Color(14,  34,  26);
    private static final Color C_HDR     = new Color(16,  68,  48);
    private static final Color C_GREEN   = new Color(34, 197,  94);
    private static final Color C_BLUE    = new Color(59, 130, 246);
    private static final Color C_TEAL    = new Color(20, 184, 166);
    private static final Color C_RED     = new Color(239, 68,  68);
    private static final Color C_AMBER   = new Color(251,191,  36);
    private static final Color C_SLATE   = new Color(70, 110,  90);
    private static final Color C_TXT     = new Color(210,248, 232);
    private static final Color C_DIM     = new Color(110,170, 140);
    private static final Color C_IN_BG   = new Color( 18, 48,  36);
    private static final Color C_IN_BDR  = new Color( 38, 95,  70);
    private static final Color C_ROW_A   = new Color(  9, 23,  16);
    private static final Color C_ROW_B   = new Color( 13, 30,  22);
    private static final Color C_SEL     = new Color( 25, 90,  65);
    private static final Color C_TBL_FG  = new Color(185,238, 212);

    public WardenDashboard(String wardenName) {
        this.wardenName = wardenName;

        setTitle("Warden Dashboard  ·  " + wardenName);
        setSize(1200, 730);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
        getContentPane().setBackground(C_BG);

        buildUI();
        loadComplaints(null, null);
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

        JLabel lTitle = new JLabel("🏠  Smart Hostel CMS  —  Warden Portal");
        lTitle.setFont(new Font("Segoe UI",Font.BOLD,16));
        lTitle.setForeground(C_TXT);
        p.add(lTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        right.setOpaque(false);
        JLabel lUser = new JLabel("🔑  " + wardenName);
        lUser.setFont(new Font("Segoe UI",Font.PLAIN,12));
        lUser.setForeground(C_DIM);
        right.add(lUser);
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

        // ── Live Stats ────────────────────────────────────────────────────────
        heading(p,"📊  Live Statistics");
        gap(p,10);

        JPanel grid = new JPanel(new GridLayout(1,3,6,0));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE,60));

        lbTotal   = statCard("—","Total",   C_BLUE);
        lbPending = statCard("—","Pending", C_AMBER);
        lbResolved= statCard("—","Resolved",C_GREEN);
        grid.add(lbTotal); grid.add(lbPending); grid.add(lbResolved);
        p.add(grid);
        gap(p,16); divider(p); gap(p,14);

        // ── Search ───────────────────────────────────────────────────────────
        heading(p,"🔍  Search Complaints");
        gap(p,8);
        fieldLabel(p,"Keyword (category or status):");
        gap(p,4);
        tfSearch = mkTextField();
        p.add(tfSearch);
        gap(p,8);

        JButton btnSearch = mkBtn("Search", C_BLUE, e -> {
            String kw = tfSearch.getText().trim();
            loadComplaints(kw.isEmpty()?null:kw, null);
        });
        btnSearch.setAlignmentX(LEFT_ALIGNMENT);
        btnSearch.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
        p.add(btnSearch);
        gap(p,16); divider(p); gap(p,14);

        // ── Filter ───────────────────────────────────────────────────────────
        heading(p,"⚙️  Filter by Status");
        gap(p,8);
        cbFilter = new JComboBox<>(new String[]{"All","Pending","Resolved"});
        styleCombo(cbFilter);
        p.add(cbFilter);
        gap(p,8);

        JButton btnFilter = mkBtn("Apply Filter", C_TEAL, e -> {
            String sel = (String)cbFilter.getSelectedItem();
            loadComplaints(null,"All".equals(sel)?null:sel);
        });
        btnFilter.setAlignmentX(LEFT_ALIGNMENT);
        btnFilter.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
        p.add(btnFilter);
        gap(p,16); divider(p); gap(p,14);

        // ── Update Status ─────────────────────────────────────────────────────
        heading(p,"✏️  Update Complaint Status");
        gap(p,6);
        fieldLabel(p,"① Click a row  ② Choose status below");
        gap(p,8);
        cbNewStatus = new JComboBox<>(new String[]{"Pending","Resolved"});
        styleCombo(cbNewStatus);
        p.add(cbNewStatus);
        gap(p,8);

        JButton btnUpdate = mkBtn("✅  Update Status", C_GREEN, e -> doUpdateStatus());
        btnUpdate.setAlignmentX(LEFT_ALIGNMENT);
        btnUpdate.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
        p.add(btnUpdate);
        gap(p,10);

        JButton btnRefresh = mkBtn("🔄  Refresh All", C_SLATE, e -> {
            tfSearch.setText(""); cbFilter.setSelectedIndex(0);
            loadComplaints(null,null);
        });
        btnRefresh.setAlignmentX(LEFT_ALIGNMENT);
        btnRefresh.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
        p.add(btnRefresh);
        p.add(Box.createVerticalGlue());

        // note at bottom
        JLabel note = new JLabel("<html><i>⚠ Wardens cannot delete complaints.</i></html>");
        note.setFont(new Font("Segoe UI",Font.PLAIN,11));
        note.setForeground(C_DIM);
        note.setAlignmentX(LEFT_ALIGNMENT);
        p.add(note);

        return p;
    }

    // ── TABLE PANEL ───────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_BG);
        p.setBorder(new EmptyBorder(18,14,18,18));

        JLabel lbl = new JLabel("All Student Complaints");
        lbl.setFont(new Font("Segoe UI",Font.BOLD,15));
        lbl.setForeground(C_TXT);
        lbl.setBorder(new EmptyBorder(0,0,10,0));
        p.add(lbl, BorderLayout.NORTH);

        // columns: complaint_id | student_id | student_name | category | description | status | date
        String[] cols = {"C.ID","Stu.ID","Student Name","Category","Description","Status","Date Created"};
        tableModel = new DefaultTableModel(cols,0){
            public boolean isCellEditable(int r,int c){return false;}
        };

        table = new JTable(tableModel){
            public Component prepareRenderer(TableCellRenderer r, int row, int col){
                Component c = super.prepareRenderer(r,row,col);
                if (isRowSelected(row)){
                    c.setBackground(C_SEL); c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row%2==0?C_ROW_A:C_ROW_B);
                    if (col==5){
                        String v=(String)getValueAt(row,col);
                        c.setForeground("Resolved".equalsIgnoreCase(v)?C_GREEN:C_AMBER);
                    } else { c.setForeground(C_TBL_FG); }
                }
                return c;
            }
        };
        table.setFont(new Font("Segoe UI",Font.PLAIN,13));
        table.setRowHeight(32);
        table.setGridColor(new Color(22,52,38));
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(C_ROW_A);
        table.setSelectionBackground(C_SEL);
        table.setSelectionForeground(Color.WHITE);

        // widths: C.ID, Stu.ID, Name, Category, Description, Status, Date
        int[] w = {50,55,120,110,300,95,155};
        for (int i=0;i<w.length;i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        JTableHeader hdr = table.getTableHeader();
        hdr.setBackground(C_HDR);
        hdr.setForeground(Color.WHITE);
        hdr.setFont(new Font("Segoe UI",Font.BOLD,13));
        hdr.setPreferredSize(new Dimension(0,36));

        // row click → sync status combo
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()){
                int row=table.getSelectedRow();
                if (row!=-1) cbNewStatus.setSelectedItem(tableModel.getValueAt(row,5));
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(28,68,50)));
        sp.getViewport().setBackground(C_ROW_A);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DB OPERATIONS
    // ─────────────────────────────────────────────────────────────────────────

    private void doUpdateStatus() {
        int row = table.getSelectedRow();
        if (row==-1){ warn("Click a complaint row first."); return; }

        int    id  = (int)    tableModel.getValueAt(row,0);
        String st  = (String) cbNewStatus.getSelectedItem();

        try (Connection conn = DBConnection.getConnection()){
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE complaints SET status=? WHERE complaint_id=?");
            ps.setString(1,st); ps.setInt(2,id);
            if (ps.executeUpdate()>0)
                info("✅  Complaint #" + id + " status → " + st);
            loadComplaints(null,null);
        } catch (SQLException ex){ dbErr(ex); }
    }

    /**
     * Core loader — handles search keyword AND/OR status filter simultaneously.
     * keyword=null means no keyword filter.
     * statusFilter=null means no status filter.
     */
    private void loadComplaints(String keyword, String statusFilter) {
        tableModel.setRowCount(0);

        // JOIN with students to get the student name
        StringBuilder sql = new StringBuilder(
            "SELECT c.complaint_id, c.student_id, s.name AS student_name, " +
            "c.category, c.description, c.status, c.date_created " +
            "FROM complaints c " +
            "LEFT JOIN students s ON c.student_id = s.id " +
            "WHERE 1=1");

        if (keyword!=null)
            sql.append(" AND (c.category LIKE ? OR c.status LIKE ? OR s.name LIKE ?)");
        if (statusFilter!=null)
            sql.append(" AND c.status = ?");
        sql.append(" ORDER BY c.date_created DESC");

        try (Connection conn = DBConnection.getConnection()){
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            int idx=1;
            if (keyword!=null){
                String kw="%"+keyword+"%";
                ps.setString(idx++,kw);
                ps.setString(idx++,kw);
                ps.setString(idx++,kw);
            }
            if (statusFilter!=null) ps.setString(idx,statusFilter);

            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                tableModel.addRow(new Object[]{
                    rs.getInt   ("complaint_id"),
                    rs.getInt   ("student_id"),
                    rs.getString("student_name"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("date_created")
                });
            }
            refreshStats(conn);
        } catch (SQLException ex){ dbErr(ex); }
    }

    private void refreshStats(Connection conn) {
        lbTotal   .setText("<html><center><b>"+count(conn,null)        +"</b><br><small>Total</small></center></html>");
        lbPending .setText("<html><center><b>"+count(conn,"Pending")   +"</b><br><small>Pending</small></center></html>");
        lbResolved.setText("<html><center><b>"+count(conn,"Resolved")  +"</b><br><small>Resolved</small></center></html>");
    }

    private int count(Connection conn, String status){
        try {
            String sql = status==null
                ? "SELECT COUNT(*) FROM complaints"
                : "SELECT COUNT(*) FROM complaints WHERE status=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            if (status!=null) ps.setString(1,status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e){ e.printStackTrace(); }
        return 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private JLabel statCard(String val, String label, Color color){
        JLabel l=new JLabel("<html><center><b>"+val+"</b><br><small>"+label+"</small></center></html>",
            SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI",Font.PLAIN,12));
        l.setForeground(color);
        l.setOpaque(true);
        l.setBackground(new Color(color.getRed()/7, color.getGreen()/7, color.getBlue()/7));
        l.setBorder(BorderFactory.createLineBorder(color.darker(),1));
        return l;
    }

    private void heading(JPanel p, String t){
        JLabel l=new JLabel(t);
        l.setFont(new Font("Segoe UI",Font.BOLD,13));
        l.setForeground(C_TXT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l);
    }

    private void fieldLabel(JPanel p, String t){
        JLabel l=new JLabel(t);
        l.setFont(new Font("Segoe UI",Font.PLAIN,11));
        l.setForeground(C_DIM);
        l.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l);
    }

    private void gap(JPanel p, int h){ p.add(Box.createVerticalStrut(h)); }

    private void divider(JPanel p){
        JSeparator s=new JSeparator();
        s.setForeground(new Color(35,75,55));
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        p.add(s);
    }

    private JTextField mkTextField(){
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

    private void styleCombo(JComboBox<String> cb){
        cb.setFont(new Font("Segoe UI",Font.PLAIN,13));
        cb.setBackground(C_IN_BG);
        cb.setForeground(C_TXT);
        cb.setAlignmentX(LEFT_ALIGNMENT);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
    }

    private JButton mkBtn(String text, Color bg, ActionListener al){
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
