package edu.sumdu.tss.elephant.helper.utils;

import edu.sumdu.tss.elephant.helper.exception.BackupException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmdUtilTest {

    static MockedStatic<Runtime> staticMock = mockStatic(Runtime.class);
    @Mock
    Process process;
    @Mock
    Runtime runtime;


    @AfterAll
    static void closeMock() {
        staticMock.close();
    }

    @SneakyThrows
    @BeforeEach
    void initMocks() {
        // make the getRuntime method to return our runtime mock
        staticMock.when(Runtime::getRuntime).thenReturn(runtime);

        // make the exec method to return our process mock
        when(runtime.exec(anyString())).thenReturn(process);
    }

    @Test
    @DisplayName("Should execute the command with no errors or/and exceptions")
    void testValidCommandExec() {
        // make the getErrorStream to return no errors
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

        CmdUtil.exec("");
    }

    @Test
    @SneakyThrows
    @DisplayName("Should execute the command but handle a non-0 return value")
    void testInvalidCommandExec() {
        String errorMessage = "Something went wrong";
        String wrongCommand = "ech hello world";
        String expectedOutput = "Perform: " + wrongCommand + System.lineSeparator() + errorMessage + System.lineSeparator();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        // change the output stream
        System.setOut(new PrintStream(output));

        // make the getErrorStream to return some error text
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream(errorMessage.getBytes(StandardCharsets.UTF_8)));

        // make the waitFor to return non 0 value
        when(process.waitFor()).thenReturn(1);

        BackupException e = assertThrows(BackupException.class, () -> CmdUtil.exec(wrongCommand));
        assertEquals(errorMessage, e.getMessage());

        // the console output and the expectedOutput should be equal
        assertEquals(expectedOutput, output.toString());

        // change back the original stream
        System.setOut(original);
    }

    @Test
    @DisplayName("Should throw exception with cause message")
    void testExecWithThrowingExWithCauseMessage() {
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

        // make the readLine method throw the IOException
        // use try catch for mock scope
        try (MockedConstruction<BufferedReader> reader = mockConstruction(BufferedReader.class,
                (mock, context) -> when(mock.readLine()).thenThrow(IOException.class))) {
            BackupException e = assertThrows(BackupException.class, () -> CmdUtil.exec(""));

            // the exception message should be equal with cause
            assertEquals(new IOException().toString(), e.getMessage());
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Should throw exception with error message")
    void testExecWithThrowingExWithErrorMessage() {
        String errorMessage = "Something went wrong";
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream(errorMessage.getBytes(StandardCharsets.UTF_8)));

        when(process.waitFor()).thenThrow(InterruptedException.class);

        BackupException e = assertThrows(BackupException.class, () -> CmdUtil.exec(""));

        assertEquals(errorMessage, e.getMessage());
    }
}