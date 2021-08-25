package edu.sumdu.tss.elephant.helper;

import edu.sumdu.tss.elephant.helper.enums.Lang;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


public class MailService {

    private static final MailService mail = new MailService();
    private final String from;
    private final Session session;

    private MailService() {
        this.from = Keys.get("EMAIL.FROM");

        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", Keys.get("EMAIL.HOST"));
        properties.put("mail.smtp.port", Keys.get("EMAIL.PORT"));
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
        session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Keys.get("EMAIL.USER"), Keys.get("EMAIL.PASSWORD"));
            }
        });
        //session.setDebug(true);
    }

    /**
     * Method to send email with activation link
     *
     * @param token  activation token
     * @param mailTo user email
     */
    public static void sendActivationLink(String token, String mailTo, Lang lang) throws MessagingException {
        Message message = new MimeMessage(mail.session);
        message.setFrom(new InternetAddress(mail.from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
        if (lang == Lang.UA) {
            message.setSubject("Elephant: Вітаємо з приєднанням!");
            message.setText(String.format(
                    "Ви успішно зареєструвалися на платформі моніторингу Elephant," +
                            "щоб продовжити тицніть сюди : %s/registration/confirm/%s" +
                            " (Якщо ви не реєструвалися проігноруйте це повідомлення)", Keys.get("APP.URL"), token));
        } else {
            message.setSubject("Elephant: Welcome to the club buddy");
            message.setText(String.format(
                    "Hello friend, it seems that you have successfully registered your Union," +
                            "what do you have to do by click the link : %s/registration/confirm/%s" +
                            " (if you did not register then just ignore this message)", Keys.get("APP.URL"), token));
        }
        Transport.send(message);
    }

    public static void sendResetLink(String token, String mailTo, Lang lang) throws MessagingException {
        Message message = new MimeMessage(mail.session);
        message.setFrom(new InternetAddress(mail.from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
        if (lang == Lang.UA) {
            message.setSubject("Elephant: Відновлення паролю");
            message.setText(String.format(
                    "Ваше посилання для відновлення паролю,  %s/login/renew/%s" +
                            " (Якщо ви не надсилали запит проігноруйте це повідомлення)", Keys.get("APP.URL"), token));
        } else {
            message.setSubject("Elephant: Reset password");
            message.setText(String.format(
                    "Hello you reset link ,  %s/login/renew/%s" +
                            " (if you did not reset you password then just ignore this message)", Keys.get("APP.URL"), token));
        }
        Transport.send(message);
    }
}


