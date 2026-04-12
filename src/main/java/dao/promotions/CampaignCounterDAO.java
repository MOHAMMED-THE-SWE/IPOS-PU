package dao.promotions;

import config.DatabaseConfig;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class CampaignCounterDAO {

    public void ensureCounter(int campaignId, int itemId) {
        String sql = "INSERT IGNORE INTO pu_campaign_counters (campaign_id, item_id, campaign_hits, item_hits_qty, item_purchased_qty) VALUES (?,?,0,0,0)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, campaignId);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("couldn't create counter row", e);
        }
    }

    public void incrementCampaignHits(int campaignId, int itemId) {
        ensureCounter(campaignId, itemId);
        String sql = "UPDATE pu_campaign_counters SET campaign_hits = campaign_hits + 1 WHERE campaign_id = ? AND item_id = ?";
        update(sql, campaignId, itemId);
    }

    public void incrementItemHitsQty(int campaignId, int itemId, int qty) {
        ensureCounter(campaignId, itemId);
        String sql = "UPDATE pu_campaign_counters SET item_hits_qty = item_hits_qty + ? WHERE campaign_id = ? AND item_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, campaignId);
            ps.setInt(3, itemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("error updating item hits", e);
        }
    }

    public void incrementItemPurchasedQty(int campaignId, int itemId, int qty) {
        ensureCounter(campaignId, itemId);
        String sql = "UPDATE pu_campaign_counters SET item_purchased_qty = item_purchased_qty + ? WHERE campaign_id = ? AND item_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, campaignId);
            ps.setInt(3, itemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("error updating purchased qty", e);
        }
    }

    // TODO: might be worth caching this instead of hitting DB every time
    public Map<Integer, int[]> findByCampaignId(int campaignId) {
        String sql = "SELECT item_id, campaign_hits, item_hits_qty, item_purchased_qty FROM pu_campaign_counters WHERE campaign_id = ?";
        Map<Integer, int[]> result = new HashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, campaignId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("item_id"), new int[]{
                        rs.getInt("campaign_hits"),
                        rs.getInt("item_hits_qty"),
                        rs.getInt("item_purchased_qty")
                    });
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("error loading counters for campaign", e);
        }
        return result;
    }

    private void update(String sql, int campaignId, int itemId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, campaignId);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("counter update failed", e);
        }
    }
}
