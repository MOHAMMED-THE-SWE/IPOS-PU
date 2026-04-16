package service.members;

import dao.members.UserDAO;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Attempts to log in with the given credentials.
     *
     * @return the authenticated User
     * @throws IllegalArgumentException if email/password are blank
     * @throws RuntimeException         if credentials are invalid
     */
    public User login(String email, String password) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password required");

        Optional<User> opt = userDAO.findByEmail(email);
        if (opt.isEmpty()) throw new RuntimeException("Invalid email or password");

        User user = opt.get();
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        return user;
    }

    /**
     * Changes the password for the given user and clears the must_change_password flag.
     *
     * @throws IllegalArgumentException if newPassword is blank or too short
     */
    public void changePassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        userDAO.updatePassword(userId, hash, false);
    }
}
