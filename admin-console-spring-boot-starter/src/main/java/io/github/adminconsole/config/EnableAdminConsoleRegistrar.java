package io.github.adminconsole.config;

import io.github.adminconsole.EnableAdminConsole;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

public final class EnableAdminConsoleRegistrar implements ImportBeanDefinitionRegistrar {
    static final String SETTINGS_BEAN = "adminConsoleAnnotationSettings";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableAdminConsole.class.getName());
        boolean enableArthas = attributes != null && Boolean.TRUE.equals(attributes.get("enableArthas"));
        RootBeanDefinition definition = new RootBeanDefinition(AdminConsoleAnnotationSettings.class);
        definition.getConstructorArgumentValues().addIndexedArgumentValue(0, enableArthas);
        registry.registerBeanDefinition(SETTINGS_BEAN, definition);
    }
}
