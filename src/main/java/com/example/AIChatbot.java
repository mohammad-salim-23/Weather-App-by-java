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
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import io.github.cdimascio.dotenv.Dotenv;
public class AIChatbot extends JFrame {
    private JTextPane chatArea;
    private JTextField userInput;
    private JButton sendButton;
    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("Chatbot_Key");
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_TEXT = "Ask anything...";

    public AIChatbot() {
        setTitle("Salim Chatbot 🤖");
        setSize(400, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(44, 62, 80));

        JLabel headerLabel = new JLabel("Salim Chatbot 🤖", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.NORTH);

        // Image panel (NEW)
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

        inputPanel.add(userInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void sendUserMessage() {
        String userText = userInput.getText().trim();
        if (!userText.isEmpty() && !userText.equals(DEFAULT_TEXT)) {
            appendMessage("You", userText, Color.BLUE);
            userInput.setText("");
            sendMessageToGPT(userText);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AIChatbot().setVisible(true));
    }
}
