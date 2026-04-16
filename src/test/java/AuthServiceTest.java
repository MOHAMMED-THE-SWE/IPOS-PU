
import dao.members.UserDAO;
import model.User;
import model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.members.AuthService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userDAO);
    }

    // ---- login() tests ----

    @Test
    void login_correctCredentials_returnsUser() {
        String rawPassword = "Secret123!";
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        User stored = new User(1, "alice@example.com", hash, UserRole.MEMBER, false, null);

        when(userDAO.findByEmail("alice@example.com")).thenReturn(Optional.of(stored));

        User result = authService.login("alice@example.com", rawPassword);

        assertNotNull(result);
        assertEquals("alice@example.com", result.getEmail());
    }

    @Test
    void login_wrongPassword_throwsRuntimeException() {
        String hash = BCrypt.hashpw("CorrectPass1!", BCrypt.gensalt());
        User stored = new User(1, "alice@example.com", hash, UserRole.MEMBER, false, null);

        when(userDAO.findByEmail("alice@example.com")).thenReturn(Optional.of(stored));

        assertThrows(RuntimeException.class, () ->
            authService.login("alice@example.com", "WrongPass!"),
            "Wrong password should throw RuntimeException");
    }

    @Test
    void login_unknownEmail_throwsRuntimeException() {
        when(userDAO.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            authService.login("nobody@example.com", "anypass"),
            "Unknown email should throw RuntimeException");
    }

    @Test
    void login_blankEmail_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            authService.login("", "somepass"),
            "Blank email should throw IllegalArgumentException");

        verify(userDAO, never()).findByEmail(any());
    }

    @Test
    void login_nullEmail_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            authService.login(null, "somepass"));

        verify(userDAO, never()).findByEmail(any());
    }

    @Test
    void login_blankPassword_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            authService.login("user@example.com", ""),
            "Blank password should throw IllegalArgumentException");

        verify(userDAO, never()).findByEmail(any());
    }

    // ---- changePassword() tests ----

    @Test
    void changePassword_validPassword_updatesWithBcryptHash() {
        authService.changePassword(42, "NewPass1234!");

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(userDAO, times(1)).updatePassword(eq(42), hashCaptor.capture(), eq(false));

        String capturedHash = hashCaptor.getValue();
        assertTrue(BCrypt.checkpw("NewPass1234!", capturedHash),
            "Stored hash should verify against the new plain-text password");
    }

    @Test
    void changePassword_tooShortPassword_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            authService.changePassword(1, "short"),
            "Password shorter than 8 chars should throw IllegalArgumentException");

        verify(userDAO, never()).updatePassword(anyInt(), anyString(), anyBoolean());
    }

    @Test
    void changePassword_exactlyEightChars_succeeds() {
        // Boundary: exactly 8 characters is acceptable
        authService.changePassword(1, "Exactly8");

        verify(userDAO, times(1)).updatePassword(eq(1), anyString(), eq(false));
    }

    @Test
    void changePassword_nullPassword_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            authService.changePassword(1, null));

        verify(userDAO, never()).updatePassword(anyInt(), anyString(), anyBoolean());
    }
}
