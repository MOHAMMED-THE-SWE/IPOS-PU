ackage dao.promotions;

import config.DatabaseConfig;
import model.Campaign;
import model.CampaignStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CampaignDAO {

    public Campaign save(Campaign c) {
        String sql = "INSERT INTO pu_campaigns (description, start_dt, end_dt, status) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getDescription());
            ps.setTimestamp(2, Timestamp.valueOf(c.getStartDt()));
            ps.setTimestamp(3, Timestamp.valueOf(c.getEndDt()));
            ps.setString(4, c.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setCampaignId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("couldn't save campaign", e);
        }
        return c;
    }

    public Optional<Campaign> findById(int id) {
        String sql = "SELECT * FROM pu_campaigns WHERE campaign_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("error looking up campaign", e);
        }
        return Optional.empty();
    }

    public List<Campaign> findAll() {
        String sql = "SELECT * FROM pu_campaigns ORDER BY start_dt DESC";
        List<Campaign> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("couldn't load campaigns", e);
        }
        return list;
    }

    public List<Campaign> findActive() {
        String sql = "SELECT * FROM pu_campaigns WHERE status = 'ACTIVE' AND start_dt <= NOW() AND end_dt > NOW()";
        List<Campaign> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("error fetching active campaigns", e);
        }
        return list;
    }

    public void updateStatus(int campaignId, CampaignStatus status) {
        String sql = "UPDATE pu_campaigns SET status = ? WHERE campaign_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, campaignId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("failed to update campaign status", e);
        }
    }

    public void update(Campaign c) {
        String sql = "UPDATE pu_campaigns SET description = ?, start_dt = ?, end_dt = ? WHERE campaign_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getDescription());
            ps.setTimestamp(2, Timestamp.valueOf(c.getStartDt()));
            ps.setTimestamp(3, Timestamp.valueOf(c.getEndDt()));
            ps.setInt(4, c.getCampaignId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("failed to update campaign", e);
        }
    }

    public void delete(int campaignId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM pu_campaign_counters WHERE campaign_id = ?")) {
                ps.setInt(1, campaignId); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM pu_campaign_items WHERE campaign_id = ?")) {
                ps.setInt(1, campaignId); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM pu_campaigns WHERE campaign_id = ?")) {
                ps.setInt(1, campaignId); ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("failed to delete campaign", e);
        }
    }

    public List<Campaign> findOverlapping(int newCampaignId, java.time.LocalDateTime startDt, java.time.LocalDateTime endDt) {
        String sql = "SELECT DISTINCT c.* FROM pu_campaigns c " +
                     "JOIN pu_campaign_items ci ON c.campaign_id = ci.campaign_id " +
                     "WHERE c.campaign_id != ? AND c.status = 'ACTIVE' " +
                     "AND c.start_dt <= ? AND c.end_dt >= ? " +
                     "AND ci.item_id IN (SELECT item_id FROM pu_campaign_items WHERE campaign_id = ?)";
        List<Campaign> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newCampaignId);
            ps.setTimestamp(2, Timestamp.valueOf(endDt));
            ps.setTimestamp(3, Timestamp.valueOf(startDt));
            ps.setInt(4, newCampaignId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("failed to check overlapping campaigns", e);
        }
        return list;
    }

    private Campaign map(ResultSet rs) throws SQLException {
        Campaign c = new Campaign();
        c.setCampaignId(rs.getInt("campaign_id"));
        c.setDescription(rs.getString("description"));
        c.setStartDt(rs.getTimestamp("start_dt").toLocalDateTime());
        c.setEndDt(rs.getTimestamp("end_dt").toLocalDateTime());
        c.setStatus(CampaignStatus.valueOf(rs.getString("status")));
        return c;
    }
}

