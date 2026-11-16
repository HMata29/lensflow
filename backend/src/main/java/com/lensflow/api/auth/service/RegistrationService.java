
package com.lensflow.api.auth.service;

import com.lensflow.api.auth.entity.AppModule;
import com.lensflow.api.auth.entity.PhotographerProfile;
import com.lensflow.api.auth.entity.User;
import com.lensflow.api.auth.entity.UserModule;
import com.lensflow.api.auth.entity.UserPreferences;
import com.lensflow.api.auth.entity.UserRole;
import com.lensflow.api.auth.repository.PhotographerProfileRepository;
import com.lensflow.api.auth.repository.UserModuleRepository;
import com.lensflow.api.auth.repository.UserPreferencesRepository;
import com.lensflow.api.auth.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PhotographerProfileRepository profileRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final UserModuleRepository moduleRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            PhotographerProfileRepository profileRepository,
            UserPreferencesRepository preferencesRepository,
            UserModuleRepository moduleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.preferencesRepository = preferencesRepository;
        this.moduleRepository = moduleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String register(
            String email,
            String password,
            String firstName,
            String lastName,
            String studioName
    ) {
        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esiste già un account con questa email"
            );
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La password supera la lunghezza massima consentita"
            );
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.PHOTOGRAPHER);
        user.setEnabled(true);

        user = userRepository.saveAndFlush(user);

        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(user);
        profile.setFirstName(firstName.trim());
        profile.setLastName(lastName.trim());
        profile.setStudioName(
                studioName == null || studioName.isBlank()
                        ? firstName.trim() + " " + lastName.trim()
                        : studioName.trim()
        );

        profileRepository.save(profile);

        UserPreferences preferences = new UserPreferences();
        preferences.setUser(user);
        preferences.setLanguage("it");
        preferences.setCurrency("EUR");
        preferences.setDateFormat("dd/MM/yyyy");
        preferences.setBrandColor("#d9b477");

        preferencesRepository.save(preferences);

        for (AppModule module : AppModule.values()) {
            UserModule userModule = new UserModule();
            userModule.setUser(user);
            userModule.setModule(module);
            userModule.setEnabled(true);

            moduleRepository.save(userModule);
        }

        return user.getEmail();
    }
}
