
import dao.members.CommercialApplicationDAO;
import dao.members.MemberApplicationDAO;
import dao.members.UserDAO;
import interfaces.ICommercialApplicationService;
import interfaces.IEmailGateway;
import model.MemberApplication;
import model.User;
import model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.members.MemberRegistrationService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberRegistrationServiceTest {

    @Mock private UserDAO userDAO;
    @Mock private MemberApplicationDAO memberApplicationDAO;
    @Mock private CommercialApplicationDAO commercialApplicationDAO;
    @Mock private IEmailGateway emailGateway;
    @Mock private ICommercialApplicationService commercialApplicationService;

    private MemberRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new MemberRegistrationService(
            userDAO, memberApplicationDAO, commercialApplicationDAO,
            emailGateway, commercialApplicationService);
    }

    // ---- isValidEmail (static utility) ----

    @Test
    void isValidEmail_validFormats_returnTrue() {
        assertTrue(MemberRegistrationService.isValidEmail("user@example.com"));
        assertTrue(MemberRegistrationService.isValidEmail("john.doe+tag@mail.co.uk"));
    }

    @Test
    void isValidEmail_invalidFormats_returnFalse() {
        assertFalse(MemberRegistrationService.isValidEmail("not-an-email"));
        assertFalse(MemberRegistrationService.isValidEmail("missing@tld"));
        assertFalse(MemberRegistrationService.isValidEmail(null));
        assertFalse(MemberRegistrationService.isValidEmail("@nodomain.com"));
    }

    // ---- registerNonCommercial() ----

    @Test
    void registerNonCommercial_validNewEmail_returnsNonNullPassword() {
        when(userDAO.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(memberApplicationDAO.save(any(MemberApplication.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailGateway.sendSystemEmail(anyString(), anyString(), anyString())).thenReturn(true);

        String password = service.registerNonCommercial("new@example.com");

        assertNotNull(password, "Returned password must not be null");
    }

    @Test
    void registerNonCommercial_validNewEmail_passwordIsExactlyTenChars() {
        when(userDAO.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(memberApplicationDAO.save(any(MemberApplication.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailGateway.sendSystemEmail(anyString(), anyString(), anyString())).thenReturn(true);

        String password = service.registerNonCommercial("new@example.com");

        assertEquals(10, password.length(), "Generated password must be exactly 10 characters");
    }

    @Test
    void registerNonCommercial_validNewEmail_passwordContainsAllRequiredCharTypes() {
        when(userDAO.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(memberApplicationDAO.save(any(MemberApplication.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailGateway.sendSystemEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // Run multiple times to reduce probability of a false pass (charset has letters, digits, specials)
        // The charset is: A-Z, a-z, 0-9, !@#$%^&*  — all chars from PASSWORD_CHARS
        for (int attempt = 0; attempt < 5; attempt++) {
            String password = service.registerNonCommercial("new@example.com");
            assertTrue(password.chars().anyMatch(Character::isLetter),
                "Password must contain at least one letter: " + password);
            assertTrue(password.chars().anyMatch(Character::isDigit) ||
                       password.chars().anyMatch(c -> "!@#$%^&*".indexOf(c) >= 0),
                "Password must contain digit or special char: " + password);
        }
    }

    @Test
    void registerNonCommercial_validEmail_queuesCredentialsEmail() {
        when(userDAO.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(memberApplicationDAO.save(any(MemberApplication.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailGateway.sendSystemEmail(anyString(), anyString(), anyString())).thenReturn(true);

        service.registerNonCommercial("new@example.com");

        verify(emailGateway, times(1)).sendSystemEmail(
            eq("new@example.com"), anyString(), anyString());
    }

    @Test
    void registerNonCommercial_invalidEmailFormat_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            service.registerNonCommercial("bad-email"),
            "Invalid email should throw IllegalArgumentException");

        verifyNoInteractions(userDAO, emailGateway);
    }

    @Test
    void registerNonCommercial_duplicateEmail_throwsIllegalArgumentException() {
        User existing = new User(5, "taken@example.com", "hash", UserRole.MEMBER, false, null);
        when(userDAO.findByEmail("taken@example.com")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () ->
            service.registerNonCommercial("taken@example.com"),
            "Duplicate email should throw IllegalArgumentException");

        verify(userDAO, never()).save(any());
        verify(emailGateway, never()).sendSystemEmail(anyString(), anyString(), anyString());
    }

    @Test
    void registerNonCommercial_createdUserHasMemberRoleAndMustChangePassword() {
        when(userDAO.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(memberApplicationDAO.save(any(MemberApplication.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailGateway.sendSystemEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // Capture the User passed to save()
        when(userDAO.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            assertEquals(UserRole.MEMBER, u.getRole(), "Role should be MEMBER");
            assertTrue(u.isMustChangePassword(), "mustChangePassword should be true");
            return u;
        });

        service.registerNonCommercial("new@example.com");
    }
}
