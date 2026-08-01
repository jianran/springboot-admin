package io.github.adminconsole.config;

import io.github.adminconsole.EnableAdminConsole;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

public final class EnableAdminConsoleImportSelector implements ImportSelector, EnvironmentAware {
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) { this.environment = environment; }

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableAdminConsole.class.getName());
        String[] profiles = attributes == null ? new String[0] : (String[]) attributes.get("profiles");
        if (profiles != null && profiles.length > 0 && !environment.acceptsProfiles(Profiles.of(profiles)))
            return new String[0];
        return new String[]{AdminConsoleConfiguration.class.getName(), EnableAdminConsoleRegistrar.class.getName()};
    }
}
