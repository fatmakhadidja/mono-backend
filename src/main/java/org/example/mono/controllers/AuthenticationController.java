package org.example.mono.controllers;


import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.mono.auth.AuthenticationRequest;
import org.example.mono.auth.AuthenticationResponse;
import org.example.mono.auth.RegisterRequest;

import org.example.mono.models.Wallet;
import org.example.mono.repositories.UserRepo;
import org.example.mono.repositories.WalletRepo;
import org.example.mono.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;





@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthenticationController {


    @Autowired
    private final WalletRepo walletRepo;

    private final AuthenticationService service;
    @Autowired
    private UserRepo userRepo;


    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            var result = service.register(request);

            Wallet wallet = new Wallet();
            wallet.setBalance(0);
            wallet.setUser(
                    userRepo.findById(result.getId())
                            .orElseThrow(() -> new RuntimeException("User not found"))
            );
            walletRepo.save(wallet);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if ("Email already exists".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error");
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
//        service.refreshToken(request, response);
    }



    
}
