package vishal.atm;

import java.util.*;

public class User {

    private int accountNumber;
    private String name;
    private String email;
    private String mobile;
    private int pin;
    private double balance = 0;
    private LinkedList<Transaction> transactions = new LinkedList<>();

    public User(int accNo, String name, String email, String mobile, int pin) {
        this.accountNumber = accNo;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.pin = pin;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public int getAccountNumber() {
        return accountNumber;
    }
    public void setPin(int newPin) {
        this.pin = newPin;
    }


    public double getBalance() {
        return balance;
    }

    public boolean validatePin(int inputPin) {
        return this.pin == inputPin;
    }

    public void deposit(double amount) throws CustomException {
        if (amount <= 0)
            throw new CustomException("Invalid amount!");
        balance += amount;
        addTransaction(TransactionType.DEPOSIT, amount);
    }

    public void withdraw(double amount) throws CustomException {
        if (amount <= 0)
            throw new CustomException("Invalid amount!");
        if (amount > balance)
            throw new CustomException("Insufficient balance!");
        balance -= amount;
        addTransaction(TransactionType.WITHDRAW, amount);
    }

    public void changePin(int oldPin, int newPin) throws CustomException {
        if (this.pin != oldPin)
            throw new CustomException("Old PIN incorrect!");
        Utils.validatePin(newPin);
        this.pin = newPin;
    }

    private void addTransaction(TransactionType type, double amount) {
        transactions.addFirst(new Transaction(type, amount));
        if (transactions.size() > 5)
            transactions.removeLast();
    }

    public void printMiniStatement() {
        System.out.println("\n📜 Last Transactions:");
        if (transactions.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        for (Transaction t : transactions) {
            System.out.println(t);
        }
    }
}
