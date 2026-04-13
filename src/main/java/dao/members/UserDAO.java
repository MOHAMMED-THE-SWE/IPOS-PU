package dao.members;

import config.DatabaseConfig;
import model.User;
import model.UserRole;

import java.sql.*;
import java.util.Optional;

public class UserDAO {

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM pu_users WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("couldn't find user by email", e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM pu_users WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("error looking up user id=" + id, e);
        }
        return Optional.empty();
    }

    public User save(User user) {
        String sql = "INSERT INTO pu_users (email, password_hash, role, must_change_password) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setBoolean(4, user.isMustChangePassword());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("error saving user", e);
        }
        return user;
    }

    public void updatePassword(int userId, String newHash, boolean mustChange) {
        String sql = "UPDATE pu_users SET password_hash = ?, must_change_password = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setBoolean(2, mustChange);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updatePassword failed", e);
        }
    }

    // TODO: might want to only count completed/delivered orders here, not all statuses
    public int countOrders(int userId) {
        String sql = "SELECT COUNT(*) FROM pu_orders WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("countOrders failed", e);
        }
        return 0;
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(UserRole.valueOf(rs.getString("role")));
        u.setMustChangePassword(rs.getBoolean("must_change_password"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }
}
