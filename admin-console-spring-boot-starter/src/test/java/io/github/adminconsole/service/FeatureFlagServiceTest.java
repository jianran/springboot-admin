package io.github.adminconsole.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.adminconsole.config.AdminConsoleProperties;
import io.github.adminconsole.feature.FeatureFlag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeatureFlagServiceTest {
    @Test void listsOnlyAnnotatedFieldsAndRequiresWriteAllowlist() {
        StaticApplicationContext context = new StaticApplicationContext();
        context.getBeanFactory().registerSingleton("settings", new Settings());
        AdminConsoleProperties properties = new AdminConsoleProperties();
        FeatureFlagService service = new FeatureFlagService(context, properties, new ObjectMapper());

        assertThat(service.list()).extracting(flag -> flag.get("id")).containsExactly("settings.enabled");
        assertThatThrownBy(() -> service.update("settings", "enabled", false)).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> service.update("settings", "secret", "changed")).isInstanceOf(IllegalArgumentException.class);
        properties.getBeans().getWritable().add("settings.enabled");
        assertThat(service.update("settings", "enabled", false)).isEqualTo(false);
    }

    static class Settings {
        @FeatureFlag(description = "Test switch") private boolean enabled = true;
        private String secret = "hidden";
    }
}
