package utb.fai.natt.core;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class NATTGUI {

    private JPanel logPanel;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private JFrame frame;

    private int guiWidth;
    private int guiHeight;

    public NATTGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("NATT GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        guiWidth = (int) (screenSize.width * 0.7);
        guiHeight = (int) (screenSize.height * 0.7);
        frame.setSize(guiWidth, guiHeight);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // base font size
        Font baseFont = new Font("Arial", Font.PLAIN, (int) (screenSize.width * 0.007));

        // panel for logs
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(new Color(40, 40, 40));

        // scroll panel for log panel
        scrollPane = new JScrollPane(logPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frame.add(scrollPane, BorderLayout.CENTER);

        // control panel with buttons on one line
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton decreaseFontSizeButton = new JButton(" - ");
        decreaseFontSizeButton.setFont(baseFont);
        JButton increaseFontSizeButton = new JButton(" + ");
        increaseFontSizeButton.setFont(baseFont);
        JButton stopButton = new JButton(" Stop Testing ");
        stopButton.setFont(baseFont);
        JButton reportButton = new JButton(" Show Report ");
        reportButton.setFont(baseFont);

        controlPanel.add(decreaseFontSizeButton);
        controlPanel.add(increaseFontSizeButton);
        controlPanel.add(stopButton);
        controlPanel.add(reportButton);

        frame.add(controlPanel, BorderLayout.NORTH);

        statusLabel = new JLabel("Testing in progress...");
        statusLabel.setForeground(Color.WHITE);
        frame.add(statusLabel, BorderLayout.SOUTH);

        frame.setVisible(true);

        // test
        for (int i = 0; i < 10; i++) {
            addLogMessage("10:00", "utb.fai.natt", "INFO",
                    "Application started. Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.Application started.");
            addLogMessage("10:01", "utb.fai.natt", "WARNING", "Potential issue detected.");
            addLogMessage("10:02", "utb.fai.natt", "ERROR", "Unexpected error occurred.");
        }
    }

    public void addLogMessage(String time, String packageName, String type, String message) {
        LogMessagePanel logMessage = new LogMessagePanel(time, packageName, type, message);
        logPanel.add(logMessage);
        logPanel.revalidate();
        logPanel.repaint();
    }

    public class LogMessagePanel extends JPanel {
        private JLabel timeLabel;
        private JLabel packageLabel;
        private JTextArea messageArea;
        private JPanel headerPanel;
        private JPanel messagePanel;
        private Color messageColor;

        public LogMessagePanel(String time, String packageName, String type, String message) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(15, 10, 15, 10));

            switch (type.toUpperCase()) {
                case "WARNING":
                    messageColor = new Color(255, 204, 0);
                    break;
                case "ERROR":
                    messageColor = new Color(255, 80, 80);
                    break;
                default:
                    messageColor = new Color(190, 190, 190);
                    break;
            }

            Color timeTypeBackgroundColor = messageColor.darker().darker();

            headerPanel = new JPanel();
            headerPanel.setLayout(new GridBagLayout());
            headerPanel.setOpaque(false); 
            headerPanel.setBackground(messageColor);

            timeLabel = new JLabel(time);
            timeLabel.setForeground(Color.WHITE);
            timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));

            JLabel typeLabel = new JLabel(type);
            typeLabel.setForeground(Color.WHITE);
            typeLabel.setFont(new Font("Arial", Font.PLAIN, 14));

            JPanel timeTypePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(timeTypeBackgroundColor);
                    g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                }
            };
            timeTypePanel.setLayout(new GridBagLayout());
            timeTypePanel.setOpaque(false);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0;
            gbc.gridy = 0;
            timeTypePanel.add(timeLabel, gbc);

            gbc.gridy = 1;
            timeTypePanel.add(typeLabel, gbc);

            gbc.gridx = 0;
            gbc.gridy = 0;
            headerPanel.add(timeTypePanel, gbc);

            messagePanel = new JPanel();
            messagePanel.setLayout(new BorderLayout());
            messagePanel.setOpaque(false);

            packageLabel = new JLabel(packageName);
            packageLabel.setFont(new Font("Arial", Font.BOLD, 14));
            packageLabel.setForeground(Color.BLACK);

            messageArea = new JTextArea(message);
            messageArea.setFont(new Font("Arial", Font.PLAIN, 14));
            messageArea.setForeground(Color.BLACK);
            messageArea.setBackground(new Color(60, 60, 60));
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            messageArea.setEditable(false);
            messageArea.setOpaque(false);

            messagePanel.add(packageLabel, BorderLayout.NORTH);
            messagePanel.add(messageArea, BorderLayout.CENTER);

            add(headerPanel, BorderLayout.WEST);
            add(messagePanel, BorderLayout.CENTER);

            int messageHeight = messageArea.getPreferredSize().height;
            int headerHeight = headerPanel.getPreferredSize().height;
            int totalHeight = headerHeight + messageHeight + 20; 

            setPreferredSize(new Dimension((int) (guiWidth * 0.65), totalHeight));
            setMaximumSize(new Dimension((int) (guiWidth * 0.65), totalHeight));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(messageColor); 
            g2d.fill(new RoundRectangle2D.Double(10, 15, getWidth()- 20, getHeight() - 30, 30, 30));
        }

    }

}
