import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class Part3Test {

    private List<JSONObject> sent;
    private List<JSONObject> stored;
    private List<JSONObject> disregarded;

    @BeforeEach
    public void setUp() {
        sent = new ArrayList<>();
        stored = new ArrayList<>();
        disregarded = new ArrayList<>();

        // Test Message 1 (Sent)
        JSONObject m1 = new JSONObject();
        m1.put("recipient", "+27834557896");
        m1.put("message", "Did you get the cake?");
        m1.put("flag", "Sent");
        sent.add(m1);

        // Test Message 2 (Stored)
        JSONObject m2 = new JSONObject();
        m2.put("recipient", "+27838884567");
        m2.put("message", "Where are you? You are late! I have asked you to be on time.");
        m2.put("flag", "Stored");
        stored.add(m2);

        // Test Message 3 (Disregarded)
        JSONObject m3 = new JSONObject();
        m3.put("recipient", "+27834484567");
        m3.put("message", "Yohoooo, I am at your gate.");
        m3.put("flag", "Disregard");
        disregarded.add(m3);

        // Test Message 4 (Sent)
        JSONObject m4 = new JSONObject();
        m4.put("recipient", "0838884567"); // Developer
        m4.put("message", "It is dinner time !");
        m4.put("flag", "Sent");
        sent.add(m4);

        // Test Message 5 (Stored)
        JSONObject m5 = new JSONObject();
        m5.put("recipient", "+27838884567");
        m5.put("message", "Ok, I am leaving without you.");
        m5.put("flag", "Stored");
        stored.add(m5);
    }

    @Test
    public void testSentMessagesPopulation() {
        assertEquals(2, sent.size(), "Sent message count should be 2.");
        assertEquals("Did you get the cake?", sent.get(0).getString("message"));
        assertEquals("It is dinner time !", sent.get(1).getString("message"));
    }

    @Test
    public void testStoredMessagesPopulation() {
        assertEquals(2, stored.size(), "Stored message count should be 2.");
        assertEquals("+27838884567", stored.get(0).getString("recipient"));
        assertEquals("Ok, I am leaving without you.", stored.get(1).getString("message"));
    }

    @Test
    public void testLongestMessage() {
        // Combine all messages to find the absolute longest
        List<JSONObject> allMessages = new ArrayList<>();
        allMessages.addAll(sent);
        allMessages.addAll(stored);
        allMessages.addAll(disregarded);

        String longest = "";
        for (JSONObject msg : allMessages) {
            String content = msg.getString("message");
            if (content.length() > longest.length()) {
                longest = content;
            }
        }
        assertEquals("Where are you? You are late! I have asked you to be on time.", 
            longest, "The longest message should be correctly identified.");
    }

    @Test
    public void testSearchByRecipient() {
        JSONObject found = null;
        for (JSONObject msg : sent) {
            if (msg.getString("recipient").equals("0838884567")) {
                found = msg;
                break;
            }
        }
        assertNotNull(found, "Message for recipient 0838884567 should be found.");
        assertEquals("It is dinner time !", found.getString("message"));
    }

    @Test
    public void testSearchAllMessagesToRecipient() {
        List<String> results = new ArrayList<>();
        for (JSONObject msg : stored) {
            if (msg.getString("recipient").equals("+27838884567")) {
                results.add(msg.getString("message"));
            }
        }
        assertEquals(2, results.size(), "There should be 2 messages sent to this recipient.");
        assertTrue(results.contains("Where are you? You are late! I have asked you to be on time."));
        assertTrue(results.contains("Ok, I am leaving without you."));
    }

    @Test
    public void testDeleteMessageByMessageContent() {
        String target = "Where are you? You are late! I have asked you to be on time.";
        boolean removed = stored.removeIf(msg -> msg.getString("message").equals(target));
        assertTrue(removed, "Message should be successfully removed.");
        assertEquals(1, stored.size(), "Stored list size should now be 1.");
        assertEquals("Ok, I am leaving without you.", stored.get(0).getString("message"));
    }
}