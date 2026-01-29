package vishal.atm;
import java.util.Random;

public class Utils {

    public static int generateAccountNumber() {
        return 100000 + new Random().nextInt(900000);
    }

    public static void validatePin(int pin) throws CustomException {
        if (pin < 1000 || pin > 9999)
            throw new CustomException("PIN must be 4 digits!");
    }
}
