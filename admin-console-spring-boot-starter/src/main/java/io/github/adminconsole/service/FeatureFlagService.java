package io.github.adminconsole.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.adminconsole.config.AdminConsoleProperties;
import io.github.adminconsole.feature.FeatureFlag;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FeatureFlagService {
    private final ApplicationContext context;
    private final AdminConsoleProperties properties;
    private final ObjectMapper mapper;

    public FeatureFlagService(ApplicationContext context, AdminConsoleProperties properties, ObjectMapper mapper) {
        this.context = context;
        this.properties = properties;
        this.mapper = mapper;
    }

    public List<Map<String, Object>> list() {
        List<Map<String, Object>> flags = new ArrayList<>();
        for (String beanName : beanNames()) {
            Object bean;
            try { bean = target(context.getBean(beanName)); } catch (RuntimeException ignored) { continue; }
            allFields(AopUtils.getTargetClass(bean)).filter(field -> field.isAnnotationPresent(FeatureFlag.class))
                .forEach(field -> flags.add(describe(beanName, bean, field)));
        }
        return flags.stream().sorted((left, right) -> ((String) left.get("id")).compareTo((String) right.get("id"))).toList();
    }

    private String[] beanNames() {
        Stream<String> names = Arrays.stream(context.getBeanDefinitionNames());
        if (context.getAutowireCapableBeanFactory() instanceof ConfigurableListableBeanFactory factory)
            names = Stream.concat(names, Arrays.stream(factory.getSingletonNames()));
        return names.distinct().toArray(String[]::new);
    }

    public Object update(String beanName, String fieldName, Object rawValue) {
        Object bean = target(context.getBean(beanName));
        Field field = ReflectionUtils.findField(AopUtils.getTargetClass(bean), fieldName);
        if (field == null || !field.isAnnotationPresent(FeatureFlag.class))
            throw new IllegalArgumentException("Unknown feature flag: " + beanName + "." + fieldName);
        if (!canWrite(beanName, field)) throw new SecurityException("Feature flag is not configured as writable");
        Object converted = mapper.convertValue(rawValue, field.getType());
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, bean, converted);
        return converted;
    }

    private Map<String, Object> describe(String beanName, Object bean, Field field) {
        ReflectionUtils.makeAccessible(field);
        FeatureFlag annotation = field.getAnnotation(FeatureFlag.class);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", beanName + "." + field.getName());
        result.put("bean", beanName);
        result.put("field", field.getName());
        result.put("description", annotation.description());
        result.put("type", field.getType().getName());
        result.put("value", ReflectionUtils.getField(field, bean));
        result.put("writable", canWrite(beanName, field));
        return result;
    }

    private boolean canWrite(String beanName, Field field) {
        return !Modifier.isFinal(field.getModifiers()) && properties.getBeans().getWritable().contains(beanName + "." + field.getName());
    }

    private Stream<Field> allFields(Class<?> type) {
        Stream.Builder<Field> fields = Stream.builder();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass())
            Arrays.stream(current.getDeclaredFields()).forEach(fields);
        return fields.build();
    }

    private Object target(Object bean) {
        try { return bean instanceof Advised advised ? advised.getTargetSource().getTarget() : bean; }
        catch (Exception e) { throw new IllegalStateException("Cannot access proxied bean target", e); }
    }
}
