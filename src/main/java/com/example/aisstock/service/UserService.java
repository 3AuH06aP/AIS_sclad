package com.example.aisstock.service;

import com.example.aisstock.model.User;
import com.example.aisstock.model.UserRole;
import com.example.aisstock.repository.DocumentRepository;
import com.example.aisstock.repository.UserRepository;
import com.example.aisstock.util.PasswordGenerator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       DocumentRepository documentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createStorekeeper(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Пароль должен содержать не менее 4 символов");
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.STOREKEEPER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Transactional
    public User createUser(User user) {
        if (user.getRole() == null) {
            user.setRole(UserRole.STOREKEEPER);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Transactional
    public void saveDirectly(User user) {
        userRepository.save(user);
    }

    @Transactional
    public String resetPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        String newPassword = PasswordGenerator.generate(10);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return newPassword;
    }

    @Transactional
    public void setEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        if (documentRepository.existsByCreatedBy(user.getUsername())) {
            throw new IllegalStateException(
                    "Невозможно удалить пользователя: у него есть связанные складские документы");
        }
        userRepository.delete(user);
    }

    /**
     * Plain-text password check (dev mode). Updates lastLoginAt and returns previous value.
     */
    @Transactional
    public Optional<LoginTickResult> verifyPlainPasswordAndTouchLogin(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username)
                .filter(User::isEnabled)
                .filter(user -> user.getPassword().equals(password))
                .map(user -> {
                    OffsetDateTime previous = user.getLastLoginAt();
                    user.setLastLoginAt(OffsetDateTime.now(ZoneOffset.UTC));
                    userRepository.save(user);
                    return new LoginTickResult(user, previous);
                });
    }
}
