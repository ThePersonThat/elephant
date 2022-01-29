package edu.sumdu.tss.elephant.helper.utils;

import io.javalin.http.Context;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ResponseUtilsTest {
    final String STATUS_KEY = "status";
    final String MESSAGE_KEY = "message";
    final String MESSAGE = "Hello world";

    @Test
    @DisplayName("Should return the ok answer with message")
    void testSuccess() {
        HashMap<String, String> result = (HashMap<String, String>) ResponseUtils.success(MESSAGE);

        assertEquals("Ok", result.get(STATUS_KEY));
        assertEquals(MESSAGE, result.get(MESSAGE_KEY));
    }

    @Test
    @DisplayName("Should return the error answer with message")
    void testError() {
        HashMap<String, String> result = (HashMap<String, String>) ResponseUtils.error(MESSAGE);

        assertEquals("Error", result.get(STATUS_KEY));
        assertEquals(MESSAGE, result.get(MESSAGE_KEY));
    }

    @Test
    @DisplayName("Should call context.sessionAttribute 2 times")
    void testFlushFlash() {
        Context context = mock(Context.class); // mock the context class

        ResponseUtils.flush_flash(context);

        /*
        * should call the method 2 times
        * since it has this line:
        * public static final String[] FLASH_KEY = {Keys.ERROR_KEY, Keys.INFO_KEY};
        */
        verify(context, times(2)).sessionAttribute(anyString(), nullable(Object.class));
    }
}