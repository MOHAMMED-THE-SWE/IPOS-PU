package dao.sales;

import config.DatabaseConfig;
import model.OrderStatus;
import model.OrderStatusHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderStatusDAO {

    public void save(OrderStatusHistory entry) {
        String sql = "INSERT INTO pu_order_status_history (order_id, status, note) VALUES (?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entry.getOrderId());
            ps.setString(2, entry.getStatus().name());
            ps.setString(3, entry.getNote());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("save OrderStatusHistory failed", e);
        }
    }

    public List<OrderStatusHistory> findByOrderId(String orderId) {
        String sql = "SELECT * FROM pu_order_status_history WHERE order_id = ? ORDER BY changed_at";
        List<OrderStatusHistory> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderStatusHistory h = new OrderStatusHistory();
                    h.setId(rs.getInt("id"));
                    h.setOrderId(rs.getString("order_id"));
                    h.setStatus(OrderStatus.valueOf(rs.getString("status")));
                    Timestamp ts = rs.getTimestamp("changed_at");
                    if (ts != null) h.setChangedAt(ts.toLocalDateTime());
                    h.setNote(rs.getString("note"));
                    list.add(h);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByOrderId status history failed", e);
        }
        return list;
    }
}
