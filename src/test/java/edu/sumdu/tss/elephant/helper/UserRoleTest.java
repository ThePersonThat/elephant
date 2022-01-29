package edu.sumdu.tss.elephant.helper;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRoleTest {

    @ParameterizedTest
    @DisplayName("Should return correct role")
    @CsvSource(value = {
            "0, ANYONE",
            "1, UNCHEKED",
            "2, BASIC_USER",
            "3, PROMOTED_USER",
            "4, ADMIN"
    })
    void testValidValues(long idRole, UserRole expectedRole) {
        UserRole actualRole = UserRole.byValue(idRole);

        assertEquals(expectedRole, actualRole);
        assertEquals(idRole, actualRole.getValue());
    }

    @ParameterizedTest
    @DisplayName("Should throw runtime exception with correct message")
    @ValueSource(ints = {-1, 5})
    void testInvalidValues(long notExistingId) {
        RuntimeException e = assertThrows(RuntimeException.class, () -> UserRole.byValue(notExistingId));

        assertEquals("UserRole not found for" + notExistingId, e.getMessage());
    }

    static Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of(UserRole.ANYONE, List.of(
                            0L, // max Connections
                            0L, // max DB
                            0L, // max Storage
                            0L, // max Backups per DB
                            0L  // max Scripts per DB
                )),
                Arguments.of(UserRole.UNCHEKED, List.of(0L, 0L, 0L, 0L, 0L)),
                Arguments.of(UserRole.BASIC_USER, List.of(5L, 2L, 20 * FileUtils.ONE_MB, 1L, 2L)),
                Arguments.of(UserRole.PROMOTED_USER, List.of(5L, 3L, 50 * FileUtils.ONE_MB, 5L, 5L)),
                Arguments.of(UserRole.ADMIN, List.of(5L, 100L, 50 * FileUtils.ONE_MB, 10L, 10L))
        );
    }

    @ParameterizedTest
    @DisplayName("Should return correct values for the role")
    @MethodSource("generateData")
    void testRolePermissions(UserRole role, List<Long> values) {
        assertEquals(role.maxConnections(), values.get(0));
        assertEquals(role.maxDB(), values.get(1));
        assertEquals(role.maxStorage(), values.get(2));
        assertEquals(role.maxBackupsPerDB(), values.get(3));
        assertEquals(role.maxScriptsPerDB(), values.get(4));
    }
}