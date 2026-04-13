package ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginPanel extends JPanel {

    private final MainFrame frame;
    private final JTextField emailField = new JTextField(25);
    private final JPasswordField passwordField = new JPasswordField(25);

    public LoginPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());

        // Card wrapper — centred on screen
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(210, 210, 210), 1, true),
            new EmptyBorder(36, 48, 36, 48)
        ));
        card.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Member Login", SwingConstants.CENTER);
        title.setFont(UIConstants.TITLE_FONT);
        title.setForeground(UIConstants.BRAND_RED);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        JLabel emailLbl = new JLabel("Email / Username:");
        emailLbl.setFont(UIConstants.LABEL_FONT);
        card.add(emailLbl, gbc);
        gbc.gridx = 1; card.add(emailField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(UIConstants.LABEL_FONT);
        card.add(passLbl, gbc);
        gbc.gridx = 1; card.add(passwordField, gbc);

        JButton loginBtn    = UIConstants.primaryButton("Login");
        JButton registerBtn = UIConstants.secondaryButton("Register");
        JButton guestBtn    = UIConstants.secondaryButton("Browse as Guest");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);
        btnPanel.add(guestBtn);

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        card.add(btnPanel, gbc);

        add(card);

        loginBtn.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> frame.showPanel("REGISTER"));
        guestBtn.addActionListener(e -> frame.onLoginSuccess(null));
        passwordField.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        try {
            var user = frame.getAuthService().login(email, password);
            frame.onLoginSuccess(user);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
}
