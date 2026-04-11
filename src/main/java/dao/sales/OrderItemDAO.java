package dao.sales;

import config.DatabaseConfig;
import model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {

    public void saveAll(List<OrderItem> items) {
        String sql = """
            INSERT INTO pu_order_items
              (order_id, item_id, qty, unit_price_at_time, discount_percent, line_total)
            VALUES (?,?,?,?,?,?)
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (OrderItem item : items) {
                ps.setString(1, item.getOrderId());
                ps.setInt(2, item.getItemId());
                ps.setInt(3, item.getQty());
                ps.setDouble(4, item.getUnitPriceAtTime());
                ps.setDouble(5, item.getDiscountPercent());
                ps.setDouble(6, item.getLineTotal());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("saveAll order items failed", e);
        }
    }

    public List<OrderItem> findByOrderId(String orderId) {
        String sql = "SELECT * FROM pu_order_items WHERE order_id = ?";
        List<OrderItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getString("order_id"));
                    item.setItemId(rs.getInt("item_id"));
                    item.setQty(rs.getInt("qty"));
                    item.setUnitPriceAtTime(rs.getDouble("unit_price_at_time"));
                    item.setDiscountPercent(rs.getDouble("discount_percent"));
                    item.setLineTotal(rs.getDouble("line_total"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByOrderId order items failed", e);
        }
        return list;
    }
}
