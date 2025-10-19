package in.moneymanager.finguardian.service;

import in.moneymanager.finguardian.dto.AuthDTO;
import in.moneymanager.finguardian.dto.ProfileDTO;
import in.moneymanager.finguardian.entity.ProfileEntity;
import in.moneymanager.finguardian.repository.ProfileRepository;
import in.moneymanager.finguardian.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    // EmailService is still kept, assuming it will be used for other purposes (e.g., password reset)
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // ❌ app.activation.url is removed as it's not needed

    /**
     * Register a new profile and set it to active immediately.
     */
    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        // Validation check
        if (profileRepository.findByEmail(profileDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        ProfileEntity newProfile = toEntity(profileDTO);

        // ✨ Account is active by default, no email verification needed
        newProfile.setIsActive(true);
        newProfile.setActivationToken(null);

        // Timestamps can be left to the entity's @CreationTimestamp/@UpdateTimestamp if configured,
        // but explicitly setting them here is also fine.
        newProfile.setCreatedAt(LocalDateTime.now());
        newProfile.setUpdatedAt(LocalDateTime.now());

        newProfile = profileRepository.save(newProfile);

        // ❌ Activation email logic is completely removed

        return toDTO(newProfile);
    }

    /**
     * The activateProfile method is now obsolete and removed.
     */

    /**
     * Check if account is active by email.
     */
    public boolean isAccountActive(String email) {
        // Since registration automatically sets isActive=true, this is mainly a check
        // against database defaults or potential future manual deactivation.
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    /**
     * Returns the currently authenticated profile entity.
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
        Optional<ProfileEntity> profileOptional = Optional.ofNullable(email)
                .flatMap(profileRepository::findByEmail)
                .or(() -> {
                    // If email is null, try to get current authenticated user
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                        return profileRepository.findByEmail(authentication.getName());
                    }
                    return Optional.empty();
                });

        ProfileEntity profile = profileOptional
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found"));

        return toDTO(profile);
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
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                // Password must be encoded here
                .password(passwordEncoder.encode(dto.getPassword()))
                .profileImageUrl(dto.getProfileImageUrl())
                // Ensure isActive is set to true/false as needed (will be overridden in registerProfile for registration)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .activationToken(null) // Not needed
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private ProfileDTO toDTO(ProfileEntity entity) {
        return ProfileDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .password(null) // NEVER send password
                .profileImageUrl(entity.getProfileImageUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isActive(entity.getIsActive())
                .build();
    }
}