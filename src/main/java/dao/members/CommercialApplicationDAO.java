package dao.members;

import config.DatabaseConfig;
import model.ApplicationStatus;
import model.CommercialApplication;

import java.sql.*;
import java.util.Optional;

public class CommercialApplicationDAO {

    public void save(CommercialApplication app) {
        String sql = "INSERT INTO pu_commercial_applications (application_id, company_name, company_reg_no, directors, business_type, address, email) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, app.getApplicationId());
            ps.setString(2, app.getCompanyName());
            ps.setString(3, app.getCompanyRegNo());
            ps.setString(4, app.getDirectors());
            ps.setString(5, app.getBusinessType());
            ps.setString(6, app.getAddress());
            ps.setString(7, app.getEmail());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("couldn't save commercial application", e);
        }
    }

    public Optional<CommercialApplication> findByApplicationId(int applicationId) {
        String sql = "SELECT ca.*, ma.status FROM pu_commercial_applications ca JOIN pu_member_applications ma ON ca.application_id = ma.id WHERE ca.application_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("error looking up commercial application", e);
        }
        return Optional.empty();
    }

    private CommercialApplication map(ResultSet rs) throws SQLException {
        CommercialApplication ca = new CommercialApplication();
        ca.setApplicationId(rs.getInt("application_id"));
        ca.setCompanyName(rs.getString("company_name"));
        ca.setCompanyRegNo(rs.getString("company_reg_no"));
        ca.setDirectors(rs.getString("directors"));
        ca.setBusinessType(rs.getString("business_type"));
        ca.setAddress(rs.getString("address"));
        ca.setEmail(rs.getString("email"));
        ca.setStatus(ApplicationStatus.valueOf(rs.getString("status")));
        return ca;
    }
}
