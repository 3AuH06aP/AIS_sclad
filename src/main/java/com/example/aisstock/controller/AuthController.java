package com.example.aisstock.controller;

import com.example.aisstock.dto.AuthRequest;
import com.example.aisstock.dto.AuthResponse;
import com.example.aisstock.model.User;
import com.example.aisstock.model.UserRole;
import com.example.aisstock.service.ActivityLogService;
import com.example.aisstock.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final ActivityLogService activityLogService;

    public AuthController(UserService userService, ActivityLogService activityLogService) {
        this.userService = userService;
        this.activityLogService = activityLogService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().build();
        }

        String username = request.getUsername().trim();
        String password = request.getPassword();

        var existingUser = userService.findByUsername(username);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (!user.getPassword().equals(password)) {
                activityLogService.log(username, "login_failed", "Failed login attempt");
                return ResponseEntity.status(401).build();
            }
            activityLogService.log(username, "login_success", "User logged in");
            return ResponseEntity.ok(new AuthResponse(user.getId(), user.getUsername(), user.getRole().name().toLowerCase()));
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRole(UserRole.USER);
        User saved = userService.createUser(newUser);
        activityLogService.log(username, "login_register", "Created new user via login");
        return ResponseEntity.ok(new AuthResponse(saved.getId(), saved.getUsername(), saved.getRole().name().toLowerCase()));
    }
}
