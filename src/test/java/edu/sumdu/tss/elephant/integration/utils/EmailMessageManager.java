package edu.sumdu.tss.elephant.integration.utils;

import com.icegreen.greenmail.util.GreenMail;
import lombok.SneakyThrows;
import org.apache.commons.lang.SystemUtils;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static org.junit.jupiter.api.Assertions.*;

public class EmailMessageManager {
    private final GreenMail mailServer;

    public EmailMessageManager(GreenMail mailServer) {
        this.mailServer = mailServer;
    }

    @SneakyThrows
    public void checkMessage(MimeMessage message, String subject, String from) {
        assertEquals(message.getSubject(), subject);
        assertEquals(message.getFrom()[0].toString(), from);
    }


    @SneakyThrows
    public void checkMessage(MimeMessage message, String subject) {
        assertEquals(message.getSubject(), subject);
    }

    public MimeMessage[] getCountMessage(String email, int count) {
        MimeMessage[] messages = mailServer.getReceivedMessagesForDomain(email);
        assertEquals(count, messages.length);

        return messages;
    }

    public MimeMessage getMessageByIndex(String email, int index) {
        MimeMessage[] messages = mailServer.getReceivedMessagesForDomain(email);

        System.out.println("LENGHT for " + email + " = " + messages.length);

        return messages[index];
    }

    @SneakyThrows
    public UrlWithToken getUrlWithTokenFromMessage(MimeMessage message) {
        String urlSubstring = getUrlFromMessage(message);

        int startToken = urlSubstring.lastIndexOf("/") + 1;
        String token = urlSubstring.substring(startToken);

        return new UrlWithToken(urlSubstring.substring(0, startToken) + "{token}", token);
    }

    @SneakyThrows
    public String getUrlFromMessage(MimeMessage message) {
        String emailMessage = ((MimeMultipart) message.getContent()).getBodyPart(0).getContent().toString();

        int start = emailMessage.indexOf("http");
        int end = emailMessage.indexOf(System.lineSeparator(), start) - 1;

        if (SystemUtils.IS_OS_WINDOWS) {
            end += 1;
        }

        return emailMessage.substring(start, end);
    }

    public static class UrlWithToken {
        private String url;
        private String token;

        public UrlWithToken(String url, String token) {
            this.url = url;
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public String getUrl() {
            return url;
        }
    }
}
