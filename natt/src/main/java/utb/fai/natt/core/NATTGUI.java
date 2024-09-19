package utb.fai.natt.core;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import utb.fai.natt.spi.NATTLogger;

public class NATTGUI {

    private JPanel logPanel;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private JFrame frame;

    private CountDownLatch latch;

    private int guiWidth;
    private int guiHeight;

    private float logsFontSize;

    public NATTGUI() {
        this.latch = new CountDownLatch(1);

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

        // base font size for buttons
        Font baseFont = new Font("Arial", Font.PLAIN, (int) (screenSize.width * 0.008));
        // base font size for logs
        logsFontSize = (float) (screenSize.width * 0.007);

        // panel for logs
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(new Color(40, 40, 40));

        // scroll panel for log panel
        scrollPane = new JScrollPane(logPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(20);
        frame.add(scrollPane, BorderLayout.CENTER);

        // control panel with buttons on one line
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // font size
        JButton decreaseFontSizeButton = new JButton(" - ");
        decreaseFontSizeButton.setFont(baseFont);
        decreaseFontSizeButton.addActionListener(e -> adjustFontSize(-2));

        JButton increaseFontSizeButton = new JButton(" + ");
        increaseFontSizeButton.setFont(baseFont);
        increaseFontSizeButton.addActionListener(e -> adjustFontSize(2));

        // test control
        JButton reportButton = new JButton(" Show Report ");
        reportButton.setFont(baseFont);
        reportButton.addActionListener(e -> showReportInBrowser());

        controlPanel.add(decreaseFontSizeButton);
        controlPanel.add(increaseFontSizeButton);
        controlPanel.add(reportButton);

        frame.add(controlPanel, BorderLayout.NORTH);

        statusLabel = new JLabel("Testing in progress...");
        statusLabel.setFont(baseFont);
        frame.add(statusLabel, BorderLayout.SOUTH);

        frame.setVisible(true);

        // register log callbacks
        NATTLogger.LogCallbackHandler.getInstance().registerCallback(new NATTLogger.LogCallback() {
            @Override
            public void onComplete(String time, String level, String className, String message) {
                addLogMessage(time, className, level, message);
            }
        });
    }

    public void closeGUI() {
        frame.setVisible(false);
        frame.dispose();
        latch.countDown();
    }

    public CountDownLatch getGUIClosedLatch() {
        return latch;
    }

    public void showReportInBrowser() {
        try {
            String jarPath = new java.io.File(NATTGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .getParent();
            URI reportUri = new URI("file://" + jarPath + "/test_report.html");
            Desktop.getDesktop().browse(reportUri);
            JOptionPane.showMessageDialog(frame, "Report opened in the default browser.");
        } catch (IOException | URISyntaxException e) {
            JOptionPane.showMessageDialog(frame, "Failed to open the report!");
        }
    }

    public void addLogMessage(String time, String packageName, String type, String message) {
        LogMessagePanel logMessage = new LogMessagePanel(time, packageName, type, message, logsFontSize);
        logPanel.add(logMessage);
        logPanel.revalidate();
        logPanel.repaint();

        // automatic scroll down
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    private void adjustFontSize(int delta) {
        logsFontSize += delta;
        for (Component component : logPanel.getComponents()) {
            if (component instanceof LogMessagePanel) {
                LogMessagePanel logPanel = (LogMessagePanel) component;
                logPanel.updateFontSize(logsFontSize);
            }
        }
        logPanel.revalidate();
        logPanel.repaint();
    }

    public class LogMessagePanel extends JPanel {
        private JLabel timeLabel;
        private JLabel typeLabel;
        private JLabel packageLabel;
        private JTextArea messageArea;
        private JPanel headerPanel;
        private JPanel messagePanel;
        private Color messageColor;

        public LogMessagePanel(String time, String packageName, String type, String message, float baseFontSize) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(15, 10, 15, 10));
            setMaximumSize(new Dimension((int) (guiWidth * 0.65), Integer.MAX_VALUE));

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
            timeLabel.setFont(new Font("Arial", Font.PLAIN, (int) baseFontSize));

            typeLabel = new JLabel(type);
            typeLabel.setForeground(Color.WHITE);
            typeLabel.setFont(new Font("Arial", Font.PLAIN, (int) baseFontSize));

            JPanel timeTypePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(timeTypeBackgroundColor);
                    g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
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
            packageLabel.setFont(new Font("Arial", Font.BOLD, (int) baseFontSize));
            packageLabel.setForeground(Color.BLACK);

            messageArea = new JTextArea(message);
            messageArea.setFont(new Font("Arial", Font.PLAIN, (int) baseFontSize));
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

            adjustComponentHeight();

            revalidate();
            repaint();
        }

        private void adjustComponentHeight() {
            int messageHeight = messageArea.getPreferredSize().height;
            int headerHeight = headerPanel.getPreferredSize().height;
            int totalHeight = headerHeight + messageHeight + 40;

            setPreferredSize(new Dimension((int) (guiWidth * 0.65), totalHeight));
            setMaximumSize(new Dimension((int) (guiWidth * 0.65), totalHeight));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(messageColor);
            g2d.fill(new RoundRectangle2D.Double(10, 15, getWidth() - 20, getHeight() - 30, 30, 30));
        }

        public void updateFontSize(float newFontSize) {
            timeLabel.setFont(new Font("Arial", Font.PLAIN, (int) newFontSize));
            typeLabel.setFont(new Font("Arial", Font.PLAIN, (int) newFontSize));
            packageLabel.setFont(new Font("Arial", Font.BOLD, (int) newFontSize));
            messageArea.setFont(new Font("Arial", Font.PLAIN, (int) newFontSize));

            adjustComponentHeight();
        }

    }

}
