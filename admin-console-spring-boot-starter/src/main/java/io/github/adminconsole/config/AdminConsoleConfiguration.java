package io.github.adminconsole.config;

import io.github.adminconsole.api.AdminConsoleController;
import io.github.adminconsole.auth.AdminCredentialVerifier;
import io.github.adminconsole.auth.AdminConsoleInterceptor;
import io.github.adminconsole.auth.DefaultAdminCredentialVerifier;
import io.github.adminconsole.auth.LoginAttemptService;
import io.github.adminconsole.service.ArthasService;
import io.github.adminconsole.service.FeatureFlagService;
import io.github.adminconsole.service.ScheduledJobService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AdminConsoleProperties.class)
@Import({AdminConsoleController.class, io.github.adminconsole.api.AdminConsoleExceptionHandler.class,
    ArthasService.class, FeatureFlagService.class, ScheduledJobService.class})
public class AdminConsoleConfiguration {
    @Bean
    public AdminConsoleInterceptor adminConsoleInterceptor() { return new AdminConsoleInterceptor(); }

    @Bean
    public LoginAttemptService adminConsoleLoginAttemptService(AdminConsoleProperties properties) {
        return new LoginAttemptService(properties);
    }

    @Bean
    public WebMvcConfigurer adminConsoleWebMvcConfigurer(AdminConsoleInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor).addPathPatterns("/admin-console/api/**")
                    .excludePathPatterns("/admin-console/api/auth/login");
            }
        };
    }

    @Bean
    public DefaultAdminCredentialVerifier adminCredentialVerifier(AdminConsoleProperties properties) {
        return new DefaultAdminCredentialVerifier(properties);
    }
}
