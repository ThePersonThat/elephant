package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.exception.HttpError500;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

public class UserTest {
    private static User user;

    @BeforeAll
    public static void initUser() {
        user = new User();
        user.setLogin("test_user");
    }

    @Test
    @DisplayName("Should return user password")
    public void testShouldReturnPassword() {
        String password = "testUserPassword";
        user.password(password);
        String actualPassword = user.getPassword();

        assertEquals(user.crypt(password), actualPassword);
    }

    @Test
    @DisplayName("Should return user role")
    public void testShouldReturnUserRole() {
        Long adminRoleId = 4L;
        UserRole adminRole = UserRole.byValue(adminRoleId);
        user.setRole(adminRoleId);
        UserRole actualRole = user.role();

        assertEquals(adminRole, actualRole);
    }

    @Test
    @DisplayName("Should crypt user password")
    public void testShouldCryptUserPassword() {
        user.setPassword("password1");
        assertNotEquals(user.getPassword(), user.crypt("password2"));
    }

    @Test
    @DisplayName("Should throw HttpError500 in crypt method")
    public void testShouldThrowExceptionInCrypt() throws NoSuchAlgorithmException {
        try (MockedStatic<MessageDigest> mockedMsgDigest = mockStatic(MessageDigest.class)) {
            mockedMsgDigest.when(() -> MessageDigest.getInstance("SHA-384")).thenThrow(NoSuchAlgorithmException.class);

            Exception e = assertThrows(HttpError500.class, () -> user.crypt("password"));
            assertEquals("Fail crypt user password", e.getMessage());

        }
    }


}
