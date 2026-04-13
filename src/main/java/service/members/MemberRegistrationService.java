package service.members;

import dao.members.CommercialApplicationDAO;
import dao.members.MemberApplicationDAO;
import dao.members.UserDAO;
import interfaces.ICommercialApplicationService;
import interfaces.IEmailGateway;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Handles member registration for IPOS-PU.
 *
 * <p>Supports two membership types:</p>
 * <ul>
 *   <li><b>Non-commercial</b> — validated by email format only; auto-approved with a
 *       generated temporary password sent via {@link interfaces.IEmailGateway}.</li>
 *   <li><b>Commercial</b> — requires full business details; forwarded to IPOS-SA via
 *       {@link interfaces.ICommercialApplicationService} for manual approval.</li>
 * </ul>
 *
 * @author Team C
 */
public class MemberRegistrationService {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[\\w.+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final String PASSWORD_CHARS =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

    private final UserDAO userDAO;
    private final MemberApplicationDAO memberApplicationDAO;
    private final CommercialApplicationDAO commercialApplicationDAO;
    private IEmailGateway emailGateway;
    private ICommercialApplicationService commercialApplicationService;

    /**
     * Creates a new MemberRegistrationService.
     *
     * @param userDAO                      DAO for persisting user accounts
     * @param memberApplicationDAO         DAO for persisting membership applications
     * @param commercialApplicationDAO     DAO for persisting commercial application details
     * @param emailGateway                 gateway for queueing credential emails
     * @param commercialApplicationService service for forwarding commercial applications to SA
     */
    public MemberRegistrationService(UserDAO userDAO,
                                     MemberApplicationDAO memberApplicationDAO,
                                     CommercialApplicationDAO commercialApplicationDAO,
                                     IEmailGateway emailGateway,
                                     ICommercialApplicationService commercialApplicationService) {
        this.userDAO = userDAO;
        this.memberApplicationDAO = memberApplicationDAO;
        this.commercialApplicationDAO = commercialApplicationDAO;
        this.emailGateway = emailGateway;
        this.commercialApplicationService = commercialApplicationService;
    }

    /**
     * Registers a new non-commercial member.
     *
     * <p>Validates the email format, generates a secure 10-character temporary password
     * (guaranteed to contain at least one letter, digit, and special character),
     * persists the user account with {@code must_change_password = true},
     * records an auto-approved application, and queues a credentials email.</p>
     *
     * @param email the applicant's email address (must be a valid format)
     * @return the generated plaintext temporary password
     * @throws IllegalArgumentException if the email format is invalid or is already registered
     */
    public String registerNonCommercial(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        if (userDAO.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        String password = generatePassword();
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hash);
        user.setRole(UserRole.MEMBER);
        user.setMustChangePassword(true);
        userDAO.save(user);

        // Record application as approved
        MemberApplication app = new MemberApplication(MemberApplication.Type.NON_COMMERCIAL, email);
        app.setStatus(ApplicationStatus.APPROVED);
        memberApplicationDAO.save(app);

        // Queue credentials email
        String body = "Welcome to IPOS-PU!\n\nYour account has been created.\n" +
                      "Email: " + email + "\nTemporary password: " + password +
                      "\n\nPlease log in and change your password immediately.";
        emailGateway.sendSystemEmail(email, "Your IPOS-PU Account Credentials", body);

        return password;
    }

    /**
     * Submits a commercial membership application.
     *
     * <p>Validates all required business fields, stores the application locally in
     * {@code pu_member_applications} and {@code pu_commercial_applications}, then
     * forwards it to IPOS-SA via {@link interfaces.ICommercialApplicationService}
     * for manual review and approval.</p>
     *
     * @param app the commercial application to submit (must not be null; all fields required)
     * @return the generated local application ID
     * @throws IllegalArgumentException if the application is null or any required field is missing/invalid
     */
    // TODO: maybe add email notification to admin when commercial app is submitted
    public int submitCommercialApplication(CommercialApplication app) {
        if (app == null) throw new IllegalArgumentException("Application must not be null");
        if (isBlank(app.getCompanyName())) throw new IllegalArgumentException("Company name required");
        if (!isValidEmail(app.getEmail())) throw new IllegalArgumentException("Invalid email in application");
        if (isBlank(app.getCompanyRegNo())) throw new IllegalArgumentException("Company registration number required");
        if (isBlank(app.getDirectors())) throw new IllegalArgumentException("Directors information required");
        if (isBlank(app.getBusinessType())) throw new IllegalArgumentException("Business type required");
        if (isBlank(app.getAddress())) throw new IllegalArgumentException("Address required");

        // Store in local tables
        MemberApplication memberApp = new MemberApplication(MemberApplication.Type.COMMERCIAL, app.getEmail());
        memberApplicationDAO.save(memberApp);
        app.setApplicationId(memberApp.getId());
        commercialApplicationDAO.save(app);

        // Forward to SA
        commercialApplicationService.submitCommercialApplication(app);

        return memberApp.getId();
    }

    /**
     * Validates an email address against the standard format pattern.
     *
     * @param email the email address to validate
     * @return {@code true} if the email is non-null and matches the expected format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private String generatePassword() {
        SecureRandom rnd = new SecureRandom();
        String letters  = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        String digits   = "0123456789";
        String specials = "!@#$%^&*";

        // Guarantee at least one of each required type (brief: letters/numbers/special)
        List<Character> chars = new ArrayList<>();
        chars.add(letters.charAt(rnd.nextInt(letters.length())));
        chars.add(digits.charAt(rnd.nextInt(digits.length())));
        chars.add(specials.charAt(rnd.nextInt(specials.length())));

        // Fill remaining 7 positions from full PASSWORD_CHARS
        for (int i = 3; i < 10; i++) {
            chars.add(PASSWORD_CHARS.charAt(rnd.nextInt(PASSWORD_CHARS.length())));
        }

        Collections.shuffle(chars, rnd);
        StringBuilder sb = new StringBuilder(10);
        for (char c : chars) sb.append(c);
        return sb.toString();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
