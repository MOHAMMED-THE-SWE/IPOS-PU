package email;

import interfaces.IEmailService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Implements {@link IEmailService} using Jakarta Mail over SMTP with STARTTLS.
 *
 * <p>Connects to the configured SMTP server (default: Gmail on port 587) and sends
 * emails synchronously. Intended to be called only by {@link EmailDispatcher},
 * which processes the outbox queue on a background thread. Credentials are read
 * from {@code config.properties} via {@link config.DatabaseConfig}.</p>
 *
 * @author Team C
 */
public class SmtpEmailService implements IEmailService {

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    /**
     * Creates a new SmtpEmailService.
     *
     * @param host     SMTP server hostname (e.g. {@code smtp.gmail.com})
     * @param port     SMTP port (typically 587 for STARTTLS)
     * @param username the sender email address and SMTP login
     * @param password the SMTP app password (not the Gmail account password)
     */
    public SmtpEmailService(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean sendEmail(String to, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
        return true;
    }
}
