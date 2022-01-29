package edu.sumdu.tss.elephant.helper;

import edu.sumdu.tss.elephant.controller.AbstractController;
import edu.sumdu.tss.elephant.helper.exception.HttpException;
import edu.sumdu.tss.elephant.helper.utils.ExceptionUtils;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.DatabaseService;
import edu.sumdu.tss.elephant.model.User;
import io.javalin.core.util.JavalinLogger;
import io.javalin.http.Context;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewHelperTest {

    @Mock
    Context context;

    @Test
    @DisplayName("Should call context methods with setted values")
    void testUserErrorForProduction() {
        try (
                MockedStatic<AbstractController> mockedController = mockStatic(AbstractController.class);
                MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)
        ) {
            String message = "not found";
            String icon = "icon";
            String stacktrace = "stacktrace";

            Map<String, ?> map = new HashMap<>();

            // make the currentModel to return our map
            mockedController.when(() -> AbstractController.currentModel(any(Context.class))).thenReturn(map);
            mockedKeys.when(Keys::isProduction).thenReturn(false);  // make the isProduction method to return false

            ViewHelper.userError(context, 404, message, icon, stacktrace);

            // the map should be setted with our values
            assertEquals("404", map.get("code"));
            assertEquals(message, map.get("message"));
            assertEquals(icon, map.get("icon"));
            assertEquals(stacktrace, map.get("stacktrace"));

            // the context should call these methods
            verify(context).status(eq(404));
            verify(context).render(eq("/velocity/error.vm"), eq(map));
        }
    }

    @Test
    @DisplayName("Should call context methods with the exception's values")
    void testUserErrorWithException() {
        try (
                MockedStatic<AbstractController> mockedController = mockStatic(AbstractController.class);
                MockedStatic<Keys> mockedKeys = mockStatic(Keys.class);
                MockedStatic<ExceptionUtils> mockedUtils = mockStatic(ExceptionUtils.class)
        ) {
            String message = "message";
            String stacktrace = "stacktrace";
            HttpException e = new HttpException(message);
            Map<String, ?> map = new HashMap<>();

            // make the currentModel to return our map
            mockedController.when(() -> AbstractController.currentModel(any(Context.class))).thenReturn(map);
            mockedKeys.when(Keys::isProduction).thenReturn(false); // make the isProduction method to return false
            mockedUtils.when(() -> ExceptionUtils.stacktrace(any(Throwable.class)))
                    .thenReturn(stacktrace); // make the stacktrace method to return our stacktrace

            ViewHelper.userError(e, context);

            // the map should be setted with our values
            assertEquals(Integer.toString(e.getCode()), map.get("code"));
            assertEquals(message, map.get("message"));
            assertEquals(e.getIcon(), map.get("icon"));
            assertEquals(stacktrace, map.get("stacktrace"));

            // the context should call these methods
            verify(context).status(eq(e.getCode()));
            verify(context).render(eq("/velocity/error.vm"), eq(map));
        }
    }

    @Test
    @DisplayName("Should return correct breadcrumb and set it to the context session")
    void testBreadcrumb() {
        when(context.sessionAttribute(anyString())).thenReturn(null);
        List<String> listBreadcrumbs = ViewHelper.breadcrumb(context);
        String breadcrumb = listBreadcrumbs.get(0);
        String expectedBreadcrumb = "<a href='/home'><ion-icon name=\"home-outline\"></ion-icon></a>";

        verify(context).sessionAttribute(eq(Keys.BREADCRUMB_KEY), eq(listBreadcrumbs));

        assertEquals(expectedBreadcrumb, breadcrumb);
    }

    @Test
    @DisplayName("Should call all the methods with specific values")
    void testCleanupSession() {
        ViewHelper.cleanupSession(context);
        verify(context).sessionAttribute(eq(Keys.MODEL_KEY), nullable(Object.class));
        verify(context).sessionAttribute(eq(Keys.DB_KEY), nullable(Object.class));
        verify(context).sessionAttribute(eq(Keys.BREADCRUMB_KEY), nullable(Object.class));
    }

    @Test
    @DisplayName("Should set the correct values for the context when the user is null")
    void testDefaultVariablesWhenUserNull() {
        // mock calling the sessionAttribute method
        doNothing().when(context).sessionAttribute(anyString(), any());
        when(context.sessionAttribute(anyString())).thenReturn("mockString");
        when(context.sessionAttribute(eq(Keys.BREADCRUMB_KEY))).thenReturn(List.of("mockString"));

        // make the sessionAttribute method to return specific value by different keys
        when(context.sessionAttribute(eq(Keys.SESSION_CURRENT_USER_KEY))).thenReturn(null);
        when(context.sessionAttribute(eq(Keys.ERROR_KEY))).thenReturn("ERROR.KEY");
        when(context.sessionAttribute(eq(Keys.INFO_KEY))).thenReturn("INFO.KEY");
        when(context.path()).thenReturn("/context/path/");

        ViewHelper.defaultVariables(context);

        // should call the sessionAttribute method with this values
        verify(context).sessionAttribute(eq(Keys.MODEL_KEY), anyMap());
        verify(context).sessionAttribute(eq(Keys.LANG_KEY));

        // flash
        verify(context).sessionAttribute(eq(Keys.ERROR_KEY), nullable(String.class));
        verify(context).sessionAttribute(eq(Keys.INFO_KEY), nullable(String.class));

    }

    @Test
    @DisplayName("Should set the correct values for the context when the user is not null")
    void testDefaultVariablesWhenUserNotNull() {
        try (
                MockedStatic<JavalinLogger> mockedLogger = mockStatic(JavalinLogger.class);
                MockedStatic<DatabaseService> mockedDatabaseService = mockStatic(DatabaseService.class)
        ) {
            String databasePath = "/context/path/database/nameofdatabase";

            // mock calling the sessionAttribute method
            doNothing().when(context).sessionAttribute(anyString(), any());
            when(context.sessionAttribute(anyString())).thenReturn("mockString");
            when(context.sessionAttribute(eq(Keys.BREADCRUMB_KEY)))
                    .thenReturn(new ArrayList<>(List.of("mockString"))); // make the mutable list, the List.of return immutable list

            // make the sessionAttribute method to return specific value by different keys
            User user = new User();
            user.setLanguage("EN");
            when(context.sessionAttribute(eq(Keys.SESSION_CURRENT_USER_KEY))).thenReturn(user);
            when(context.sessionAttribute(eq(Keys.ERROR_KEY))).thenReturn("ERROR.KEY");
            when(context.sessionAttribute(eq(Keys.INFO_KEY))).thenReturn("INFO.KEY");
            when(context.path()).thenReturn(databasePath);

            Database database = new Database();
            mockedDatabaseService.when(() -> DatabaseService.activeDatabase(eq(user.getUsername()), eq("nameofdatabase")))
                    .thenReturn(database);


            ViewHelper.defaultVariables(context);

            // should call the sessionAttribute method with this values
            verify(context).sessionAttribute(eq(Keys.MODEL_KEY), anyMap());

            // should call the info method with the user info
            mockedLogger.verify(() -> JavalinLogger.info(eq(user.toString())));

            // should call the info method with the database path
            mockedLogger.verify(() -> JavalinLogger.info(eq(databasePath)));

            verify(context).sessionAttribute(eq(Keys.DB_KEY), eq(database));

            // flash
            verify(context).sessionAttribute(eq(Keys.ERROR_KEY), nullable(String.class));
            verify(context).sessionAttribute(eq(Keys.INFO_KEY), nullable(String.class));
        }
    }

    @ParameterizedTest
    @DisplayName("Should generate the correct pager")
    @CsvSource({"5, 2", "10, 5", "15, 4"})
    void testPager(int totalPage, int currentPage) {
        String formatForCurrentPage = "<li class=\"page-item active\"><a class=\"page-link\" href=\"#\">%d</a></li>";
        String formatForAnyPage = "<li class=\"page-item\"><a class=\"page-link\" href=\"?offset=%d\">%d</a></li>";

        // Emulate the pager method
        String expectedPager = "<nav>" + System.lineSeparator() + "<ul class=\"pagination\">"
                + IntStream.range(1, totalPage).mapToObj(i ->
                i == currentPage ?
                        String.format(formatForCurrentPage, i) :
                        String.format(formatForAnyPage, i, i)
        ).collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator() + "</ul>"
                + System.lineSeparator() + "</nav>";

        String actualResult = ViewHelper.pager(totalPage, currentPage);

        assertEquals(expectedPager, actualResult);
    }

    @Test
    @DisplayName("Should redirect with setted message")
    void testSoftError() {
        String message = "message";
        String redirectPath = "/home";
        when(context.header(anyString())).thenReturn(redirectPath);

        ViewHelper.softError(message, context);

        verify(context).sessionAttribute(eq(Keys.ERROR_KEY), eq(message));
        verify(context).redirect(eq(redirectPath));
    }

    @Test
    @DisplayName("Should redirect back")
    void testRedirectBack() {
        String redirectPath = "/";

        ViewHelper.redirectBack(context);

        verify(context).redirect(eq(redirectPath));
    }
}