package com.senestro.annotations;

import java.lang.annotation.*;

/**
 * An annotation to indicate that a method, field, parameter, local variable, or
 * type use can accept or be assigned a {@code null} value.
 *
 * <p>
 * This annotation is primarily used for documentation and static analysis tools
 * to allow nullable values explicitly.</p>
 *
 * <p>
 * Custom metadata or messages can be specified to provide additional context or
 * guidance for the usage of nullable elements.</p>
 *
 * <p>
 * For instance, this can be used to highlight the nullable nature of API inputs
 * or outputs for developers.</p>
 *
 * @see NonNull
 * @see java.util.Optional
 * <p>
 * author Senestro
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.PARAMETER,
        ElementType.LOCAL_VARIABLE,
        ElementType.TYPE_USE
})
public @interface Nullable {

    /**
     * Optional custom value for additional metadata or usage-specific contexts.
     *
     * @return A string representing the custom value.
     */
    String value() default "";

    /**
     * A custom message to document or describe the nullable nature of the
     * element.
     *
     * @return The message string.
     */
    String message() default "This value can be null.";
}
