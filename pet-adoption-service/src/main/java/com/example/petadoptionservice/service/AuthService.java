package com.example.petadoptionservice.service;

import com.example.petadoptionservice.dto.auth.AuthResponse;
import com.example.petadoptionservice.dto.auth.LoginRequest;
import com.example.petadoptionservice.dto.auth.RegisterRequest;
import com.example.petadoptionservice.entity.Role;
import com.example.petadoptionservice.entity.User;
import com.example.petadoptionservice.repository.UserRepository;
import com.example.petadoptionservice.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    //все нам нужные зависимости
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    //регистр нового юзера
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));//кодируем пароль а потом сохраняем
        user.setMagicTolerance(request.getMagicTolerance());
        user.setHomeType(request.getHomeType());
        user.setRole(Role.USER);

        //сохраняем нового юзера
        User savedUser = userRepository.save(user);
        //наши данные для того чтобы сделать JWT
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(savedUser.getEmail())
                .password(savedUser.getPassword())
                .roles(savedUser.getRole().name())
                .build();

        //сервер сразу создает JWT токен
        return new AuthResponse(jwtService.generateToken(userDetails));
    }

    //вход в систему
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        //ищем юзере в базе userRepository
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        //и опять создается обьект удобный для Spring Security
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        return new AuthResponse(jwtService.generateToken(userDetails));
    }
}
