package io.github.adminconsole;

import io.github.adminconsole.config.EnableAdminConsoleImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Explicitly enables the privileged embedded administration console. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(EnableAdminConsoleImportSelector.class)
public @interface EnableAdminConsole {
    /** Enables access to the configured Arthas HTTP API. Disabled by default. */
    boolean enableArthas() default false;

    /**
     * Spring profile expressions in which the console is enabled. An empty array enables it in every profile.
     * Multiple entries use OR semantics, for example {@code {"dev", "staging"}}.
     */
    String[] profiles() default {};
}
