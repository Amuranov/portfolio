package be.ac.ulb.infof307.g04.model.client;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/**
 * This class is an "interface" that let us send emails.
 */
public class ServerCommunication {
	
	private static final String MAIL_HOST    = "smtp.gmail.com";
	private static final String FROM_ADDRESS = "noreply.infof3071617@gmail.com";
	private static final String FROM_PWD     = "noreplyinfof307";
	
	/**
	 * Send a mail.
	 * @param destination An email destination.
	 * @param message A message to send.
	 * @param userID The id of the destination user.
	 * @param token A generate token.
	 * @return An ok http code if the mail is sent ok a bad request status code.
	 */
	public Boolean sendMail(String destination, String message, int userID, String token) {
		Boolean mailSent = false;
		Properties props = new Properties();
		props.put("mail.smtp.host", MAIL_HOST);
		props.put("mail.smtp.auth", "true");
		props.put("mail.debug", "false");
		props.put("mail.smtp.ssl.enable", "true");
		props.put("mail.smtp.port", "465");
		Session session = Session.getInstance(props, null);

		try {
			String emailVerificationCode ="http://localhost:8000/server/users/checkmail/"+userID+"/"+token ;
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(FROM_ADDRESS);
			msg.setRecipients(Message.RecipientType.TO, destination);
			msg.setSubject("infof3071617-Project");
			msg.setText( message + emailVerificationCode);
			Transport.send(msg, FROM_ADDRESS, FROM_PWD);
			mailSent = true;
		} catch (MessagingException mex) {
			System.out.println("send failed, exception: " + mex);
		}
		return mailSent;
	}
}
