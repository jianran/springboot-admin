package io.github.adminconsole.auth;

import io.github.adminconsole.config.AdminConsoleProperties;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Internal property-backed fallback; custom verifier beans take precedence. */
public final class DefaultAdminCredentialVerifier implements AdminCredentialVerifier {
    private final AdminConsoleProperties properties;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DefaultAdminCredentialVerifier(AdminConsoleProperties properties) { this.properties = properties; }

    @Override public boolean verify(String username, String password) {
        return properties.getPasswordHash() != null && properties.getUsername().equals(username)
            && encoder.matches(password, properties.getPasswordHash());
    }
}
