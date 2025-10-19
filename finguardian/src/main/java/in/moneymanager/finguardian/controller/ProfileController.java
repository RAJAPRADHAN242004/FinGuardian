package in.moneymanager.finguardian.controller;

import in.moneymanager.finguardian.dto.AuthDTO;
import in.moneymanager.finguardian.dto.ProfileDTO;
import in.moneymanager.finguardian.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<?> registerProfile(@RequestBody ProfileDTO profileDTO) {
        try {
            ProfileDTO registeredProfile = profileService.registerProfile(profileDTO);
            // HTTP 201 Created for successful registration
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
        } catch (Exception e) {
            // HTTP 400 Bad Request for errors like "Email already registered"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /* * The /activate endpoint is removed as accounts are now active by default.
     */

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthDTO authDTO) {
        try {
            // Check if account is active is still necessary
            if (!profileService.isAccountActive(authDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "message", "Account is not active. Please activate your account first."
                ));
            }
            Map<String, Object> response = profileService.authenticateAndGenerateToken(authDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }
}