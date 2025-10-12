package in.moneymanager.finguardian.service;

import in.moneymanager.finguardian.dto.AuthDTO;
import in.moneymanager.finguardian.dto.ProfileDTO;
import in.moneymanager.finguardian.entity.ProfileEntity;
import in.moneymanager.finguardian.repository.ProfileRepository;
import in.moneymanager.finguardian.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // Activation base URL (e.g. https://finguardian.onrender.com/api/activate)
    @Value("${app.activation.url}")
    private String activationURL;

    /**
     * Register a new profile, persist it and send activation email.
     */
    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        if (profileRepository.findByEmail(profileDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setIsActive(false);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile.setCreatedAt(LocalDateTime.now());
        newProfile.setUpdatedAt(LocalDateTime.now());

        newProfile = profileRepository.save(newProfile);

        String activationLink = activationURL + "?token=" + newProfile.getActivationToken();
        String subject = "Activate your FinGuardian Account";
        String body = "Hello " + newProfile.getFullName() + ",\n\n" +
                "Click the following link to activate your account:\n" +
                activationLink + "\n\nThank you for joining FinGuardian!";

        emailService.sendEmail(newProfile.getEmail(), subject, body);

        return toDTO(newProfile);
    }

    /**
     * Activate a profile using the activation token.
     */
    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profile.setActivationToken(null);
                    profile.setUpdatedAt(LocalDateTime.now());
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Check if account is active by email.
     */
    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    /**
     * Returns the currently authenticated profile entity.
     * (You flagged this — included here.)
     */
    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UsernameNotFoundException("No authenticated user found");
        }
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + authentication.getName()));
    }

    /**
     * Get a public-profile DTO. If email == null, returns the current authenticated user's public profile.
     */
    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity currentUser;
        if (email == null) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
        }

        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .isActive(currentUser.getIsActive())
                .build();
    }

    /**
     * Authenticate credentials and generate JWT token + return public user info.
     */
    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword())
            );
            String token = jwtUtil.generateToken(authDTO.getEmail());
            return Map.of(
                    "token", token,
                    "user", getPublicProfile(authDTO.getEmail())
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password", e);
        }
    }

    /* -------------------- Helper mappers -------------------- */

    private ProfileEntity toEntity(ProfileDTO dto) {
        return ProfileEntity.builder()
                // id intentionally left null to allow DB to generate
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .profileImageUrl(dto.getProfileImageUrl())
                .isActive(false)
                .activationToken(null)
                .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now())
                .updatedAt(dto.getUpdatedAt() != null ? dto.getUpdatedAt() : LocalDateTime.now())
                .build();
    }

    private ProfileDTO toDTO(ProfileEntity entity) {
        return ProfileDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                // DO NOT send hashed password back to client — exclude or null it if present in DTO
                .password(null)
                .profileImageUrl(entity.getProfileImageUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isActive(entity.getIsActive())
                .build();
    }
}
