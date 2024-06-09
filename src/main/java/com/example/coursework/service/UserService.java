package com.example.coursework.service;

import com.example.coursework.model.Role;
import com.example.coursework.model.User;
import com.example.coursework.repository.RoleRepository;
import com.example.coursework.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Користувач з таким ім'ям вже існує.");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        // Отримуємо роль "USER" з бази даних
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Роль 'USER' не знайдена в базі даних."));

        user.setRoles(Collections.singleton(userRole));

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Поточний користувач не знайдений!");
        }
        String username = authentication.getName();
        return findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Поточний користувач не знайдений"));
    }

}