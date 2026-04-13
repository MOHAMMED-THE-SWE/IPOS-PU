package dao.members;

import config.DatabaseConfig;
import model.ApplicationStatus;
import model.MemberApplication;

import java.sql.*;
import java.util.Optional;

public class MemberApplicationDAO {

    public MemberApplication save(MemberApplication app) {
        String sql = "INSERT INTO pu_member_applications (type, email, status) VALUES (?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, app.getType().name());
            ps.setString(2, app.getEmail());
            ps.setString(3, app.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) app.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("couldn't save member application", e);
        }
        return app;
    }

    // TODO: add findByEmail method if needed later
    public Optional<MemberApplication> findById(int id) {
        String sql = "SELECT * FROM pu_member_applications WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("error finding application id=" + id, e);
        }
        return Optional.empty();
    }

    public void updateStatus(int id, ApplicationStatus status) {
        String sql = "UPDATE pu_member_applications SET status = ?, processed_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("failed to update application status", e);
        }
    }

    private MemberApplication map(ResultSet rs) throws SQLException {
        MemberApplication a = new MemberApplication();
        a.setId(rs.getInt("id"));
        a.setType(MemberApplication.Type.valueOf(rs.getString("type")));
        a.setEmail(rs.getString("email"));
        a.setStatus(ApplicationStatus.valueOf(rs.getString("status")));
        Timestamp sub = rs.getTimestamp("submitted_at");
        if (sub != null) a.setSubmittedAt(sub.toLocalDateTime());
        Timestamp proc = rs.getTimestamp("processed_at");
        if (proc != null) a.setProcessedAt(proc.toLocalDateTime());
        return a;
    }
}
