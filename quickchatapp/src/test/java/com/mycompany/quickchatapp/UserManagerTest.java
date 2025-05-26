import com.mycompany.quickchatapp.UserManager;

import org.junit.jupiter.api.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserManagerTest {
   

    private static final String TEST_FILE = "test_users.txt";
    private static final String ORIGINAL_FILE = "users.txt";

    @BeforeEach
    public void setup() throws IOException {
        // Rename original file if it exists
        File original = new File(ORIGINAL_FILE);
        if (original.exists()) {
            original.renameTo(new File("backup_users.txt"));
        }

        // Create a clean test file
        FileWriter writer = new FileWriter(ORIGINAL_FILE, false);
        writer.close();
    }

    @AfterEach
    public void teardown() {
        // Clean up test user file
        File testFile = new File(ORIGINAL_FILE);
        testFile.delete();

        // Restore backup
        File backup = new File("backup_users.txt");
        if (backup.exists()) {
            backup.renameTo(new File(ORIGINAL_FILE));
        }
    }
}

    
