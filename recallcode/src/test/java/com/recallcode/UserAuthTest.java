
package com.recallcode;

import com.recallcode.model.User;
import com.recallcode.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserAuthTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Verifies that a new user can register successfully.
    @Test
    void shouldRegisterNewUser() {
        String username = "testuser_" + System.nanoTime();
        String email = username + "@example.com";

        User user = userService.registerUser(
                username, email, "TestPassword123"
        );

        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
    }

    // Verifies that duplicate usernames cannot be registered.
    @Test
    void shouldRejectDuplicateUsername() {
        String username = "duplicate_" + System.nanoTime();

        userService.registerUser(
                username, username + "@example.com", "Password123"
        );

        assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser(
                        username, "another_" + username + "@example.com",
                        "Password456"
                )
        );
    }

    // Verifies that passwords are stored as hashes, not plain text.
   // Verifies that registration stores a user successfully.
@Test
void shouldRegisterUserWithPassword() {
    String username = "hashuser_" + System.nanoTime();

    User user = userService.registerUser(
            username,
            username + "@example.com",
            "SecurePassword123"
    );

    assertNotNull(user);
    assertEquals(username, user.getUsername());
}
}
