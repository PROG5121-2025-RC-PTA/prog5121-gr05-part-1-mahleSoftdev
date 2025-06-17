package com.mycompany.quickchatapp;

import javax.swing.*;
import java.io.*;
import java.util.Scanner;

/**
 * USER MANAGEMENT - Handles user registration and login functionality
 * Features: User registration with validation, secure login, and user data persistence
 */
public class UserManager {
    
    // File constant for user data storage
    private static final String USERS_FILE = "users.txt";
    
    // Global state variables
    private static String loginMessage = "";

    /**
     * Handle user registration process
     * @return true if registration successful 0and user wants to login, false otherwise
     */
    public static boolean handleRegistration() {
        JOptionPane.showMessageDialog(null, "=== Sign Up ===");

        // Username validation loop
        String username;
        while (true) {
            username = JOptionPane.showInputDialog("Enter username (must contain '_' and max 5 characters):");
            
            if (username == null) return false;
            
            if (!checkUserName(username)) {
                JOptionPane.showMessageDialog(null, "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than five characters in length.");
                continue;
            }
            
            if (userExists(username)) {
                JOptionPane.showMessageDialog(null, "That username is already taken.");
            } else {
                JOptionPane.showMessageDialog(null, "Username successfully captured.");
                break;
            }
        }

        // Password validation loop
        String password;
        while (true) {
            password = promptForPassword("Enter password (min 8 chars, 1 capital, 1 number, 1 special char):");
            if (password == null) return false;
            
            if (!checkPasswordComplexity(password)) {
                JOptionPane.showMessageDialog(null, "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.");
                continue;
            }
            JOptionPane.showMessageDialog(null, "Password successfully captured.");
            break;
        }

        // Phone number validation loop
        String cellphone;
        while (true) {
            cellphone = JOptionPane.showInputDialog("Enter your cellphone number with international code (e.g. +27821234567):");
            if (cellphone == null) return false;
            
            if (checkCellPhoneNumber(cellphone)) {
                JOptionPane.showMessageDialog(null, "Cell phone number successfully added.");
                break;
            }
            JOptionPane.showMessageDialog(null, "Cell phone number incorrectly formatted or does not contain international code.");
        }

        // Name collection
        String firstName = JOptionPane.showInputDialog("Enter first name:");
        if (firstName == null) return false;
        String lastName = JOptionPane.showInputDialog("Enter last name:");
        if (lastName == null) return false;

        // Save user data
        saveUserToFile(username, password, cellphone, firstName, lastName);
        JOptionPane.showMessageDialog(null, "Registration successful!");

        // Offer immediate login
        int loginNow = JOptionPane.showConfirmDialog(null, "Would you like to log in now?", "Login?", JOptionPane.YES_NO_OPTION);
        return loginNow == JOptionPane.YES_OPTION;
    }

    /**
     * Handle user login process
     * @return username if login successful, null otherwise
     */
    public static String handleLogin() {
        JOptionPane.showMessageDialog(null, "=== Login ===");

        String loginUsername = JOptionPane.showInputDialog("Enter username:");
        if (loginUsername == null) return null;

        String loginPassword = promptForPassword("Enter your password:");
        if (loginPassword == null) return null;

        if (loginUser(loginUsername, loginPassword)) {
            JOptionPane.showMessageDialog(null, returnLoginStatus(true));
            return loginUsername;
        } else {
            JOptionPane.showMessageDialog(null, returnLoginStatus(false));
            return null;
        }
    }

    /**
     * Save user data to file
     */
    public static void saveUserToFile(String username, String password, String cellphone, String firstName, String lastName) {
        try (FileWriter writer = new FileWriter(USERS_FILE, true)) {
            writer.write(username + "," + password + "," + cellphone + "," + firstName + "," + lastName + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving user data.");
        }
    }

    /**
     * Check if user exists in the system
     */
    public static boolean userExists(String username) {
        try (Scanner scanner = new Scanner(new File(USERS_FILE))) {
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");
                if (data.length >= 1 && data[0].equals(username)) {
                    return true;
                }
            }
        } catch (IOException ignored) {
            // File doesn't exist, return false
        }
        return false;
    }

    /**
     * Authenticate user login
     */
    public static boolean loginUser(String username, String password) {
        try (Scanner scanner = new Scanner(new File(USERS_FILE))) {
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");
                if (data.length >= 5 && data[0].equals(username) && data[1].equals(password)) {
                    loginMessage = "Welcome " + data[3] + " " + data[4] + ", it is great to see you again.";
                    return true;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading user data.");
        }
        return false;
    }

    /**
     * Return login status message
     * @param success
     */
    public static String returnLoginStatus(boolean success) {
        return success ? loginMessage : "Username or password incorrect, please try again.";
    }

    /**
     * Validate username format
     * @param username
     */
    public static boolean checkUserName(String username) {
        return username != null && username.contains("_") && username.length() <= 5;
    }

    /**
     * Validate password complexity
     * @param password
     */
    public static boolean checkPasswordComplexity(String password) {
        return password != null && password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{8,}$");
    }

    /**
     * Validate cellphone number format
     * @param cellphone
     */
    public static boolean checkCellPhoneNumber(String cellphone) {
        return cellphone != null && cellphone.matches("^\\+27\\d{9}$");
    }

    /**
     * Secure password input with show/hide option
     */
    public static String promptForPassword(String message) {
        JPasswordField passwordField = new JPasswordField(20);
        JCheckBox showPassword = new JCheckBox("Show Password");

        showPassword.addActionListener(e -> passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '•'));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(message));
        panel.add(passwordField);
        panel.add(showPassword);

        int result = JOptionPane.showConfirmDialog(null, panel, "Password Entry", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            return null;
        }

        return new String(passwordField.getPassword());
    }
}