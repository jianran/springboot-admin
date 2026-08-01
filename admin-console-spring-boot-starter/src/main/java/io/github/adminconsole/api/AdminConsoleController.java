package io.github.adminconsole.api;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import io.github.adminconsole.auth.AdminCredentialVerifier;
import io.github.adminconsole.auth.DefaultAdminCredentialVerifier;
import io.github.adminconsole.auth.LoginAttemptService;
import io.github.adminconsole.auth.LoginAttemptService.LoginLockedException;
import jakarta.servlet.http.HttpServletRequest;
import io.github.adminconsole.config.AdminConsoleProperties;
import io.github.adminconsole.service.ArthasService;
import io.github.adminconsole.service.FeatureFlagService;
import io.github.adminconsole.service.ScheduledJobService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/admin-console/api")
public class AdminConsoleController {
    private final AdminCredentialVerifier verifier;
    private final AdminConsoleProperties properties;
    private final ArthasService arthas;
    private final FeatureFlagService featureFlags;
    private final ScheduledJobService jobs;
    private final LoginAttemptService loginAttempts;

    public AdminConsoleController(List<AdminCredentialVerifier> verifiers, AdminConsoleProperties properties,
                                  ArthasService arthas, FeatureFlagService featureFlags, ScheduledJobService jobs,
                                  LoginAttemptService loginAttempts) {
        List<AdminCredentialVerifier> custom = verifiers.stream()
            .filter(candidate -> !(candidate instanceof DefaultAdminCredentialVerifier)).toList();
        if (custom.size() > 1) throw new IllegalStateException("Configure only one custom AdminCredentialVerifier bean");
        this.verifier = custom.isEmpty() ? verifiers.stream().filter(DefaultAdminCredentialVerifier.class::isInstance)
            .findFirst().orElseThrow() : custom.get(0);
        this.properties = properties; this.arthas = arthas; this.featureFlags = featureFlags; this.jobs = jobs;
        this.loginAttempts = loginAttempts;
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String address = servletRequest.getRemoteAddr();
        try { loginAttempts.assertAllowed(request.username(), address); }
        catch (LoginLockedException exception) { throw locked(exception); }
        if (!verifier.verify(request.username(), request.password())) {
            try {
                int remaining = loginAttempts.recordFailure(request.username(), address);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials; " + remaining + " attempts remaining");
            } catch (LoginLockedException exception) { throw locked(exception); }
        }
        loginAttempts.recordSuccess(request.username(), address);
        StpUtil.login(request.username(), new SaLoginModel().setTimeout(properties.getSessionTimeout().toSeconds()));
        return Map.of("token", StpUtil.getTokenValue(), "username", request.username());
    }

    private ResponseStatusException locked(LoginLockedException exception) {
        return new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
            "Too many failed login attempts; locked until " + exception.getLockedUntil());
    }

    @PostMapping("/auth/logout") public void logout() { StpUtil.logout(); }
    @GetMapping("/auth/me") public Map<String, Object> me() { return Map.of("loginId", StpUtil.getLoginId()); }
    @PostMapping("/arthas/execute") public Object arthas(@RequestBody CommandRequest request) { return arthas.execute(request.command()); }
    @GetMapping("/arthas/status") public Object arthasStatus() { return arthas.status(); }
    @PostMapping("/arthas/attach") public Object attachArthas() { return arthas.attach(); }
    @GetMapping("/feature-flags") public Object featureFlags() { return featureFlags.list(); }
    @PutMapping("/feature-flags/{bean}/{field}") public Object updateFeatureFlag(@PathVariable("bean") String bean,
        @PathVariable("field") String field, @RequestBody ValueRequest request) {
        return Map.of("value", featureFlags.update(bean, field, request.value()));
    }
    @GetMapping("/jobs") public Object jobs() { return jobs.list(); }
    @PostMapping("/jobs/trigger") public void trigger(@RequestBody JobRequest request) { jobs.trigger(request.id()); }

    public record LoginRequest(String username, String password) {}
    public record CommandRequest(String command) {}
    public record ValueRequest(Object value) {}
    public record JobRequest(String id) {}
}
