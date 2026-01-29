package vishal.atm;

public class SMSService {

    public static void sendSMS(String mobile, String message) {
        System.out.println("\n📱 SMS SENT TO " + mobile);
        System.out.println("--------------------------------");
        System.out.println(message);
        System.out.println("--------------------------------\n");
    }
}
