package com.utilitybilling.config;

import com.utilitybilling.entity.Role;
import com.utilitybilling.enums.RoleName;
import com.utilitybilling.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds default roles on application startup if they do not exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                log.info("Seeding role: {}", roleName);
                return roleRepository.save(Role.builder().name(roleName).build());
            });
        }
    }
}
