package io.github.adminconsole.service;

import io.github.adminconsole.config.AdminConsoleProperties;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class ScheduledJobService {
    private final ApplicationContext context;
    private final AdminConsoleProperties properties;

    public ScheduledJobService(ApplicationContext context, AdminConsoleProperties properties) {
        this.context = context;
        this.properties = properties;
    }

    public List<Map<String, Object>> list() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String beanName : context.getBeanDefinitionNames()) {
            Class<?> type;
            try { type = AopUtils.getTargetClass(context.getBean(beanName)); } catch (RuntimeException ignored) { continue; }
            ReflectionUtils.doWithMethods(type, method -> {
                Scheduled scheduled = method.getAnnotation(Scheduled.class);
                if (scheduled != null) {
                    String id = beanName + "." + method.getName();
                    result.add(Map.of(
                        "id", id,
                        "schedule", schedule(scheduled),
                        "triggerable", properties.getJobs().getTriggerable().contains(id)));
                }
            });
        }
        return result;
    }

    public void trigger(String id) {
        if (!properties.getJobs().getTriggerable().contains(id)) throw new SecurityException("Job is not configured as triggerable");
        int separator = id.lastIndexOf('.');
        if (separator < 1) throw new IllegalArgumentException("Job id must be beanName.methodName");
        Object bean = target(context.getBean(id.substring(0, separator)));
        Method method = ReflectionUtils.findMethod(AopUtils.getTargetClass(bean), id.substring(separator + 1));
        if (method == null || method.getParameterCount() != 0 || method.getAnnotation(Scheduled.class) == null)
            throw new IllegalArgumentException("No zero-argument @Scheduled method: " + id);
        CompletableFuture.runAsync(() -> { ReflectionUtils.makeAccessible(method); ReflectionUtils.invokeMethod(method, bean); });
    }

    private String schedule(Scheduled scheduled) {
        if (!scheduled.cron().isBlank()) return "cron: " + scheduled.cron();
        if (scheduled.fixedRate() >= 0) return "fixed rate: " + scheduled.fixedRate() + "ms";
        if (!scheduled.fixedRateString().isBlank()) return "fixed rate: " + scheduled.fixedRateString();
        if (scheduled.fixedDelay() >= 0) return "fixed delay: " + scheduled.fixedDelay() + "ms";
        return "scheduled";
    }

    private Object target(Object bean) {
        try { return bean instanceof Advised advised ? advised.getTargetSource().getTarget() : bean; }
        catch (Exception e) { throw new IllegalStateException("Cannot access proxied job target", e); }
    }
}
