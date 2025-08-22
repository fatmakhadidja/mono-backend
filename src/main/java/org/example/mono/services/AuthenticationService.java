package org.example.mono.services;

import lombok.RequiredArgsConstructor;
import org.example.mono.auth.AuthenticationRequest;
import org.example.mono.auth.AuthenticationResponse;
import org.example.mono.auth.RegisterRequest;
import org.example.mono.models.User;
import org.example.mono.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Autowired
 private UserRepo userRepo;

 private final PasswordEncoder passwordEncoder;

 private final JwtService jwtService;

 private final AuthenticationManager authenticationManager;


    public AuthenticationResponse register(@RequestBody RegisterRequest request) {
        // Check if email already exists
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        byte[] imageData = null;
        try {
            // Decode Base64 image if provided
            if (request.getImage() != null && !request.getImage().isEmpty()) {
                imageData = Base64.getDecoder().decode(request.getImage());
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to decode image file", e);
        }

        // Build the user entity
        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(User.Role.USER)
                .password(passwordEncoder.encode(request.getPassword()))
                .profileImage(imageData) // save decoded image bytes
                .build();

        // Save user
        userRepo.save(user);

        // Generate JWT token
        var jwtToken = jwtService.generateToken(user);

        // Return authentication response
        return AuthenticationResponse.builder()
                .id(user.getId())
                .token(jwtToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }



    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepo.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);

//        var refreshToken = jwtService.generateRefreshToken(user);
//        revokeAllUserTokens(user);
//        saveUserToken(user, jwtToken);

        return AuthenticationResponse.builder()
                .id(user.getId())
                .token(jwtToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}
