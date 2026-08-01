package io.github.adminconsole;

import io.github.adminconsole.config.AdminConsoleConfiguration;
import io.github.adminconsole.config.EnableAdminConsoleRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Explicitly enables the privileged embedded administration console. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({AdminConsoleConfiguration.class, EnableAdminConsoleRegistrar.class})
public @interface EnableAdminConsole {
    /** Enables access to the configured Arthas HTTP API. Disabled by default. */
    boolean enableArthas() default false;
}
