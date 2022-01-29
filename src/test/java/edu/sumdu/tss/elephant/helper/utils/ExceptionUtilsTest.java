package edu.sumdu.tss.elephant.helper.utils;

import io.javalin.core.util.JavalinLogger;
import io.javalin.core.validation.ValidationError;
import io.javalin.core.validation.ValidationException;
import io.javalin.http.Context;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExceptionUtilsTest {
    final String SQL_UNIQ_EXCEPTION_MESSAGE = "duplicate key value violates unique constraint";

    @Test
    @DisplayName("Should return true if an exception has the sql uniq message")
    void testUniqueException() {
        assertTrue(ExceptionUtils.isSQLUniqueException(new RuntimeException(SQL_UNIQ_EXCEPTION_MESSAGE)));
    }

    @Test
    @DisplayName("Should return true if an exception has a cause with sql uniq message")
    void testUniqueExceptionWithCause() {
        Exception exception = new IllegalStateException("No message",
                new RuntimeException(new NullPointerException(SQL_UNIQ_EXCEPTION_MESSAGE))
        );

        assertTrue(ExceptionUtils.isSQLUniqueException(exception));
    }


    static Stream<Exception> generateExceptions() {
        return Stream.of(
                new RuntimeException("with no sql message", new NullPointerException()),
                new RuntimeException(),
                new IllegalStateException(new RuntimeException("Has errors"))
        );
    }

    @ParameterizedTest
    @DisplayName("Should return false if an exception does not have the sql uniq message")
    @MethodSource("generateExceptions")
    void testNotUniqueException(Exception e) {
        assertFalse(ExceptionUtils.isSQLUniqueException(e));
    }

    @Test
    @DisplayName("Should return the exception stacktrace as string")
    void testStackTrace() {
        String stackTrace = ExceptionUtils.stacktrace(new RuntimeException());

        assertTrue(StringUtils.isNotBlank(stackTrace));
    }

    static Stream<Map<String, List<String>>> generateMaps() {
        return Stream.of(
                Map.of("Key", List.of("Message")),
                Map.of("Key", List.of("Message", "Message2")),
                Map.of("key1", List.of("Message1, Message2"), "key2", List.of("Message2", "Message3"),
                        "key3", List.of("Message4", "Message5"))
        );
    }

    @ParameterizedTest
    @DisplayName("Should return a validation message in the html format")
    @MethodSource("generateMaps")
    void testValidationMessage(Map<String, List<String>> map) {
        /* mock map for the exception */
        Map<String, List<ValidationError<Object>>> mockMap =
                map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry ->
                        entry.getValue().stream()
                                .map(message -> new ValidationError<>(message, Map.of("", ""), null))
                                .collect(Collectors.toList())
                ));

        // Emulate the validationMessage method with saving order
        String expectedString = "<ul>" + mockMap.entrySet().stream()
                .map(entry ->
                        "<li><b>" + entry.getKey() + "</b>&nbsp;" +
                                entry.getValue().stream().map(ValidationError::getMessage)
                                        .collect(Collectors.joining(" ")) + " </li>"
                ).collect(Collectors.joining()) + "</ul>";

        assertEquals(expectedString, ExceptionUtils.validationMessages(new ValidationException(mockMap)));
    }

    @Test
    @DisplayName("Should call the validateMessage method if an exception instanceof ValidationException")
    void testWrapErrorIfInstanceOf() {
        // output from the validateMessage method
        String param = "<ul></ul>";

        Context context = mock(Context.class);
        ValidationException e = mock(ValidationException.class);

        ExceptionUtils.wrapError(context, e);

        // should call the sessionAttribute method with returned param
        verify(context).sessionAttribute(anyString(), eq(param));
    }

    @Test
    @DisplayName("Should call get message and logger if an exception does not instanceof ValidationException")
    void testWrapErrorIfNotInstanceOf() {
        Context context = mock(Context.class);
        String param = "Some message";
        Exception e = new Exception(param);

        try (MockedStatic<JavalinLogger> mockedLogger = mockStatic(JavalinLogger.class)) {
            ExceptionUtils.wrapError(context, e);

            // should call logger with the exception string and with the exception
            mockedLogger.verify(() -> JavalinLogger.error(eq("Exception:"), eq(e)));
        };

        // should call the sessionAttribute method with setted param
        verify(context).sessionAttribute(anyString(), eq(param));
    }
}