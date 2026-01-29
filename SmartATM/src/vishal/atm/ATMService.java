package vishal.atm;

import java.util.*;

public class ATMService {

    private HashMap<Integer, User> users = new HashMap<>();
    private Scanner sc = new Scanner(System.in);
    private User loggedInUser = null;

    private int generatedOTP;
    private long otpTime;

    public void start() {
        while (true) {
            System.out.println("\n====== SMART ATM ======");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> {
                    System.out.println("Thank you for using SmartATM!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    // ================= REGISTER =================
    private void register() {
        try {
            System.out.print("Enter Name: ");
            String name = sc.next();

            System.out.print("Enter Email: ");
            String email = sc.next();

            System.out.print("Enter Mobile Number: ");
            String mobile = sc.next();

            System.out.print("Enter 4-digit PIN: ");
            int pin = sc.nextInt();
            Utils.validatePin(pin);

            int accNo = Utils.generateAccountNumber();
            User user = new User(accNo, name, email, mobile, pin);

            users.put(accNo, user);

            System.out.println("✅ Account Created Successfully!");
            System.out.println("Your Account Number: " + accNo);

            EmailService.sendEmail(
                    email,
                    "SmartATM Account Created",
                    "Hello " + name +
                            "\nYour account has been created successfully." +
                            "\nAccount Number: " + accNo +
                            "\n\nThank you for using SmartATM!"
            );

        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ================= LOGIN WITH FORGOT PIN =================
    private void login() {
        try {
            System.out.print("Enter Account Number: ");
            int accNo = sc.nextInt();

            System.out.print("Enter PIN: ");
            int pin = sc.nextInt();

            User user = users.get(accNo);

            if (user == null)
                throw new CustomException("Account not found!");

            // ✅ If PIN correct → login directly
            if (user.validatePin(pin)) {
                loggedInUser = user;
                System.out.println("✅ Login Successful. Welcome " + user.getName());
                dashboard();
                return;
            }

            // ❌ Wrong PIN → OTP verification
            System.out.println("⚠️ Incorrect PIN! OTP verification required.");

            generatedOTP = generateOTP();
            otpTime = System.currentTimeMillis();

            EmailService.sendEmail(
                    user.getEmail(),
                    "SmartATM Login OTP",
                    "Your OTP for PIN recovery is: " + generatedOTP +
                    "\nValid for 2 minutes."
            );

            System.out.print("Enter OTP sent to your email: ");
            int enteredOTP = sc.nextInt();

            if (enteredOTP != generatedOTP)
                throw new CustomException("Invalid OTP!");

            if ((System.currentTimeMillis() - otpTime) > 120000)
                throw new CustomException("OTP Expired!");

            // ✅ OTP verified → Show Forgot PIN menu
            System.out.println("\nOTP Verified!");
            System.out.println("1. Reset PIN (Forgot PIN)");
            System.out.println("2. Cancel Login");
            System.out.print("Choose: ");
            int choice = sc.nextInt();

            if (choice == 1) {
                resetPinAfterOTP(user);
                loggedInUser = user;
                System.out.println("✅ Login Successful after PIN reset. Welcome " + user.getName());
                dashboard();
            } else {
                System.out.println("Login cancelled.");
            }

        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ================= RESET PIN AFTER OTP =================
    private void resetPinAfterOTP(User user) {
        try {
            System.out.print("Enter New 4-digit PIN: ");
            int newPin = sc.nextInt();
            Utils.validatePin(newPin);

            System.out.print("Confirm New PIN: ");
            int confirmPin = sc.nextInt();

            if (newPin != confirmPin)
                throw new CustomException("PIN does not match!");

            user.setPin(newPin);   // setter in User class

            System.out.println("✅ PIN Reset Successful!");

            EmailService.sendEmail(
                    user.getEmail(),
                    "PIN Reset Successful",
                    "Your ATM PIN has been reset successfully."
            );

        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ================= DASHBOARD =================
    private void dashboard() {
        while (loggedInUser != null) {
            System.out.println("\n------ DASHBOARD ------");
            System.out.println("1. Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Change PIN");
            System.out.println("5. Mini Statement");
            System.out.println("6. Logout");
            System.out.print("Choose: ");

            int ch = sc.nextInt();

            switch (ch) {
                case 1 -> System.out.println("💰 Balance: ₹" + loggedInUser.getBalance());
                case 2 -> deposit();
                case 3 -> withdraw();
                case 4 -> changePin();
                case 5 -> loggedInUser.printMiniStatement();
                case 6 -> logout();
                default -> System.out.println("Invalid option!");
            }
        }
    }

    // ================= DEPOSIT =================
    private void deposit() {
        try {
            System.out.print("Enter amount: ");
            double amt = sc.nextDouble();
            loggedInUser.deposit(amt);
            System.out.println("✅ Amount Deposited!");

            EmailService.sendEmail(
                    loggedInUser.getEmail(),
                    "Deposit Successful",
                    "₹" + amt + " deposited successfully into " +
                    maskAccountNumber(loggedInUser.getAccountNumber()) + " account\n" +
                    "Current Balance: ₹" + loggedInUser.getBalance()
            );

            SMSService.sendSMS(
                    loggedInUser.getMobile(),
                    "₹" + amt + " deposited into " +
                    maskAccountNumber(loggedInUser.getAccountNumber()) +
                    " account. Balance: ₹" + loggedInUser.getBalance()
            );

        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ================= WITHDRAW (OTP) =================
    private void withdraw() {
        try {
            System.out.print("Enter amount: ");
            double amt = sc.nextDouble();

            if (!verifyOTP(loggedInUser.getEmail())) {
                System.out.println("🚫 Withdrawal cancelled due to OTP failure.");
                return;
            }

            loggedInUser.withdraw(amt);
            System.out.println("✅ Withdrawal Successful!");

            EmailService.sendEmail(
                    loggedInUser.getEmail(),
                    "Withdrawal Successful",
                    "₹" + amt + " withdrawn successfully from " +
                    maskAccountNumber(loggedInUser.getAccountNumber()) + " account\n" +
                    "Current Balance: ₹" + loggedInUser.getBalance()
            );

            SMSService.sendSMS(
                    loggedInUser.getMobile(),
                    "₹" + amt + " withdrawn from " +
                    maskAccountNumber(loggedInUser.getAccountNumber()) +
                    " account. Balance: ₹" + loggedInUser.getBalance()
            );

        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ================= CHANGE PIN =================
    private void changePin() {
        try {
            System.out.print("Enter Old PIN: ");
            int oldPin = sc.nextInt();

            System.out.print("Enter New PIN: ");
            int newPin = sc.nextInt();

            loggedInUser.changePin(oldPin, newPin);
            System.out.println("✅ PIN Changed Successfully!");

            EmailService.sendEmail(
                    loggedInUser.getEmail(),
                    "PIN Changed Successfully",
                    "Your ATM PIN has been changed successfully.\nIf this wasn't you, contact support immediately."
            );

        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ================= LOGOUT =================
    private void logout() {
        loggedInUser = null;
        System.out.println("👋 Logged out successfully.");
    }

    // ================= OTP HELPERS =================
    private int generateOTP() {
        return 100000 + new Random().nextInt(900000);
    }

    private boolean verifyOTP(String email) {

        generatedOTP = generateOTP();
        otpTime = System.currentTimeMillis();

        EmailService.sendEmail(
                email,
                "SmartATM Transaction OTP",
                "Your OTP for withdrawal is: " + generatedOTP +
                "\nValid for 2 minutes."
        );

        System.out.print("Enter OTP sent to your email: ");
        int enteredOTP = sc.nextInt();

        if (enteredOTP != generatedOTP) {
            System.out.println("❌ Invalid OTP!");
            return false;
        }

        if ((System.currentTimeMillis() - otpTime) > 120000) {
            System.out.println("❌ OTP Expired!");
            return false;
        }

        return true;
    }

    // ================= UTIL =================
    private String maskAccountNumber(int accNo) {
        String acc = String.valueOf(accNo);
        return "XXXX-" + acc.substring(acc.length() - 4);
    }
}
