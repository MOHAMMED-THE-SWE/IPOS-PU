package dao.comms;

import config.DatabaseConfig;
import model.PaymentLog;

import java.sql.*;

public class PaymentLogDAO {

    public PaymentLog save(PaymentLog log) {
        String sql = """
            INSERT INTO pu_payment_log
              (payee, reference, amount, card_type, card_first4, card_last4, expiry, status, provider)
            VALUES (?,?,?,?,?,?,?,?,?)
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, log.getPayee());
            ps.setString(2, log.getReference());
            ps.setDouble(3, log.getAmount());
            ps.setString(4, log.getCardType());
            ps.setString(5, log.getCardFirst4());
            ps.setString(6, log.getCardLast4());
            ps.setString(7, log.getExpiry());
            ps.setString(8, log.getStatus().name());
            ps.setString(9, log.getProvider().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) log.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("save PaymentLog failed", e);
        }
        return log;
    }
}
