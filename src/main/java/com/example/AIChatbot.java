package com.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIChatbot extends JFrame {
    private JTextPane chatArea;
    private JTextField userInput;
    private JButton sendButton;
    private JButton toggleModeButton;
    private JButton clearChatButton;
    private JButton viewHistoryButton;
    private boolean darkMode = true;
    private ArrayList<String> chatHistory = new ArrayList<>();
    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("Chatbot_Key");
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_TEXT = "Ask anything...";

    public AIChatbot() {
        setTitle("Salim Chatbot 🤖");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(44, 62, 80));

        JLabel headerLabel = new JLabel("Salim Chatbot 🤖", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.NORTH);

        // Image panel
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imagePanel.setBackground(new Color(30, 30, 30));

        URL imageUrl = AIChatbot.class.getClassLoader().getResource("AIChatbot.jpg");
        if (imageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imagePanel.add(imageLabel);
        }

        headerPanel.add(imagePanel, BorderLayout.CENTER);

        // Chat area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(Color.WHITE);
        chatArea.setForeground(Color.BLACK);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        userInput = new JTextField(DEFAULT_TEXT);
        userInput.setForeground(Color.GRAY);
        userInput.setBackground(Color.WHITE);

        userInput.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (userInput.getText().equals(DEFAULT_TEXT)) {
                    userInput.setText("");
                    userInput.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (userInput.getText().isEmpty()) {
                    userInput.setText(DEFAULT_TEXT);
                    userInput.setForeground(Color.GRAY);
                }
            }
        });

        sendButton = new JButton("Send ➤");
        sendButton.addActionListener(e -> sendUserMessage());

        toggleModeButton = new JButton("Toggle Mode");
        toggleModeButton.addActionListener(e -> toggleDarkMode());

        clearChatButton = new JButton("Clear Chat");
        clearChatButton.addActionListener(e -> clearChat());

        viewHistoryButton = new JButton("View History");
        viewHistoryButton.addActionListener(e -> viewChatHistory());

        inputPanel.add(userInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(toggleModeButton, BorderLayout.NORTH);
        inputPanel.add(clearChatButton, BorderLayout.WEST);
        inputPanel.add(viewHistoryButton, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        loadChatHistory(); // Load chat history on startup
    }

    private void sendUserMessage() {
        String userText = userInput.getText().trim();
        if (!userText.isEmpty() && !userText.equals(DEFAULT_TEXT)) {
            appendMessage("You", userText, Color.BLUE);
            userInput.setText("");
            sendMessageToGPT(userText);
            chatHistory.add("You: " + userText);
        }
    }

    private void sendMessageToGPT(String userMessage) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject json = new JSONObject();
                json.put("model", "gpt-3.5-turbo");
                json.put("messages", new JSONArray().put(new JSONObject().put("role", "user").put("content", userMessage)));

                RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    String aiResponse = new JSONObject(responseBody)
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                    SwingUtilities.invokeLater(() -> appendMessage("AI", aiResponse, Color.BLACK));
                    chatHistory.add("AI: " + aiResponse);
                } else {
                    SwingUtilities.invokeLater(() -> appendMessage("AI", "⚠️ API Error!", Color.RED));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> appendMessage("AI", "⚠️ Connection error!", Color.RED));
            }
        }).start();
    }

    private void appendMessage(String sender, String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = chatArea.getStyledDocument();
            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setForeground(style, color);
            StyleConstants.setBold(style, true);
            StyleConstants.setFontSize(style, 16);

            try {
                doc.insertString(doc.getLength(), sender + ": ", style);
                StyleConstants.setBold(style, false);
                doc.insertString(doc.getLength(), message + "\n\n", style);  // gap
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private void toggleDarkMode() {
        darkMode = !darkMode;
        if (darkMode) {
            setBackground(Color.BLACK);
            chatArea.setBackground(Color.BLACK);
            chatArea.setForeground(Color.WHITE);
            userInput.setBackground(Color.BLACK);
            userInput.setForeground(Color.WHITE);
            toggleModeButton.setText("Light Mode");
        } else {
            setBackground(Color.WHITE);
            chatArea.setBackground(Color.WHITE);
            chatArea.setForeground(Color.BLACK);
            userInput.setBackground(Color.WHITE);
            userInput.setForeground(Color.BLACK);
            toggleModeButton.setText("Dark Mode");
        }
    }

    private void clearChat() {
        chatArea.setText("");
        // chatHistory.clear();
    }

   private void viewChatHistory() {
    StringBuilder history = new StringBuilder();
    for (String message : chatHistory) {
        history.append(message).append("\n");
    }

    JTextArea textArea = new JTextArea(history.toString());
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setFont(new Font("Arial", Font.PLAIN, 14));

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new java.awt.Dimension(350, 300)); // fixed size

    JOptionPane.showMessageDialog(this, scrollPane, "Chat History", JOptionPane.INFORMATION_MESSAGE);
}

    private void loadChatHistory() {
        for (String message : chatHistory) {
            String[] parts = message.split(": ", 2);
            if (parts.length == 2) {
                appendMessage(parts[0], parts[1], parts[0].equals("You") ? Color.BLUE : Color.BLACK);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AIChatbot().setVisible(true));
    }
}
