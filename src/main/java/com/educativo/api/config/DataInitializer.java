package com.educativo.api.config;

import com.educativo.api.entity.CreditCard;
import com.educativo.api.entity.ERole;
import com.educativo.api.entity.Role;
import com.educativo.api.entity.User;
import com.educativo.api.repository.CreditCardRepository;
import com.educativo.api.repository.RoleRepository;
import com.educativo.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CreditCardRepository creditCardRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
// ... existing code
        // Initialize Roles
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(null, ERole.ROLE_ADMIN));
        }
        if (roleRepository.findByName(ERole.ROLE_PARENT).isEmpty()) {
            roleRepository.save(new Role(null, ERole.ROLE_PARENT));
        }

        // Initialize Admin User
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).get();
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .dni("00000000")
                    .firstName("System")
                    .lastName("Administrator")
                    .roles(roles)
                    .build();

            userRepository.save(admin);
            System.out.println("Admin user created: admin / admin123");
        }

        // Ensure all parents have a simulated credit card
        Random random = new Random();
        userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream().anyMatch(r -> r.getName() == ERole.ROLE_PARENT))
                .forEach(parent -> {
                    if (creditCardRepository.findByUser(parent).isEmpty()) {
                        String cardNumber = String.format("%04d %04d %04d %04d", 
                            4000 + random.nextInt(1000), 
                            random.nextInt(10000), 
                            random.nextInt(10000), 
                            random.nextInt(10000));
                        
                        CreditCard card = CreditCard.builder()
                                .cardNumber(cardNumber)
                                .cardHolderName(parent.getFirstName().toUpperCase() + " " + parent.getLastName().toUpperCase())
                                .expirationDate("12/28")
                                .cvv(String.format("%03d", random.nextInt(1000)))
                                .balance(1500.0)
                                .user(parent)
                                .build();
                        creditCardRepository.save(card);
                    }
                });
    }
}
