package io.github.adminconsole.auth;

import io.github.adminconsole.config.AdminConsoleProperties;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class LoginAttemptService {
    private final AdminConsoleProperties properties;
    private final Clock clock;
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(AdminConsoleProperties properties) { this(properties, Clock.systemUTC()); }
    LoginAttemptService(AdminConsoleProperties properties, Clock clock) { this.properties = properties; this.clock = clock; }

    public void assertAllowed(String username, String clientAddress) {
        String key = key(username, clientAddress);
        Attempt attempt = attempts.get(key);
        if (attempt == null) return;
        if (attempt.lockedUntil() != null && attempt.lockedUntil().isAfter(clock.instant()))
            throw new LoginLockedException(attempt.lockedUntil());
        if (attempt.lockedUntil() != null) attempts.remove(key, attempt);
    }

    public int recordFailure(String username, String clientAddress) {
        String key = key(username, clientAddress);
        Attempt updated = attempts.compute(key, (ignored, current) -> {
            int failures = current == null ? 1 : current.failures() + 1;
            Instant lockedUntil = failures >= properties.getMaxLoginAttempts()
                ? clock.instant().plus(properties.getLoginLockout()) : null;
            return new Attempt(failures, lockedUntil);
        });
        if (updated.lockedUntil() != null) throw new LoginLockedException(updated.lockedUntil());
        return Math.max(0, properties.getMaxLoginAttempts() - updated.failures());
    }

    public void recordSuccess(String username, String clientAddress) { attempts.remove(key(username, clientAddress)); }

    private String key(String username, String clientAddress) {
        return clientAddress + '\0' + (username == null ? "" : username.toLowerCase());
    }

    private record Attempt(int failures, Instant lockedUntil) {}

    public static final class LoginLockedException extends RuntimeException {
        private final Instant lockedUntil;
        LoginLockedException(Instant lockedUntil) { super("Too many failed login attempts"); this.lockedUntil = lockedUntil; }
        public Instant getLockedUntil() { return lockedUntil; }
    }
}
