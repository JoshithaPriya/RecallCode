
package com.recallcode.controller;

import com.recallcode.model.User;
import com.recallcode.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles new user registration.
     * Returns safe account details without exposing the stored password hash.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @RequestBody RegisterRequest request) {

        User user = userService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        UserResponse response = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public static class RegisterRequest {

        private String username;
        private String email;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class UserResponse {

        private final Long id;
        private final String username;
        private final String email;

        public UserResponse(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }
    }
}
