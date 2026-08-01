package io.github.adminconsole.config;

import io.github.adminconsole.EnableAdminConsole;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.core.type.AnnotationMetadata;

import static org.assertj.core.api.Assertions.assertThat;

class EnableAdminConsoleProfilesTest {
    @Test void loadsWhenConfiguredProfileIsActive() {
        EnableAdminConsoleImportSelector selector = new EnableAdminConsoleImportSelector();
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("dev");
        selector.setEnvironment(environment);
        assertThat(selector.selectImports(AnnotationMetadata.introspect(DevConsole.class))).hasSize(2);
    }

    @Test void doesNotLoadWhenConfiguredProfileIsInactive() {
        EnableAdminConsoleImportSelector selector = new EnableAdminConsoleImportSelector();
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        selector.setEnvironment(environment);
        assertThat(selector.selectImports(AnnotationMetadata.introspect(DevConsole.class))).isEmpty();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAdminConsole(profiles = {"dev", "staging"})
    static class DevConsole {}
}
