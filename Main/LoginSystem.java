package Main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class LoginSystem extends JFrame {

    private static final long serialVersionUID = 1L;

    // =========================================================
    // COLORS
    // =========================================================

    private static final Color BG = new Color(15, 23, 42);
    private static final Color SIDEBAR = new Color(17, 24, 39);
    private static final Color CARD = new Color(30, 41, 59);
    private static final Color CARD2 = new Color(51, 65, 85);
    private static final Color BLUE = new Color(59, 130, 246);
    private static final Color BLUE_DARK = new Color(37, 99, 235);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color RED = new Color(239, 68, 68);
    private static final Color YELLOW = new Color(234, 179, 8);
    private static final Color PURPLE = new Color(139, 92, 246);
    private static final Color TEXT = new Color(241, 245, 249);
    private static final Color MUTED = new Color(148, 163, 184);

    // =========================================================
    // ACCOUNT FILE
    // =========================================================

    private static final String ACCOUNT_FILE = "accounts.txt";
    private static final String DATA_DIRECTORY = "team_workspace_data";

    // =========================================================
    // SINGLETON DESIGN PATTERN
    // =========================================================

    static class UserSession {

        private static UserSession instance;

        private String username;

        private UserSession() {
        }

        public static UserSession getInstance() {

            if (instance == null) {
                instance = new UserSession();
            }

            return instance;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void logout() {
            username = null;
        }
    }

    // =========================================================
    // TASK CLASS
    // =========================================================

    static class TaskData implements Serializable {

        private static final long serialVersionUID = 1L;

        String title;
        String priority;
        String deadline;
        String assignedTo;
        boolean completed;

        TaskData(
                String title,
                String priority,
                String deadline,
                String assignedTo) {

            this.title = title;
            this.priority = priority;
            this.deadline = deadline;
            this.assignedTo = assignedTo;
            this.completed = false;
        }

        @Override
        public String toString() {

            String status;

            if (completed) {
                status = "[DONE]";
            } else {
                status = "[TODO]";
            }

            return status
                    + "  "
                    + title
                    + "  | Priority: "
                    + priority
                    + "  | Due: "
                    + deadline
                    + "  | Assigned: "
                    + assignedTo;
        }
    }

    // =========================================================
    // SPACE CLASS
    // =========================================================

    static class SpaceData implements Serializable {

        private static final long serialVersionUID = 1L;

        String name;
        String description;

        ArrayList<String> members =
                new ArrayList<String>();

        ArrayList<TaskData> tasks =
                new ArrayList<TaskData>();

        ArrayList<String> notes =
                new ArrayList<String>();

        ArrayList<String> achievements =
                new ArrayList<String>();

        int xp = 0;

        SpaceData(
                String name,
                String description) {

            this.name = name;
            this.description = description;
        }

        int completedTasks() {

            int count = 0;

            for (TaskData task : tasks) {

                if (task.completed) {
                    count++;
                }
            }

            return count;
        }

        int progress() {

            if (tasks.size() == 0) {
                return 0;
            }

            return (completedTasks() * 100)
                    / tasks.size();
        }

        int level() {

            if (xp >= 350) {
                return 5;
            }

            if (xp >= 200) {
                return 4;
            }

            if (xp >= 100) {
                return 3;
            }

            if (xp >= 50) {
                return 2;
            }

            return 1;
        }

        String levelName() {

            switch (level()) {

                case 5:
                    return "Workspace Legend";

                case 4:
                    return "Task Master";

                case 3:
                    return "Productive Team";

                case 2:
                    return "Active Team";

                default:
                    return "New Team";
            }
        }
    }

    // =========================================================
    // VARIABLES
    // =========================================================

    private ArrayList<SpaceData> spaces =
            new ArrayList<SpaceData>();

    private JPanel contentPanel;

    private JTextField usernameField;
    private JPasswordField passwordField;

    private JCheckBox showPassword;

    private JProgressBar loginProgress;

    // Kept separately so the whiteboard survives tab changes.
    private String whiteboardText = "";

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public LoginSystem() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveCurrentUserData();
            }
        });
        showLogin();
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {

                        new LoginSystem();
                    }
                }
        );
    }

    // =========================================================
    // LABEL
    // =========================================================

    private JLabel makeLabel(
            String text,
            int size,
            int style,
            Color color) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font(
                        "Segoe UI",
                        style,
                        size
                )
        );

        label.setForeground(color);

        return label;
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private JButton makeButton(String text) {

        JButton button = new JButton(text);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        button.setForeground(Color.WHITE);
        button.setBackground(BLUE);
        button.setFocusPainted(false);

        button.setBorder(
                new EmptyBorder(
                        10,
                        18,
                        10,
                        18
                )
        );

        button.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(
                            MouseEvent e) {

                        button.setBackground(
                                BLUE_DARK
                        );
                    }

                    @Override
                    public void mouseExited(
                            MouseEvent e) {

                        button.setBackground(
                                BLUE
                        );
                    }
                }
        );

        return button;
    }

    // =========================================================
    // SIDEBAR BUTTON
    // =========================================================

    private JButton sidebarButton(String text) {

        JButton button =
                new JButton(text);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        button.setForeground(MUTED);
        button.setBackground(SIDEBAR);

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setFocusPainted(false);

        button.setBorder(
                new EmptyBorder(
                        12,
                        18,
                        12,
                        18
                )
        );

        button.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(
                            MouseEvent e) {

                        button.setBackground(CARD);
                        button.setForeground(Color.WHITE);
                    }

                    @Override
                    public void mouseExited(
                            MouseEvent e) {

                        button.setBackground(SIDEBAR);
                        button.setForeground(MUTED);
                    }
                }
        );

        return button;
    }

    // =========================================================
    // TEXT FIELD STYLE
    // =========================================================

    private void styleField(JTextField field) {

        field.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        field.setForeground(TEXT);
        field.setBackground(BG);
        field.setCaretColor(Color.WHITE);

        field.setBorder(
                new EmptyBorder(
                        12,
                        12,
                        12,
                        12
                )
        );

        field.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        45
                )
        );
    }

    // =========================================================
    // CARD
    // =========================================================

    private JPanel createCard() {

        JPanel panel = new JPanel();

        panel.setBackground(CARD);

        panel.setBorder(
                new EmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        return panel;
    }

    // =========================================================
    // PASSWORD EYE BUTTON
    // =========================================================

    private JButton createEyeButton(
            JPasswordField passwordField) {

        JButton eye =
                new JButton("Show");

        eye.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        eye.setForeground(Color.WHITE);
        eye.setBackground(CARD2);
        eye.setFocusPainted(false);

        eye.setBorder(
                new EmptyBorder(
                        8,
                        10,
                        8,
                        10
                )
        );

        eye.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        eye.addActionListener(
                new ActionListener() {

                    boolean showing = false;

                    @Override
                    public void actionPerformed(
                            ActionEvent e) {

                        showing = !showing;

                        if (showing) {

                            passwordField.setEchoChar(
                                    (char) 0
                            );

                            eye.setText("Hide");

                        } else {

                            passwordField.setEchoChar(
                                    '•'
                            );

                            eye.setText("Show");
                        }
                    }
                }
        );

        return eye;
    }

    // =========================================================
    // PASSWORD FIELD WITH EYE
    // =========================================================

    private JPanel passwordFieldPanel(
            JPasswordField field) {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setBackground(BG);

        styleField(field);

        field.setEchoChar('•');

        panel.add(
                field,
                BorderLayout.CENTER
        );

        panel.add(
                createEyeButton(field),
                BorderLayout.EAST
        );

        panel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        45
                )
        );

        return panel;
    }

    // =========================================================
    // LOGIN SCREEN
    // =========================================================

    private void showLogin() {

        getContentPane().removeAll();

        setTitle(
                "Team Workspace Management System"
        );

        setSize(1050, 700);

        setMinimumSize(
                new Dimension(
                        950,
                        600
                )
        );

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLocationRelativeTo(null);

        JPanel main =
                new JPanel(
                        new GridLayout(
                                1,
                                2
                        )
                );

        main.setBackground(BG);

        // LEFT SIDE

        JPanel left = new JPanel();

        left.setBackground(BLUE_DARK);

        left.setLayout(
                new BoxLayout(
                        left,
                        BoxLayout.Y_AXIS
                )
        );

        left.setBorder(
                new EmptyBorder(
                        80,
                        60,
                        60,
                        60
                )
        );

        JLabel logo =
                makeLabel(
                        "TW",
                        45,
                        Font.BOLD,
                        Color.WHITE
                );

        JLabel title1 =
                makeLabel(
                        "Team Workspace",
                        29,
                        Font.BOLD,
                        Color.WHITE
                );

        JLabel title2 =
                makeLabel(
                        "Management System",
                        29,
                        Font.BOLD,
                        Color.WHITE
                );

        JLabel description =
                makeLabel(
                        "<html>"
                                + "Organize your team.<br>"
                                + "Track your progress.<br>"
                                + "Complete your goals."
                                + "</html>",
                        17,
                        Font.PLAIN,
                        new Color(
                                219,
                                234,
                                254
                        )
                );

        left.add(logo);

        left.add(
                Box.createVerticalStrut(35)
        );

        left.add(title1);
        left.add(title2);

        left.add(
                Box.createVerticalStrut(25)
        );

        left.add(description);

        left.add(
                Box.createVerticalGlue()
        );

        left.add(
                makeLabel(
                        "Java OOP + Singleton Design Pattern",
                        12,
                        Font.PLAIN,
                        new Color(
                                191,
                                219,
                                254
                        )
                )
        );

        // RIGHT SIDE

        JPanel right =
                new JPanel(
                        new GridBagLayout()
                );

        right.setBackground(BG);

        JPanel loginCard =
                new JPanel();

        loginCard.setBackground(CARD);

        loginCard.setBorder(
                new EmptyBorder(
                        35,
                        40,
                        35,
                        40
                )
        );

        loginCard.setLayout(
                new BoxLayout(
                        loginCard,
                        BoxLayout.Y_AXIS
                )
        );

        loginCard.setPreferredSize(
                new Dimension(
                        400,
                        480
                )
        );

        JLabel welcome =
                makeLabel(
                        "Welcome Back",
                        28,
                        Font.BOLD,
                        TEXT
                );

        JLabel subtitle =
                makeLabel(
                        "Sign in to continue to your workspace",
                        13,
                        Font.PLAIN,
                        MUTED
                );

        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField =
                new JTextField();

        passwordField =
                new JPasswordField();

        styleField(usernameField);

        // Keep the login controls a consistent width and centered in the card.
        Dimension loginControlSize = new Dimension(320, 45);
        usernameField.setPreferredSize(loginControlSize);
        usernameField.setMaximumSize(loginControlSize);
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        showPassword =
                new JCheckBox(
                        "Show password"
                );

        showPassword.setBackground(CARD);
        showPassword.setForeground(MUTED);
        showPassword.setFocusPainted(false);
        showPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        showPassword.setMaximumSize(new Dimension(320, 25));

        showPassword.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(
                            ActionEvent e) {

                        if (showPassword.isSelected()) {

                            passwordField.setEchoChar(
                                    (char) 0
                            );

                        } else {

                            passwordField.setEchoChar(
                                    '•'
                            );
                        }
                    }
                }
        );

        loginProgress =
                new JProgressBar(
                        0,
                        100
                );

        loginProgress.setValue(0);

        loginProgress.setString(
                "Ready"
        );

        loginProgress.setStringPainted(
                true
        );

        loginProgress.setForeground(BLUE);
        loginProgress.setBackground(BG);
        loginProgress.setPreferredSize(new Dimension(320, 20));
        loginProgress.setMaximumSize(new Dimension(320, 20));
        loginProgress.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton login =
                makeButton(
                        "SIGN IN"
                );

        login.setAlignmentX(Component.CENTER_ALIGNMENT);
        login.setPreferredSize(new Dimension(320, 44));
        login.setMaximumSize(new Dimension(320, 44));

        JButton create =
                makeButton(
                        "CREATE NEW ACCOUNT"
                );

        create.setBackground(CARD2);

        create.setAlignmentX(Component.CENTER_ALIGNMENT);
        create.setPreferredSize(new Dimension(320, 44));
        create.setMaximumSize(new Dimension(320, 44));

        login.addActionListener(
                e -> login()
        );

        create.addActionListener(
                e -> createAccount()
        );

        loginCard.add(welcome);

        loginCard.add(
                Box.createVerticalStrut(8)
        );

        loginCard.add(subtitle);

        loginCard.add(
                Box.createVerticalStrut(25)
        );

        
        JLabel usernameLabel = makeLabel(
                "Username", 12, Font.BOLD, MUTED
        );
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameLabel.setMaximumSize(new Dimension(320, 20));
        loginCard.add(usernameLabel);


        loginCard.add(
                Box.createVerticalStrut(5)
        );

        loginCard.add(usernameField);

        loginCard.add(
                Box.createVerticalStrut(15)
        );

        JLabel passwordLabel = makeLabel(
                "Password", 12, Font.BOLD, MUTED
        );
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordLabel.setMaximumSize(new Dimension(320, 20));
        loginCard.add(passwordLabel);


        loginCard.add(
                Box.createVerticalStrut(5)
        );

        JPanel passwordPanel = passwordFieldPanel(passwordField);
        passwordPanel.setPreferredSize(loginControlSize);
        passwordPanel.setMaximumSize(loginControlSize);
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginCard.add(passwordPanel);

        loginCard.add(showPassword);

        loginCard.add(
                Box.createVerticalStrut(15)
        );

        loginCard.add(loginProgress);

        loginCard.add(
                Box.createVerticalStrut(20)
        );

        loginCard.add(login);

        loginCard.add(
                Box.createVerticalStrut(10)
        );

        loginCard.add(create);

        GridBagConstraints cardConstraints = new GridBagConstraints();
        cardConstraints.gridx = 0;
        cardConstraints.gridy = 0;
        cardConstraints.anchor = GridBagConstraints.CENTER;
        right.add(loginCard, cardConstraints);

        main.add(left);
        main.add(right);

        setContentPane(main);

        revalidate();
        repaint();

        setVisible(true);
    }

    // =========================================================
    // CREATE ACCOUNT
    // =========================================================

    private void createAccount() {

        JTextField username =
                new JTextField();

        JPasswordField password =
                new JPasswordField();

        JPasswordField confirm =
                new JPasswordField();

        password.setEchoChar('•');
        confirm.setEchoChar('•');

        JPanel passwordPanel =
                passwordFieldPanel(password);

        JPanel confirmPanel =
                passwordFieldPanel(confirm);

        JPanel panel =
                new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        panel.setBorder(
                new EmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        panel.setPreferredSize(
                new Dimension(
                        350,
                        230
                )
        );

        panel.add(
                new JLabel("Username")
        );

        panel.add(
                Box.createVerticalStrut(5)
        );

        panel.add(username);

        panel.add(
                Box.createVerticalStrut(12)
        );

        panel.add(
                new JLabel("Password")
        );

        panel.add(
                Box.createVerticalStrut(5)
        );

        panel.add(passwordPanel);

        panel.add(
                Box.createVerticalStrut(12)
        );

        panel.add(
                new JLabel("Confirm Password")
        );

        panel.add(
                Box.createVerticalStrut(5)
        );

        panel.add(confirmPanel);

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Create Account",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String user =
                username.getText().trim();

        String pass =
                new String(
                        password.getPassword()
                );

        String confirmPass =
                new String(
                        confirm.getPassword()
                );

        if (user.length() == 0 ||
                pass.length() == 0 ||
                confirmPass.length() == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please complete all fields.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (user.contains("|") ||
                pass.contains("|")) {

            JOptionPane.showMessageDialog(
                    this,
                    "The character | cannot be used.",
                    "Invalid Character",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (pass.length() < 4) {

            JOptionPane.showMessageDialog(
                    this,
                    "Password must contain at least 4 characters.",
                    "Weak Password",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!pass.equals(confirmPass)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Passwords do not match.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        if (accountExists(user)) {

            JOptionPane.showMessageDialog(
                    this,
                    "That username already exists.",
                    "Account Exists",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            File file =
                    new File(ACCOUNT_FILE);

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter writer =
                    new FileWriter(
                            file,
                            true
                    );

            BufferedWriter bufferedWriter =
                    new BufferedWriter(writer);

            bufferedWriter.write(
                    user + "|" + pass
            );

            bufferedWriter.newLine();

            bufferedWriter.close();

            JOptionPane.showMessageDialog(
                    this,
                    "Account created successfully!\n\n"
                            + "Username: "
                            + user
                            + "\n\n"
                            + "You can now login.",
                    "Account Created",
                    JOptionPane.INFORMATION_MESSAGE
            );

            usernameField.setText(user);
            passwordField.setText("");

        } catch (IOException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to save the account.\n\n"
                            + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // ACCOUNT EXISTS
    // =========================================================

    private boolean accountExists(
            String username) {

        File file =
                new File(ACCOUNT_FILE);

        if (!file.exists()) {
            return false;
        }

        try {

            BufferedReader reader =
                    new BufferedReader(
                            new FileReader(file)
                    );

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts =
                        line.split(
                                "\\|",
                                -1
                        );

                if (parts.length >= 2) {

                    if (parts[0]
                            .trim()
                            .equalsIgnoreCase(username)) {

                        reader.close();

                        return true;
                    }
                }
            }

            reader.close();

        } catch (IOException ex) {

            return false;
        }

        return false;
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private void login() {

        String username =
                usernameField.getText().trim();

        String password =
                new String(
                        passwordField.getPassword()
                );

        if (username.length() == 0 ||
                password.length() == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your username and password.",
                    "Login Required",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        loginProgress.setValue(20);

        loginProgress.setString(
                "Checking account..."
        );

        Timer timer =
                new Timer(
                        150,
                        null
                );

        timer.addActionListener(
                new ActionListener() {

                    int progress = 20;

                    @Override
                    public void actionPerformed(
                            ActionEvent e) {

                        progress += 20;

                        loginProgress.setValue(
                                progress
                        );

                        if (progress < 60) {

                            loginProgress.setString(
                                    "Verifying..."
                            );

                        } else if (progress < 100) {

                            loginProgress.setString(
                                    "Loading..."
                            );

                        } else {

                            timer.stop();

                            boolean valid =
                                    checkLogin(
                                            username,
                                            password
                                    );

                            if (valid) {

                                UserSession
                                        .getInstance()
                                        .setUsername(
                                                username
                                        );

                                loginProgress.setValue(
                                        100
                                );

                                loginProgress.setString(
                                        "Login successful!"
                                );

                                loadUserData(username);

                                Timer openTimer =
                                        new Timer(
                                                400,
                                                null
                                        );

                                openTimer.addActionListener(
                                        new ActionListener() {

                                            @Override
                                            public void actionPerformed(
                                                    ActionEvent e) {

                                                openTimer.stop();

                                                showDashboard();
                                            }
                                        }
                                );

                                openTimer.setRepeats(false);

                                openTimer.start();

                            } else {

                                loginProgress.setValue(0);

                                loginProgress.setString(
                                        "Login failed"
                                );

                                JOptionPane.showMessageDialog(
                                        LoginSystem.this,
                                        "Incorrect username or password.",
                                        "Login Failed",
                                        JOptionPane.ERROR_MESSAGE
                                );
                            }
                        }
                    }
                }
        );

        timer.start();
    }

    // =========================================================
    // CHECK LOGIN
    // =========================================================

    private boolean checkLogin(
            String username,
            String password) {

        File file =
                new File(ACCOUNT_FILE);

        if (!file.exists()) {
            return false;
        }

        try {

            BufferedReader reader =
                    new BufferedReader(
                            new FileReader(file)
                    );

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts =
                        line.split(
                                "\\|",
                                -1
                        );

                if (parts.length >= 2) {

                    String savedUser =
                            parts[0].trim();

                    String savedPassword =
                            parts[1];

                    if (savedUser.equals(username)
                            && savedPassword.equals(password)) {

                        reader.close();

                        return true;
                    }
                }
            }

            reader.close();

        } catch (IOException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not read accounts.txt.\n\n"
                            + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        return false;
    }

    // =========================================================
    // USER DATA PERSISTENCE
    // =========================================================

    private File getUserDataFile(String username, String suffix) {
        String safeUsername = username.replaceAll("[^a-zA-Z0-9._-]", "_");
        File directory = new File(DATA_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return new File(directory, safeUsername + suffix);
    }

    @SuppressWarnings("unchecked")
    private void loadUserData(String username) {
        File spacesFile = getUserDataFile(username, "_spaces.dat");
        File boardFile = getUserDataFile(username, "_whiteboard.txt");

        if (spacesFile.exists()) {
            try (ObjectInputStream input = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(spacesFile)))) {
                Object stored = input.readObject();
                if (stored instanceof ArrayList<?>) {
                    ArrayList<?> loaded = (ArrayList<?>) stored;
                    boolean valid = true;
                    for (Object item : loaded) {
                        if (!(item instanceof SpaceData)) {
                            valid = false;
                            break;
                        }
                    }
                    if (valid) {
                        spaces = (ArrayList<SpaceData>) loaded;
                    } else {
                        loadDefaultSpaces();
                    }
                } else {
                    loadDefaultSpaces();
                }
            } catch (IOException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this,
                        "Saved workspace data could not be read. Default workspaces will be loaded.\n\n"
                                + "Details: " + ex.getMessage(),
                        "Saved Data Warning", JOptionPane.WARNING_MESSAGE);
                loadDefaultSpaces();
            }
        } else {
            loadDefaultSpaces();
        }

        whiteboardText = "";
        if (boardFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(boardFile), "UTF-8"))) {
                StringBuilder text = new StringBuilder();
                char[] buffer = new char[4096];
                int count;
                while ((count = reader.read(buffer)) != -1) {
                    text.append(buffer, 0, count);
                }
                whiteboardText = text.toString();
            } catch (IOException ex) {
                whiteboardText = "";
            }
        }

        saveCurrentUserData();
    }

    private void saveWhiteboardText() {
        String username = UserSession.getInstance().getUsername();
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        File boardFile = getUserDataFile(username, "_whiteboard.txt");
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(boardFile), "UTF-8")) {
            writer.write(whiteboardText == null ? "" : whiteboardText);
        } catch (IOException ex) {
            System.err.println("Could not save whiteboard: " + ex.getMessage());
        }
    }

    private void saveCurrentUserData() {
        String username = UserSession.getInstance().getUsername();
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        File spacesFile = getUserDataFile(username, "_spaces.dat");
        File tempFile = new File(spacesFile.getPath() + ".tmp");
        try (ObjectOutputStream output = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(tempFile)))) {
            output.writeObject(spaces);
            output.flush();
            if (!tempFile.renameTo(spacesFile)) {
                try (InputStream in = new FileInputStream(tempFile);
                     OutputStream out = new FileOutputStream(spacesFile)) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
                }
                tempFile.delete();
            }
        } catch (IOException ex) {
            System.err.println("Could not save workspace data: " + ex.getMessage());
        }

        File boardFile = getUserDataFile(username, "_whiteboard.txt");
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(boardFile), "UTF-8")) {
            writer.write(whiteboardText == null ? "" : whiteboardText);
        } catch (IOException ex) {
            System.err.println("Could not save whiteboard: " + ex.getMessage());
        }
    }

    // =========================================================
    // DEFAULT SPACES
    // =========================================================

    private void loadDefaultSpaces() {

        spaces.clear();

        SpaceData java =
                new SpaceData(
                        "Java Project",
                        "Team workspace for your Java project."
                );

        spaces.add(java);

        SpaceData personal =
                new SpaceData(
                        "Personal Workspace",
                        "Your personal tasks and notes."
                );

        spaces.add(personal);
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private void showDashboard() {

        getContentPane().removeAll();

        JPanel root =
                new JPanel(
                        new BorderLayout()
                );

        root.setBackground(BG);

        root.add(
                createSidebar(),
                BorderLayout.WEST
        );

        contentPanel =
                new JPanel(
                        new BorderLayout()
                );

        contentPanel.setBackground(BG);

        contentPanel.setBorder(
                new EmptyBorder(
                        25,
                        30,
                        25,
                        30
                )
        );

        root.add(
                contentPanel,
                BorderLayout.CENTER
        );

        setContentPane(root);

        showDashboardContent();

        revalidate();
        repaint();
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    private JPanel createSidebar() {

        JPanel sidebar =
                new JPanel(
                        new BorderLayout()
                );

        sidebar.setBackground(SIDEBAR);

        sidebar.setPreferredSize(
                new Dimension(
                        235,
                        0
                )
        );

        JPanel top =
                new JPanel();

        top.setBackground(SIDEBAR);

        top.setLayout(
                new BoxLayout(
                        top,
                        BoxLayout.Y_AXIS
                )
        );

        top.setBorder(
                new EmptyBorder(
                        25,
                        18,
                        15,
                        18
                )
        );

        top.add(
                makeLabel(
                        "TW",
                        30,
                        Font.BOLD,
                        BLUE
                )
        );

        top.add(
                Box.createVerticalStrut(8)
        );

        top.add(
                makeLabel(
                        "Team Workspace",
                        17,
                        Font.BOLD,
                        TEXT
                )
        );

        top.add(
                makeLabel(
                        "Management System",
                        11,
                        Font.PLAIN,
                        MUTED
                )
        );

        top.add(
                Box.createVerticalStrut(25)
        );

        JButton dashboard =
                sidebarButton("Dashboard");

        JButton spacesButton =
                sidebarButton("Spaces");

        JButton search =
                sidebarButton("Search");

        JButton whiteboard =
                sidebarButton("Whiteboard");

        JButton settings =
                sidebarButton("Settings");

        dashboard.addActionListener(
                e -> showDashboardContent()
        );

        spacesButton.addActionListener(
                e -> showSpaces()
        );

        search.addActionListener(
                e -> showSearch()
        );

        whiteboard.addActionListener(
                e -> showWhiteboard()
        );

        settings.addActionListener(
                e -> showSettings()
        );

        top.add(dashboard);

        top.add(
                Box.createVerticalStrut(5)
        );

        top.add(spacesButton);

        top.add(
                Box.createVerticalStrut(5)
        );

        top.add(search);

        top.add(
                Box.createVerticalStrut(5)
        );

        top.add(whiteboard);

        top.add(
                Box.createVerticalStrut(5)
        );

        top.add(settings);

        sidebar.add(
                top,
                BorderLayout.NORTH
        );

        JPanel bottom =
                new JPanel();

        bottom.setBackground(SIDEBAR);

        bottom.setLayout(
                new BoxLayout(
                        bottom,
                        BoxLayout.Y_AXIS
                )
        );

        bottom.setBorder(
                new EmptyBorder(
                        15,
                        18,
                        20,
                        18
                )
        );

        bottom.add(
                makeLabel(
                        "Logged in as: "
                                + UserSession
                                .getInstance()
                                .getUsername(),
                        11,
                        Font.BOLD,
                        GREEN
                )
        );

        bottom.add(
                Box.createVerticalStrut(10)
        );

        JButton logout =
                sidebarButton("Logout");

        logout.addActionListener(
                e -> logout()
        );

        bottom.add(logout);

        sidebar.add(
                bottom,
                BorderLayout.SOUTH
        );

        return sidebar;
    }

    // =========================================================
    // DASHBOARD CONTENT
    // =========================================================

    private void showDashboardContent() {

        contentPanel.removeAll();

        String user =
                UserSession
                        .getInstance()
                        .getUsername();

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(BG);

        JPanel titles =
                new JPanel();

        titles.setBackground(BG);

        titles.setLayout(
                new BoxLayout(
                        titles,
                        BoxLayout.Y_AXIS
                )
        );

        titles.add(
                makeLabel(
                        "Welcome back, "
                                + user
                                + "!",
                        27,
                        Font.BOLD,
                        TEXT
                )
        );

        titles.add(
                Box.createVerticalStrut(5)
        );

        titles.add(
                makeLabel(
                        "Here's what's happening in your workspace.",
                        13,
                        Font.PLAIN,
                        MUTED
                )
        );

        header.add(
                titles,
                BorderLayout.WEST
        );

        contentPanel.add(
                header,
                BorderLayout.NORTH
        );

        JPanel main =
                new JPanel(
                        new BorderLayout(
                                0,
                                20
                        )
                );

        main.setBackground(BG);

        int totalTasks = 0;
        int completed = 0;
        int totalXP = 0;

        for (SpaceData space : spaces) {

            totalTasks +=
                    space.tasks.size();

            completed +=
                    space.completedTasks();

            totalXP +=
                    space.xp;
        }

        JPanel stats =
                new JPanel(
                        new GridLayout(
                                1,
                                4,
                                15,
                                15
                        )
                );

        stats.setBackground(BG);

        stats.add(
                statCard(
                        "SPACES",
                        String.valueOf(spaces.size()),
                        "Workspaces",
                        BLUE
                )
        );

        stats.add(
                statCard(
                        "TASKS",
                        completed
                                + "/"
                                + totalTasks,
                        "Completed",
                        GREEN
                )
        );

        stats.add(
                statCard(
                        "XP",
                        String.valueOf(totalXP),
                        "Experience",
                        YELLOW
                )
        );

        stats.add(
                statCard(
                        "MEMBERS",
                        String.valueOf(totalMembers()),
                        "Team members",
                        PURPLE
                )
        );

        JPanel bottom =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                15,
                                15
                        )
                );

        bottom.setBackground(BG);

        bottom.add(
                recentTasks()
        );

        bottom.add(
                workspaceProgress()
        );

        main.add(
                stats,
                BorderLayout.NORTH
        );

        main.add(
                bottom,
                BorderLayout.CENTER
        );

        contentPanel.add(
                main,
                BorderLayout.CENTER
        );

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================
    // TOTAL MEMBERS
    // =========================================================

    private int totalMembers() {

        int total = 0;

        for (SpaceData space : spaces) {

            total +=
                    space.members.size();
        }

        return total;
    }

    // =========================================================
    // STAT CARD
    // =========================================================

    private JPanel statCard(
            String title,
            String value,
            String description,
            Color color) {

        JPanel panel =
                createCard();

        panel.setLayout(
                new BorderLayout(
                        12,
                        0
                )
        );

        JPanel bar =
                new JPanel();

        bar.setBackground(color);

        bar.setPreferredSize(
                new Dimension(
                        5,
                        0
                )
        );

        JPanel text =
                new JPanel();

        text.setBackground(CARD);

        text.setLayout(
                new BoxLayout(
                        text,
                        BoxLayout.Y_AXIS
                )
        );

        text.add(
                makeLabel(
                        title,
                        11,
                        Font.BOLD,
                        MUTED
                )
        );

        text.add(
                Box.createVerticalStrut(5)
        );

        text.add(
                makeLabel(
                        value,
                        27,
                        Font.BOLD,
                        TEXT
                )
        );

        text.add(
                makeLabel(
                        description,
                        11,
                        Font.PLAIN,
                        MUTED
                )
        );

        panel.add(
                bar,
                BorderLayout.WEST
        );

        panel.add(
                text,
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // RECENT TASKS
    // =========================================================

    private JPanel recentTasks() {

        JPanel panel =
                createCard();

        panel.setLayout(
                new BorderLayout(
                        0,
                        15
                )
        );

        panel.add(
                makeLabel(
                        "Recent Tasks",
                        18,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.NORTH
        );

        DefaultListModel<String> model =
                new DefaultListModel<String>();

        for (SpaceData space : spaces) {

            for (TaskData task : space.tasks) {

                model.addElement(
                        (task.completed
                                ? "[DONE] "
                                : "[TODO] ")
                                + task.title
                                + "  -  "
                                + space.name
                );
            }
        }

        if (model.isEmpty()) {

            model.addElement(
                    "No tasks yet. Add your first task in Spaces."
            );
        }

        JList<String> list =
                new JList<String>(model);

        list.setBackground(CARD);
        list.setForeground(TEXT);

        list.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        panel.add(
                new JScrollPane(list),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // WORKSPACE PROGRESS
    // =========================================================

    private JPanel workspaceProgress() {

        JPanel panel =
                createCard();

        panel.setLayout(
                new BorderLayout(
                        0,
                        15
                )
        );

        panel.add(
                makeLabel(
                        "Workspace Progress",
                        18,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.NORTH
        );

        JPanel list =
                new JPanel();

        list.setBackground(CARD);

        list.setLayout(
                new BoxLayout(
                        list,
                        BoxLayout.Y_AXIS
                )
        );

        for (SpaceData space : spaces) {

            JLabel name =
                    makeLabel(
                            space.name,
                            12,
                            Font.BOLD,
                            TEXT
                    );

            JProgressBar progress =
                    new JProgressBar(
                            0,
                            100
                    );

            progress.setValue(
                    space.progress()
            );

            progress.setString(
                    space.progress()
                            + "%"
            );

            progress.setStringPainted(true);

            progress.setForeground(BLUE);
            progress.setBackground(BG);

            list.add(name);

            list.add(
                    Box.createVerticalStrut(5)
            );

            list.add(progress);

            list.add(
                    Box.createVerticalStrut(15)
            );
        }

        panel.add(
                list,
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // SPACES
    // =========================================================

    private void showSpaces() {

        contentPanel.removeAll();

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(BG);

        header.add(
                makeLabel(
                        "Your Spaces",
                        27,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.WEST
        );

        JButton create =
                makeButton(
                        "+ NEW SPACE"
                );

        create.addActionListener(
                e -> createSpace()
        );

        header.add(
                create,
                BorderLayout.EAST
        );

        contentPanel.add(
                header,
                BorderLayout.NORTH
        );

        JPanel grid =
                new JPanel(
                        new GridLayout(
                                0,
                                2,
                                15,
                                15
                        )
                );

        grid.setBackground(BG);

        for (SpaceData space : spaces) {

            grid.add(
                    spaceCard(space)
            );
        }

        contentPanel.add(
                new JScrollPane(grid),
                BorderLayout.CENTER
        );

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================
    // SPACE CARD
    // =========================================================

    private JPanel spaceCard(
            SpaceData space) {

        JPanel panel =
                createCard();

        panel.setLayout(
                new BorderLayout(
                        0,
                        12
                )
        );

        JPanel info =
                new JPanel();

        info.setBackground(CARD);

        info.setLayout(
                new BoxLayout(
                        info,
                        BoxLayout.Y_AXIS
                )
        );

        info.add(
                makeLabel(
                        space.name,
                        19,
                        Font.BOLD,
                        TEXT
                )
        );

        info.add(
                Box.createVerticalStrut(7)
        );

        info.add(
                makeLabel(
                        space.description,
                        12,
                        Font.PLAIN,
                        MUTED
                )
        );

        info.add(
                Box.createVerticalStrut(15)
        );

        info.add(
                makeLabel(
                        space.members.size()
                                + " members     "
                                + space.completedTasks()
                                + "/"
                                + space.tasks.size()
                                + " tasks",
                        11,
                        Font.PLAIN,
                        MUTED
                )
        );

        JProgressBar progress =
                new JProgressBar(
                        0,
                        100
                );

        progress.setValue(
                space.progress()
        );

        progress.setString(
                space.progress()
                        + "%"
        );

        progress.setStringPainted(true);

        progress.setForeground(BLUE);
        progress.setBackground(BG);

        JButton open =
                makeButton(
                        "OPEN SPACE"
                );

        open.addActionListener(
                e -> openSpace(space)
        );

        panel.add(
                info,
                BorderLayout.NORTH
        );

        panel.add(
                progress,
                BorderLayout.CENTER
        );

        panel.add(
                open,
                BorderLayout.SOUTH
        );

        return panel;
    }

    // =========================================================
    // CREATE SPACE
    // =========================================================

    private void createSpace() {

        JTextField name =
                new JTextField();

        JTextArea description =
                new JTextArea(
                        4,
                        25
                );

        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                8,
                                8
                        )
                );

        panel.add(
                new JLabel("Space Name"),
                BorderLayout.NORTH
        );

        panel.add(
                name,
                BorderLayout.CENTER
        );

        JPanel bottom =
                new JPanel(
                        new BorderLayout(
                                5,
                                5
                        )
                );

        bottom.add(
                new JLabel("Description"),
                BorderLayout.NORTH
        );

        bottom.add(
                new JScrollPane(description),
                BorderLayout.CENTER
        );

        panel.add(
                bottom,
                BorderLayout.SOUTH
        );

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Create Space",
                        JOptionPane.OK_CANCEL_OPTION
                );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String spaceName =
                name.getText().trim();

        String desc =
                description.getText().trim();

        if (spaceName.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a space name."
            );

            return;
        }

        if (desc.isEmpty()) {
            desc = "Team workspace";
        }

        SpaceData space =
                new SpaceData(
                        spaceName,
                        desc
                );

        spaces.add(space);
        saveCurrentUserData();

        showSpaces();
    }

    // =========================================================
    // OPEN SPACE
    // =========================================================

    private void openSpace(
            SpaceData space) {

        contentPanel.removeAll();

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(BG);

        JPanel title =
                new JPanel();

        title.setBackground(BG);

        title.setLayout(
                new BoxLayout(
                        title,
                        BoxLayout.Y_AXIS
                )
        );

        title.add(
                makeLabel(
                        space.name,
                        27,
                        Font.BOLD,
                        TEXT
                )
        );

        title.add(
                Box.createVerticalStrut(5)
        );

        title.add(
                makeLabel(
                        space.description,
                        13,
                        Font.PLAIN,
                        MUTED
                )
        );

        JButton back =
                makeButton("BACK");

        back.addActionListener(
                e -> showSpaces()
        );

        header.add(
                title,
                BorderLayout.WEST
        );

        header.add(
                back,
                BorderLayout.EAST
        );

        contentPanel.add(
                header,
                BorderLayout.NORTH
        );

        JTabbedPane tabs =
                new JTabbedPane();

        tabs.addTab(
                "Members",
                membersTab(space)
        );

        tabs.addTab(
                "Tasks",
                tasksTab(space)
        );

        tabs.addTab(
                "Achievements",
                achievementsTab(space)
        );

        tabs.addTab(
                "Rewards",
                rewardsTab(space)
        );

        tabs.addTab(
                "Deadlines",
                deadlinesTab(space)
        );

        tabs.addTab(
                "Notes",
                notesTab(space)
        );

        tabs.addTab(
                "Summary",
                summaryTab(space)
        );

        contentPanel.add(
                tabs,
                BorderLayout.CENTER
        );

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================
    // MEMBERS TAB
    // =========================================================

    private JPanel membersTab(
            SpaceData space) {

        JPanel panel =
                createCard();

        panel.setLayout(
                new BorderLayout(
                        0,
                        15
                )
        );

        DefaultListModel<String> model =
                new DefaultListModel<String>();

        for (String member : space.members) {
            model.addElement(member);
        }

        if (model.isEmpty()) {

            model.addElement(
                    "No members yet. Add a member."
            );
        }

        JList<String> list =
                new JList<String>(model);

        list.setBackground(CARD);
        list.setForeground(TEXT);

        JButton add =
                makeButton(
                        "+ ADD MEMBER"
                );

        add.addActionListener(
                e -> {

                    String member =
                            JOptionPane.showInputDialog(
                                    this,
                                    "Member name:"
                            );

                    if (member != null &&
                            !member.trim().isEmpty()) {

                        String clean =
                                member.trim();

                        if (space.members.isEmpty()) {
                            model.clear();
                        }

                        space.members.add(clean);
                        saveCurrentUserData();

                        model.addElement(clean);

                        checkAchievements(space);
                    }
                }
        );

        JButton remove =
                makeButton(
                        "REMOVE"
                );

        remove.setBackground(RED);

        remove.addActionListener(
                e -> {

                    int index =
                            list.getSelectedIndex();

                    if (index >= 0 &&
                            index < space.members.size()) {

                        space.members.remove(index);
                        saveCurrentUserData();

                        model.clear();

                        for (String member :
                                space.members) {

                            model.addElement(member);
                        }

                        if (model.isEmpty()) {

                            model.addElement(
                                    "No members yet. Add a member."
                            );
                        }
                    }
                }
        );

        JPanel top =
                new JPanel(
                        new BorderLayout()
                );

        top.setBackground(CARD);

        top.add(
                makeLabel(
                        "Team Members",
                        19,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.WEST
        );

        top.add(
                add,
                BorderLayout.EAST
        );

        JPanel bottom =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        bottom.setBackground(CARD);

        bottom.add(remove);

        panel.add(
                top,
                BorderLayout.NORTH
        );

        panel.add(
                new JScrollPane(list),
                BorderLayout.CENTER
        );

        panel.add(
                bottom,
                BorderLayout.SOUTH
        );

        return panel;
    }

    // =========================================================
    // TASKS TAB
    // =========================================================

    private JPanel tasksTab(
            SpaceData space) {

        JPanel panel =
                createCard();

        panel.setLayout(
                new BorderLayout(
                        0,
                        15
                )
        );

        DefaultListModel<String> model =
                new DefaultListModel<String>();

        refreshTasks(
                space,
                model
        );

        JList<String> list =
                new JList<String>(model);

        list.setBackground(CARD);
        list.setForeground(TEXT);

        list.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        JButton add =
                makeButton(
                        "+ ADD TASK"
                );

        add.addActionListener(
                e -> addTask(
                        space,
                        model
                )
        );

        JButton complete =
                makeButton(
                        "TOGGLE COMPLETE"
                );

        complete.addActionListener(
                e -> {

                    int index =
                            list.getSelectedIndex();

                    if (index >= 0 &&
                            index < space.tasks.size()) {

                        TaskData task =
                                space.tasks.get(index);

                        task.completed =
                                !task.completed;
                        saveCurrentUserData();

                        refreshTasks(
                                space,
                                model
                        );

                        checkAchievements(
                                space
                        );
                    }
                }
        );

        JButton delete =
                makeButton(
                        "DELETE"
                );

        delete.setBackground(RED);

        delete.addActionListener(
                e -> {

                    int index =
                            list.getSelectedIndex();

                    if (index >= 0 &&
                            index < space.tasks.size()) {

                        space.tasks.remove(index);
                        saveCurrentUserData();

                        refreshTasks(
                                space,
                                model
                        );
                    }
                }
        );

        JPanel top =
                new JPanel(
                        new BorderLayout()
                );

        top.setBackground(CARD);

        top.add(
                makeLabel(
                        "Tasks",
                        19,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.WEST
        );

        top.add(
                add,
                BorderLayout.EAST
        );

        JPanel bottom =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        bottom.setBackground(CARD);

        bottom.add(complete);
        bottom.add(delete);

        panel.add(
                top,
                BorderLayout.NORTH
        );

        panel.add(
                new JScrollPane(list),
                BorderLayout.CENTER
        );

        panel.add(
                bottom,
                BorderLayout.SOUTH
        );

        return panel;
    }

    // =========================================================
    // ADD TASK
    // MEMBER DROPDOWN
    // =========================================================

    private void addTask(
            SpaceData space,
            DefaultListModel<String> model) {

        JTextField title =
                new JTextField();

        JComboBox<String> priority =
                new JComboBox<String>(
                        new String[]{
                                "Low",
                                "Medium",
                                "High"
                        }
                );

        JTextField deadline =
                new JTextField();

        // Member dropdown
        JComboBox<String> assigned =
                new JComboBox<String>();

        if (space.members.isEmpty()) {

            assigned.addItem(
                    "Not assigned"
            );

        } else {

            for (String member :
                    space.members) {

                assigned.addItem(member);
            }
        }

        JPanel form =
                new JPanel(
                        new GridLayout(
                                8,
                                1,
                                6,
                                6
                        )
                );

        form.add(
                new JLabel(
                        "Task Name"
                )
        );

        form.add(title);

        form.add(
                new JLabel(
                        "Priority"
                )
        );

        form.add(priority);

        form.add(
                new JLabel(
                        "Deadline"
                )
        );

        form.add(deadline);

        form.add(
                new JLabel(
                        "Assigned To"
                )
        );

        form.add(assigned);

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        form,
                        "Add Task",
                        JOptionPane.OK_CANCEL_OPTION
                );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String taskName =
                title.getText().trim();

        if (taskName.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter a task name."
            );

            return;
        }

        String assignedName =
                assigned
                        .getSelectedItem()
                        .toString();

        
        
        String dueDate =
        deadline.getText().trim();

        if (dueDate.isEmpty()) {
        dueDate = "Not set";
        } else {
        try {
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter
                        .ofPattern("MM-dd-uuuu")
                        .withResolverStyle(
                                java.time.format.ResolverStyle.STRICT
                        );

        LocalDate.parse(
                dueDate,
                formatter
        );

        } catch (DateTimeParseException e) {
        JOptionPane.showMessageDialog(
                this,
                "Please enter a valid date in MM-DD-YYYY format.",
                "Invalid Deadline",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }
}



        TaskData task =
                new TaskData(
                        taskName,
                        priority
                                .getSelectedItem()
                                .toString(),
                        dueDate,
                        assignedName
                );

        space.tasks.add(task);
        saveCurrentUserData();

        refreshTasks(
                space,
                model
        );

        checkAchievements(space);
    }

    // =========================================================
    // REFRESH TASKS
    // =========================================================

    private void refreshTasks(
            SpaceData space,
            DefaultListModel<String> model) {

        model.clear();

        for (TaskData task :
                space.tasks) {

            model.addElement(
                    task.toString()
            );
        }

        if (model.isEmpty()) {

            model.addElement(
                    "No tasks yet. Add your first task."
            );
        }
    }

    // =========================================================
    // ACHIEVEMENTS
    // =========================================================

    private JPanel achievementsTab(
            SpaceData space) {

        JPanel panel =
                createCard();

        panel.setLayout(
                new BorderLayout(
                        0,
                        15
                )
        );

        panel.add(
                makeLabel(
                        "Achievements",
                        19,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.NORTH
        );

        JPanel list =
                new JPanel();

        list.setBackground(CARD);

        list.setLayout(
                new BoxLayout(
                        list,
                        BoxLayout.Y_AXIS
                )
        );

        achievementRow(
                list,
                space,
                "FIRST TASK",
                "Create your first task",
                10
        );

        achievementRow(
                list,
                space,
                "HALF WAY",
                "Complete at least half of your tasks",
                25
        );

        achievementRow(
                list,
                space,
                "PROJECT COMPLETE",
                "Complete all tasks",
                50
        );

        achievementRow(
                list,
                space,
                "TEAM PLAYER",
                "Have at least 3 members",
                20
        );

        achievementRow(
                list,
                space,
                "PRIORITY MASTER",
                "Create a high priority task",
                40
        );

        achievementRow(
                list,
                space,
                "TASK CRUSHER",
                "Complete 3 tasks",
                50
        );

        achievementRow(
                list,
                space,
                "NOTE KEEPER",
                "Create a note",
                20
        );

        panel.add(
                new JScrollPane(list),
                BorderLayout.CENTER
        );

        return panel;
    }

    private void achievementRow(
            JPanel parent,
            SpaceData space,
            String name,
            String description,
            int xp) {

        boolean unlocked =
                space.achievements.contains(name);

        JPanel row =
                new JPanel(
                        new BorderLayout(
                                15,
                                0
                        )
                );

        row.setBackground(
                unlocked
                        ? new Color(
                                22,
                                101,
                                52
                        )
                        : BG
        );

        row.setBorder(
                new EmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        row.add(
                makeLabel(
                        unlocked
                                ? "[UNLOCKED]"
                                : "[LOCKED]",
                        12,
                        Font.BOLD,
                        unlocked
                                ? GREEN
                                : MUTED
                ),
                BorderLayout.WEST
        );

        JPanel text =
                new JPanel();

        text.setBackground(
                row.getBackground()
        );

        text.setLayout(
                new BoxLayout(
                        text,
                        BoxLayout.Y_AXIS
                )
        );

        text.add(
                makeLabel(
                        name,
                        13,
                        Font.BOLD,
                        TEXT
                )
        );

        text.add(
                makeLabel(
                        description,
                        11,
                        Font.PLAIN,
                        MUTED
                )
        );

        row.add(
                text,
                BorderLayout.CENTER
        );

        row.add(
                makeLabel(
                        "+" + xp + " XP",
                        12,
                        Font.BOLD,
                        YELLOW
                ),
                BorderLayout.EAST
        );

        parent.add(row);

        parent.add(
                Box.createVerticalStrut(8)
        );
    }

    // =========================================================
    // CHECK ACHIEVEMENTS
    // =========================================================

    private void checkAchievements(
            SpaceData space) {

        int total =
                space.tasks.size();

        int completed =
                space.completedTasks();

        if (total >= 1) {

            unlock(
                    space,
                    "FIRST TASK",
                    10
            );
        }

        if (total > 0 &&
                completed * 2 >= total) {

            unlock(
                    space,
                    "HALF WAY",
                    25
            );
        }

        if (total > 0 &&
                completed == total) {

            unlock(
                    space,
                    "PROJECT COMPLETE",
                    50
            );
        }

        if (space.members.size() >= 3) {

            unlock(
                    space,
                    "TEAM PLAYER",
                    20
            );
        }

        for (TaskData task :
                space.tasks) {

            if (task.priority
                    .equalsIgnoreCase("High")) {

                unlock(
                        space,
                        "PRIORITY MASTER",
                        40
                );

                break;
            }
        }

        if (completed >= 3) {

            unlock(
                    space,
                    "TASK CRUSHER",
                    50
            );
        }

        if (!space.notes.isEmpty()) {

            unlock(
                    space,
                    "NOTE KEEPER",
                    20
            );
        }
    }

    // =========================================================
    // UNLOCK
    // =========================================================

    private void unlock(
            SpaceData space,
            String name,
            int xp) {

        if (!space.achievements.contains(name)) {

            space.achievements.add(name);

            space.xp += xp;
            saveCurrentUserData();

            JOptionPane.showMessageDialog(
                    this,
                    "Achievement Unlocked!\n\n"
                            + name
                            + "\n\n+"
                            + xp
                            + " XP",
                    "Achievement",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    // =========================================================
    // REWARDS
    // =========================================================

    private JPanel rewardsTab(
            SpaceData space) {

        JPanel panel =
                createCard();

        panel.setLayout(
                new BorderLayout()
        );

        JPanel center =
                new JPanel();

        center.setBackground(CARD);

        center.setLayout(
                new BoxLayout(
                        center,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel level =
                makeLabel(
                        "LEVEL "
                                + space.level(),
                        36,
                        Font.BOLD,
                        PURPLE
                );

        level.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        JLabel levelName =
                makeLabel(
                        space.levelName(),
                        19,
                        Font.BOLD,
                        TEXT
                );

        levelName.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        JLabel xp =
                makeLabel(
                        space.xp + " XP",
                        15,
                        Font.BOLD,
                        YELLOW
                );

        xp.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        center.add(
                Box.createVerticalStrut(30)
        );

        center.add(level);

        center.add(
                Box.createVerticalStrut(8)
        );

        center.add(levelName);

        center.add(
                Box.createVerticalStrut(8)
        );

        center.add(xp);

        center.add(
                Box.createVerticalStrut(25)
        );

        JProgressBar progress =
                new JProgressBar(
                        0,
                        350
                );

        progress.setValue(
                Math.min(
                        space.xp,
                        350
                )
        );

        progress.setString(
                space.xp
                        + " / 350 XP"
        );

        progress.setStringPainted(true);

        progress.setForeground(PURPLE);
        progress.setBackground(BG);

        center.add(progress);

        center.add(
                Box.createVerticalStrut(30)
        );

        center.add(
                makeLabel(
                        "Level 1 - New Team",
                        13,
                        Font.BOLD,
                        MUTED
                )
        );

        center.add(
                Box.createVerticalStrut(8)
        );

        center.add(
                makeLabel(
                        "Level 2 - Active Team",
                        13,
                        Font.BOLD,
                        MUTED
                )
        );

        center.add(
                Box.createVerticalStrut(8)
        );

        center.add(
                makeLabel(
                        "Level 3 - Productive Team",
                        13,
                        Font.BOLD,
                        MUTED
                )
        );

        center.add(
                Box.createVerticalStrut(8)
        );

        center.add(
                makeLabel(
                        "Level 4 - Task Master",
                        13,
                        Font.BOLD,
                        MUTED
                )
        );

        center.add(
                Box.createVerticalStrut(8)
        );

        center.add(
                makeLabel(
                        "Level 5 - Workspace Legend",
                        13,
                        Font.BOLD,
                        MUTED
                )
        );

        panel.add(
                center,
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // DEADLINES
    // =========================================================

    private JPanel deadlinesTab(
            SpaceData space) {

        JPanel panel =
                createCard();

        panel.setLayout(
                new BorderLayout(
                        0,
                        15
                )
        );

        panel.add(
                makeLabel(
                        "Deadlines",
                        19,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.NORTH
        );

        DefaultListModel<String> model =
                new DefaultListModel<String>();

        for (TaskData task :
                space.tasks) {

            model.addElement(
                    task.deadline
                            + "  -  "
                            + task.title
            );
        }

        if (model.isEmpty()) {

            model.addElement(
                    "No deadlines yet."
            );
        }

        JList<String> list =
                new JList<String>(model);

        list.setBackground(CARD);
        list.setForeground(TEXT);

        panel.add(
                new JScrollPane(list),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // NOTES
    // =========================================================

    private JPanel notesTab(
            SpaceData space) {

        JPanel panel =
                createCard();

        panel.setLayout(
                new BorderLayout(
                        0,
                        15
                )
        );

        DefaultListModel<String> model =
                new DefaultListModel<String>();

        for (String note :
                space.notes) {

            model.addElement(note);
        }

        if (model.isEmpty()) {

            model.addElement(
                    "No notes yet."
            );
        }

        JList<String> list =
                new JList<String>(model);

        list.setBackground(CARD);
        list.setForeground(TEXT);

        JButton add =
                makeButton(
                        "+ ADD NOTE"
                );

        add.addActionListener(
                e -> {

                    JTextArea area =
                            new JTextArea(
                                    6,
                                    30
                            );

                    area.setLineWrap(true);
                    area.setWrapStyleWord(true);

                    int result =
                            JOptionPane.showConfirmDialog(
                                    this,
                                    new JScrollPane(area),
                                    "Add Note",
                                    JOptionPane.OK_CANCEL_OPTION
                            );

                    if (result ==
                            JOptionPane.OK_OPTION) {

                        String note =
                                area.getText()
                                        .trim();

                        if (!note.isEmpty()) {

                            if (space.notes.isEmpty()) {
                                model.clear();
                            }

                            space.notes.add(note);
                            saveCurrentUserData();

                            model.addElement(note);

                            checkAchievements(space);
                        }
                    }
                }
        );

        JPanel top =
                new JPanel(
                        new BorderLayout()
                );

        top.setBackground(CARD);

        top.add(
                makeLabel(
                        "Team Notes",
                        19,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.WEST
        );

        top.add(
                add,
                BorderLayout.EAST
        );

        panel.add(
                top,
                BorderLayout.NORTH
        );

        panel.add(
                new JScrollPane(list),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // SUMMARY
    // =========================================================

    private JPanel summaryTab(
            SpaceData space) {

        JPanel panel =
                createCard();

        panel.setLayout(
                new BorderLayout(
                        0,
                        20
                )
        );

        panel.add(
                makeLabel(
                        "Space Summary",
                        19,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.NORTH
        );

        JPanel grid =
                new JPanel(
                        new GridLayout(
                                4,
                                2,
                                12,
                                12
                        )
                );

        grid.setBackground(CARD);

        summaryItem(
                grid,
                "Members",
                String.valueOf(
                        space.members.size()
                )
        );

        summaryItem(
                grid,
                "Tasks",
                String.valueOf(
                        space.tasks.size()
                )
        );

        summaryItem(
                grid,
                "Completed",
                String.valueOf(
                        space.completedTasks()
                )
        );

        summaryItem(
                grid,
                "Progress",
                space.progress() + "%"
        );

        summaryItem(
                grid,
                "Achievements",
                String.valueOf(
                        space.achievements.size()
                )
        );

        summaryItem(
                grid,
                "XP",
                String.valueOf(
                        space.xp
                )
        );

        summaryItem(
                grid,
                "Level",
                String.valueOf(
                        space.level()
                )
        );

        summaryItem(
                grid,
                "Notes",
                String.valueOf(
                        space.notes.size()
                )
        );

        panel.add(
                grid,
                BorderLayout.CENTER
        );

        JButton delete =
                makeButton(
                        "DELETE SPACE"
                );

        delete.setBackground(RED);

        delete.addActionListener(
                e -> {

                    int result =
                            JOptionPane.showConfirmDialog(
                                    this,
                                    "Delete this space?",
                                    "Confirm",
                                    JOptionPane.YES_NO_OPTION
                            );

                    if (result ==
                            JOptionPane.YES_OPTION) {

                        spaces.remove(space);
                        saveCurrentUserData();

                        showSpaces();
                    }
                }
        );

        JPanel bottom =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        bottom.setBackground(CARD);

        bottom.add(delete);

        panel.add(
                bottom,
                BorderLayout.SOUTH
        );

        return panel;
    }

    // =========================================================
    // SUMMARY ITEM
    // =========================================================

    private void summaryItem(
            JPanel parent,
            String title,
            String value) {

        JPanel item =
                new JPanel(
                        new BorderLayout()
                );

        item.setBackground(BG);

        item.setBorder(
                new EmptyBorder(
                        12,
                        12,
                        12,
                        12
                )
        );

        item.add(
                makeLabel(
                        title,
                        11,
                        Font.BOLD,
                        MUTED
                ),
                BorderLayout.NORTH
        );

        item.add(
                makeLabel(
                        value,
                        22,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.CENTER
        );

        parent.add(item);
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private void showSearch() {

        contentPanel.removeAll();

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(BG);

        header.add(
                makeLabel(
                        "Search Workspace",
                        27,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.WEST
        );

        JTextField search =
                new JTextField();

        styleField(search);

        // Make the search bar clearly visible against the dark theme.
        search.setBackground(Color.WHITE);
        search.setForeground(Color.BLACK);
        search.setCaretColor(Color.BLACK);
        search.setSelectionColor(new Color(191, 219, 254));
        search.setSelectedTextColor(Color.BLACK);
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        search.setPreferredSize(
                new Dimension(
                        300,
                        45
                )
        );

        header.add(
                search,
                BorderLayout.EAST
        );

        DefaultListModel<String> model =
                new DefaultListModel<String>();

        JList<String> results =
                new JList<String>(model);

        results.setBackground(CARD);
        results.setForeground(TEXT);
        results.setSelectionBackground(BLUE);
        results.setSelectionForeground(TEXT);
        results.setFont(new Font("SansSerif", Font.PLAIN, 14));

        contentPanel.add(
                header,
                BorderLayout.NORTH
        );

        contentPanel.add(
                new JScrollPane(results),
                BorderLayout.CENTER
        );

        // Search updates automatically while the user types.
        DocumentListener searchListener =
                new DocumentListener() {

                    private void updateSearch() {

                        String query = search.getText()
                                .trim()
                                .toLowerCase();

                        model.clear();

                        if (query.isEmpty()) {
                            model.addElement("Type something to search...");
                            return;
                        }

                        int matchCount = 0;

                        for (SpaceData space : spaces) {

                            String spaceName = String.valueOf(space.name);
                            String description = String.valueOf(space.description);

                            // SPACE NAME
                            if (spaceName.toLowerCase().contains(query)) {
                                model.addElement("SPACE: " + spaceName);
                                matchCount++;
                            }

                            // SPACE DESCRIPTION
                            if (description.toLowerCase().contains(query)) {
                                model.addElement("SPACE DESCRIPTION: " + spaceName);
                                matchCount++;
                            }

                            // MEMBERS
                            if (space.members != null) {
                                for (String member : space.members) {
                                    String memberName = String.valueOf(member);
                                    if (memberName.toLowerCase().contains(query)) {
                                        model.addElement("MEMBER: " + memberName + " - " + spaceName);
                                        matchCount++;
                                    }
                                }
                            }

                            // TASKS AND TASK DETAILS
                            if (space.tasks != null) {
                                for (TaskData task : space.tasks) {
                                    if (task == null) {
                                        continue;
                                    }

                                    String taskTitle = String.valueOf(task.title);
                                    String assignedTo = String.valueOf(task.assignedTo);
                                    String priority = String.valueOf(task.priority);
                                    String deadline = String.valueOf(task.deadline);
                                    String status = task.completed ? "completed" : "incomplete";

                                    if (taskTitle.toLowerCase().contains(query)) {
                                        model.addElement("TASK: " + taskTitle + " - " + spaceName);
                                        matchCount++;
                                    }

                                    if (assignedTo.toLowerCase().contains(query)) {
                                        model.addElement("ASSIGNED TASK: " + taskTitle + " - " + assignedTo + " - " + spaceName);
                                        matchCount++;
                                    }

                                    if (priority.toLowerCase().contains(query)) {
                                        model.addElement("PRIORITY: " + priority + " - " + taskTitle + " - " + spaceName);
                                        matchCount++;
                                    }

                                    if (deadline.toLowerCase().contains(query)) {
                                        model.addElement("DEADLINE: " + deadline + " - " + taskTitle + " - " + spaceName);
                                        matchCount++;
                                    }

                                    if (status.contains(query)) {
                                        model.addElement("STATUS: " + status + " - " + taskTitle + " - " + spaceName);
                                        matchCount++;
                                    }
                                }
                            }

                            // NOTES
                            if (space.notes != null) {
                                for (String note : space.notes) {
                                    String noteText = String.valueOf(note);
                                    if (noteText.toLowerCase().contains(query)) {
                                        model.addElement("NOTE: " + noteText + " - " + spaceName);
                                        matchCount++;
                                    }
                                }
                            }

                            // ACHIEVEMENTS
                            if (space.achievements != null) {
                                for (String achievement : space.achievements) {
                                    String achievementText = String.valueOf(achievement);
                                    if (achievementText.toLowerCase().contains(query)) {
                                        model.addElement("ACHIEVEMENT: " + achievementText + " - " + spaceName);
                                        matchCount++;
                                    }
                                }
                            }

                            // WORKSPACE LEVEL
                            String levelName = String.valueOf(space.levelName());
                            if (levelName.toLowerCase().contains(query)) {
                                model.addElement("WORKSPACE LEVEL: " + levelName + " - " + spaceName);
                                matchCount++;
                            }
                        }

                        if (matchCount == 0) {
                            model.addElement("No results found for: " + search.getText().trim());
                        }
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateSearch();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateSearch();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateSearch();
                    }
                };

        search.getDocument().addDocumentListener(searchListener);

        model.addElement("Type something to search...");
        search.requestFocusInWindow();

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================
    // WHITEBOARD
    // =========================================================

    private void showWhiteboard() {

        contentPanel.removeAll();

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(BG);

        header.add(
                makeLabel(
                        "Team Whiteboard",
                        27,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.WEST
        );

        header.add(
                makeLabel(
                        "Write ideas and plans here.",
                        12,
                        Font.PLAIN,
                        MUTED
                ),
                BorderLayout.EAST
        );

        JTextArea board =
                new JTextArea(whiteboardText);

        board.getDocument().addDocumentListener(new DocumentListener() {
            private void updateText() {
                whiteboardText = board.getText();
                saveWhiteboardText();
            }
            @Override public void insertUpdate(DocumentEvent e) { updateText(); }
            @Override public void removeUpdate(DocumentEvent e) { updateText(); }
            @Override public void changedUpdate(DocumentEvent e) { updateText(); }
        });

        board.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        16
                )
        );

        board.setLineWrap(true);
        board.setWrapStyleWord(true);

        board.setBorder(
                new EmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        board.setBackground(Color.WHITE);

        board.setForeground(
                new Color(
                        15,
                        23,
                        42
                )
        );

        contentPanel.add(
                header,
                BorderLayout.NORTH
        );

        contentPanel.add(
                new JScrollPane(board),
                BorderLayout.CENTER
        );

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private void showSettings() {

        contentPanel.removeAll();

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(BG);

        header.add(
                makeLabel(
                        "Settings",
                        27,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.WEST
        );

        contentPanel.add(
                header,
                BorderLayout.NORTH
        );

        JPanel settings =
                createCard();

        settings.setLayout(
                new BoxLayout(
                        settings,
                        BoxLayout.Y_AXIS
                )
        );

        settings.add(
                makeLabel(
                        "Application Settings",
                        19,
                        Font.BOLD,
                        TEXT
                )
        );

        settings.add(
                Box.createVerticalStrut(20)
        );

        JCheckBox notifications =
                new JCheckBox(
                        "Achievement notifications",
                        true
                );

        notifications.setBackground(CARD);
        notifications.setForeground(TEXT);
        notifications.setFocusPainted(false);

        settings.add(notifications);

        settings.add(
                Box.createVerticalStrut(15)
        );

        settings.add(
                makeLabel(
                        "Current User: "
                                + UserSession
                                .getInstance()
                                .getUsername(),
                        13,
                        Font.PLAIN,
                        MUTED
                )
        );

        settings.add(
                Box.createVerticalStrut(25)
        );

        JButton reset =
                makeButton(
                        "RESET WORKSPACES"
                );

        reset.setBackground(RED);

        reset.addActionListener(
                e -> {

                    int result =
                            JOptionPane.showConfirmDialog(
                                    this,
                                    "Reset all workspaces?",
                                    "Reset",
                                    JOptionPane.YES_NO_OPTION
                            );

                    if (result ==
                            JOptionPane.YES_OPTION) {

                        loadDefaultSpaces();
                        saveCurrentUserData();

                        showDashboardContent();
                    }
                }
        );

        settings.add(reset);

        contentPanel.add(
                new JScrollPane(settings),
                BorderLayout.CENTER
        );

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private void logout() {

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to logout?",
                        "Logout",
                        JOptionPane.YES_NO_OPTION
                );

        if (result ==
                JOptionPane.YES_OPTION) {

            saveCurrentUserData();

            UserSession
                    .getInstance()
                    .logout();

            spaces.clear();

            showLogin();
        }
    }
}