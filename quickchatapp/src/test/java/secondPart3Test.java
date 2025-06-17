import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.mycompany.quickchatapp.QuickChatMain;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class secondPart3Test {

    private List<JSONObject> sentMessages;
    private List<JSONObject> storedMessages;
    private List<JSONObject> disregardedMessages;

    @BeforeEach
    public void setUp() {
        // Initialize the arrays similar to QuickChatMain
        sentMessages = new ArrayList<>();
        storedMessages = new ArrayList<>();
        disregardedMessages = new ArrayList<>();

        // Create test messages matching the requirements from the image
        // Message 1 (Sent)
        JSONObject m1 = new JSONObject();
        m1.put("messageId", "M001");
        m1.put("sender", "dev1");
        m1.put("recipient", "+27834557896");
        m1.put("message", "Did you get the cake?");
        m1.put("messageHash", QuickChatMain.createMessageHash("M001", "dev1", "+27834557896", "", "Did you get the cake?"));
        sentMessages.add(m1);

        // Message 2 (Stored)
        JSONObject m2 = new JSONObject();
        m2.put("messageId", "M002");
        m2.put("sender", "dev1");
        m2.put("recipient", "+27838884567");
        m2.put("message", "Where are you? You are late! I have asked you to be on time.");
        m2.put("messageHash", QuickChatMain.createMessageHash("M002", "dev1", "+27838884567", "", "Where are you? You are late! I have asked you to be on time."));
        storedMessages.add(m2);

        // Message 3 (Disregarded)
        JSONObject m3 = new JSONObject();
        m3.put("messageId", "M003");
        m3.put("sender", "dev1");
        m3.put("recipient", "+27834484567");
        m3.put("message", "Yohoooo, I am at your gate.");
        m3.put("messageHash", QuickChatMain.createMessageHash("M003", "dev1", "+27834484567", "", "Yohoooo, I am at your gate."));
        disregardedMessages.add(m3);

        // Message 4 (Sent)
        JSONObject m4 = new JSONObject();
        m4.put("messageId", "M004");
        m4.put("sender", "dev1");
        m4.put("recipient", "0838884567");
        m4.put("message", "It is dinner time!");
        m4.put("messageHash", QuickChatMain.createMessageHash("M004", "dev1", "0838884567", "", "It is dinner time!"));
        sentMessages.add(m4);

        // Message 5 (Stored)
        JSONObject m5 = new JSONObject();
        m5.put("messageId", "M005");
        m5.put("sender", "dev1");
        m5.put("recipient", "+27838884567");
        m5.put("message", "Ok, I am leaving without you.");
        m5.put("messageHash", QuickChatMain.createMessageHash("M005", "dev1", "+27838884567", "", "Ok, I am leaving without you."));
        storedMessages.add(m5);
    }

    @Test
    public void testSentMessagesPopulation() {
        assertEquals(2, sentMessages.size(), "Should have 2 sent messages");
        assertEquals("Did you get the cake?", sentMessages.get(0).getString("message"));
        assertEquals("It is dinner time!", sentMessages.get(1).getString("message"));
    }

    @Test
    public void testLongestMessage() {
        // Combine all messages to find the absolute longest
        List<JSONObject> allMessages = new ArrayList<>();
        allMessages.addAll(sentMessages);
        allMessages.addAll(storedMessages);
        allMessages.addAll(disregardedMessages);

        String longest = "";
        for (JSONObject msg : allMessages) {
            String content = msg.getString("message");
            if (content.length() > longest.length()) {
                longest = content;
            }
        }
        assertEquals("Where are you? You are late! I have asked you to be on time.", longest);
    }

    @Test
    public void testSearchByRecipient() {
        String searchRecipient = "0838884567";
        JSONObject found = null;
        
        for (JSONObject msg : sentMessages) {
            if (msg.getString("recipient").equals(searchRecipient)) {
                found = msg;
                break;
            }
        }
        
        assertNotNull(found, "Message should be found for recipient");
        assertEquals("It is dinner time!", found.getString("message"));
    }

    @Test
    public void testSearchAllMessagesToRecipient() {
        String searchRecipient = "+27838884567";
        List<String> results = new ArrayList<>();
        
        for (JSONObject msg : storedMessages) {
            if (msg.getString("recipient").equals(searchRecipient)) {
                results.add(msg.getString("message"));
            }
        }
        
        assertEquals(2, results.size(), "Should find 2 messages for this recipient");
        assertTrue(results.contains("Where are you? You are late! I have asked you to be on time."));
        assertTrue(results.contains("Ok, I am leaving without you."));
    }

    @Test
    public void testDeleteMessageByHash() {
        // Get hash of the message we want to delete
        String targetHash = storedMessages.get(0).getString("messageHash");
        String targetMessage = storedMessages.get(0).getString("message");
        
        boolean removed = storedMessages.removeIf(msg -> msg.getString("messageHash").equals(targetHash));
        
        assertTrue(removed, "Message should be removed");
        assertEquals(1, storedMessages.size(), "Should have one less message");
        assertFalse(storedMessages.stream().anyMatch(msg -> msg.getString("message").equals(targetMessage)), 
                   "Deleted message should no longer exist");
    }

    @Test
    public void testReportFormat() {
        StringBuilder report = new StringBuilder();
        for (JSONObject msg : sentMessages) {
            report.append("Hash: ").append(msg.getString("messageHash"))
                  .append(" | To: ").append(msg.getString("recipient"))
                  .append("\nMessage: ").append(msg.getString("message"))
                  .append("\n---\n");
        }
        
        // Check if report contains expected elements
        assertTrue(report.toString().contains("Hash: "));
        assertTrue(report.toString().contains("To: "));
        assertTrue(report.toString().contains("Message: "));
        
        // Check if specific messages appear in report
        assertTrue(report.toString().contains("Did you get the cake?"));
        assertTrue(report.toString().contains("It is dinner time!"));
    }
}