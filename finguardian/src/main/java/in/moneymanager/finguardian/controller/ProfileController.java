package in.moneymanager.finguardian.controller;

import in.moneymanager.finguardian.dto.ProfileDTO;
import in.moneymanager.finguardian.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {
    private final ProfileService profileService;
    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }
    @PostMapping("/register")
    public ResponseEntity<ProfileDTO>registerProfile(@RequestBody ProfileDTO profileDTO){
        ProfileDTO registeredProfile = profileService.registerProfile(profileDTO);
        return  ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
    }
}
