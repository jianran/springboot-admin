package io.github.adminconsole.auth;

import io.github.adminconsole.config.AdminConsoleProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptServiceTest {
    @Test void locksAfterFiveFailures() {
        AdminConsoleProperties properties = new AdminConsoleProperties();
        LoginAttemptService service = new LoginAttemptService(properties,
            Clock.fixed(Instant.parse("2026-08-01T00:00:00Z"), ZoneOffset.UTC));

        assertThat(service.recordFailure("admin", "127.0.0.1")).isEqualTo(4);
        assertThat(service.recordFailure("admin", "127.0.0.1")).isEqualTo(3);
        assertThat(service.recordFailure("admin", "127.0.0.1")).isEqualTo(2);
        assertThat(service.recordFailure("admin", "127.0.0.1")).isEqualTo(1);
        assertThatThrownBy(() -> service.recordFailure("admin", "127.0.0.1"))
            .isInstanceOf(LoginAttemptService.LoginLockedException.class);
        assertThatThrownBy(() -> service.assertAllowed("admin", "127.0.0.1"))
            .isInstanceOf(LoginAttemptService.LoginLockedException.class);
    }

    @Test void successfulLoginClearsFailures() {
        AdminConsoleProperties properties = new AdminConsoleProperties();
        LoginAttemptService service = new LoginAttemptService(properties);
        service.recordFailure("admin", "127.0.0.1");
        service.recordSuccess("admin", "127.0.0.1");
        assertThat(service.recordFailure("admin", "127.0.0.1")).isEqualTo(4);
    }
}
