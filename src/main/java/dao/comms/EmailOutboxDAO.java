package dao.comms;

import config.DatabaseConfig;
import model.EmailOutbox;
import model.EmailStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmailOutboxDAO {

    // Purpose: Queue an email into the DB outbox table (status usually starts as PENDING).
    // This does NOT send the email. It only stores it for later dispatching.
    public EmailOutbox save(EmailOutbox email) {
        String sql = "INSERT INTO pu_email_outbox (to_email, subject, body, status) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email.getToEmail());
            ps.setString(2, email.getSubject());
            ps.setString(3, email.getBody());
            ps.setString(4, email.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) email.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("couldn't queue email", e);
        }
        return email;
    }

    // Purpose: Fetch up to 50 emails that are still waiting to be sent.
    // This is used by EmailDispatcher.    
    public List<EmailOutbox> findPending() {
        String sql = "SELECT * FROM pu_email_outbox WHERE status = 'PENDING' ORDER BY created_at LIMIT 50";
        List<EmailOutbox> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("error fetching pending emails", e);
        }
        return list;
    }

    // Purpose: After trying to send an email:
    // - if success => set status=SENT and clear last_error
    // - if fail    => set status=FAILED and store error text
    public void updateStatus(int id, EmailStatus status, String lastError) {
        String sql = "UPDATE pu_email_outbox SET status = ?, last_error = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, lastError);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("failed to update email status", e);
        }
    }

    // Purpose: Convert a DB row into a Java EmailOutbox object.
    private EmailOutbox map(ResultSet rs) throws SQLException {
        EmailOutbox e = new EmailOutbox();
        e.setId(rs.getInt("id"));
        e.setToEmail(rs.getString("to_email"));
        e.setSubject(rs.getString("subject"));
        e.setBody(rs.getString("body"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) e.setCreatedAt(ts.toLocalDateTime());
        e.setStatus(EmailStatus.valueOf(rs.getString("status")));
        e.setLastError(rs.getString("last_error"));
        return e;
    }
}
