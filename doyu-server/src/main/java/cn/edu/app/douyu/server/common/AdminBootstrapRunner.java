package cn.edu.app.douyu.server.common;

import cn.edu.app.douyu.server.common.entity.AdminUserEntity;
import cn.edu.app.douyu.server.common.entity.AdminUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {
    private final AdminUserRepository adminUserRepository;
    private final DouyuProperties properties;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapRunner(AdminUserRepository adminUserRepository,
                                DouyuProperties properties,
                                PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String username = properties.admin().bootstrapUsername();
        if (adminUserRepository.findByUsername(username).isPresent()) {
            return;
        }

        Instant now = Instant.now();
        adminUserRepository.save(new AdminUserEntity(
                "admin_bootstrap",
                username,
                passwordEncoder.encode(properties.admin().bootstrapPassword()),
                "ACTIVE",
                now,
                now
        ));
    }
}
