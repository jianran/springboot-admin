package io.github.adminconsole.feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Exposes a bean field in the embedded admin console's Feature Flags page. */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FeatureFlag {
    String description() default "";
}
