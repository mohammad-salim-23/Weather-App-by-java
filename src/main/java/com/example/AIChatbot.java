package com.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AIChatbot extends JFrame {
    private JTextPane chatArea;
    private JTextField userInput;
    private JButton sendButton;
    private static final String API_KEY = "55a85549d9c94e2dcf4a9fe2be6d6610"; 

    public AIChatbot() {
        setTitle("Salim Chatbot 🤖");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(30, 30, 30));

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(44, 62, 80));
        headerPanel.setLayout(new BorderLayout());

        JLabel headerLabel = new JLabel("Salim Chatbot 🤖", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);

        JLabel helpText = new JLabel("What can I help with?", SwingConstants.CENTER);
        helpText.setFont(new Font("Arial", Font.PLAIN, 14));
        helpText.setForeground(Color.LIGHT_GRAY);

        headerPanel.add(headerLabel, BorderLayout.NORTH);
        headerPanel.add(helpText, BorderLayout.SOUTH);

        // Chat display area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setContentType("text/html");
        JScrollPane scrollPane = new JScrollPane(chatArea);

        // Image Panel
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imagePanel.setBackground(new Color(30, 30, 30));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        URL imageUrl = AIChatbot.class.getClassLoader().getResource("AIChatbot.jpg");
        JLabel imageLabel;
        if (imageUrl == null) {
            System.out.println("⚠️ Image not found!");
            imageLabel = new JLabel();
        } else {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            Image resizedImage = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(resizedImage);
            imageLabel = new JLabel(resizedIcon);
        }
        imagePanel.add(imageLabel);

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(30, 30, 30));

        userInput = new JTextField("Ask anything...");
        userInput.setFont(new Font("Arial", Font.PLAIN, 16));
        userInput.setForeground(Color.GRAY);

        // Placeholder behavior
        userInput.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (userInput.getText().equals("Ask anything...")) {
                    userInput.setText("");
                    userInput.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (userInput.getText().isEmpty()) {
                    userInput.setText("Ask anything...");
                    userInput.setForeground(Color.GRAY);
                }
            }
        });

        sendButton = new JButton("Send ➤");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.addActionListener(e -> sendUserMessage());

        inputPanel.add(userInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Fixing layout issue
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(imagePanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Adding components to JFrame
        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void sendUserMessage() {
        String userText = userInput.getText().trim();
        if (!userText.isEmpty() && !userText.equals("Ask anything...")) {
            System.out.println("📝 User message: " + userText);
            appendMessage("You", userText, new Color(52, 152, 219));
            userInput.setText("");
            sendMessageToAI(userText);
        }
    }

    private void sendMessageToAI(String userMessage) {
        new Thread(() -> {
            try {
                // Example: Download a dataset using Kaggle API
                downloadDataset();

                // For now, just display a placeholder response
                String aiResponse = "This is a placeholder response from Kaggle API.";
                SwingUtilities.invokeLater(() -> appendMessage("AI", aiResponse, new Color(46, 204, 113)));
            } catch (Exception e) {
                System.out.println("❌ Error fetching response: " + e.getMessage());
                SwingUtilities.invokeLater(() -> appendMessage("AI", "Error fetching response.", Color.RED));
            }
        }).start();
    }

    private void downloadDataset() {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://www.kaggle.com/api/v1/datasets/download/<owner>/<dataset-name>")
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    try (ResponseBody responseBody = response.body()) {
                        // Save the dataset to a file
                        FileOutputStream fos = new FileOutputStream("dataset.zip");
                        fos.write(responseBody.bytes());
                        fos.close();
                        System.out.println("✅ Dataset downloaded successfully.");
                    }
                } else {
                    System.out.println("❌ API request failed with code: " + response.code());
                }
            } catch (IOException e) {
                System.out.println("❌ Error downloading dataset: " + e.getMessage());
            }
        }).start();
    }

    private void appendMessage(String sender, String message, Color color) {
        chatArea.setText(chatArea.getText() + "<p style='color:" + toHex(color) + ";'><b>" + sender + ":</b> " + message + "</p>");
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AIChatbot().setVisible(true));
    }
}