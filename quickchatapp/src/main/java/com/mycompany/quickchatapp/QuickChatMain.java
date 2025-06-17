// Updated QuickChatMain.java with Part 3 PoE requirements

package com.mycompany.quickchatapp;

import com.mycompany.quickchatapp.UserManager;
import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class QuickChatMain {
    public static final String MESSAGES_FILE = "messages.json";

    private static String currentUser = "";
    private static int maxMessages = 0;
    private static int messageCount = 0;
    private static List<JSONObject> currentSessionMessages = new ArrayList<>();

    private static List<JSONObject> sentMessagesArray = new ArrayList<>();
    private static List<JSONObject> disregardedMessagesArray = new ArrayList<>();
    private static List<JSONObject> storedMessagesArray = new ArrayList<>();
    private static List<String> messageHashes = new ArrayList<>();
    private static List<String> messageIds = new ArrayList<>();
    private static Object base;

    public static void main(String[] args) {
        initializeMessagesFile();

        while (true) {
            int choice = JOptionPane.showConfirmDialog(null, "Do you already have an account?", "Welcome", JOptionPane.YES_NO_CANCEL_OPTION);

            if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
                JOptionPane.showMessageDialog(null, "Goodbye! 👋");
                break;
            }

            if (choice == JOptionPane.NO_OPTION) {
                if (!UserManager.handleRegistration()) continue;
            }

            String loggedInUser = UserManager.handleLogin();
            if (loggedInUser != null) {
                currentUser = loggedInUser;
                messageCount = 0;
                currentSessionMessages.clear();
                showQuickChatWelcome();
                setMessageLimit();
                loadStoredMessages();
                runQuickChatMenu();
            }

            int again = JOptionPane.showConfirmDialog(null, "Would another user like to log in?", "Continue?", JOptionPane.YES_NO_OPTION);
            if (again != JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(null, "Goodbye! 👋");
                break;
            }
        }
    }

    private static void initializeMessagesFile() {
        try {
            if (!Files.exists(Paths.get(MESSAGES_FILE))) {
                Files.write(Paths.get(MESSAGES_FILE), "[]".getBytes());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error initializing messages file");
        }
    }

    private static void showQuickChatWelcome() {
        JOptionPane.showMessageDialog(null, "Welcome to QuickChat.", "QuickChat", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void setMessageLimit() {
        while (true) {
            String input = JOptionPane.showInputDialog("How many messages do you wish to enter?");
            if (input == null) {
                maxMessages = 5;
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

    private static void runQuickChatMenu() {
        while (true) {
            String[] options = {
                "1) Send Messages", "2) Show recently sent messages", "3) Store Messages",
                "4) Print Messages", "5) Show Total Messages", "6) Quit",
                "7) Show sender and recipient of sent messages", "8) Show longest sent message",
                "9) Search by Message ID", "10) Search messages to recipient",
                "11) Delete message by hash", "12) Show Message Report"
            };

            String choice = (String) JOptionPane.showInputDialog(null, "Please choose an option:", "QuickChat Menu",
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (choice == null || choice.startsWith("6")) {
                JOptionPane.showMessageDialog(null, "Thank you for using QuickChat. Goodbye!");
                break;
            } else if (choice.startsWith("1")) {
                sendMessages();
            } else if (choice.startsWith("2")) {
                showRecentMessages();
            } else if (choice.startsWith("3")) {
                storeMessages();
            } else if (choice.startsWith("4")) {
                printMessages();
            } else if (choice.startsWith("5")) {
                showTotalMessages();
            } else if (choice.startsWith("7")) {
                showSendersAndRecipients();
            } else if (choice.startsWith("8")) {
                showLongestMessage();
            } else if (choice.startsWith("9")) {
                searchByMessageId();
            } else if (choice.startsWith("10")) {
                searchByRecipient();
            } else if (choice.startsWith("11")) {
                deleteByHash();
            } else if (choice.startsWith("12")) {
                showFullReport();
            }
        }
    }

    private static void sendMessages() {
        while (messageCount < maxMessages) {
            String messageId = JOptionPane.showInputDialog("Enter Message ID (max 10 characters):");
            if (messageId == null || messageId.length() > 10) return;
            String recipient = JOptionPane.showInputDialog("Enter Recipient (+27 format):");
            if (recipient == null || !recipient.matches("\\+27\\d{9}")) return;
            String subject = JOptionPane.showInputDialog("Subject:");
            String message = JOptionPane.showInputDialog("Message:");
            String hash = createMessageHash(messageId, currentUser, recipient, subject, message);

            JSONObject msg = new JSONObject();
            msg.put("messageId", messageId);
            msg.put("sender", currentUser);
            msg.put("recipient", recipient);
            msg.put("subject", subject);
            msg.put("message", message);
            msg.put("timestamp", LocalDateTime.now().toString());
            msg.put("messageHash", hash);

            String action = sentMessage(msg);
            if (action.equals("Send")) {
                sentMessagesArray.add(msg);
                currentSessionMessages.add(msg);
                messageIds.add(messageId);
                messageHashes.add(hash);
                messageCount++;
            } else if (action.equals("Store")) {
                storedMessagesArray.add(msg);
                currentSessionMessages.add(msg);
                messageIds.add(messageId);
                messageHashes.add(hash);
                messageCount++;
            } else {
                disregardedMessagesArray.add(msg);
            }
        }
    }

    private static void storeMessages() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(MESSAGES_FILE)));
            JSONArray array = new JSONArray(content);
            for (JSONObject msg : currentSessionMessages) {
                array.put(msg);
            }
            Files.write(Paths.get(MESSAGES_FILE), array.toString(2).getBytes());
            JOptionPane.showMessageDialog(null, "Messages stored successfully.");
            currentSessionMessages.clear();
            messageCount = 0;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error storing messages.");
        }
    }

    private static void loadStoredMessages() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(MESSAGES_FILE)));
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < array.length(); i++) {
                storedMessagesArray.add(array.getJSONObject(i));
            }
        } catch (IOException e) {
            // ignore
        }
    }

    private static void showSendersAndRecipients() {
        StringBuilder sb = new StringBuilder();
        for (JSONObject msg : sentMessagesArray) {
            sb.append("From: ").append(msg.getString("sender"))
              .append(" | To: ").append(msg.getString("recipient"))
              .append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private static void showLongestMessage() {
        int max = 0;
        String longest = "";
        for (JSONObject msg : sentMessagesArray) {
            if (msg.getString("message").length() > max) {
                max = msg.getString("message").length();
                longest = msg.getString("message");
            }
        }
        JOptionPane.showMessageDialog(null, "Longest Message:\n" + longest);
    }

    private static void searchByMessageId() {
        String id = JOptionPane.showInputDialog("Enter Message ID:");
        for (JSONObject msg : sentMessagesArray) {
            if (msg.getString("messageId").equals(id)) {
                JOptionPane.showMessageDialog(null, "Recipient: " + msg.getString("recipient") +
                        "\nMessage: " + msg.getString("message"));
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Message not found.");
    }

    private static void searchByRecipient() {
        String rec = JOptionPane.showInputDialog("Enter recipient number:");
        StringBuilder sb = new StringBuilder();
        for (JSONObject msg : sentMessagesArray) {
            if (msg.getString("recipient").equals(rec)) {
                sb.append(msg.getString("message")).append("\n");
            }
        }
        JOptionPane.showMessageDialog(null, sb.length() > 0 ? sb.toString() : "No messages found.");
    }

    private static void deleteByHash() {
        String hash = JOptionPane.showInputDialog("Enter message hash to delete:");
        for (int i = 0; i < sentMessagesArray.size(); i++) {
            if (sentMessagesArray.get(i).getString("messageHash").equals(hash)) {
                String msg = sentMessagesArray.get(i).getString("message");
                sentMessagesArray.remove(i);
                JOptionPane.showMessageDialog(null, "Deleted: " + msg);
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Message not found.");
    }

    private static void showFullReport() {
        StringBuilder sb = new StringBuilder("=== FULL REPORT ===\n");
        for (JSONObject msg : sentMessagesArray) {
            sb.append("Hash: ").append(msg.getString("messageHash"))
              .append(" | To: ").append(msg.getString("recipient"))
              .append("\nMessage: ").append(msg.getString("message"))
              .append("\n---\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    public static String createMessageHash(String id, String sender, String rec, String subj, String msg) {
        try {
            String base = id + sender + rec + subj + msg;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString().substring(0, 8).toUpperCase();
        } catch (Exception e) {
            return String.valueOf(base.hashCode());
        }
    }

    private static String sentMessage(JSONObject msg) {
        String[] choices = {"Send", "Store", "Disregard"};
        return (String) JOptionPane.showInputDialog(null,
                "Choose action:\n" + msg.getString("message"),
                "Message Decision", JOptionPane.QUESTION_MESSAGE, null,
                choices, choices[0]);
    }

    private static void showRecentMessages() {
        try {
            String fileContent = new String(Files.readAllBytes(Paths.get(MESSAGES_FILE)));
            JSONArray messages = new JSONArray(fileContent);
            StringBuilder sb = new StringBuilder("=== RECENT MESSAGES ===\n");
            int start = Math.max(0, messages.length() - 5);
            for (int i = start; i < messages.length(); i++) {
                JSONObject msg = messages.getJSONObject(i);
                sb.append("\nID: ").append(msg.getString("messageId"))
                  .append(" | From: ").append(msg.getString("sender"))
                  .append(" | To: ").append(msg.getString("recipient"))
                  .append("\nMessage: ").append(msg.getString("message"))
                  .append("\nHash: ").append(msg.optString("messageHash", "N/A"))
                  .append("\n------------------\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not read messages file.");
        }
    }

    private static void printMessages() {
        StringBuilder sb = new StringBuilder();
        for (JSONObject msg : currentSessionMessages) {
            sb.append("ID: ").append(msg.getString("messageId"))
              .append(" | To: ").append(msg.getString("recipient"))
              .append("\nMessage: ").append(msg.getString("message"))
              .append("\n---\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private static void showTotalMessages() {
        int total = storedMessagesArray.size();
        int session = currentSessionMessages.size();
        JOptionPane.showMessageDialog(null, "Total stored: " + total + "\nSession: " + session);
    }
} 
