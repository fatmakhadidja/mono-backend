package org.example.mono.controllers;


import org.example.mono.models.User;
import org.example.mono.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    UserRepo userRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PutMapping("/change_fullname/{fullName}")
    public ResponseEntity<?> changeFullName(@PathVariable String fullName){
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            String email;
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            } else {
                email = principal.toString();
            }

            // Get the user entity from the email
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setFullName(fullName);
            userRepo.save(user);
            return ResponseEntity.ok(user.getFullName());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/change_password")
    public ResponseEntity<?> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword
           ) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            String email;
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            } else {
                email = principal.toString();
            }

            // Get the user entity from the email
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }

            // Set new password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepo.save(user);

            return ResponseEntity.ok("Password changed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error changing password: " + e.getMessage());
        }
    }
    @GetMapping("/picture")
    public ResponseEntity<byte[]> getProfilePicture() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        // Get the user entity from the email
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getProfileImage() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // or IMAGE_PNG depending on what you save
                .body(user.getProfileImage());
    }


    @PutMapping("/change_picture")
    public ResponseEntity<?> changeProfilePicture(@RequestBody Map<String, String> body) {
        try {
            String base64Image = body.get("image");
            if (base64Image == null || base64Image.isEmpty()) {
                return ResponseEntity.badRequest().body("No image provided");
            }

            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String email;
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            } else {
                email = principal.toString();
            }

            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Decode Base64 string and save as byte[]
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            user.setProfileImage(imageBytes);
            userRepo.save(user);

            return ResponseEntity.ok("Profile picture updated successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating profile picture: " + e.getMessage());
        }
    }


}
