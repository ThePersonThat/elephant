package edu.sumdu.tss.elephant.helper;

import com.google.common.io.Resources;
import edu.sumdu.tss.elephant.helper.enums.Lang;
import io.javalin.core.util.JavalinLogger;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

/* also, this class need to be covered with integration tests */
class MailServiceTest {

    static MockedStatic<JavalinLogger> mockedLogger = mockStatic(JavalinLogger.class);
    static MockedStatic<Transport> mockTransport = mockStatic(Transport.class);
    static MockedStatic<Keys> mockKeys = mockStatic(Keys.class);
    static MockedStatic<Session> mockSession = mockStatic(Session.class);
    static String mockToken = "Mock token";
    static String mockEmailTo = "alex@gmail.com";
    static String mockEmailFrom = "dummy@mail.com";


    @AfterAll
    static void closeMocks() {
        mockedLogger.close();
        mockTransport.close();
        mockKeys.close();
        mockSession.close();
    }

    @BeforeEach
    void clearInvocations() {
        // drop count invocations
        mockedLogger.clearInvocations();
        mockTransport.clearInvocations();

        // make the Transport class to send nothing
        mockTransport.when(() -> Transport.send(any(Message.class))).then(invocationOnMock -> null);
        // make the Keys to return dummy smtp parameters
        mockKeys.when(() -> Keys.get(anyString())).thenReturn(mockEmailFrom);
        // make the Session to return dummy session with smtp server
        mockSession.when(() -> Session.getInstance(any(Properties.class), any(Authenticator.class)))
                .thenReturn(null);
    }

    @SneakyThrows
    static List<String> getContentFromFile(String filepath) {
        File file = new File(Objects.requireNonNull(MailServiceTest.class.getClassLoader()
                .getResource(filepath)).getFile());

        // get info about file in the same order as in the sendActivationLink method
        return List.of(filepath, file.toURI().toString(),
                FileUtils.readFileToString(file, Charset.defaultCharset()));
    }

    static List<String> getContentForLog(String txtPath, String htmlPath) {
        List<String> loggerInfos = new ArrayList<>();

        loggerInfos.addAll(getContentFromFile(txtPath));
        loggerInfos.addAll(getContentFromFile(htmlPath));


        return loggerInfos;
    }

    static Stream<Arguments> prepareData() {
        List<String> loggerEnglishInfos = getContentForLog("i18n/mail_conformation_en.txt",
                "i18n/mail_conformation_en.html");
        List<String> loggerUkraineInfos = getContentForLog("i18n/mail_conformation_uk.txt",
                "i18n/mail_conformation_uk.html");


        return Stream.of(
                Arguments.of(Lang.EN, loggerEnglishInfos),
                Arguments.of(Lang.UK, loggerUkraineInfos)
        );
    }

    @ParameterizedTest
    @DisplayName("Should call the Transport.send method with specific content which is selected by lang")
    @SneakyThrows
    @MethodSource("prepareData")
    void testSendActivationLink(Lang lang, List<String> loggerMessages) {
        MailService.sendActivationLink(mockToken, mockEmailTo, lang);

        // should call the JavalinLogger.info for each message
        loggerMessages.forEach(message -> mockedLogger.verify(() -> JavalinLogger.info(eq(message))));

        // should call the send method
        mockTransport.verify(() -> Transport.send(any(Message.class)));
    }

    @Test
    @DisplayName("Should throw the IOException if the resource is not found and log this exception")
    @SneakyThrows
    void testSendActivationLinkWithIOException() {
        try (MockedStatic<Resources> mockedResources = mockStatic(Resources.class)) {
            IOException e = new IOException("Something went wrong");

            // make the Resources.getResource method to return dummy url
            mockedResources.when(() -> Resources.getResource(anyString()))
                    .thenReturn(new URL("http", "localhost", "file"));

            // make the Resources.toString method to throw the IOException
            mockedResources.when(() -> Resources.toString(any(URL.class), any(Charset.class)))
                    .thenThrow(e);

            MailService.sendActivationLink(mockToken, mockEmailTo, Lang.EN);

            // should call the info method with the url
            mockedLogger.verify(() -> JavalinLogger.info(eq("http://localhostfile")), times(2));

            // should call the info method with the exception message
            mockedLogger.verify(() -> JavalinLogger.info(eq(e.getMessage())), times(2));

            // should call the send method
            mockTransport.verify(() -> Transport.send(any(Message.class)));
        }
    }

    @ParameterizedTest
    @SneakyThrows
    @DisplayName("Should call the Transport.send method for sending a reset link with setted message")
    @ValueSource(strings = {"EN", "UK"})
    void testSendResetLink(Lang lang) {
        MailService.sendResetLink(mockToken, mockEmailTo, lang);

        // should call the send method
        mockTransport.verify(() -> Transport.send(any(Message.class)));
    }
}