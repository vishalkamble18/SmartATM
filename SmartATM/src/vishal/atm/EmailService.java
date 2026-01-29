package vishal.atm;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

	// ✅ Read from environment variables
	private static final String FROM_EMAIL = System.getenv("ATM_EMAIL");
	private static final String APP_PASSWORD = System.getenv("ATM_EMAIL_PASSWORD");

	public static void sendEmail(String toEmail, String subject, String messageText) {

		// Safety check
		if (FROM_EMAIL == null || APP_PASSWORD == null) {
			System.out.println("❌ Email credentials not found in Environment Variables.");
			System.out.println("Please set ATM_EMAIL and ATM_EMAIL_PASSWORD.");
			return;
		}

		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");

		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
			}
		});

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(FROM_EMAIL));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
			message.setSubject(subject);
			message.setText(messageText);

			Transport.send(message);
			System.out.println("📧 Email sent successfully!");

		} catch (Exception e) {
			System.out.println("❌ Email failed: " + e.getMessage());
		}
	}
}
