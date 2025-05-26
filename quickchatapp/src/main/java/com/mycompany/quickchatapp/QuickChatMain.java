package com.mycompany.quickchatapp;

import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * QUICKCHAT MAIN APPLICATION - GUI-based messaging application
 * Features: Message sending, viewing, and user session management
 */
public class QuickChatMain {
    
    // File constant for message storage
    public static final String MESSAGES_FILE = "messages.json";
    
    // Global state variables
    private static String currentUser = "";
    private static int maxMessages = 0;
    private static int messageCount = 0;

    /**
     * Main application entry point
     */
    public static void main(String[] args) {
        initializeMessagesFile();
        
        while (true) {
            // Initial user choice dialog
            int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Do you already have an account?",
                    "Welcome",
                    JOptionPane.YES_NO_CANCEL_OPTION);

            // Handle exit condition
            if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
                JOptionPane.showMessageDialog(null, "Goodbye! 👋");
                break;
            }

            // Registration flow for new users
            if (choice == JOptionPane.NO_OPTION) {
                if (!UserManager.handleRegistration()) continue;
            }

            // Login flow
            String loggedInUser = UserManager.handleLogin();
            if (loggedInUser != null) {
                currentUser = loggedInUser;
                messageCount = 0;
                showQuickChatWelcome();
                setMessageLimit();
                runQuickChatMenu();
            }

            // Ask if another user wants to log in
            int again = JOptionPane.showConfirmDialog(null, "Would another user like to log in?", "Continue?", JOptionPane.YES_NO_OPTION);
            if (again != JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(null, "Goodbye! 👋");
                break;
            }
        }
    }

    /**
     * Initialize messages file with empty JSON array if it doesn't exist
     */
    private static void initializeMessagesFile() {
        try {
            if (!Files.exists(Paths.get(MESSAGES_FILE))) {
                Files.write(Paths.get(MESSAGES_FILE), "[]".getBytes());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error initializing messages file");
        }
    }

    /**
     * Show QuickChat welcome message
     */
    private static void showQuickChatWelcome() {
        JOptionPane.showMessageDialog(null, "Welcome to QuickChat.", "QuickChat", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Set message limit for user session
     */
    private static void setMessageLimit() {
        while (true) {
            String input = JOptionPane.showInputDialog("How many messages do you wish to enter?");
            
            if (input == null) {
                maxMessages = 5; // Default value
                break;
            }
            
            try {
                maxMessages = Integer.parseInt(input);
                if (maxMessages > 0) {
                    JOptionPane.showMessageDialog(null, "You can send up to " + maxMessages + " messages.");
                    break;
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a positive number.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number.");
            }
        }
    }

    /**
     * Main QuickChat menu system
     */
    private static void runQuickChatMenu() {
        while (true) {
            String[] options = {"1) Send Messages", "2) Show recently sent messages", "3) Quit"};
            
            String choice = (String) JOptionPane.showInputDialog(
                    null,
                    "Please choose an option:",
                    "QuickChat Menu",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == null || choice.startsWith("3")) {
                JOptionPane.showMessageDialog(null, "Thank you for using QuickChat. Goodbye!");
                break;
            } else if (choice.startsWith("1")) {
                sendMessages();
            } else if (choice.startsWith("2")) {
                showRecentMessages();
            }
        }
    }

    /**
     * Handle message sending functionality
     */
    private static void sendMessages() {
        if (messageCount >= maxMessages) {
            JOptionPane.showMessageDialog(null, "You have reached your message limit of " + maxMessages + " messages.");
            return;
        }

        int remaining = maxMessages - messageCount;
        JOptionPane.showMessageDialog(null, "=== SEND MESSAGES ===\nMessages remaining: " + remaining);

        while (messageCount < maxMessages) {
            int messageNum = messageCount + 1;
            
            // Get recipient
            String recipient = JOptionPane.showInputDialog("Message " + messageNum + " of " + maxMessages + "\nTo (recipient):");
            if (recipient == null || recipient.trim().isEmpty()) {
                if (recipient == null) return;
                JOptionPane.showMessageDialog(null, "Recipient cannot be empty.");
                continue;
            }

            // Get subject
            String subject = JOptionPane.showInputDialog("Subject:");
            if (subject == null || subject.trim().isEmpty()) {
                if (subject == null) return;
                JOptionPane.showMessageDialog(null, "Subject cannot be empty.");
                continue;
            }

            // Get message content
            String content = JOptionPane.showInputDialog("Message:");
            if (content == null || content.trim().isEmpty()) {
                if (content == null) return;
                JOptionPane.showMessageDialog(null, "Message content cannot be empty.");
                continue;
            }

            // Save message
            saveMessage(messageNum, currentUser, recipient.trim(), subject.trim(), content.trim());
            messageCount++;
            
            JOptionPane.showMessageDialog(null, "Message " + messageNum + " sent successfully!");

            // Check if user wants to continue
            if (messageCount < maxMessages) {
                int continueChoice = JOptionPane.showConfirmDialog(
                    null, 
                    "Send another message? (" + (maxMessages - messageCount) + " remaining)", 
                    "Continue?", 
                    JOptionPane.YES_NO_OPTION
                );
                if (continueChoice != JOptionPane.YES_OPTION) {
                    break;
                }
            } else {
                JOptionPane.showMessageDialog(null, "You have sent all " + maxMessages + " messages!");
                break;
            }
        }
    }

    /**
     * Show recent messages from JSON file
     */
    private static void showRecentMessages() {
        try {
            String fileContent = new String(Files.readAllBytes(Paths.get(MESSAGES_FILE)));
            JSONArray messages = new JSONArray(fileContent);
            
            StringBuilder sb = new StringBuilder("=== RECENT MESSAGES ===\n");
            
            // Show last 5 messages or all if less than 5
            int start = Math.max(0, messages.length() - 5);
            for (int i = start; i < messages.length(); i++) {
                JSONObject msg = messages.getJSONObject(i);
                sb.append("\nMessage ID: ").append(msg.getInt("messageId"))
                  .append("\nFrom: ").append(msg.getString("sender"))
                  .append("\nTo: ").append(msg.getString("recipient"))
                  .append("\nSubject: ").append(msg.getString("subject"))
                  .append("\nMessage: ").append(msg.getString("message"))
                  .append("\nTime: ").append(msg.getString("timestamp"))
                  .append("\n-------------------\n");
            }
            
            JOptionPane.showMessageDialog(null, sb.toString(), "Recent Messages", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading messages: " + e.getMessage());
        }
    }

    /**
     * Save message to JSON file
     */
    private static void saveMessage(int messageNum, String sender, String recipient, String subject, String content) {
        try {
            // Read existing messages
            String fileContent = new String(Files.readAllBytes(Paths.get(MESSAGES_FILE)));
            JSONArray messages = new JSONArray(fileContent);
            
            // Create new message object
            JSONObject message = new JSONObject();
            message.put("messageId", messageNum);
            message.put("sender", sender);
            message.put("recipient", recipient);
            message.put("subject", subject);
            message.put("message", content);
            message.put("timestamp", LocalDateTime.now().toString());
            
            // Add to array
            messages.put(message);
            
            // Write back to file
            Files.write(Paths.get(MESSAGES_FILE), messages.toString().getBytes());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error saving message: " + e.getMessage());
        }
    }
}