package com.mushroom.config;

import com.mushroom.user.entity.Role;
import com.mushroom.user.entity.User;
import com.mushroom.user.entity.UserStatus;
import com.mushroom.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Khởi tạo tài khoản Administrator đầu tiên theo quy trình ủy quyền (SRS BR-SEC-003).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            log.info("Chưa có tài khoản Administrator trong hệ thống. Tiến hành khởi tạo tài khoản quản trị mặc định...");
            User admin = User.builder()
                    .fullName("Quản trị viên Hệ thống")
                    .email("admin@mushroom.farm")
                    .passwordHash(passwordEncoder.encode("Admin@Mushroom2026!"))
                    .phone("0901234567")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .emailVerifiedAt(Instant.now())
                    .build();
            userRepository.save(admin);
            log.info(">>> Khởi tạo tài khoản Administrator thành công: admin@mushroom.farm / Admin@Mushroom2026!");
        }
    }
}
