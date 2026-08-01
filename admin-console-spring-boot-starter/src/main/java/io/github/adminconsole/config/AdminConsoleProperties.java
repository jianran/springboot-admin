package io.github.adminconsole.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

@ConfigurationProperties("admin.console")
public class AdminConsoleProperties {
    private String username = "admin";
    private String passwordHash;
    private Duration sessionTimeout = Duration.ofMinutes(30);
    private int maxLoginAttempts = 5;
    private Duration loginLockout = Duration.ofMinutes(15);
    private final Arthas arthas = new Arthas();
    private final Beans beans = new Beans();
    private final Jobs jobs = new Jobs();

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Duration getSessionTimeout() { return sessionTimeout; }
    public void setSessionTimeout(Duration sessionTimeout) { this.sessionTimeout = sessionTimeout; }
    public int getMaxLoginAttempts() { return maxLoginAttempts; }
    public void setMaxLoginAttempts(int maxLoginAttempts) { this.maxLoginAttempts = maxLoginAttempts; }
    public Duration getLoginLockout() { return loginLockout; }
    public void setLoginLockout(Duration loginLockout) { this.loginLockout = loginLockout; }
    public Arthas getArthas() { return arthas; }
    public Beans getBeans() { return beans; }
    public Jobs getJobs() { return jobs; }

    public static class Arthas {
        private boolean enabled;
        private URI endpoint = URI.create("http://127.0.0.1:8563/api");
        private Duration timeout = Duration.ofSeconds(10);
        private Set<String> allowedCommands = new LinkedHashSet<>(Set.of("version", "dashboard", "thread", "jvm", "sysprop", "sysenv", "logger", "sc", "sm"));
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public URI getEndpoint() { return endpoint; }
        public void setEndpoint(URI endpoint) { this.endpoint = endpoint; }
        public Duration getTimeout() { return timeout; }
        public void setTimeout(Duration timeout) { this.timeout = timeout; }
        public Set<String> getAllowedCommands() { return allowedCommands; }
        public void setAllowedCommands(Set<String> allowedCommands) { this.allowedCommands = allowedCommands; }
    }
    public static class Beans {
        private Set<String> writable = new LinkedHashSet<>();
        public Set<String> getWritable() { return writable; }
        public void setWritable(Set<String> writable) { this.writable = writable; }
    }
    public static class Jobs {
        private Set<String> triggerable = new LinkedHashSet<>();
        public Set<String> getTriggerable() { return triggerable; }
        public void setTriggerable(Set<String> triggerable) { this.triggerable = triggerable; }
    }
}
