package com.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;

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

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIChatbot extends JFrame {
    private JTextPane chatArea;
    private JTextField userInput;
    private JButton sendButton;
    private static final String API_KEY = "YOUR_OPENAI_API_KEY";

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

        // Input panel with icon
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(30, 30, 30));

        // Icon in the input field
        JLabel inputIcon = new JLabel(new ImageIcon("https://i.pinimg.com/736x/4b/81/08/4b8108cbf99c531fdcc2d7c3e4edd31a.jpg")); 
        inputIcon.setPreferredSize(new Dimension(30, 30));
        inputPanel.add(inputIcon, BorderLayout.WEST);

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

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void sendUserMessage() {
        String userText = userInput.getText().trim();
        if (!userText.isEmpty() && !userText.equals("Ask anything...")) {
            appendMessage("You", userText, new Color(52, 152, 219));
            userInput.setText("");
            sendMessageToAI(userText);
        }
    }

    private void sendMessageToAI(String userMessage) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject json = new JSONObject();
                json.put("model", "gpt-3.5-turbo");
                json.put("messages", new JSONObject[]{new JSONObject().put("role", "user").put("content", userMessage)});

                RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

                Request request = new Request.Builder()
                        .url("https://api.openai.com/v1/chat/completions")
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseText = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseText);
                    String aiResponse = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

                    SwingUtilities.invokeLater(() -> appendMessage("AI", aiResponse, new Color(46, 204, 113)));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> appendMessage("AI", "Error fetching response.", Color.RED));
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
