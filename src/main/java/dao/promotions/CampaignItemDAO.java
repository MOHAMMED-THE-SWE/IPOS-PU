package dao.promotions;

import config.DatabaseConfig;
import model.CampaignItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CampaignItemDAO {

    public void save(CampaignItem item) {
        String sql = "INSERT INTO pu_campaign_items (campaign_id, item_id, discount_percent) VALUES (?,?,?) " +
                     "ON DUPLICATE KEY UPDATE discount_percent = VALUES(discount_percent)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getCampaignId());
            ps.setInt(2, item.getItemId());
            ps.setDouble(3, item.getDiscountPercent());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("save CampaignItem failed", e);
        }
    }

    public List<CampaignItem> findByCampaignId(int campaignId) {
        String sql = "SELECT * FROM pu_campaign_items WHERE campaign_id = ?";
        List<CampaignItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, campaignId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByCampaignId failed", e);
        }
        return list;
    }

    /** Returns campaign items for a product across all active campaigns. */
    public List<CampaignItem> findActiveByItemId(int itemId) {
        String sql = """
            SELECT ci.* FROM pu_campaign_items ci
            JOIN pu_campaigns c ON ci.campaign_id = c.campaign_id
            WHERE ci.item_id = ? AND c.status = 'ACTIVE' AND c.start_dt <= NOW() AND c.end_dt > NOW()
            """;
        List<CampaignItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findActiveByItemId failed", e);
        }
        return list;
    }

    public void deleteByCampaignId(int campaignId) {
        String sql = "DELETE FROM pu_campaign_items WHERE campaign_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, campaignId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteByCampaignId failed", e);
        }
    }

    private CampaignItem map(ResultSet rs) throws SQLException {
        return new CampaignItem(
            rs.getInt("campaign_id"),
            rs.getInt("item_id"),
            rs.getDouble("discount_percent")
        );
    }
}