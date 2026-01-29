package vishal.atm;
import java.time.LocalDateTime;

public class Transaction {

    private TransactionType type;
    private double amount;
    private LocalDateTime dateTime;

    public Transaction(TransactionType type, double amount) {
        this.type = type;
        this.amount = amount;
        this.dateTime = LocalDateTime.now();
    }

    public String toString() {
        return type + " | ₹" + amount + " | " + dateTime;
    }
}
