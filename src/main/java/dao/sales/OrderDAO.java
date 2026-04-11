package dao.sales;

import config.DatabaseConfig;
import model.Order;
import model.OrderStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAO {

    public Order save(Order order) {
        String sql = "INSERT INTO pu_orders (order_id, user_id, status, total, delivery_address) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getOrderId());
            if (order.getUserId() != null) ps.setInt(2, order.getUserId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, order.getStatus().name());
            ps.setDouble(4, order.getTotal());
            ps.setString(5, order.getDeliveryAddress());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("couldn't save order", e);
        }
        return order;
    }

    // TODO: consider caching frequently looked-up orders
    public Optional<Order> findById(String orderId) {
        String sql = "SELECT * FROM pu_orders WHERE order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("error looking up order: " + orderId, e);
        }
        return Optional.empty();
    }

    public List<Order> findByUserId(int userId) {
        String sql = "SELECT * FROM pu_orders WHERE user_id = ? ORDER BY created_at DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("error fetching orders for user", e);
        }
        return list;
    }

    public List<Order> findAll() {
        String sql = "SELECT * FROM pu_orders ORDER BY created_at DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findAll orders failed", e);
        }
        return list;
    }

    public List<Order> findByDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        String sql = "SELECT * FROM pu_orders WHERE DATE(created_at) BETWEEN ? AND ? ORDER BY created_at";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("date range query failed", e);
        }
        return list;
    }

    public void updateStatus(String orderId, OrderStatus newStatus) {
        String sql = "UPDATE pu_orders SET status = ?, updated_at = NOW() WHERE order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setString(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("failed to update order status", e);
        }
    }

    private Order map(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getString("order_id"));
        int uid = rs.getInt("user_id");
        o.setUserId(rs.wasNull() ? null : uid);
        o.setStatus(OrderStatus.valueOf(rs.getString("status")));
        o.setTotal(rs.getDouble("total"));
        o.setDeliveryAddress(rs.getString("delivery_address"));
        Timestamp c = rs.getTimestamp("created_at");
        if (c != null) o.setCreatedAt(c.toLocalDateTime());
        Timestamp u = rs.getTimestamp("updated_at");
        if (u != null) o.setUpdatedAt(u.toLocalDateTime());
        return o;
    }
}
