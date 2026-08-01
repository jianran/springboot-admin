package io.github.adminconsole.demo;

import io.github.adminconsole.EnableAdminConsole;
import io.github.adminconsole.auth.AdminCredentialVerifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAdminConsole(enableArthas = true, profiles = "default")
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) { SpringApplication.run(DemoApplication.class, args); }

    /** Demo-only credentials: admin / admin123. Never use plaintext credentials in production. */
    @Bean AdminCredentialVerifier demoVerifier() {
        return (username, password) -> "admin".equals(username) && "admin123".equals(password);
    }
}
