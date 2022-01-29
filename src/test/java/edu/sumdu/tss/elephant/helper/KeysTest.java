package edu.sumdu.tss.elephant.helper;

import io.javalin.core.util.JavalinLogger;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParametersHelper {
    public static final String FILE_PATH = File.separator + "tmp" +
            File.separator + "mockConfig.properties"; // change the path if you are not on linux;

    private final Map<String, String> map = Map.ofEntries(
            Map.entry("DB.PORT", "5433"),
            Map.entry("DB.LOCAL_PATH", "/tmp/elephant"),
            Map.entry("DB.HOST", "127.0.0.1"),
            Map.entry("DB.URL", "127.0.0.1"),
            Map.entry("DB.NAME", "test"),
            Map.entry("DB.USERNAME", "postgres"),
            Map.entry("DB.PASSWORD", "root"),
            Map.entry("DB.OS_USER", "postgres"),
            Map.entry("APP.URL", "http://127.0.0.1:7000"),
            Map.entry("APP.PORT", "7000"),
            Map.entry("EMAIL.HOST", "smtp.gmail.com"),
            Map.entry("EMAIL.PORT", "465"),
            Map.entry("EMAIL.FROM", "service-mail@gmail.com"),
            Map.entry("EMAIL.PASSWORD", "my-secret-password"),
            Map.entry("EMAIL.USER", "service-mail@gmail.com"),
            Map.entry("EMAIL.SSL", "true"),
            Map.entry("DEFAULT_LANG", "EN"),
            Map.entry("ENV", "DEVELOP")
    );

    /**
     * @param key
     * @return true if the key is secured
     */
    public boolean isSecureParameter(String key) {
        return key.equalsIgnoreCase("DB.PASSWORD") || key.equalsIgnoreCase("EMAIL.PASSWORD");
    }

    /**
     * @return new copy of map
     */
    public Map<String, String> getParamsMap() {
        return new HashMap<>(map);
    }

    /**
     * @param map
     * @return config with custom parameters
     */
    public File getConfigFile(Map<String, String> map) {
        String content = getConfigFromMap(map);

        return createFile(content);
    }

    /**
     * @return mock config file with all the parameters
     */
    public File getConfigFile() {
        String content = getConfigFromMap(map);

        return createFile(content);
    }

    /**
     * @param excludeFields to be excluded from the config file
     * @return mock config file with all the parameters except excluded
     */
    public File getConfigFile(List<String> excludeFields) {
        String content = getConfigFromMap(map, excludeFields);

        return createFile(content);
    }

    @SneakyThrows
    private File createFile(String content) {
        File file = new File(FILE_PATH);
        FileUtils.writeStringToFile(file, content, Charset.defaultCharset());

        return file;
    }

    private String getConfigFromMap(Map<String, String> map) {
        return mapToString(map.entrySet().stream());
    }

    private String getConfigFromMap(Map<String, String> map, List<String> excludeFields) {
        Stream<Map.Entry<String, String>> stream = map.entrySet().stream()
                .filter(entry -> !excludeFields.contains(entry.getKey()));

        return mapToString(stream);
    }

    private String mapToString(Stream<Map.Entry<String, String>> stream) {
        return stream
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(System.lineSeparator()));
    }
}

class KeysTest {

    static MockedStatic<JavalinLogger> mockedLogger = mockStatic(JavalinLogger.class);
    static ParametersHelper helper = new ParametersHelper();

    static Stream<Map.Entry<String, String>> prepareValidConfig() {
        File file = helper.getConfigFile();
        Map<String, String> map = helper.getParamsMap();

        mockedLogger.clearInvocations(); // clear logger info

        Keys.loadParams(file);

        // check if the env is not production because the developing env was setted
        assertFalse(Keys.isProduction());

        return map.entrySet().stream();
    }

    @AfterAll
    static void closeLogger() {
        mockedLogger.close();
    }

    @ParameterizedTest
    @MethodSource("prepareValidConfig")
    @DisplayName("Should load the valid params from file and show warn if the secured parameter is in the file")
    void testLoadParamsWithValidProperties(Map.Entry<String, String> entry) {
        String key = entry.getKey();
        String value = entry.getValue();

        assertEquals(value, Keys.get(key));

        // if the parameter is secured should show warn
        if (helper.isSecureParameter(key)) {
            String warn = String.format("Property %s set in config file. It is insecure", key);
            mockedLogger.verify(() -> JavalinLogger.warn(eq(warn)));
        }
    }

   @Test
    @DisplayName("Should load the parameters with secured params from env")
    @SneakyThrows
    @SetEnvironmentVariable(key = "DB.PASSWORD", value = "databasePassword")
    @SetEnvironmentVariable(key = "EMAIL.PASSWORD", value = "emailPassword")
    void testLoadParamsWithValidPropertiesFromEnv() {
        // create mock file with excluded the secured parameters
        File file = helper.getConfigFile(List.of("DB.PASSWORD", "EMAIL.PASSWORD"));
        Map<String, String> map = helper.getParamsMap();


        // should not show warn because the secured parameters in env
        Keys.loadParams(file);
        mockedLogger.verify(() -> JavalinLogger.warn(anyString()), never());

        /*
         * it is a bad idea to write assert in loop,
         * but the mock env's variables work only for the scope of one method.
         * So I can't write a prepared method for it
         * TODO: if you have time try to research about powermockito maybe there is a solution
        */
        map.forEach((key, expectedValue) -> {
            String actualValue = Keys.get(key);

            assertEquals(expectedValue, actualValue);
        });
    }

    @Test
    @DisplayName("Should throw the IllegalArgumentException with correct message if the parameter is not found")
    void testLoadParamsWithNotExistingProperty() {
        String excludeParam = "DB.URL";
        File file = helper.getConfigFile(List.of(excludeParam)); // exclude the db url parameter

        String expectedMessage = String.format("Property %s not found in %s and or system environment",
                excludeParam, ParametersHelper.FILE_PATH);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Keys.loadParams(file));

        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    @DisplayName("Should throw the IllegalArgumentException with correct message if the SECURED parameter is not found")
    void testLoadParamsWithNotExistingSecuredProperty() {
        String excludeParam = "DB.PASSWORD";
        File file = helper.getConfigFile(List.of(excludeParam)); // exclude the secured db password parameter

        String expectedMessage = String.format("Property %s not found in %s and or system environment (last - preferable)",
                excludeParam, ParametersHelper.FILE_PATH);


        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Keys.loadParams(file));

        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    @DisplayName("Should throw the RuntimeException if can't read file")
    @SneakyThrows
    void testLoadParamsWithFileError() {
        // use try catch for mock scope
        try (MockedStatic<FileUtils> mock = mockStatic(FileUtils.class)) {
            mock.when(() -> FileUtils.openInputStream(null)).thenThrow(IOException.class);

            assertThrows(RuntimeException.class, () -> Keys.loadParams(null));
        }
    }


    @Test
    @DisplayName("Should throw the RuntimeException for unknown property")
    @SetEnvironmentVariable(key = "DB.PASSWORD", value = "databasePassword")
    @SetEnvironmentVariable(key = "EMAIL.PASSWORD", value = "emailPassword")
    void testGetNotExistingParam() {
        String unknownParam = "NOT_EXISTING_PARAM";
        String expectedMessage = String.format("Unknown key %s in app properties", unknownParam);
        File file = helper.getConfigFile(List.of("DB.PASSWORD", "EMAIL.PASSWORD"));
        Keys.loadParams(file);

        RuntimeException e = assertThrows(RuntimeException.class, () -> Keys.get(unknownParam));

        assertEquals(expectedMessage, e.getMessage());
    }

    /* FIXME:
     * this test does not cover the entire method
     * because there is the false condition (bug) which is not executed
     * if (!keys.containsKey(key)) {
     *       throw new RuntimeException(String.format("No value for key %s", key));
     * }
     * and the test can't cover the line with the exception
     */
    @Test
    @DisplayName("Should throw the RuntimeException if the value for the key does not exist")
    void testGetNotExistingValueForExistingParam() {
        String key = "DB.USERNAME";
        String expectedMessage = String.format("No value for key %s", key);

        /* remove value by key */
        Map<String, String> params = helper.getParamsMap();
        params.put(key, "");

        File file = helper.getConfigFile(params);

        Keys.loadParams(file);

        // should show warn 2 times for the 2 secured parameters
        mockedLogger.verify(() -> JavalinLogger.warn(anyString()), times(2));

        RuntimeException e = assertThrows(RuntimeException.class, () -> Keys.get(key));

        assertEquals(expectedMessage, e.getMessage());
    }

    /* small hack to use the BeforeEach block for specific tests */
    @Nested
    class UsingBeforeEach {
        @BeforeEach
        @SneakyThrows
        void clearKeys() {
            // set null to the keys field by reflection
            Field field = Keys.class.getDeclaredField("keys");
            field.setAccessible(true);

            field.set(null, null);
        }

        @Test
        @DisplayName("Should throw the runtime exception if the config is not read")
        void testGetWithoutConfig() {
            String expectedMessage = "Add path to config.property to your application on init";
            RuntimeException e = assertThrows(RuntimeException.class, () -> Keys.get("NOT_EXISTING_PARAMETER"));

            assertEquals(expectedMessage, e.getMessage());
        }
    }
}