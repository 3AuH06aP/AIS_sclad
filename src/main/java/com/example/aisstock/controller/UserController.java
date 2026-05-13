package com.example.aisstock.controller;

import com.example.aisstock.dto.UserCreateRequest;
import com.example.aisstock.dto.UserDto;
import com.example.aisstock.model.User;
import com.example.aisstock.model.UserRole;
import com.example.aisstock.service.ActivityLogService;
import com.example.aisstock.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final ActivityLogService activityLogService;

    public UserController(UserService userService, ActivityLogService activityLogService) {
        this.userService = userService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public List<UserDto> list() {
        return userService.findAll().stream()
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getRole().name().toLowerCase()))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestHeader(value = "X-User-Name", defaultValue = "anonymous") String currentUser,
                                          @RequestBody UserCreateRequest request) {
        if (request.getUsername() == null || request.getPassword() == null || request.getUsername().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(request.getPassword());
        user.setRole(UserRole.valueOf((request.getRole() == null ? "USER" : request.getRole()).trim().toUpperCase()));
        User created = userService.createUser(user);
        activityLogService.log(currentUser, "create_user", "Created user " + created.getUsername() + " with role " + created.getRole().name().toLowerCase());
        return ResponseEntity.ok(new UserDto(created.getId(), created.getUsername(), created.getRole().name().toLowerCase()));
    }
}
