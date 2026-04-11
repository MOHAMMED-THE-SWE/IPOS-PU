package dao.sales;

import config.DatabaseConfig;
import model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAO {

    public List<Product> findAll() {
        String sql = "SELECT * FROM pu_products ORDER BY item_id";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("findAll products failed", e);
        }
        return list;
    }

    public List<Product> findByKeyword(String keyword) {
        String sql = "SELECT * FROM pu_products WHERE description LIKE ? ORDER BY item_id";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByKeyword failed", e);
        }
        return list;
    }

    public Optional<Product> findById(int itemId) {
        String sql = "SELECT * FROM pu_products WHERE item_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById product failed", e);
        }
        return Optional.empty();
    }

    private Product map(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("item_id"),
            rs.getString("description"),
            rs.getDouble("unit_price"),
            rs.getInt("stock_qty")
        );
    }
}
