package edu.sumdu.tss.elephant;

import edu.sumdu.tss.elephant.controller.*;
import edu.sumdu.tss.elephant.controller.api.ApiController;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.exception.HttpException;
import io.javalin.Javalin;
import io.javalin.http.ExceptionHandler;
import io.javalin.http.Handler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

/* also, this class need to be covered with integration tests */
@ExtendWith(MockitoExtension.class)
class ServerTest {
    @Mock
    private Javalin app;

    /* prevent creating controllers */
    private static MockedConstruction<ApiController> mockedApi = mockConstruction(ApiController.class);
    private static MockedConstruction<BackupController> mockedBackup = mockConstruction(BackupController.class);
    private static MockedConstruction<DatabaseController> mockedDatabase = mockConstruction(DatabaseController.class);
    private static MockedConstruction<TableController> mockedTable = mockConstruction(TableController.class);
    private static MockedConstruction<HomeController> mockedHome = mockConstruction(HomeController.class);
    private static MockedConstruction<LoginController> mockedLogin = mockConstruction(LoginController.class);
    private static MockedConstruction<ProfileController> mockedProfile = mockConstruction(ProfileController.class);
    private static MockedConstruction<RegistrationController> mockedRegistration = mockConstruction(RegistrationController.class);
    private static MockedConstruction<ScriptsController> mockedScript = mockConstruction(ScriptsController.class);
    private static MockedConstruction<SqlController> mockedSql = mockConstruction(SqlController.class);

    @AfterAll
    static void closeControllers() {
        mockedApi.close();
        mockedBackup.close();
        mockedDatabase.close();
        mockedTable.close();
        mockedHome.close();
        mockedLogin.close();
        mockedProfile.close();
        mockedRegistration.close();
        mockedScript.close();
        mockedSql.close();
    }

    @Test
    @DisplayName("Should throw a RuntimeException if the config is not setted")
    void testStartServerWithoutConfig() {
        String[] args = new String[0];

        RuntimeException e = assertThrows(RuntimeException.class, () -> Server.main(args));
        assertEquals("File with properties must be specified on startup", e.getMessage());
    }

    @Test
    @DisplayName("Should start the server")
    void testStartServer() {
        String path = "/tmp/config";
        String[] args = {path};

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class);
             MockedStatic<Javalin> mockedJavalin = mockStatic(Javalin.class)) {
            mockedKeys.when(() -> Keys.get(eq("APP.PORT"))).thenReturn("8080");
            mockedJavalin.when(() -> Javalin.create(any())).thenReturn(app);
            when(app.before(any())).thenReturn(app);
            when(app.before(any(), any())).thenReturn(app);
            when(app.after(any())).thenReturn(app);
            when(app.exception(any(), any())).thenReturn(app);

            Server.main(args);

            mockedKeys.verify(() -> Keys.loadParams(isA(File.class)));
            verify(app, times(2)).before(eq("/"), isA(Handler.class));
            verify(app, times(2)).before(isA(Handler.class));
            verify(app).after(isA(Handler.class));
            verify(app).exception(same(HttpException.class), isA(ExceptionHandler.class));
            verify(app).exception(same(Exception.class), isA(ExceptionHandler.class));
        }
    }
}