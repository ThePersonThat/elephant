package edu.sumdu.tss.elephant.middleware;

import edu.sumdu.tss.elephant.controller.AbstractController;
import edu.sumdu.tss.elephant.controller.HomeController;
import edu.sumdu.tss.elephant.controller.LoginController;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.model.User;
import io.javalin.core.util.JavalinLogger;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAccessManagerTest {

    @Mock
    Context context;
    @Mock
    Handler handler;
    String roleFormat = "User role %s";
    String contextPath = "/context/path";
    static MockedStatic<JavalinLogger> mockedLogger = mockStatic(JavalinLogger.class);

    @AfterAll
    static void closeLogger() {
        mockedLogger.close();
    }

    @AfterEach
    void clearLogger() {
        mockedLogger.clearInvocations();
    }

    @Test
    @DisplayName("Should return if there is not any permissions")
    @SneakyThrows
    void testAccessManagerIfNoPermissions() {
        try (MockedStatic<AbstractController> mockedController = mockStatic(AbstractController.class)) {
            doNothing().when(handler).handle(context); // make the handle method to do nothing

            CustomAccessManager.accessManager.manage(handler, context, Set.of());

            // should call the handle method with the context
            verify(handler).handle(eq(context));
            // should not call any methods
            mockedController.verify(() -> AbstractController.currentUser(context), never());
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Should redirect to the basic page with the 302 error")
    void testAccessManagerIfUserIsNullAndNotPermitted() {
        try (MockedStatic<AbstractController> mockedController = mockStatic(AbstractController.class)) {
            String loggerInfo = String.format("Permission deny to %s for %s", contextPath, "NoUser");

            // make the currentUser method to return null
            mockedController.when(() -> AbstractController.currentUser(context)).thenReturn(null);
            // make the contextPath method to return our path
            when(context.contextPath()).thenReturn(contextPath);

            CustomAccessManager.accessManager.manage(handler, context, Set.of(UserRole.AUTHED));

            // should call the info method with the ANYONE user role
            mockedLogger.verify(() -> JavalinLogger.info(eq(String.format(roleFormat, UserRole.ANYONE))));
            // should call the info method with the description of error
            mockedLogger.verify(() -> JavalinLogger.info(eq(loggerInfo)));

            // should call the redirect method with the login page and 302 error
            verify(context).redirect(eq(LoginController.BASIC_PAGE), eq(302));
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Should redirect to home page with the 302 error")
    void testAccessManagerIfUserIsNotNullAndNotPermitted() {
        try (MockedStatic<AbstractController> mockedController = mockStatic(AbstractController.class)) {
            User user = new User();
            user.setRole(2L);
            String loggerInfo = String.format("Permission deny to %s for %s", contextPath, user);

            // make the currentUser method to return our user
            mockedController.when(() -> AbstractController.currentUser(context)).thenReturn(user);
            // make the contextPath method to return our path
            when(context.contextPath()).thenReturn(contextPath);

            CustomAccessManager.accessManager.manage(handler, context, Set.of(UserRole.PROMOTED_USER));

            // should call the info method with the BASIC_USER user role
            mockedLogger.verify(() -> JavalinLogger.info(eq(String.format(roleFormat, UserRole.byValue(user.getRole())))));
            // should call the info method with the description of error
            mockedLogger.verify(() -> JavalinLogger.info(eq(loggerInfo)));

            // should call the redirect method with the home page and 302 error
            verify(context).redirect(eq(HomeController.BASIC_PAGE), eq(302));
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Should handle if the user has the permissions")
    void testAccessManagerIfUserHasPermissions() {
        try (MockedStatic<AbstractController> mockedController = mockStatic(AbstractController.class)) {
            User user = new User();
            user.setRole(2L);

            // make the currentUser method to return our user
            mockedController.when(() -> AbstractController.currentUser(context)).thenReturn(user);

            CustomAccessManager.accessManager.manage(handler, context, Set.of(UserRole.AUTHED));

            // should call the info method with the BASIC_USER user role
            mockedLogger.verify(() -> JavalinLogger.info(eq(String.format(roleFormat, UserRole.byValue(user.getRole())))));
            verify(handler).handle(eq(context));  // should call the handle method with the context

            verify(context, never()).redirect(anyString());  // should not call the redirect method
        }
    }
}