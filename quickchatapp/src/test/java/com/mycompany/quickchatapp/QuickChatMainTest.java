package com.mycompany.quickchatapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for QuickChatMain
 * Tests core functionality without GUI dependencies
 */
public class QuickChatMainTest {

    private static final String TEST_MESSAGES_FILE = "test_messages.json";
    private static final String MESSAGES_FILE = "messages.json";

    @BeforeEach
    void setUp() throws IOException {
        // Clean up any existing test files
        deleteFileIfExists(TEST_MESSAGES_FILE);
        deleteFileIfExists(MESSAGES_FILE);
        
        // Create empty JSON array file if it doesn't exist
        Files.write(Paths.get(MESSAGES_FILE), "[]".getBytes());
        
        // Reset static fields using reflection
        resetStaticFields();
    }

    @AfterEach
    void tearDown() {
        deleteFileIfExists(TEST_MESSAGES_FILE);
        deleteFileIfExists(MESSAGES_FILE);
    }

    @Test
    @DisplayName("Test saveMessage functionality with JSON")
    void testSaveMessage() throws Exception {
        Method saveMessageMethod = QuickChatMain.class.getDeclaredMethod(
            "saveMessage", int.class, String.class, String.class, String.class, String.class
        );
        saveMessageMethod.setAccessible(true);

        // Test data
        int messageNum = 1;
        String sender = "testUser";
        String recipient = "recipient@test.com";
        String subject = "Test Subject";
        String content = "Test message content";

        // Call the method
        saveMessageMethod.invoke(null, messageNum, sender, recipient, subject, content);

        // Verify file was created and contains expected content
        assertTrue(Files.exists(Paths.get(MESSAGES_FILE)), "Messages file should be created");
        
        String fileContent = Files.readString(Paths.get(MESSAGES_FILE));
        JSONArray messagesArray = new JSONArray(fileContent);
        assertEquals(1, messagesArray.length(), "Should have one message in array");
        
        JSONObject message = messagesArray.getJSONObject(0);
        assertEquals(messageNum, message.getInt("messageId"), "Message ID should match");
        assertEquals(sender, message.getString("sender"), "Sender should match");
        assertEquals(recipient, message.getString("recipient"), "Recipient should match");
        assertEquals(subject, message.getString("subject"), "Subject should match");
        assertEquals(content, message.getString("message"), "Content should match");
        assertTrue(message.has("timestamp"), "Should have timestamp");
    }

    @Test
    @DisplayName("Test saveMessage with multiple messages in JSON")
    void testSaveMessageMultiple() throws Exception {
        Method saveMessageMethod = QuickChatMain.class.getDeclaredMethod(
            "saveMessage", int.class, String.class, String.class, String.class, String.class
        );
        saveMessageMethod.setAccessible(true);

        // Save multiple messages
        saveMessageMethod.invoke(null, 1, "user1", "rec1", "subj1", "content1");
        saveMessageMethod.invoke(null, 2, "user2", "rec2", "subj2", "content2");

        // Verify both messages are saved
        String fileContent = Files.readString(Paths.get(MESSAGES_FILE));
        JSONArray messagesArray = new JSONArray(fileContent);
        assertEquals(2, messagesArray.length(), "Should have two messages in array");
        
        JSONObject msg1 = messagesArray.getJSONObject(0);
        assertEquals("user1", msg1.getString("sender"), "First message sender should match");
        
        JSONObject msg2 = messagesArray.getJSONObject(1);
        assertEquals("user2", msg2.getString("sender"), "Second message sender should match");
    }

    @Test
    @DisplayName("Test saveMessage with special characters in JSON")
    void testSaveMessageWithSpecialCharacters() throws Exception {
        Method saveMessageMethod = QuickChatMain.class.getDeclaredMethod(
            "saveMessage", int.class, String.class, String.class, String.class, String.class
        );
        saveMessageMethod.setAccessible(true);

        // Test with special characters
        String specialContent = "Hello! @#$%^&*()_+ Testing 123";
        String specialSubject = "Test: Special Characters & Symbols";
        
        saveMessageMethod.invoke(null, 1, "testUser", "test@example.com", specialSubject, specialContent);

        String fileContent = Files.readString(Paths.get(MESSAGES_FILE));
        JSONArray messagesArray = new JSONArray(fileContent);
        JSONObject message = messagesArray.getJSONObject(0);
        
        assertEquals(specialSubject, message.getString("subject"), "Special characters in subject should be preserved");
        assertEquals(specialContent, message.getString("message"), "Special characters in content should be preserved");
    }

    @Test
    @DisplayName("Test saveMessage with empty strings in JSON")
    void testSaveMessageWithEmptyStrings() throws Exception {
        Method saveMessageMethod = QuickChatMain.class.getDeclaredMethod(
            "saveMessage", int.class, String.class, String.class, String.class, String.class
        );
        saveMessageMethod.setAccessible(true);

        // Test with empty strings
        saveMessageMethod.invoke(null, 1, "", "", "", "");

        String fileContent = Files.readString(Paths.get(MESSAGES_FILE));
        JSONArray messagesArray = new JSONArray(fileContent);
        JSONObject message = messagesArray.getJSONObject(0);
        
        assertEquals("", message.getString("sender"), "Empty sender should be preserved");
        assertEquals("", message.getString("recipient"), "Empty recipient should be preserved");
        assertEquals("", message.getString("subject"), "Empty subject should be preserved");
        assertEquals("", message.getString("message"), "Empty content should be preserved");
    }

    @Test
    @DisplayName("Test MESSAGES_FILE constant value is JSON")
    void testMessagesFileConstant() throws Exception {
        Field messagesFileField = QuickChatMain.class.getDeclaredField("MESSAGES_FILE");
        messagesFileField.setAccessible(true);
        String messagesFile = (String) messagesFileField.get(null);
        assertEquals("messages.json", messagesFile, "MESSAGES_FILE constant should be 'messages.json'");
    }

    @Test
    @DisplayName("Test JSON file append functionality")
    void testFileAppendFunctionality() throws Exception {
        Method saveMessageMethod = QuickChatMain.class.getDeclaredMethod(
            "saveMessage", int.class, String.class, String.class, String.class, String.class
        );
        saveMessageMethod.setAccessible(true);

        // Save first message
        saveMessageMethod.invoke(null, 1, "user1", "rec1", "subj1", "content1");
        long firstFileSize = Files.size(Paths.get(MESSAGES_FILE));

        // Save second message
        saveMessageMethod.invoke(null, 2, "user2", "rec2", "subj2", "content2");
        long secondFileSize = Files.size(Paths.get(MESSAGES_FILE));

        // File size should increase (append mode)
        assertTrue(secondFileSize > firstFileSize, "File size should increase when appending");

        // Verify both messages are present
        String fileContent = Files.readString(Paths.get(MESSAGES_FILE));
        JSONArray messagesArray = new JSONArray(fileContent);
        assertEquals(2, messagesArray.length(), "JSON array should contain 2 messages");
    }

    @Test
    @DisplayName("Test static field initialization")
    void testStaticFieldInitialization() throws Exception {
        assertEquals("", getFieldValue("currentUser"), "currentUser should be initialized to empty string");
        assertEquals(0, getFieldValue("maxMessages"), "maxMessages should be initialized to 0");
        assertEquals(0, getFieldValue("messageCount"), "messageCount should be initialized to 0");
    }

    @Test
    @DisplayName("Test static field modification")
    void testStaticFieldModification() throws Exception {
        setFieldValue("currentUser", "testUser123");
        setFieldValue("maxMessages", 10);
        setFieldValue("messageCount", 5);

        assertEquals("testUser123", getFieldValue("currentUser"));
        assertEquals(10, getFieldValue("maxMessages"));
        assertEquals(5, getFieldValue("messageCount"));
    }

    // Helper methods
    private void deleteFileIfExists(String fileName) {
        try {
            Files.deleteIfExists(Paths.get(fileName));
        } catch (IOException e) {
            // Ignore deletion errors in tests
        }
    }

    private void resetStaticFields() {
        try {
            setFieldValue("currentUser", "");
            setFieldValue("maxMessages", 0);
            setFieldValue("messageCount", 0);
            // Ensure messages file is empty array
            Files.write(Paths.get(MESSAGES_FILE), "[]".getBytes());
        } catch (Exception e) {
            // Ignore reset errors
        }
    }

    private void setFieldValue(String fieldName, Object value) throws Exception {
        Field field = QuickChatMain.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    private Object getFieldValue(String fieldName) throws Exception {
        Field field = QuickChatMain.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }
}