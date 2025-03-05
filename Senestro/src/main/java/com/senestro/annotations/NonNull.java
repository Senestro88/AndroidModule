package com.senestro.annotations;

import java.lang.annotation.*;

/**
 * An annotation to indicate that a method, field, parameter, local variable,
 * or type use cannot accept or be assigned a {@code null} value.
 *
 * <p>This annotation is primarily used for documentation and static analysis tools
 * to enforce non-null constraints at compile time or runtime.</p>
 *
 * <p>Custom error messages and exception types can be specified for runtime validations.</p>
 *
 * @author Senestro
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
public @interface NonNull {

    /**
     * Optional custom value for additional metadata or usage-specific contexts.
     *
     * @return A string representing the custom value.
     */
    String value() default "";

    /**
     * A custom error message to be used if the non-null constraint is violated.
     *
     * @return The error message string.
     */
    String message() default "This value cannot be null.";

    /**
     * The type of exception to be thrown if the non-null constraint is violated.
     *
     * @return A {@link Class} object representing the exception type.
     */
    Class<? extends Exception> exception() default Exception.class;
}
