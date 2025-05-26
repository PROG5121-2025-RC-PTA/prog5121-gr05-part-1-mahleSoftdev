package com.mycompany.quickchatapp;

/**
 * QUICKCHAT APPLICATION WRAPPER
 * This class serves as a wrapper to maintain backward compatibility
 * with existing Maven configurations that expect QuickChatApp as the main class.
 * 
 * IMPORTANT: Save this file as QuickChatApp.java in your project's 
 * src/main/java/com/mycompany/quickchatapp/ directory
 */
public class QuickChatApp {
    
    /**
     * Main method that delegates to QuickChatMain
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Delegate to the actual main application
        QuickChatMain.main(args);
    }
}