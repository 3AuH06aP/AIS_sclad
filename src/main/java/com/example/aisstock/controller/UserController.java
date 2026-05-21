package com.example.aisstock.controller;

import com.example.aisstock.dto.ResetPasswordResponse;
import com.example.aisstock.dto.UserCreateRequest;
import com.example.aisstock.dto.UserDto;
import com.example.aisstock.model.User;
import com.example.aisstock.model.UserRole;
import com.example.aisstock.service.ActivityLogService;
import com.example.aisstock.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader(value = "X-User-Name", defaultValue = "anonymous") String currentUser,
                                    @RequestBody UserCreateRequest request) {
        if (request.getUsername() == null || request.getPassword() == null || request.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Укажите логин и пароль"));
        }
        if (request.getPasswordConfirm() != null
                && !request.getPassword().equals(request.getPasswordConfirm())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Пароли не совпадают"));
        }
        try {
            User created = userService.createStorekeeper(
                    request.getUsername().trim(),
                    request.getPassword());
            activityLogService.logAdmin(
                    currentUser,
                    "admin_create_user",
                    created.getUsername(),
                    "Создан пользователь с ролью STOREKEEPER");
            return ResponseEntity.ok(toDto(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@RequestHeader(value = "X-User-Name", defaultValue = "anonymous") String currentUser,
                                           @PathVariable Long id) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
            String newPassword = userService.resetPassword(id);
            activityLogService.logAdmin(
                    currentUser,
                    "admin_reset_password",
                    user.getUsername(),
                    "Сброшен пароль пользователя");
            return ResponseEntity.ok(new ResetPasswordResponse(user.getUsername(), newPassword));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/enabled")
    public ResponseEntity<?> setEnabled(@RequestHeader(value = "X-User-Name", defaultValue = "anonymous") String currentUser,
                                        @PathVariable Long id,
                                        @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body != null ? body.get("enabled") : null;
        if (enabled == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Укажите поле enabled"));
        }
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
            userService.setEnabled(id, enabled);
            User updated = userService.findById(id).orElseThrow();
            activityLogService.logAdmin(
                    currentUser,
                    enabled ? "admin_unblock_user" : "admin_block_user",
                    updated.getUsername(),
                    enabled ? "Учётная запись разблокирована" : "Учётная запись заблокирована");
            return ResponseEntity.ok(toDto(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(value = "X-User-Name", defaultValue = "anonymous") String currentUser,
                                    @PathVariable Long id) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
            String target = user.getUsername();
            userService.deleteUser(id);
            activityLogService.logAdmin(
                    currentUser,
                    "admin_delete_user",
                    target,
                    "Пользователь удалён");
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                mapRoleForApi(user.getRole()),
                user.getCreatedAt(),
                user.isEnabled());
    }

    /** API role labels: admin | user (STOREKEEPER and USER → user). */
    private static String mapRoleForApi(UserRole role) {
        if (role == UserRole.ADMIN) {
            return "admin";
        }
        return "user";
    }
}
