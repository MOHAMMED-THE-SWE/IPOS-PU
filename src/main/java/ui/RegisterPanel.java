package ui;

import model.CommercialApplication;

import javax.swing.*;
import java.awt.*;

public class RegisterPanel extends JPanel {

    private final MainFrame frame;
    private final JTabbedPane tabs = new JTabbedPane();

    public RegisterPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Member Registration", SwingConstants.CENTER);
        title.setFont(UIConstants.TITLE_FONT);
        title.setForeground(UIConstants.BRAND_RED);
        title.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 4, 0));
        add(title, BorderLayout.NORTH);

        tabs.addTab("Non-Commercial", buildNonCommercialTab());
        tabs.addTab("Commercial Business", buildCommercialTab());
        add(tabs, BorderLayout.CENTER);

        JButton backBtn = UIConstants.secondaryButton("Back to Login");
        backBtn.addActionListener(e -> frame.showPanel("LOGIN"));
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(backBtn);
        add(south, BorderLayout.SOUTH);
    }

    private JPanel buildNonCommercialTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField emailField = new JTextField(30);
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Email Address:"), gbc);
        gbc.gridx = 1; panel.add(emailField, gbc);

        JLabel info = new JLabel("<html><i>A temporary password will be emailed to you.</i></html>");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(info, gbc);

        JButton registerBtn = UIConstants.primaryButton("Register");
        gbc.gridy = 2; panel.add(registerBtn, gbc);

        registerBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            try {
                String password = frame.getRegistrationService().registerNonCommercial(email);
                JOptionPane.showMessageDialog(frame,
                    "Registration successful!\n\nYour temporary password: " + password +
                    "\n\n(This has also been queued to be emailed to you.)\nPlease log in and change your password.",
                    "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                emailField.setText("");
                frame.showPanel("LOGIN");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel buildCommercialTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JTextField companyNameField = new JTextField(30);
        JTextField emailField = new JTextField(30);
        JTextField companyRegField = new JTextField(30);
        JTextField directorsField = new JTextField(30);
        JTextField businessTypeField = new JTextField(30);
        JTextArea addressArea = new JTextArea(3, 30);
        addressArea.setLineWrap(true);

        String[][] fields = {
            {"Company Name:", null},
            {"Email:", null},
            {"Company Reg. No.:", null},
            {"Directors:", null},
            {"Business Type:", null},
            {"Address:", null}
        };

        JComponent[] components = {companyNameField, emailField, companyRegField, directorsField,
            businessTypeField, new JScrollPane(addressArea)};

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.gridwidth = 1;
            panel.add(new JLabel(fields[i][0]), gbc);
            gbc.gridx = 1;
            panel.add(components[i], gbc);
        }

        JButton submitBtn = UIConstants.primaryButton("Submit Commercial Application");
        gbc.gridx = 0; gbc.gridy = fields.length; gbc.gridwidth = 2;
        panel.add(submitBtn, gbc);

        submitBtn.addActionListener(e -> {
            CommercialApplication app = new CommercialApplication(
                companyNameField.getText().trim(),
                companyRegField.getText().trim(),
                directorsField.getText().trim(),
                businessTypeField.getText().trim(),
                addressArea.getText().trim(),
                emailField.getText().trim()
            );
            try {
                int appId = frame.getRegistrationService().submitCommercialApplication(app);
                JOptionPane.showMessageDialog(frame,
                    "Commercial application submitted.\nApplication ID: " + appId +
                    "\n\nYour application will be reviewed by IPOS-SA. You will be notified by email.",
                    "Application Submitted", JOptionPane.INFORMATION_MESSAGE);
                frame.showPanel("LOGIN");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Submission Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }
}
