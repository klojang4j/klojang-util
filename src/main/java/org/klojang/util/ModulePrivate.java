package org.klojang.util;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * This annotation is meant to be applied to publicly accessible elements within the
 * exported packages of a Java 9+ module that should nevertheless be treated as
 * module-private. Clients of the module <b>should not</b> use elements annotated
 * with {@code @ModulePrivate}. They are meant to be accessed from the non-exported
 * packages in the module only. Elements annotated with {@code @ModulePrivate} may
 * disappear or change in any respect without notice in subsequent versions of the
 * module. Note that this is a {@code @Documented} annotation, so the element's
 * quasi-invisibility is in a sense part of the module's public API.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, CONSTRUCTOR, FIELD, METHOD, ANNOTATION_TYPE})
public @interface ModulePrivate {
}
