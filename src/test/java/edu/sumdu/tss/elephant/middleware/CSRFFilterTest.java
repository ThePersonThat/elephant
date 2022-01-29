package edu.sumdu.tss.elephant.middleware;

import edu.sumdu.tss.elephant.helper.exception.CheckTokenException;
import io.javalin.http.Context;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CSRFFilterTest {

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;

    @Test
    @DisplayName("Should not get the token if the method is not post")
    void testCheckIfMethodIsNotPost() {
        Context context = spy(new Context(request, response, Map.of())); // need to setting request
        when(request.getMethod()).thenReturn("GET"); // make the getMethod to return GET

        CSRFFilter.check(context);

        // should not call any methods
        verify(context, never()).header(anyString());
    }

    @Test
    @DisplayName("Should not throw the exception")
    void testCheckWithValidData() {
        String mockToken = "mockToken";
        Context context = spy(new Context(request, response, Map.of())); // need to setting request
        when(request.getMethod()).thenReturn("POST"); // make the getMethod to return POST

        // mock the methods to prevent throwing an exception
        doReturn(mockToken).when(context).header("X-CSRF-TOKEN");
        doReturn(mockToken).when(context).formParam("_csrf");
        doReturn(null).when(context).sessionAttribute("SessionID");

        try (MockedStatic<CSRFTokenService> mockedService = mockStatic(CSRFTokenService.class)) {
            mockedService.when(() -> CSRFTokenService.validateToken(anyString(), anyString())).thenReturn(true);

            CSRFFilter.check(context);

            // should call the validateToken method with specific parameters and without throwing the exception
            mockedService.verify(() -> CSRFTokenService.validateToken(eq(mockToken), eq(CSRFTokenService.NO_AUTH)));
        }
    }

    @Test
    @DisplayName("Should throw the exception")
    void testCheckWithNull() {
        Context context = spy(new Context(request, response, Map.of())); // need to setting request
        when(request.getMethod()).thenReturn("POST"); // make the getMethod to return POST

        // mock the methods to prevent throwing an exception
        doReturn(null).when(context).header("X-CSRF-TOKEN");
        doReturn(null).when(context).formParam("_csrf");
        doReturn(null).when(context).sessionAttribute("SessionID");

        try (MockedStatic<CSRFTokenService> mockedService = mockStatic(CSRFTokenService.class)) {
            mockedService.when(() -> CSRFTokenService.validateToken(anyString(), anyString())).thenReturn(true);

            assertThrows(CheckTokenException.class, () -> CSRFFilter.check(context));
        }
    }

    @Test
    @DisplayName("Should generate the token if the method is not GET")
    void testGenerateIfMethodIsNotGet() {
        Context context = spy(new Context(request, response, Map.of())); // need to setting request
        when(request.getMethod()).thenReturn("POST"); // make the getMethod to return GET

        CSRFFilter.generate(context);

        // should not call any methods
        verify(context, never()).sessionAttribute(anyString());
    }

    @Test
    @DisplayName("Should generate the token")
    void testGenerate() {
        String mockCSRFToken = "mockCSRFToken";
        Context context = spy(new Context(request, response, Map.of())); // need to setting request
        when(request.getMethod()).thenReturn("GET"); // make the getMethod to return GET

        // make the sessionAttribute to return null
        doReturn(null).when(context).sessionAttribute("SessionID");
        doNothing().when(context).sessionAttribute(anyString(), anyString());

        try (MockedStatic<CSRFTokenService> mockedService = mockStatic(CSRFTokenService.class)) {
            mockedService.when(() -> CSRFTokenService.generateToken(anyString())).thenReturn(mockCSRFToken);

            CSRFFilter.generate(context);

            verify(context).sessionAttribute(eq("csrf"), eq(mockCSRFToken));
        }
    }
}