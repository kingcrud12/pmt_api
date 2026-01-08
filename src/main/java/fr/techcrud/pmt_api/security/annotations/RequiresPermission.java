package fr.techcrud.pmt_api.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to check if user has a specific permission
 * Format: "RESOURCE:ACTION" (e.g., "USER:READ", "PROJECT:ADMIN")
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    String value();
}
