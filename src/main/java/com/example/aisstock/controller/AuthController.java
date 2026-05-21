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
        return userService.findByUsername(request.getUsername())
                .filter(user -> !user.isEnabled())
                .map(user -> ResponseEntity.status(403).<AuthResponse>build())
                .orElseGet(() -> userService.verifyPlainPasswordAndTouchLogin(request.getUsername(), request.getPassword())
                .map(result -> {
                    var user = result.user();
                    activityLogService.log(user.getUsername(), "login_success", "User logged in (Security Disabled)");
                    AuthResponse response = new AuthResponse(user.getId(), user.getUsername(), user.getRole().name().toLowerCase());
                    response.setToken("debug-token-security-disabled");
                    response.setFullName(user.getFullName());
                    response.setLastLoginAt(result.previousLoginAt());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(401).build()));
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        if (user.getRole() == null) {
            user.setRole(UserRole.STOREKEEPER);
        }
        activityLogService.log("system", "register", "New user registered: " + user.getUsername());
        return ResponseEntity.ok(userService.createUser(user));
    }
}
