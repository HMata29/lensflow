
package com.lensflow.api.auth.bootstrap;

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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PhotographerProfileRepository profileRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final UserModuleRepository moduleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${LENSFLOW_ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${LENSFLOW_ADMIN_PASSWORD:}")
    private String adminPassword;

    public AdminBootstrap(
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

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (adminEmail == null || adminEmail.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "Configurare LENSFLOW_ADMIN_EMAIL e LENSFLOW_ADMIN_PASSWORD"
            );
        }

        String normalizedEmail = adminEmail.trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> createAdmin(normalizedEmail));

        if (user.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException(
                    "L'email configurata esiste già con un ruolo diverso da ADMIN"
            );
        }

        createProfileIfMissing(user);
        createPreferencesIfMissing(user);
        createModulesIfMissing(user);
    }

    private User createAdmin(String email) {

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(adminPassword));
        user.setRole(UserRole.ADMIN);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    private void createProfileIfMissing(User user) {

        if (profileRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }

        PhotographerProfile profile = new PhotographerProfile();
        profile.setUser(user);
        profile.setFirstName("Hedrick");
        profile.setLastName("Matamorosa");
        profile.setStudioName("LensFlow Studio");

        profileRepository.save(profile);
    }

    private void createPreferencesIfMissing(User user) {

        if (preferencesRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }

        UserPreferences preferences = new UserPreferences();
        preferences.setUser(user);
        preferences.setLanguage("it");
        preferences.setCurrency("EUR");
        preferences.setDateFormat("dd/MM/yyyy");
        preferences.setBrandColor("#d9b477");

        preferencesRepository.save(preferences);
    }

    private void createModulesIfMissing(User user) {

        for (AppModule module : AppModule.values()) {

            if (moduleRepository
                    .findByUserIdAndModule(user.getId(), module)
                    .isPresent()) {
                continue;
            }

            UserModule userModule = new UserModule();
            userModule.setUser(user);
            userModule.setModule(module);
            userModule.setEnabled(true);

            moduleRepository.save(userModule);
        }
    }
}
